package com.yandex.ydb.jdbc.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.jdbc.YdbStatement;
import com.yandex.ydb.jdbc.exception.YdbConditionallyRetryableException;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.table.result.ResultSetReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.TestHelper.TEST_TYPE;
import static com.yandex.ydb.jdbc.TestHelper.UNIVERSAL;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsgLike;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfSystemProperty(named = TEST_TYPE, matches = UNIVERSAL)
class YdbStatementImplTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(YdbStatementImplTest.class);

    private YdbConnection connection;
    private YdbStatement statement;

    @BeforeEach
    void beforeEach() throws SQLException {
        this.connection = getTestConnection();
        this.statement = connection.createStatement();
        this.configureOnce(AbstractTest::recreateSimpleTestTable);
    }

    @Test
    void executeQuery() throws SQLException {
        ResultSet rs = statement.executeQuery("select 2 + 2");
        assertFalse(rs.isClosed());
    }

    @Test
    void executeUpdate() throws SQLException {
        assertEquals(1, statement.executeUpdate("upsert into unit_1(key, c_Utf8) values (1, '2')"));
        assertEquals(1, statement.executeUpdate("upsert into unit_1(key, c_Utf8) values (2, '3')",
                Statement.NO_GENERATED_KEYS));
    }

    @Test
    void executeUpdateInvalid() {
        assertThrowsMsg(SQLException.class,
                () -> statement.executeUpdate("select 2 + 2"), // Cannot be select operation
                "Query must not return ResultSet");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> statement.executeUpdate("select 2 + 2", new int[0]),
                "Auto-generated keys are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> statement.executeUpdate("select 2 + 2", new String[0]),
                "Auto-generated keys are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> statement.executeUpdate("select 2 + 2", Statement.RETURN_GENERATED_KEYS),
                "Auto-generated keys are not supported");

    }

    @Test
    void executeSchemeQueryExplicitly() throws SQLException {
        createTestTable("unit_1_tmp", CREATE_TABLE);
        assertThrowsMsgLike(YdbNonRetryableException.class,
                () -> connection.createStatement().execute("drop table unit_1_tmp"),
                "'DROP TABLE' not supported in query prepare mode");

        connection.createStatement().executeSchemeQuery("drop table unit_1_tmp");
    }

    @Test
    void executeDataQuery() throws SQLException {
        ResultSet rs = statement.executeQuery("--jdbc:DATA\n" +
                "select 2 + 2");
        assertFalse(rs.isClosed());

        assertTrue(rs.next());
        assertEquals(4, rs.getInt(1));
        assertFalse(rs.next());
    }

    @Test
    void executeDataQueryInTx() throws SQLException {
        // Cannot select from table already updated in transaction
        statement.executeUpdate("upsert into unit_1(key, c_Utf8) values (1, '2')");
        assertThrowsMsgLike(YdbNonRetryableException.class,
                () -> statement.executeQuery("select * from unit_1"),
                "Data modifications previously made to table");
    }

    @Test
    void executeScanQuery() throws SQLException, NoSuchFieldException, IllegalAccessException {
        YdbResultSet rs = statement.executeQuery("--jdbc:SCAN\n" +
                "select 2 + 2");
        assertFalse(rs.isClosed());


        // FOR TESTING PURPOSES
        ResultSetReader reader = rs.getYdbResultSetReader();
        LOGGER.info("Reader: {}", reader);
        Field field = reader.getClass().getDeclaredField("resultSet");
        field.setAccessible(true);

        LOGGER.info("Original proto object: {}", field.get(reader));

        assertTrue(rs.next());
        assertEquals(4, rs.getInt(1));
        assertFalse(rs.next());
    }

    @Test
    void executeScanQueryOnSystemTable() throws SQLException {
        YdbResultSet rs = statement.executeScanQuery("select * from `.sys/partition_stats`");
        assertTrue(rs.next());
    }

    @Test
    void executeScanQueryMultiResult() {
        assertThrowsMsgLike(YdbConditionallyRetryableException.class,
                () -> statement.executeUpdate("--jdbc:SCAN\n" +
                        "select 2 + 2;select 2 + 3"),
                "Scan query should have a single result set");
    }

    @Test
    void executeScanQueryInTx() throws SQLException {
        cleanupSimpleTestTable();
        statement.executeUpdate("upsert into unit_1(key, c_Utf8) values (1, '2')");
        ResultSet rs = statement.executeQuery("--jdbc:SCAN\n" +
                "select * from unit_1");
        assertFalse(rs.next());

        // alternative mode
        rs = statement.executeQuery("!--jdbc:SCAN\n" +
                "select * from unit_1");
        assertFalse(rs.next());

        connection.commit();
        rs = statement.executeQuery("--jdbc:SCAN\n" +
                "select * from unit_1");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("key"));

    }

    @Test
    void executeScanQueryExplicitlyInTx() throws SQLException {
        cleanupSimpleTestTable();

        statement.executeUpdate("upsert into unit_1(key, c_Utf8) values (1, '2')");
        ResultSet rs = statement.executeScanQuery("select * from unit_1");
        assertFalse(rs.next());

        connection.commit();
        rs = statement.executeScanQuery("select * from unit_1");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("key"));

    }

    @Test
    void executeScanQueryAsUpdate() {
        // Looks weird
        assertThrowsMsgLike(YdbConditionallyRetryableException.class,
                () -> statement.executeUpdate("--jdbc:SCAN\n" +
                        "upsert into unit_1(key, c_Utf8) values (1, '2')"),
                "Scan query should have a single result set");
    }

    @Test
    void executeQueryExplainAndExplicitly() throws SQLException {
        ResultSet rs = statement.executeQuery("--jdbc:EXPLAIN\n" +
                "select 2 + 2");
        assertFalse(rs.isClosed());

        String ast = "AST";
        String plan = "PLAN";

        assertTrue(rs.next());
        assertNotNull(rs.getString(ast));
        assertNotNull(rs.getString(plan));
        LOGGER.info("AST: {}", rs.getString(ast));
        LOGGER.info("PLAN: {}", rs.getString(plan));
        assertFalse(rs.next());

        rs = statement.executeQuery("--jdbc:EXPLAIN\n" +
                "upsert into unit_1(key, c_Utf8) values (1, '2')");
        assertTrue(rs.next());
        LOGGER.info("AST: {}", rs.getString(ast));
        LOGGER.info("PLAN: {}", rs.getString(plan));
        assertFalse(rs.next());

        rs = statement.executeExplainQuery("select * from unit_1");
        assertTrue(rs.next());
        LOGGER.info("AST: {}", rs.getString(ast));
        LOGGER.info("PLAN: {}", rs.getString(plan));
        assertFalse(rs.next());
    }

    @Test
    void close() throws SQLException {
        assertFalse(statement.isClosed());
        statement.close();
        assertTrue(statement.isClosed());
    }

    @Test
    void maxFieldSize() throws SQLException {
        assertEquals(0, statement.getMaxFieldSize());
        statement.setMaxFieldSize(99); // do nothing
        assertEquals(0, statement.getMaxFieldSize());
    }

    @Test
    void maxRows() throws SQLException {
        assertEquals(1000, statement.getMaxRows());
        statement.setMaxRows(99); // do nothing
        assertEquals(1000, statement.getMaxRows());
    }

    @Test
    void setEscapeProcessing() throws SQLException {
        statement.setEscapeProcessing(true); // do nothing
        statement.setEscapeProcessing(false);
        statement.setEscapeProcessing(true);
    }

    @Test
    void queryTimeout() throws SQLException {
        assertEquals(0, statement.getQueryTimeout());
        statement.setQueryTimeout(3);
        assertEquals(3, statement.getQueryTimeout());
    }

    @Test
    void cancel() throws SQLException {
        statement.cancel(); // do nothing
        statement.cancel();
    }

    @Test
    void warnings() throws SQLException {
        // TODO: check warnings
        assertNull(statement.getWarnings());
        statement.clearWarnings();
        assertNull(statement.getWarnings());
    }


    @Test
    void setCursorName() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> statement.setCursorName("cursor"),
                "Named cursors are not supported");
    }

    @Test
    void execute() throws SQLException {
        assertTrue(statement.execute("select 2 + 2"));
        assertFalse(statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2')"));

        assertTrue(statement.execute("select 2 + 2", Statement.NO_GENERATED_KEYS));
    }

    @Test
    void executeInvalid() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> statement.execute("select 2 + 2", new int[0]),
                "Auto-generated keys are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> statement.execute("select 2 + 2", new String[0]),
                "Auto-generated keys are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> statement.execute("select 2 + 2", Statement.RETURN_GENERATED_KEYS),
                "Auto-generated keys are not supported");
    }

    @Test
    void getResultSet() throws SQLException {
        assertNull(statement.getResultSet());

        statement.execute("select 2 + 2");
        assertNotNull(statement.getResultSet());

        statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2')");
        assertNull(statement.getResultSet());
    }

    @Test
    void getUpdateCount() throws SQLException {
        assertEquals(-1, statement.getUpdateCount());

        statement.execute("select 2 + 2");
        assertEquals(-1, statement.getUpdateCount());

        statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2')");
        assertEquals(1, statement.getUpdateCount());

        statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2');\n" +
                "upsert into unit_1(key, c_Utf8) values (2, '3');");
        assertEquals(1, statement.getUpdateCount()); // just a single statement

        statement.execute("select 2 + 2");
        assertEquals(-1, statement.getUpdateCount());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void getMoreResults() throws SQLException {
        assertFalse(statement.getMoreResults());
        assertFalse(statement.getMoreResults());

        statement.execute("select 2 + 2");
        assertNotNull(statement.getResultSet());
        assertFalse(statement.getMoreResults());


        statement.execute("select 1 + 2; select 2 + 3; select 3 + 4");
        ResultSet rs0 = statement.getResultSet();
        assertTrue(statement.getMoreResults());
        ResultSet rs1 = statement.getResultSet();
        assertTrue(statement.getMoreResults());
        ResultSet rs2 = statement.getResultSet();
        assertFalse(statement.getMoreResults());

        assertNotSame(rs0, rs1);
        assertNotSame(rs0, rs2);

        assertSame(rs0, statement.getResultSetAt(0).get());
        assertSame(rs1, statement.getResultSetAt(1).get());
        assertSame(rs2, statement.getResultSetAt(2).get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void getMoreResultsDifferentMode() throws SQLException {
        statement.execute("select 1 + 2; select 2 + 3; select 3 + 4");

        ResultSet rs0 = statement.getResultSet();
        assertTrue(statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT));
        assertNull(statement.getResultSetAt(0).orElse(null));

        ResultSet rs1 = statement.getResultSet();
        assertTrue(statement.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        assertSame(rs1, statement.getResultSetAt(1).get());

        ResultSet rs2 = statement.getResultSet();
        assertFalse(statement.getMoreResults(Statement.CLOSE_ALL_RESULTS));

        assertNull(statement.getResultSetAt(1).orElse(null));
        assertSame(rs2, statement.getResultSetAt(2).get());

        assertNotSame(rs0, rs1);
        assertNotSame(rs0, rs2);
    }

    @Test
    void fetchDirection() throws SQLException {
        assertEquals(ResultSet.FETCH_FORWARD, statement.getFetchDirection());

        statement.setFetchDirection(ResultSet.FETCH_FORWARD);
        assertEquals(ResultSet.FETCH_FORWARD, statement.getFetchDirection());

        statement.setFetchDirection(ResultSet.FETCH_UNKNOWN);
        assertEquals(ResultSet.FETCH_FORWARD, statement.getFetchDirection());

        assertThrowsMsg(SQLException.class,
                () -> statement.setFetchDirection(ResultSet.FETCH_REVERSE),
                "Direction is not supported: " + ResultSet.FETCH_REVERSE);
    }

    @Test
    void fetchSize() throws SQLException {
        assertEquals(1000, statement.getFetchSize());
        statement.setFetchSize(100); // do nothing
        assertEquals(1000, statement.getFetchSize());
    }

    @Test
    void getResultSetConcurrency() throws SQLException {
        assertEquals(ResultSet.CONCUR_READ_ONLY, statement.getResultSetConcurrency());
    }

    @Test
    void getResultSetType() throws SQLException {
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, statement.getResultSetType());
    }

    @Test
    void executeBatch() throws SQLException {
        cleanupSimpleTestTable();

        statement.addBatch("upsert into unit_1(key, c_Utf8) values (1, '2')");
        statement.addBatch("upsert into unit_1(key, c_Utf8) values (2, '3')");
        statement.addBatch("upsert into unit_1(key, c_Utf8) values (3, '4')");

        int NI = Statement.SUCCESS_NO_INFO;
        assertArrayEquals(new int[]{NI, NI, NI}, statement.executeBatch());
        assertNull(statement.getResultSet());

        assertArrayEquals(new int[0], statement.executeBatch()); // Second run does nothing - batch is cleared

        connection.commit();

        YdbResultSet result = statement.executeQuery("select * from unit_1");
        assertEquals(3, result.getYdbResultSetReader().getRowCount());
    }

    @Test
    void clearBatch() throws SQLException {

        statement.addBatch("upsert into unit_1(key, c_Utf8) values (1, '2')");
        statement.addBatch("upsert into unit_1(key, c_Utf8) values (2, '3')");
        statement.addBatch("upsert into unit_1(key, c_Utf8) values (3, '4')");
        statement.clearBatch();

        assertArrayEquals(new int[0], statement.executeBatch()); // no actions were executed
    }

    @Test
    void getConnection() throws SQLException {
        assertSame(connection, statement.getConnection());
    }


    @Test
    void getGeneratedKeys() throws SQLException {
        assertNull(statement.getGeneratedKeys());
    }

    @Test
    void getResultSetHoldability() throws SQLException {
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, statement.getResultSetHoldability());
    }

    @Test
    void poolable() throws SQLException {
        assertFalse(statement.isPoolable());
        statement.execute("select 1 + 1");

        statement.setPoolable(true);
        assertTrue(statement.isPoolable());
        statement.execute("select 1 + 1");

        statement.setPoolable(false);
        assertFalse(statement.isPoolable());
        statement.execute("select 1 + 1");

        // basically no visible effect
    }

    @Test
    void closeOnCompletion() throws SQLException {
        assertFalse(statement.isCloseOnCompletion());
        statement.closeOnCompletion();

        assertFalse(statement.isCloseOnCompletion());
    }

    @Test
    void unwrap() throws SQLException {
        assertTrue(statement.isWrapperFor(YdbStatement.class));
        assertSame(statement, statement.unwrap(YdbStatement.class));

        assertFalse(statement.isWrapperFor(YdbPreparedStatement.class));
        assertThrowsMsg(SQLException.class,
                () -> statement.unwrap(YdbPreparedStatement.class),
                "Cannot unwrap to " + YdbPreparedStatement.class);
    }
}
