package com.yandex.ydb.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.yandex.ydb.core.Issue;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.jdbc.YdbStatement;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.jdbc.exception.YdbResultTruncatedException;
import com.yandex.ydb.jdbc.settings.YdbOperationProperties;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.ExplainDataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.result.impl.ProtoValueReaders;
import com.yandex.ydb.table.settings.ExecuteDataQuerySettings;
import com.yandex.ydb.table.settings.ExecuteScanQuerySettings;
import com.yandex.ydb.table.settings.ExecuteSchemeQuerySettings;
import com.yandex.ydb.table.settings.ExplainDataQuerySettings;
import com.yandex.ydb.table.transaction.TxControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.YdbConst.AUTO_GENERATED_KEYS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.CANNOT_UNWRAP_TO;
import static com.yandex.ydb.jdbc.YdbConst.CLOSED_CONNECTION;
import static com.yandex.ydb.jdbc.YdbConst.DIRECTION_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.NAMED_CURSORS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.QUERY_EXPECT_RESULT_SET;
import static com.yandex.ydb.jdbc.YdbConst.QUERY_EXPECT_UPDATE;
import static com.yandex.ydb.jdbc.YdbConst.RESULT_IS_TRUNCATED;
import static com.yandex.ydb.jdbc.YdbConst.RESULT_SET_MODE_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.RESULT_SET_UNAVAILABLE;

