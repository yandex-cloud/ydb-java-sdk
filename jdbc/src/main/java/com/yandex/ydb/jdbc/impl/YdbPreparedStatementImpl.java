package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;

import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.table.query.DataQuery;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.BATCH_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.INDEXED_PARAMETERS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.PARAMETER_NOT_FOUND;

public class YdbPreparedStatementImpl extends AbstractYdbPreparedStatementImpl {

    private final DataQuery dataQuery;
    private final MutableState state = new MutableState();
    private final PreparedConfiguration cfg;
    private final boolean enforceVariablePrefix;

    public YdbPreparedStatementImpl(YdbConnectionImpl connection,
                                    int resultSetType,
                                    String query,
                                    DataQuery dataQuery) throws SQLException {
        super(connection, resultSetType, query, dataQuery);
        this.dataQuery = dataQuery;
        this.cfg = asPreparedConfiguration(dataQuery.types());
        this.enforceVariablePrefix = connection.getYdbProperties().isEnforceVariablePrefix();
        this.clearParameters();
    }

    @Override
    public void clearParameters() {
        this.state.params = dataQuery.newParams();
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

    //

    @Override
    protected void setImpl(String origParameterName, Object x) throws SQLException {
        String parameterName;
        if (enforceVariablePrefix && !origParameterName.startsWith("$")) {
            parameterName = "$" + origParameterName;
        } else {
            parameterName = origParameterName;
        }
        TypeDescription description = getParameter(parameterName);
        Value<?> value = getValue(parameterName, description, x);
        state.params.put(parameterName, value);
    }

    @Override
    protected void setImpl(int parameterIndex, Object x) throws SQLException {
        /*
        Yes, we can figure out the parameter index - we know how to build parameters metadata.
        However, the actual parameter indexes could be different from ones specified in SQL.
        YDB will reorder them, probably sort and so on.

        We can't simply provide this method until figuring out how not to screw parameters order.
         */
        throw new SQLFeatureNotSupportedException(INDEXED_PARAMETERS_UNSUPPORTED);
    }

    @Override
    protected Params getParams() {
        return state.params;
    }

    @Override
    protected Map<String, Type> getParameterTypes() {
        return dataQuery.types();
    }

    //

    private TypeDescription getParameter(String name) throws YdbExecutionException {
        TypeDescription description = cfg.descriptions.get(name);
        if (description == null) {
            throw new YdbExecutionException(PARAMETER_NOT_FOUND + name);
        }
        return description;
    }

    private static PreparedConfiguration asPreparedConfiguration(Map<String, Type> types) {
        int count = types.size();

        Map<String, TypeDescription> descriptions = new HashMap<>(count);
        for (Map.Entry<String, Type> entry : types.entrySet()) {
            descriptions.put(entry.getKey(), TypeDescription.of(entry.getValue()));
        }

        return new PreparedConfiguration(descriptions);
    }

    private static class PreparedConfiguration {
        private final Map<String, TypeDescription> descriptions;

        private PreparedConfiguration(Map<String, TypeDescription> descriptions) {
            this.descriptions = descriptions;
        }
    }

    private static class MutableState {
        private Params params;
    }
}
