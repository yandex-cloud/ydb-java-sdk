package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.yandex.ydb.jdbc.YdbParameterMetaData;
import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.BATCH_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.INDEXED_PARAMETERS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.PARAMETER_TYPE_UNKNOWN;
import static com.yandex.ydb.jdbc.YdbConst.UNSUPPORTED_QUERY_TYPE_IN_PS;

public class YdbPreparedStatementInMemoryImpl extends AbstractYdbPreparedStatementImpl {

    private final MutableState state = new MutableState();
    private final boolean enforceVariablePrefix;

    public YdbPreparedStatementInMemoryImpl(YdbConnectionImpl connection,
                                            int resultSetType,
                                            String query) throws SQLException {
        super(connection, resultSetType, query);
        this.enforceVariablePrefix = connection.getYdbProperties().isEnforceVariablePrefix();
        this.clearParameters();
    }

    @Override
    public void clearParameters() {
        this.state.params = Params.create();
    }

    @Override
    protected void afterExecute() {
        this.clearParameters();
    }

    @Override
    public void addBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException(BATCH_UNSUPPORTED);
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException(BATCH_UNSUPPORTED);
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException(BATCH_UNSUPPORTED);
    }

    @Override
    public YdbParameterMetaData getParameterMetaData() {
        return new YdbParameterMetaDataImpl(state.descriptions);
    }

    //
    public void setObject(String origParameterName, Object x, Type type) throws SQLException {
        String parameterName;
        if (enforceVariablePrefix && !origParameterName.startsWith("$")) {
            parameterName = "$" + origParameterName;
        } else {
            parameterName = origParameterName;
        }
        TypeDescription description = getParameter(parameterName, type);
        Value<?> value = getValue(parameterName, description, x);
        state.params.put(parameterName, value);
    }

    public void setObject(int parameterName, Object x, Type type) throws SQLException {
        throw new SQLFeatureNotSupportedException(INDEXED_PARAMETERS_UNSUPPORTED);
    }


    //


    @Override
    protected void setImpl(String origParameterName, @Nullable Object x, int sqlType) throws SQLException {
        String parameterName;
        if (enforceVariablePrefix && !origParameterName.startsWith("$")) {
            parameterName = "$" + origParameterName;
        } else {
            parameterName = origParameterName;
        }
        TypeDescription description = getParameter(parameterName, x, sqlType);
        Value<?> value = getValue(parameterName, description, x);
        state.params.put(parameterName, value);
    }

    @Override
    protected void setImpl(int parameterIndex, @Nullable Object x, int sqlType) throws SQLException {
        // Disable this for compatibility reasons
        throw new SQLFeatureNotSupportedException(INDEXED_PARAMETERS_UNSUPPORTED);
    }

    @Override
    protected Params getParams() {
        return state.params;
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
                        (tx, params, execParams) -> {
                            try {
                                return session.executeDataQuery(sql, tx, params, execParams);
                            } finally {
                                afterExecute();
                            }
                        });
            case SCAN_QUERY:
                return executeScanQueryImpl();
            default:
                throw new SQLException(UNSUPPORTED_QUERY_TYPE_IN_PS + queryType);
        }
    }

    //

    private TypeDescription getParameter(String name, Type type) throws YdbExecutionException {
        TypeDescription description = state.descriptions.get(name);
        if (description == null) {
            description = TypeDescription.of(type);
            state.descriptions.put(name, description);
        }
        return description;
    }

    private TypeDescription getParameter(String name, @Nullable Object x, int sqlType) throws YdbExecutionException {
        TypeDescription description = state.descriptions.get(name);
        if (description == null) {
            Type type = toYdbType(x, sqlType);
            if (type == null) {
                throw new YdbExecutionException(String.format(PARAMETER_TYPE_UNKNOWN, sqlType, name));
            }
            description = TypeDescription.of(type.makeOptional()); // All parameters are optional by default
            state.descriptions.put(name, description);
        }
        return description;
    }

    @Nullable
    private Type toYdbType(@Nullable Object value, int sqlType) {
        if (value instanceof Value<?>) {
            Value<?> actualValue = (Value<?>) value;
            Type actualType = actualValue.getType();
            if (actualType.getKind() == Type.Kind.OPTIONAL) {
                return actualType.unwrapOptional();
            }
        }
        YdbTypes types = getConnection().getYdbTypes();
        Type type = types.toYdbType(sqlType);
        if (type == null && value != null) {
            return types.toYdbType(types.toSqlType(value.getClass()));
        }
        return type;
    }

    private static class MutableState {
        private final Map<String, TypeDescription> descriptions = new LinkedHashMap<>();
        private Params params;
    }
}