public class YdbStatementImpl implements YdbStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(YdbStatementImpl.class);

    private final MutableState state = new MutableState();

    private final List<String> batch = new ArrayList<>();
    private final YdbConnectionImpl connection;
    private final YdbOperationProperties properties;
    private final Validator validator;
    private final int resultSetType;

    public YdbStatementImpl(YdbConnectionImpl connection, int resultSetType) {
        this.connection = Objects.requireNonNull(connection);
        this.validator = connection.getValidator();
        this.resultSetType = resultSetType;

        this.properties = connection.getYdbProperties();
        state.queryTimeout = properties.getQueryTimeout();
        state.keepInQueryCache = properties.isKeepInQueryCache();
    }

    @Override
    public void executeSchemeQuery(String sql) throws SQLException {
        boolean result = this.executeSchemeQueryImpl(sql);
        if (result) {
            throw new SQLException(QUERY_EXPECT_UPDATE);
        }
    }

    @Override
    public YdbResultSet executeScanQuery(String sql) throws SQLException {
        boolean result = this.executeScanQueryImpl(sql);
        if (!result) {
            throw new SQLException(QUERY_EXPECT_RESULT_SET);
        }
        return getResultSet(0);
    }

    @Override
    public YdbResultSet executeExplainQuery(String sql) throws SQLException {
        boolean result = this.executeExplainQueryImpl(sql);
        if (!result) {
            throw new SQLException(QUERY_EXPECT_RESULT_SET);
        }
        return getResultSet(0);
    }

    @Override
    public YdbResultSet executeQuery(String sql) throws SQLException {
        boolean result = this.execute(sql);
        if (!result) {
            throw new SQLException(QUERY_EXPECT_RESULT_SET);
        }
        return getResultSet(0);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        boolean result = this.execute(sql);
        if (result) {
            throw new SQLException(QUERY_EXPECT_UPDATE);
        }
        return state.updateCount;
    }

    @Override
    public void close() {
        // do nothing
        state.closed = true;
    }

    @Override
    public int getMaxFieldSize() {
        return 0; // not supported
    }

    @Override
    public void setMaxFieldSize(int max) {
        // not supported
    }

    @Override
    public int getMaxRows() {
        return properties.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) {
        // has not effect
    }

    @Override
    public void setEscapeProcessing(boolean enable) {
        // has not effect
    }

    @Override
    public int getQueryTimeout() {
        return (int) state.queryTimeout.getSeconds();
    }

    @Override
    public void setQueryTimeout(int seconds) {
        state.queryTimeout = Duration.ofSeconds(seconds);
    }

    @Override
    public void cancel() {
        // has not effect
    }

    @Override
    public SQLWarning getWarnings() {
        return validator.toSQLWarnings(state.lastIssues);
    }

    @Override
    public void clearWarnings() {
        state.lastIssues = Issue.EMPTY_ARRAY;
    }

    @Override
    public void setCursorName(String name) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException(NAMED_CURSORS_UNSUPPORTED);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return executeImpl(sql);
    }

    @Override
    public YdbResultSet getResultSet() throws SQLException {
        ensureOpened();

        if (state.lastResultSets != null && state.resultSetIndex < state.lastResultSets.length) {
            return getResultSet(state.resultSetIndex);
        } else {
            return null;
        }
    }

    @Override
    public Optional<YdbResultSet> getResultSetAt(int resultSetIndex) throws SQLException {
        ensureOpened();

        if (state.lastResultSets != null && resultSetIndex < state.lastResultSets.length) {
            return Optional.ofNullable(state.lastResultSets[resultSetIndex]);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int getUpdateCount() {
        return state.updateCount;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        ensureOpened();

        return getMoreResults(Statement.KEEP_CURRENT_RESULT);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        if (direction != ResultSet.FETCH_FORWARD && direction != ResultSet.FETCH_UNKNOWN) {
            throw new SQLException(DIRECTION_UNSUPPORTED + direction);
        }
    }

    @Override
    public int getFetchDirection() {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) {
        // has not effect
    }

    @Override
    public int getFetchSize() {
        return getMaxRows();
    }

    @Override
    public int getResultSetConcurrency() {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() {
        return resultSetType;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        ensureOpened();

        batch.add(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        batch.clear();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        ensureOpened();

        if (batch.isEmpty()) {
            LOGGER.debug("Batch is empty, nothing to execute");
            return new int[0];
        }

        try {
            LOGGER.debug("Executing batch of {} item(s)", batch.size());

            String sql = String.join(";\n", batch);
            execute(sql);

            int[] ret = new int[batch.size()];
            Arrays.fill(ret, SUCCESS_NO_INFO);
            return ret;
        } finally {
            clearBatch();
        }
    }

    @Override
    public YdbConnection getConnection() {
        return connection;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        ensureOpened();

        YdbResultSetImpl[] lastResultSets = state.lastResultSets;
        int resultSetIndex = state.resultSetIndex;
        switch (current) {
            case Statement.KEEP_CURRENT_RESULT:
                // do nothing
                break;
            case Statement.CLOSE_CURRENT_RESULT:
                if (lastResultSets != null && resultSetIndex < lastResultSets.length) {
                    lastResultSets[resultSetIndex] = null;
                }
                break;
            case Statement.CLOSE_ALL_RESULTS:
                if (lastResultSets != null && resultSetIndex < lastResultSets.length) {
                    Arrays.fill(lastResultSets, 0, resultSetIndex, null);
                }
                break;
            default:
                throw new SQLException(RESULT_SET_MODE_UNSUPPORTED + current);
        }
        if (lastResultSets != null) {
            state.resultSetIndex++;
            return state.resultSetIndex < lastResultSets.length;
        } else {
            return false;
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys != Statement.NO_GENERATED_KEYS) {
            throw new SQLFeatureNotSupportedException(AUTO_GENERATED_KEYS_UNSUPPORTED);
        }
        return executeUpdate(sql);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys != Statement.NO_GENERATED_KEYS) {
            throw new SQLFeatureNotSupportedException(AUTO_GENERATED_KEYS_UNSUPPORTED);
        }
        return execute(sql);
    }

    @Override
    public int getResultSetHoldability() {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public boolean isClosed() {
        return state.closed;
    }

    @Override
    public void setPoolable(boolean poolable) {
        state.keepInQueryCache = poolable;
    }

    @Override
    public boolean isPoolable() {
        return state.keepInQueryCache;
    }

    @Override
    public void closeOnCompletion() {
        // do nothing
    }

    @Override
    public boolean isCloseOnCompletion() {
        return false;
    }

    protected boolean executeImpl(String origSql) throws SQLException {
        ensureOpened();

        String sql = connection.prepareYdbSql(origSql);

        QueryType queryType = connection.decodeQueryType(sql);
        Preconditions.checkState(queryType != null, "queryType cannot be null");
        this.clearState();
        switch (queryType) {
            case SCHEME_QUERY:
                return executeSchemeQueryImpl(sql);
            case DATA_QUERY:
                return executeDataQueryImpl(sql);
            case SCAN_QUERY:
                return executeScanQueryImpl(sql);
            case EXPLAIN_QUERY:
                return executeExplainQueryImpl(sql);
            default:
                throw new IllegalStateException("Internal error. Unsupported query type " + queryType);
        }
    }

    private boolean executeSchemeQueryImpl(String origSql) throws SQLException {
        ensureOpened();

        String sql = connection.prepareYdbSql(origSql);

        // Scheme query does not affect transactions or result sets
        ExecuteSchemeQuerySettings settings = new ExecuteSchemeQuerySettings();
        validator.init(settings);

        Session session = connection.getYdbSession();
        Status status = validator.joinStatus(LOGGER,
                () -> QueryType.SCHEME_QUERY + " >>\n" + sql,
                () -> session.executeSchemeQuery(sql, settings));

        state.lastIssues = status.getIssues();
        return false;
    }

    protected boolean executeDataQueryImpl(Params params,
                                           Function<Params, String> operation,
                                           DataQueryExecutor executor) throws SQLException {
        ensureOpened();

        ExecuteDataQuerySettings settings = new ExecuteDataQuerySettings();
        validator.init(settings);
        if (state.keepInQueryCache) {
            settings.keepInQueryCache();
        }
        settings.setTimeout(state.queryTimeout);

        TxControl<?> txControl = connection.getTxControl();

        Result<DataQueryResult> result;
        try {
            result = validator.joinResult(
                    LOGGER,
                    () -> operation.apply(params),
                    () -> executor.executeDataQuery(txControl, params, settings));
        } catch (YdbNonRetryableException e) {
            connection.clearTx();
            throw e;
        }

        state.lastIssues = result.getIssues();

        DataQueryResult dataQueryResult = result.expect("Result set");
        connection.setTx(dataQueryResult.getTxId());

        printResultSetDetails(dataQueryResult);
        checkResultSetTruncated(dataQueryResult);

        int resultSetCount = dataQueryResult.getResultSetCount();
        state.lastResultSets = new YdbResultSetImpl[resultSetCount];
        if (resultSetCount == 0) {
            state.updateCount = 0;
        } else {
            for (int i = 0; i < resultSetCount; i++) {
                state.lastResultSets[i] = new YdbResultSetImpl(this, dataQueryResult.getResultSet(i));
            }
        }
        return resultSetCount > 0; // TODO: check;
    }


    protected boolean executeScanQueryImpl(String origSql,
                                           Params params,
                                           Function<Params, String> operation) throws SQLException {
        ensureOpened();

        String sql = connection.prepareYdbSql(origSql);

        // TODO: support stats?
        Duration scanQueryTimeout = properties.getScanQueryTimeout();
        ExecuteScanQuerySettings.Builder settingsBuilder = ExecuteScanQuerySettings.newBuilder();
        if (scanQueryTimeout.toNanos() > 0) {
            settingsBuilder.timeout(scanQueryTimeout);
        }
        ExecuteScanQuerySettings settings = settingsBuilder.build();

        Session session = connection.getYdbSession();
        Collection<ResultSetReader> resultSets = new LinkedBlockingQueue<>();
        Status status = validator.joinStatus(
                LOGGER,
                () -> operation.apply(params),
                () -> session.executeScanQuery(sql, params, settings, resultSets::add));

        state.lastIssues = status.getIssues();

        ResultSetReader resultSet = ProtoValueReaders.forResultSets(resultSets);
        printResultSetDetails(resultSet);

        checkResultSetTruncated(resultSet);
        state.lastResultSets = new YdbResultSetImpl[]{new YdbResultSetImpl(this, resultSet)};
        return true;
    }

    protected boolean executeExplainQueryImpl(String origSql) throws SQLException {
        ensureOpened();

        String sql = connection.prepareYdbSql(origSql);

        ExplainDataQuerySettings settings = new ExplainDataQuerySettings();
        validator.init(settings);

        Session session = connection.getYdbSession();
        Result<ExplainDataQueryResult> result = validator.joinResult(LOGGER,
                () -> QueryType.EXPLAIN_QUERY + " >>\n" + sql,
                () -> session.explainDataQuery(sql, settings));
        state.lastIssues = result.getIssues();

        ExplainDataQueryResult explainDataQuery = result.expect("Explain Data Query");

        Map<String, Object> params = MappingResultSets.stableMap(
                YdbConst.EXPLAIN_COLUMN_AST, explainDataQuery.getQueryAst(),
                YdbConst.EXPLAIN_COLUMN_PLAN, explainDataQuery.getQueryPlan());
        ResultSetReader resultSetReader = MappingResultSets.readerFromMap(params);
        state.lastResultSets = new YdbResultSetImpl[]{new YdbResultSetImpl(this, resultSetReader)};
        return true;
    }


    private boolean executeDataQueryImpl(String sql) throws SQLException {
        Session session = connection.getYdbSession();
        return this.executeDataQueryImpl(
                Params.empty(),
                params -> QueryType.DATA_QUERY + " >>\n" + sql,
                (tx, params, settings) -> session.executeDataQuery(sql, tx, params, settings));
    }

    private boolean executeScanQueryImpl(String sql) throws SQLException {
        return this.executeScanQueryImpl(
                sql,
                Params.empty(),
                params -> QueryType.SCAN_QUERY + " >>\n" + sql);
    }


    //

    private void clearState() {
        this.clearWarnings();
        state.lastResultSets = null;
        state.resultSetIndex = 0;
        state.updateCount = -1;
    }

    private void printResultSetDetails(DataQueryResult dataQueryResult) {
        StringBuilder buffer = new StringBuilder(36);
        int resultSetCount = dataQueryResult.getResultSetCount();
        for (int i = 0; i < resultSetCount; i++) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(dataQueryResult.getRowCount(i)).append(" rows");
            if (dataQueryResult.isTruncated(i)) {
                buffer.append(" - truncated");
            }
        }

        LOGGER.debug("OK, {} results ({})", resultSetCount, buffer);
    }

    private void printResultSetDetails(ResultSetReader resultSetReader) {
        LOGGER.debug("OK, {} rows{}", resultSetReader.getRowCount(),
                resultSetReader.isTruncated() ? " - truncated" : "");
    }

    private void checkResultSetTruncated(DataQueryResult dataQueryResult) throws SQLException {
        if (properties.isFailOnTruncatedResult()) {
            int resultSetCount = dataQueryResult.getResultSetCount();
            for (int i = 0; i < resultSetCount; i++) {
                if (dataQueryResult.isTruncated(i)) {
                    throw new YdbResultTruncatedException(String.format(
                            RESULT_IS_TRUNCATED, i, dataQueryResult.getRowCount(i)));
                }
            }
        }
    }

    private void checkResultSetTruncated(ResultSetReader resultSetReader) throws SQLException {
        if (properties.isFailOnTruncatedResult()) {
            if (resultSetReader.isTruncated()) {
                throw new YdbResultTruncatedException(String.format(
                        RESULT_IS_TRUNCATED, 0, resultSetReader.getRowCount()));
            }
        }
    }

    protected YdbResultSet getResultSet(int index) throws SQLException {
        YdbResultSetImpl[] lastResultSets = state.lastResultSets;
        if (lastResultSets == null) {
            throw new IllegalStateException("Internal error, not result to use");
        }
        if (index < 0 || index >= lastResultSets.length) {
            throw new IllegalStateException("Internal error, no result at position: " + index);
        }
        YdbResultSetImpl resultSet = lastResultSets[index];
        if (resultSet == null) {
            throw new SQLException(RESULT_SET_UNAVAILABLE + index);
        }
        return resultSet;
    }

    protected void ensureOpened() throws SQLException {
        if (state.closed) {
            throw new SQLException(CLOSED_CONNECTION);
        }
    }

    interface DataQueryExecutor {
        CompletableFuture<Result<DataQueryResult>> executeDataQuery(TxControl<?> txControl,
                                                                    Params params,
                                                                    ExecuteDataQuerySettings settings);
    }

    private static class MutableState {

        private Issue[] lastIssues = Issue.EMPTY_ARRAY;

        private YdbResultSetImpl[] lastResultSets;
        private int resultSetIndex;
        private int updateCount = -1; // TODO: figure out how to get update count from DML

        private Duration queryTimeout;
        private boolean keepInQueryCache;

        private boolean closed;
    }


    // UNSUPPORTED

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null; // --
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException(AUTO_GENERATED_KEYS_UNSUPPORTED);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException(AUTO_GENERATED_KEYS_UNSUPPORTED);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException(AUTO_GENERATED_KEYS_UNSUPPORTED);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException(AUTO_GENERATED_KEYS_UNSUPPORTED);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException(CANNOT_UNWRAP_TO + iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(getClass());
    }
}
