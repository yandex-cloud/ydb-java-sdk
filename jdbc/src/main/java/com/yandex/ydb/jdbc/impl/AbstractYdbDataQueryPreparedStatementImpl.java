package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.yandex.ydb.jdbc.YdbParameterMetaData;
import com.yandex.ydb.table.query.DataQuery;

import static com.yandex.ydb.jdbc.YdbConst.UNSUPPORTED_QUERY_TYPE_IN_PS;

public abstract class AbstractYdbDataQueryPreparedStatementImpl extends AbstractYdbPreparedStatementImpl {

    private final Supplier<YdbParameterMetaData> metaDataSupplier;
    private final DataQuery dataQuery;

    protected AbstractYdbDataQueryPreparedStatementImpl(YdbConnectionImpl connection,
                                               int resultSetType,
                                               String query,
                                               DataQuery dataQuery) throws SQLException {
        super(connection, resultSetType, query);
        this.dataQuery = Objects.requireNonNull(dataQuery);
        this.metaDataSupplier = Suppliers.memoize(() ->
                new YdbParameterMetaDataImpl(getParameterTypes()))::get;
    }

    @Override
    public YdbParameterMetaData getParameterMetaData() {
        return metaDataSupplier.get();
    }


    protected DataQuery getDataQuery() {
        return dataQuery;
    }

    @Override
    protected boolean executeImpl() throws SQLException {
        QueryType queryType = getQueryType();
        switch (queryType) {
            case DATA_QUERY:
                return executeDataQueryImpl(
                        getParams(),
                        params -> QueryType.DATA_QUERY + " [" + dataQuery.getId() + "] >>\n" +
                                dataQuery.getText().orElse("<empty>") +
                                "\n\nParams: " + paramsToString(params),
                        (tx, params, execParams) -> {
                            try {
                                return dataQuery.execute(tx, params, execParams);
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

    protected abstract Map<String, TypeDescription> getParameterTypes();
}
