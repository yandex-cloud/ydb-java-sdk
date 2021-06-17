package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.yandex.ydb.jdbc.YdbParameterMetaData;
import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.values.ListType;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.StructType;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.BATCH_INVALID;
import static com.yandex.ydb.jdbc.YdbConst.DEFAULT_BATCH_PARAMETER;
import static com.yandex.ydb.jdbc.YdbConst.INDEXED_PARAMETER_PREFIX;
import static com.yandex.ydb.jdbc.YdbConst.PARAMETER_TYPE_UNKNOWN;
import static com.yandex.ydb.jdbc.YdbConst.TRY_EXECUTE_ON_BATCH_STATEMENT;
import static com.yandex.ydb.jdbc.YdbConst.UNKNOWN_PARAMETER_IN_BATCH;
import static com.yandex.ydb.jdbc.YdbConst.UNSUPPORTED_QUERY_TYPE_IN_PS;
import static com.yandex.ydb.jdbc.YdbConst.VARIABLE_PARAMETER_PREFIX;

// This implementation support all possible scenarios with batched and simple execution mode
// It's a default configuration for all queries
public class YdbPreparedStatementImpl extends AbstractYdbPreparedStatementImpl {

    private final MutableState state = new MutableState();
    private final boolean enforceVariablePrefix;

    public YdbPreparedStatementImpl(YdbConnectionImpl connection,
                                    int resultSetType,
                                    String query) throws SQLException {
        super(connection, resultSetType, query);
        this.enforceVariablePrefix = connection.getYdbProperties().isEnforceVariablePrefix();
        this.clearParameters();
    }

    @Override
    public void clearParameters() {
        this.state.params = new LinkedHashMap<>(this.state.descriptions.size());
    }

