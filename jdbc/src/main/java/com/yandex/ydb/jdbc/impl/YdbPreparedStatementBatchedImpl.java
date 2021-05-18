package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.table.query.DataQuery;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.values.ListType;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.StructType;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.INDEXED_PARAMETERS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.MISSING_VALUE_FOR_PARAMETER;
import static com.yandex.ydb.jdbc.YdbConst.PARAMETER_NOT_FOUND;

public class YdbPreparedStatementBatchedImpl extends AbstractYdbPreparedStatementImpl {

    private final StructBatchConfiguration cfg;
    private final StructMutableState state;

    protected YdbPreparedStatementBatchedImpl(YdbConnectionImpl connection,
                                              int resultSetType,
                                              String query,
                                              DataQuery dataQuery,
                                              StructBatchConfiguration cfg) throws SQLException {
        super(connection, resultSetType, query, dataQuery);
        this.cfg = Objects.requireNonNull(cfg);
        this.state = new StructMutableState(cfg);
        this.clearParameters();
    }

    @Override
    public void clearParameters() {
        state.clear();
    }

    @Override
    protected void afterExecute() {
        clearParameters();
        clearBatch();
    }

    @Override
    public void addBatch() throws SQLException {
        state.flush();
    }

    @Override
    public void clearBatch() {
        state.batch.clear();
        state.clear();
    }

    @Override
    public boolean execute() throws SQLException {
        addBatch();
        return super.execute();
    }

    @Override
    public int executeUpdate() throws SQLException {
        addBatch();
        return super.executeUpdate();
    }

    @Override
    public YdbResultSet executeQuery() throws SQLException {
        addBatch();
        return super.executeQuery();
    }

    /**
     * There is a difference between this method and all other 'execute' methods.
     * All methods except 'executeBatch' will be executed unconditionally, i.e. calling other 'execute' methods
     * without settings parameter first could cause an exception
     */
    @Override
    public int[] executeBatch() throws SQLException {
        int batchSize = state.batch.size();
        if (batchSize == 0) {
            return new int[0];
        }
        super.execute();
        return new int[batchSize]; // TODO: not actual batches, no update count
    }

    @Override
    protected Params getParams() {
        // Do not flush parameters
        Params params = getDataQuery().newParams();
        params.put(cfg.paramName, cfg.listType.newValue(state.batch)); // The fastest way to prepare list
        return params;
    }

    @Override
    protected Map<String, Type> getParameterTypes() {
        return cfg.types;
    }

    @Override
    protected void setImpl(String parameterName, Object x) throws SQLException {
        int index = cfg.getIndex(parameterName);
        TypeDescription description = cfg.descriptions[index];
        Value<?> value = getValue(parameterName, description, x);
        state.addParam(index, value);
    }

    @Override
    protected void setImpl(int parameterIndex, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException(INDEXED_PARAMETERS_UNSUPPORTED);
    }

    @Override
    protected String paramsToString(Params params) {
        Map<String, Value<?>> values = params.values();
        if (values.size() == 1) {
            Map.Entry<String, Value<?>> entry = values.entrySet().iterator().next();
            String key = entry.getKey();
            Value<?> value = entry.getValue();
            if (value instanceof ListValue) {
                ListValue list = (ListValue) value;
                if (list.size() > 10) {
                    String first10Elements = IntStream.range(0, 10)
                            .mapToObj(list::get)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                    return "{" + key + "=List[" + first10Elements + "...], and " + (list.size() - 10) + " more";
                }
            }
        }
        return super.paramsToString(params);
    }

    //

    static Optional<StructBatchConfiguration> asColumns(Map<String, Type> types) {
        if (types.size() != 1) {
            return Optional.empty(); // ---
        }
        // Only single parameter

        Map.Entry<String, Type> entry = types.entrySet().iterator().next();

        String paramName = entry.getKey();

        // Only list of values
        Type paramType = entry.getValue();
        if (paramType.getKind() != Type.Kind.LIST) {
            return Optional.empty(); // ---
        }

        ListType listType = (ListType) paramType;
        Type itemType = listType.getItemType();
        // Component - must be struct (i.e. list of structs)
        if (itemType.getKind() == Type.Kind.STRUCT) {
            return Optional.of(fromStruct(paramName, listType, (StructType) itemType));
        }
        return Optional.empty(); // ---
    }

    private static StructBatchConfiguration fromStruct(String paramName, ListType listType, StructType structType) {
        int membersCount = structType.getMembersCount();

        Map<String, Type> types = new LinkedHashMap<>(membersCount);
        Map<String, Integer> indexes = new HashMap<>(membersCount);
        String[] names = new String[membersCount];
        TypeDescription[] descriptions = new TypeDescription[membersCount];
        for (int i = 0; i < membersCount; i++) {
            String name = structType.getMemberName(i);
            Type type = structType.getMemberType(i);
            TypeDescription description = TypeDescription.of(type);
            if (indexes.put(name, i) != null) {
                throw new IllegalStateException("Internal error. YDB must not bypass this struct " +
                        "with duplicate member " + paramName);
            }
            types.put(name, type);
            names[i] = name;
            descriptions[i] = description;
        }
        return new StructBatchConfiguration(paramName, listType, structType, types, indexes, names, descriptions);
    }

    static class StructBatchConfiguration {
        private final String paramName;
        private final ListType listType;
        private final StructType structType;
        private final Map<String, Type> types;
        private final Map<String, Integer> indexes;
        private final String[] names;
        private final TypeDescription[] descriptions;

        private StructBatchConfiguration(String paramName,
                                         ListType listType,
                                         StructType structType,
                                         Map<String, Type> types,
                                         Map<String, Integer> indexes,
                                         String[] names,
                                         TypeDescription[] descriptions) {
            this.paramName = Objects.requireNonNull(paramName);
            this.listType = Objects.requireNonNull(listType);
            this.structType = Objects.requireNonNull(structType);
            this.types = Objects.requireNonNull(types);
            this.indexes = Objects.requireNonNull(indexes);
            this.names = Objects.requireNonNull(names);
            this.descriptions = Objects.requireNonNull(descriptions);
            Preconditions.checkState(descriptions.length == names.length);
            Preconditions.checkState(descriptions.length == indexes.size());
            Preconditions.checkState(descriptions.length == types.size());
        }

        int getIndex(String name) throws SQLException {
            Integer index = indexes.get(name);
            if (index == null) {
                throw new YdbExecutionException(PARAMETER_NOT_FOUND + name);
            }
            return index;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class StructMutableState {
        private final List<Value> batch = new ArrayList<>();
        private final StructBatchConfiguration cfg;
        private Value[] members;
        private boolean modified;

        StructMutableState(StructBatchConfiguration cfg) {
            this.cfg = cfg;
            this.members = new Value[cfg.descriptions.length];
        }

        void addParam(int index, Value<?> value) {
            members[index] = value;
            modified = true;
        }

        void flush() throws SQLException {
            if (modified) {
                for (int i = 0; i < members.length; i++) {
                    if (members[i] == null) {
                        throw new SQLException(MISSING_VALUE_FOR_PARAMETER + cfg.names[i]);
                    }
                }
                batch.add(cfg.structType.newValueUnsafe(members)); // The fastest way to prepare struct
                clear();
            }
        }

        void clear() {
            members = new Value[cfg.descriptions.length];
            modified = false;
        }
    }
}
