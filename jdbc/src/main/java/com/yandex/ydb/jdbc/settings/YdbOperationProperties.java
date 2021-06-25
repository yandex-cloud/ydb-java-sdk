package com.yandex.ydb.jdbc.settings;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class YdbOperationProperties {
    public static final int MAX_ROWS = 1000; // TODO: how to figure out the max rows of current connection?

    private final Map<YdbOperationProperty<?>, ParsedProperty> params;
    private final Duration joinDuration;
    private final boolean keepInQueryCache;
    private final Duration queryTimeout;
    private final Duration scanQueryTimeout;
    private final boolean failOnTruncatedResult;
    private final Duration sessionTimeout;
    private final Duration deadlineTimeout;
    private final boolean autoCommit;
    private final int transactionLevel;
    private final int maxRows;
    private final boolean autoPreparedBatches;
    private final boolean enforceSqlV1;
    private final boolean enforceVariablePrefix;
    private final boolean cacheConnectionsInDriver;
    private final boolean detectSqlOperations;
    private final boolean alwaysPrepareDataQuery;
    private final boolean transformStandardJdbcQueries;
    private final int transformedJdbcQueriesCache;

    public YdbOperationProperties(Map<YdbOperationProperty<?>, ParsedProperty> params) {
        this.params = Objects.requireNonNull(params);

        this.joinDuration = params.get(YdbOperationProperty.JOIN_DURATION).getParsedValue();
        this.keepInQueryCache = params.get(YdbOperationProperty.KEEP_IN_QUERY_CACHE).getParsedValue();
        this.queryTimeout = params.get(YdbOperationProperty.QUERY_TIMEOUT).getParsedValue();
        this.scanQueryTimeout = params.get(YdbOperationProperty.SCAN_QUERY_TIMEOUT).getParsedValue();
        this.failOnTruncatedResult = params.get(YdbOperationProperty.FAIL_ON_TRUNCATED_RESULT).getParsedValue();
        this.sessionTimeout = params.get(YdbOperationProperty.SESSION_TIMEOUT).getParsedValue();
        this.deadlineTimeout = params.get(YdbOperationProperty.DEADLINE_TIMEOUT).getParsedValue();
        this.autoCommit = params.get(YdbOperationProperty.AUTOCOMMIT).getParsedValue();
        this.transactionLevel = params.get(YdbOperationProperty.TRANSACTION_LEVEL).getParsedValue();
        this.maxRows = MAX_ROWS;
        this.autoPreparedBatches = params.get(YdbOperationProperty.AUTO_PREPARED_BATCHES).getParsedValue();
        this.enforceSqlV1 = params.get(YdbOperationProperty.ENFORCE_SQL_V1).getParsedValue();
        this.enforceVariablePrefix = params.get(YdbOperationProperty.ENFORCE_VARIABLE_PREFIX).getParsedValue();
        this.cacheConnectionsInDriver = params.get(YdbOperationProperty.CACHE_CONNECTIONS_IN_DRIVER).getParsedValue();
        this.detectSqlOperations = params.get(YdbOperationProperty.DETECT_SQL_OPERATIONS).getParsedValue();
        this.alwaysPrepareDataQuery = params.get(YdbOperationProperty.ALWAYS_PREPARE_DATAQUERY).getParsedValue();
        this.transformStandardJdbcQueries =
                params.get(YdbOperationProperty.TRANSFORM_STANDARD_JDBC_QUERIES).getParsedValue();
        this.transformedJdbcQueriesCache =
                params.get(YdbOperationProperty.TRANSFORMED_JDBC_QUERIES_CACHE).getParsedValue();
    }

    public Map<YdbOperationProperty<?>, ParsedProperty> getParams() {
        return params;
    }

    public Duration getJoinDuration() {
        return joinDuration;
    }

    public boolean isKeepInQueryCache() {
        return keepInQueryCache;
    }

    public Duration getQueryTimeout() {
        return queryTimeout;
    }

    public Duration getScanQueryTimeout() {
        return scanQueryTimeout;
    }

    public boolean isFailOnTruncatedResult() {
        return failOnTruncatedResult;
    }

    public Duration getSessionTimeout() {
        return sessionTimeout;
    }

    public Duration getDeadlineTimeout() {
        return deadlineTimeout;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public int getTransactionLevel() {
        return transactionLevel;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public boolean isAutoPreparedBatches() {
        return autoPreparedBatches;
    }

    public boolean isEnforceSqlV1() {
        return enforceSqlV1;
    }

    public boolean isEnforceVariablePrefix() {
        return enforceVariablePrefix;
    }

    public boolean isCacheConnectionsInDriver() {
        return cacheConnectionsInDriver;
    }

    public boolean isDetectSqlOperations() {
        return detectSqlOperations;
    }

    public boolean isAlwaysPrepareDataQuery() {
        return alwaysPrepareDataQuery;
    }

    public boolean isTransformStandardJdbcQueries() {
        return transformStandardJdbcQueries;
    }

    public int getTransformedJdbcQueriesCache() {
        return transformedJdbcQueriesCache;
    }
}
