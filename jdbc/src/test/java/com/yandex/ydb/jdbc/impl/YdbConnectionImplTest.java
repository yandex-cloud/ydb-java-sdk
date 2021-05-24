package com.yandex.ydb.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbDatabaseMetaData;
import com.yandex.ydb.jdbc.YdbDriver;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.jdbc.YdbStatement;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.table.values.ListType;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsgLike;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YdbConnectionImplTest extends AbstractTest {
    private YdbConnection connection;

    @BeforeEach
    void beforeEach() throws SQLException {
        connection = getTestConnection();
    }

    @Test
    void createStatement() throws SQLException {
        Statement statement = connection.createStatement();
        assertTrue(statement.execute("select 2 + 2"));
    }

    @Test
    void createStatement2() throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        assertTrue(statement.execute("select 2 + 2"));
    }

    @Test
    void createStatement2s() throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        assertTrue(statement.execute("select 2 + 2"));
    }

    @Test
    void createStatement3() throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.HOLD_CURSORS_OVER_COMMIT);
        assertTrue(statement.execute("select 2 + 2"));
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void createStatementInvalid() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createStatement(
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT),
                "resultSetType must be ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_INSENSITIVE");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createStatement(
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY + 1,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT),
                "resultSetConcurrency must be ResultSet.CONCUR_READ_ONLY");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createStatement(
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT + 1),
                "resultSetHoldability must be ResultSet.HOLD_CURSORS_OVER_COMMIT");
    }

    @Test
    void prepareStatement() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select 2 + 2");
        assertFalse(ps.isClosed());
    }

    @Test
    void prepareStatement1() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select 2 + 2", Statement.NO_GENERATED_KEYS);
        assertFalse(ps.isClosed());
    }

    @Test
    void prepareStatement2() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select 2 + 2",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        assertFalse(ps.isClosed());
    }

    @Test
    void prepareStatement2s() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select 2 + 2",
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        assertFalse(ps.isClosed());
    }

    @Test
    void prepareStatement3() throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select 2 + 2",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        assertFalse(ps.isClosed());
    }

    @SuppressWarnings("MagicConstant")
    @Test
    void prepareStatementInvalid() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareStatement("select 2 + 2", new int[0]),
                "Auto-generated keys are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareStatement("select 2 + 2", new String[0]),
                "Auto-generated keys are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareStatement("select 2 + 2", Statement.RETURN_GENERATED_KEYS),
                "Auto-generated keys are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareStatement("select 2 + 2",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT),
                "resultSetType must be ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_INSENSITIVE");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareStatement("select 2 + 2",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY + 1,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT),
                "resultSetConcurrency must be ResultSet.CONCUR_READ_ONLY");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareStatement("select 2 + 2",
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY,
                        ResultSet.HOLD_CURSORS_OVER_COMMIT + 1),
                "resultSetHoldability must be ResultSet.HOLD_CURSORS_OVER_COMMIT");
    }

    @Test
    void prepareCall() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareCall("select 2 + 2"),
                "Prepared calls are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareCall("select 2 + 2",
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY),
                "Prepared calls are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.prepareCall("select 2 + 2",
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT),
                "Prepared calls are not supported");
    }

    @Test
    void nativeSQL() throws SQLException {
        assertEquals("select ? + ?", connection.nativeSQL("select ? + ?"));
    }

    @Test
    void invalidSQLCancelTransaction() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("select 2 + 2");
        String txId = connection.getYdbTxId();
        assertNotNull(txId);

        assertThrowsMsgLike(YdbNonRetryableException.class,
                () -> statement.execute("select 2 + x"),
                "Column reference 'x'");
        statement.execute("select 2 + 2");
        assertNotNull(connection.getYdbTxId());
        assertNotEquals(txId, connection.getYdbTxId());
    }

    @Test
    void autoCommit() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("select 2 + 2");
        String txId = connection.getYdbTxId();
        assertNotNull(txId);

        assertFalse(connection.getAutoCommit());

        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertEquals(txId, connection.getYdbTxId());

        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit());
        assertNull(connection.getYdbTxId());

        statement.execute("select 2 + 2");
        assertNull(connection.getYdbTxId());

        statement.execute("select 2 + 2");
        assertNull(connection.getYdbTxId());

        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit());
        assertNull(connection.getYdbTxId());
    }

    @Test
    void commit() throws SQLException {
        this.initTestTable();

        connection.commit();
        assertNull(connection.getYdbTxId());

        connection.commit(); // does nothing
        assertNull(connection.getYdbTxId());
    }

    @Test
    void rollback() throws SQLException {
        this.initTestTable();

        connection.rollback();
        assertNull(connection.getYdbTxId());

        connection.rollback(); // does nothing
        assertNull(connection.getYdbTxId());
    }

    @Test
    void commitInvalidTx() throws SQLException {
        cleanupSimpleTestTable();

        Statement statement = connection.createStatement();
        statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2')");
        statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2')");

        assertThrowsMsgLike(YdbNonRetryableException.class,
                () -> statement.executeQuery("select * from unit_1"),
                "Data modifications previously made to table");

        assertNull(connection.getYdbTxId());

        connection.commit(); // Нечего коммитить - транзакция уже откатилась
        assertNull(connection.getYdbTxId());

        YdbResultSet result = (YdbResultSet) statement.executeQuery("select * from unit_1");
        assertEquals(0, result.getYdbResultSetReader().getRowCount());

    }

    @Test
    void rollbackInvalidTx() throws SQLException {
        cleanupSimpleTestTable();

        Statement statement = connection.createStatement();
        statement.execute("insert into unit_1(key, c_Utf8) values (1, '2')");

        assertThrowsMsg(YdbNonRetryableException.class,
                () -> statement.executeQuery("select * from unit_1"),
                e -> Assertions.assertTrue(e.getMessage().contains("Data modifications previously made to table")),
                null);

        assertNull(connection.getYdbTxId());

        connection.rollback();

        assertNull(connection.getYdbTxId());

        YdbResultSet result = (YdbResultSet) statement.executeQuery("select * from unit_1");
        assertEquals(0, result.getYdbResultSetReader().getRowCount());

    }

    @Test
    void close() throws SQLException {
        connection.close();
        connection.close(); // no effect
    }

    @Test
    void isClosed() throws SQLException {
        assertFalse(connection.isClosed());
        connection.close();
        assertTrue(connection.isClosed());
    }

    @Test
    void getMetaData() throws SQLException {
        YdbDatabaseMetaData metaData = connection.getMetaData();
        assertNotNull(metaData);
    }

    @Test
    void readOnly() throws SQLException {
        assertFalse(connection.isReadOnly());
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation());

        connection.setReadOnly(true);

        assertTrue(connection.isReadOnly());
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, connection.getTransactionIsolation());

        connection.setReadOnly(false);
        assertFalse(connection.isReadOnly());
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation());
    }

    @Test
    void catalog() throws SQLException {
        connection.setCatalog("any");
        assertNull(connection.getCatalog());
    }

    @ParameterizedTest
    @MethodSource("supportedIsolations")
    void transactionIsolation(int level) throws SQLException {
        connection.setTransactionIsolation(level);
        assertEquals(level, connection.getTransactionIsolation());

        Statement statement = connection.createStatement();
        statement.execute("select 2 + 2");
    }

    @ParameterizedTest
    @MethodSource("unsupportedIsolations")
    void transactionIsolationInvalid(int level) {
        assertThrows(SQLException.class,
                () -> connection.setTransactionIsolation(level),
                "Unsupported transaction level: " + level);
    }

    @Test
    void getWarnings() throws SQLException {
        // TODO: generate warnings
        assertNull(connection.getWarnings());
    }

    @Test
    void clearWarnings() throws SQLException {
        assertNull(connection.getWarnings());
        connection.clearWarnings();
        assertNull(connection.getWarnings());
    }

    @Test
    void typeMap() throws SQLException {
        assertEquals(new HashMap<>(), connection.getTypeMap());

        Map<String, Class<?>> newMap = new HashMap<>();
        newMap.put("type1", String.class);
        connection.setTypeMap(newMap);

        // not implemented
        assertEquals(new HashMap<>(), connection.getTypeMap());
    }


    @Test
    void holdability() throws SQLException {
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, connection.getHoldability());
        connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT + 1),
                "resultSetHoldability must be ResultSet.HOLD_CURSORS_OVER_COMMIT");
    }


    @Test
    void setSavepoint() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.setSavepoint(),
                "Savepoints are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.setSavepoint("name"),
                "Savepoints are not supported");
    }

    @Test
    void releaseSavepoint() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.releaseSavepoint(null),
                "Savepoints are not supported");
    }

    @Test
    void createClob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createClob(),
                "Clobs are not supported");
    }

    @Test
    void createBlob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createBlob(),
                "Blobs are not supported");
    }

    @Test
    void createNClob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createNClob(),
                "NClobs are not supported");
    }

    @Test
    void createSQLXML() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createSQLXML(),
                "SQLXMLs are not supported");
    }

    @Test
    void isValid() throws SQLException {
        assertTrue(connection.isValid(1));
        YdbDriver.getConnectionsCache().close();
        try {
            assertFalse(connection.isValid(1));
        } finally {
            connection = null;
        }
    }

    @Test
    void clientInfo() throws SQLException {
        assertEquals(new Properties(), connection.getClientInfo());

        Properties properties = new Properties();
        properties.setProperty("key", "value");
        connection.setClientInfo(properties);

        assertEquals(new Properties(), connection.getClientInfo());
        connection.setClientInfo("key", "value");

        assertEquals(new Properties(), connection.getClientInfo());
        assertNull(connection.getClientInfo("key"));
    }

    @Test
    void createArrayOf() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createArrayOf("type", new Object[0]),
                "Arrays are not supported");
    }

    @Test
    void createStruct() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.createStruct("type", new Object[0]),
                "Structs are not supported");
    }

    @Test
    void schema() throws SQLException {
        assertNull(connection.getSchema());
        connection.setSchema("test");
        assertNull(connection.getSchema());
    }


    @Test
    void abort() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.abort(null),
                "Abort operation is not supported yet");
    }

    @Test
    void setNetworkTimeout() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> connection.setNetworkTimeout(null, 1),
                "Set network timeout is not supported yet");
    }

    @Test
    void getNetworkTimeout() throws SQLException {
        assertEquals(0, connection.getNetworkTimeout());
    }

    @ParameterizedTest
    @MethodSource("unsupportedTypes")
    void testUnsupportedTableTypes(String paramName, String sqlExpression, String expectedError)
            {
        String tableName = "unsupported_" + paramName;
        String sql = String.format("--jdbc:SCHEME\ncreate table ${tableName} (key Int32, %s %s, primary key(key))",
                paramName, sqlExpression);
        assertThrowsMsgLike(YdbNonRetryableException.class,
                () -> createTestTable(tableName, sql),
                expectedError);
    }

    @Test
    void testDDLInsideTransaction() throws SQLException {
        YdbStatement statement = connection.createStatement();

        statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2')");
        statement.executeSchemeQuery("create table unit_11(id Int32, value Int32, primary key(id))");
        try {
            // No commits in case of ddl
            assertThrowsMsgLike(YdbNonRetryableException.class,
                    () -> statement.executeQuery("select * from unit_1"),
                    "Data modifications previously made to table");
        } finally {
            statement.executeSchemeQuery("drop table unit_11");
        }
    }

    @Test
    void testWarningInIndexUsage() throws SQLException {
        connection.createStatement().executeSchemeQuery("--!syntax_v1\n" +
                "create table unit_0_indexed (" +
                "id Int32, value Int32, " +
                "primary key (id), " +
                "index idx_value global on (value))");

        String query = "--!syntax_v1\n" +
                "declare $list as List<Int32>;\n" +
                "select * from unit_0_indexed view idx_value where value in $list;";

        ListValue value = ListType.of(PrimitiveType.int32()).newValue(
                Arrays.asList(PrimitiveValue.int32(1), PrimitiveValue.int32(2)));
        YdbPreparedStatement ps = connection.prepareStatement(query);
        ps.setObject("list", value);
        YdbResultSet rs = ps.executeQuery();
        assertFalse(rs.next());

        SQLWarning warnings = ps.getWarnings();
        assertNotNull(warnings);

        // TODO: add proper tests and driver property after https://a.yandex-team.ru/review/1781801/details merge
        System.out.println("WARNING: " + warnings);
    }

    //


    private void initTestTable() throws SQLException {
        assertNull(connection.getYdbTxId());

        Statement statement = connection.createStatement();
        assertTrue(statement.execute("select 2 + 2"));
        String txId = connection.getYdbTxId();
        assertNotNull(txId);

        assertTrue(statement.execute("select 2 + 2"));
        assertEquals(txId, connection.getYdbTxId());

        assertTrue(statement.execute("select * from unit_1"));
        assertEquals(txId, connection.getYdbTxId());

        assertFalse(statement.execute("upsert into unit_1(key, c_Utf8) values (1, '2')"));
        assertEquals(txId, connection.getYdbTxId());

        assertTrue(statement.execute("select 2 + 2"));
        assertEquals(txId, connection.getYdbTxId());
    }

    static List<Integer> supportedIsolations() {
        return Arrays.asList(
                YdbConst.TRANSACTION_SERIALIZABLE_READ_WRITE,
                YdbConst.ONLINE_CONSISTENT_READ_ONLY,
                YdbConst.ONLINE_INCONSISTENT_READ_ONLY,
                YdbConst.STALE_CONSISTENT_READ_ONLY);
    }

    static List<Integer> unsupportedIsolations() {
        List<Integer> unsupported = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        unsupported.removeAll(supportedIsolations());
        assertEquals(6, unsupported.size());
        return unsupported;
    }

    static List<Arguments> unsupportedTypes() {
        String simpleTypeError = "is not supported by storage";
        String complexTypeError = "Only core YQL data types are currently supported";
        return Arrays.asList(
                Arguments.of("c_Int8", "Int8", simpleTypeError),
                Arguments.of("c_Int16", "Int16", simpleTypeError),
                Arguments.of("c_Uint16", "Uint16", simpleTypeError),
                Arguments.of("c_Uuid", "Uuid", simpleTypeError),
                Arguments.of("c_TzDate", "TzDate", simpleTypeError),
                Arguments.of("c_TzDatetime", "TzDatetime", simpleTypeError),
                Arguments.of("c_TzTimestamp", "TzTimestamp", simpleTypeError),
                Arguments.of("c_List", "List<Int32>", complexTypeError),
                Arguments.of("c_Struct", "Struct<name:Int32>", complexTypeError),
                Arguments.of("c_Tuple", "Tuple<Int32>", complexTypeError),
                Arguments.of("c_Dict", "Dict<Utf8,Int32>", complexTypeError));
    }

}
