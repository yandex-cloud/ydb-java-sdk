package com.yandex.ydb.jdbc;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public interface YdbStatement extends Statement {

    // TODO: add future calls

    // TODO: add bulk upsert

    //

    /**
     * Explicitly execute query as a schema query
     *
     * @param sql query (DDL) to execute
     * @throws SQLException if query cannot be executed
     */
    void executeSchemeQuery(String sql) throws SQLException;

    /**
     * Explicitly execute query as a scan query
     *
     * @param sql query to execute
     * @return result set
     * @throws SQLException if query cannot be executed
     */
    YdbResultSet executeScanQuery(String sql) throws SQLException;

    /**
     * Explicitly explain this query
     *
     * @param sql query to explain
     * @return result set of two string columns: {@link YdbConst#EXPLAIN_COLUMN_AST}
     * and {@link YdbConst#EXPLAIN_COLUMN_PLAN}
     * @throws SQLException if query cannot be explained
     */
    YdbResultSet executeExplainQuery(String sql) throws SQLException;

    /**
     * Cant return previous results sets after {@link #getMoreResults()} traverse,
     * in case previous rows were not removed by providing {@link Statement#CLOSE_ALL_RESULTS}
     * or {@link Statement#CLOSE_CURRENT_RESULT}
     *
     * @param resultSetIndex index
     * @return optional result (if traversed before) with {@link #getMoreResults()}
     * @throws SQLException when trying to get result set on closed connection
     */
    Optional<YdbResultSet> getResultSetAt(int resultSetIndex) throws SQLException;

    @Override
    YdbResultSet executeQuery(String sql) throws SQLException;

    @Override
    YdbResultSet getResultSet() throws SQLException;

    @Override
    YdbConnection getConnection() throws SQLException;

}