    @Override
    protected void afterExecute() {
        this.clearBatch();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addBatch() throws SQLException {
        QueryType queryType = getQueryType();
        if (queryType != QueryType.DATA_QUERY) {
            throw new SQLException(BATCH_INVALID + queryType);
        }
        initBatchStruct();

        // All values are passed in sorted order
        state.batch.add(state.batchStruct.newValue((Map) state.params));
        this.clearParameters();
    }

    @Override
    public void clearBatch() {
        state.batch.clear();
        state.executeBatch = false;
        this.clearParameters();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        int batchSize = state.batch.size();
        if (batchSize == 0) {
            return new int[0];
        }
        state.executeBatch = true;
        super.execute();
        int[] ret = new int[batchSize];
        Arrays.fill(ret, SUCCESS_NO_INFO);
        return ret;
    }

    @Override
    public YdbParameterMetaData getParameterMetaData() {
        if (enforceVariablePrefix) {
            Map<String, TypeDescription> descriptions = new LinkedHashMap<>(state.descriptions.size());
            for (Map.Entry<String, TypeDescription> entry : state.descriptions.entrySet()) {
                String name = entry.getKey();
                String parameterName;
                if (!name.startsWith(VARIABLE_PARAMETER_PREFIX)) {
                    parameterName = VARIABLE_PARAMETER_PREFIX + name;
                } else {
                    parameterName = name;
                }
                descriptions.put(parameterName, entry.getValue());
            }
            return new YdbParameterMetaDataImpl(descriptions);
        } else {
            return new YdbParameterMetaDataImpl(state.descriptions);
        }
    }

    @Override
    protected void setImpl(String parameterName, @Nullable Object value,
                           int sqlType, @Nullable String typeName, @Nullable Type type)
            throws SQLException {
        TypeDescription description = getParameter(parameterName, value, sqlType, typeName, type);
        Value<?> ydbValue = getValue(parameterName, description, value);
        state.params.put(parameterName, ydbValue);
    }

    @Override
    protected void setImpl(int parameterIndex, @Nullable Object value,
                           int sqlType, @Nullable String typeName, @Nullable Type type)
            throws SQLException {
        setImpl(INDEXED_PARAMETER_PREFIX + parameterIndex, value, sqlType, typeName, type);
    }

    //

    @Override
    protected Params getParams() throws SQLException {
        if (state.batch.isEmpty()) {
            // Single params
            if (enforceVariablePrefix) {
                Params params = Params.create(state.params.size());
                for (Map.Entry<String, Value<?>> entry : state.params.entrySet()) {
                    String name = entry.getKey();
                    String parameterName;
                    if (!name.startsWith(VARIABLE_PARAMETER_PREFIX)) {
                        parameterName = VARIABLE_PARAMETER_PREFIX + name;
                    } else {
                        parameterName = name;
                    }
                    params.put(parameterName, entry.getValue());
                }
                return params;
            } else {
                return Params.copyOf(state.params);
            }

        } else {
            if (!state.executeBatch) {
                throw new YdbExecutionException(TRY_EXECUTE_ON_BATCH_STATEMENT);
            }

            // Batch params
            ListValue listValue = state.batchList.newValue(state.batch);
            return Params.of(DEFAULT_BATCH_PARAMETER, listValue);
        }
    }

    @Override
    protected boolean executeImpl() throws SQLException {
        QueryType queryType = getQueryType();
        switch (queryType) {
            case DATA_QUERY:
                String sql = getQuery();
                Session session = getConnection().getYdbSession();
                return executeDataQueryImpl(
                        getParams(),
                        params -> QueryType.DATA_QUERY + " >>\n" + sql +
                                "\n\nParams: " + paramsToString(params),
                        (tx, params, execParams) -> session.executeDataQuery(sql, tx, params, execParams));
            case SCAN_QUERY:
                return executeScanQueryImpl();
            default:
                throw new SQLException(UNSUPPORTED_QUERY_TYPE_IN_PS + queryType);
        }
    }

    //

    private TypeDescription getParameter(String name,
                                         @Nullable Object value,
                                         int sqlType,
                                         @Nullable String typeName,
                                         @Nullable Type type) throws YdbExecutionException {
        TypeDescription description = state.descriptions.get(name);
        if (description != null) {
            return description;
        }
        if (!state.batch.isEmpty()) {
            throw new YdbExecutionException(UNKNOWN_PARAMETER_IN_BATCH + name);
        }
        Type actualType = toYdbType(name, value, sqlType, typeName, type);
        TypeDescription newDescription = TypeDescription.of(actualType);
        state.descriptions.put(name, newDescription);
        return newDescription;

    }

    private Type toYdbType(String parameterName,
                           @Nullable Object value,
                           int sqlType,
                           @Nullable String typeName,
                           @Nullable Type type)
            throws YdbExecutionException {
        if (value instanceof Value<?>) {
            Value<?> actualValue = (Value<?>) value;
            return actualValue.getType();
        }
        if (type != null) {
            return type;
        }

        YdbTypes types = getConnection().getYdbTypes();
        Type actualType = null;
        if (typeName != null) {
            actualType = types.toYdbType(typeName);
        }
        if (actualType == null) {
            actualType = types.toYdbType(sqlType);
        }
        if (actualType == null && value != null) {
            actualType = types.toYdbType(value.getClass());
        }
        // All types are optional when detected from sqlType
        if (actualType == null) {
            throw new YdbExecutionException(String.format(PARAMETER_TYPE_UNKNOWN,
                    sqlType, typeName, parameterName));
        }
        return actualType.makeOptional();
    }

    private void initBatchStruct() {
        if (state.batchStruct == null) {
            Map<String, Type> typeMap = new LinkedHashMap<>(state.params.size());
            for (Map.Entry<String, Value<?>> entry : state.params.entrySet()) {
                typeMap.put(entry.getKey(), entry.getValue().getType());
            }
            state.batchStruct = StructType.of(typeMap);
            state.batchList = ListType.of(state.batchStruct);
        }
    }

    @SuppressWarnings("rawtypes")
    private static class MutableState {
        // Batches
        private final List<Value> batch = new ArrayList<>();
        private StructType batchStruct;
        private ListType batchList;
        private boolean executeBatch;

        // All known descriptions
        private final Map<String, TypeDescription> descriptions = new LinkedHashMap<>();
        private Map<String, Value<?>> params;
    }

    private interface TypeSource {
        Type getType() throws YdbExecutionException;
    }
}
