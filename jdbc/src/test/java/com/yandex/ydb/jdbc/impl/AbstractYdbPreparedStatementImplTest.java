package com.yandex.ydb.jdbc.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.sql.rowset.serial.SerialBlob;

import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbParameterMetaData;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.jdbc.exception.YdbConditionallyRetryableException;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.jdbc.exception.YdbRetryableException;
import com.yandex.ydb.table.values.DecimalType;
import com.yandex.ydb.table.values.DecimalValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.MultipleFailuresError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsgLike;
import static com.yandex.ydb.jdbc.impl.AbstractYdbPreparedStatementImplTest.Pair.pair;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractYdbPreparedStatementImplTest extends AbstractTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    static final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);
    static final long MICROS_IN_DAY = TimeUnit.DAYS.toMicros(1);

    @BeforeEach
    void beforeEach() throws SQLException {
        configureOnce(AbstractTest::recreatePreparedTestTable);
    }

    @Test
    void executeQuery() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.execute();
            connection.commit();

            statement = connection
                    .prepareStatement("declare $key as Int32?;" +
                            "select c_Utf8 from unit_2 where key = $key");
            statement.setInt("key", 2);
            ResultSet rs = statement.executeQuery();
            assertFalse(rs.next());

            statement.setInt("key", 1);
            rs = statement.executeQuery();

            assertTrue(rs.next());
            assertEquals("value-1", rs.getString("c_Utf8"));

            assertFalse(rs.next());
        });
    }

    @Test
    void execute() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.execute();

            statement.setInt("key", 2);
            statement.setString("c_Utf8", "value-2");
            statement.execute();
            connection.commit();

            checkSimpleResultSet(connection);
        });
    }

    @Test
    void executeDataQuery() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.execute();
            connection.commit();

            statement = connection
                    .prepareStatement("declare $key as Int32?;" +
                            "select c_Utf8 from unit_2 where key = $key");
            statement.setInt("key", 2);
            ResultSet rs = statement.executeQuery();
            assertFalse(rs.next());

            statement.setInt("key", 1);
            rs = statement.executeQuery();

            assertTrue(rs.next());
            assertEquals("value-1", rs.getString("c_Utf8"));

            assertFalse(rs.next());
        });
    }

    @Test
    void executeQueryInTx() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.execute();

            YdbPreparedStatement selectStatement = connection
                    .prepareStatement("declare $key as Int32?;" +
                            "select c_Utf8 from unit_2 where key = $key");
            selectStatement.setInt("key", 1);
            assertThrowsMsgLike(YdbNonRetryableException.class,
                    selectStatement::executeQuery,
                    "Data modifications previously made to table");
        });
    }

    @Test
    void executeScanQueryInTx() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.execute();

            YdbPreparedStatement selectStatement = connection
                    .prepareStatement("--jdbc:SCAN\n" +
                            "declare $key as Int32?;" +
                            "select c_Utf8 from unit_2 where key = $key");
            selectStatement.setInt("key", 1);
            ResultSet rs = selectStatement.executeQuery();
            assertFalse(rs.next());

            connection.commit();

            selectStatement.setInt("key", 1);
            rs = selectStatement.executeQuery();
            assertTrue(rs.next());
            assertEquals("value-1", rs.getString("c_Utf8"));
            assertFalse(rs.next());
        });
    }

    @Test
    void executeScanQueryExplicitlyInTx() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.execute();

            YdbPreparedStatement selectStatement = connection
                    .prepareStatement("declare $key as Int32?;" +
                            "select c_Utf8 from unit_2 where key = $key");
            selectStatement.setInt("key", 1);
            ResultSet rs = selectStatement.executeScanQuery();
            assertFalse(rs.next());

            connection.commit();

            rs = selectStatement.executeScanQuery();
            assertTrue(rs.next());
            assertEquals("value-1", rs.getString("c_Utf8"));
            assertFalse(rs.next());
        });
    }

    @Test
    void executeScanQueryAsUpdate() throws SQLException {
        retry(connection -> {
            String query = getUtf8Statement(connection).getQuery();

            YdbPreparedStatement statement = connection
                    .prepareStatement(insertMode(query, "--jdbc:SCAN"));
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            assertThrowsMsgLike(YdbConditionallyRetryableException.class,
                    statement::execute,
                    "Scan query should have a single result set");
        });
    }

    @ParameterizedTest
    @EnumSource(QueryType.class)
    void executeUnsupportedModes(QueryType type) throws SQLException {
        switch (type) {
            case DATA_QUERY:
            case SCAN_QUERY:
                return; // --- supported
        }
        retry(connection -> {
            String query = getUtf8Statement(connection).getQuery();
            assertThrowsMsg(SQLException.class,
                    () -> connection.prepareStatement(insertMode(query, type.getPrefix())),
                    String.format("Query type in prepared statement not supported: %s", type));
        });
    }

    @Test
    void executeExplainQueryExplicitly() throws SQLException {
        retry(connection -> {

            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.execute();

            ResultSet rs = statement.executeExplainQuery();
            String ast = "AST";
            String plan = "PLAN";

            assertTrue(rs.next());
            assertNotNull(rs.getString(ast));
            assertNotNull(rs.getString(plan));
            logger.info("AST: {}", rs.getString(ast));
            logger.info("PLAN: {}", rs.getString(plan));
            assertFalse(rs.next());

            YdbPreparedStatement selectStatement = connection
                    .prepareStatement("declare $key as Int32;" +
                            "select c_Utf8 from unit_2 where key = $key");
            rs = selectStatement.executeExplainQuery();
            assertTrue(rs.next());
            logger.info("AST: {}", rs.getString(ast));
            logger.info("PLAN: {}", rs.getString(plan));
            assertFalse(rs.next());
        });
    }

    @Test
    void executeRequired() throws SQLException {
        retry(connection ->
                assertThrowsMsgLike(SQLException.class,
                        () -> {
                            YdbPreparedStatement statement = getTestStatement(connection, "c_Utf8", "Utf8");
                            statement.setInt("key", 1);
                            statement.setObject("c_Utf8", PrimitiveType.utf8().makeOptional().emptyValue());
                            statement.execute();
                        },
                        "Missing required value for parameter"));
    }

    @Test
    void executePartialSet() throws SQLException {
        retry(connection ->
                assertThrowsMsgLike(SQLException.class,
                        () -> {
                            YdbPreparedStatement statement = getTestStatement(connection, "c_Utf8", "Utf8");
                            statement.setInt("key", 1); // no c_Utf8 param in both simple and batched PS is an error
                            statement.execute();
                        },
                        "Missing value for parameter"));
    }

    @Test
    void executeUpdate() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeSchemeQuery("select 1 + 2"),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeScanQuery("select 1 + 2"),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeExplainQuery("select 1 + 2"),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeQuery("select 1 + 2"),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.execute("select 1 + 2"),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.execute("select 1 + 2", Statement.NO_GENERATED_KEYS),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.execute("select 1 + 2", new int[0]),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.execute("select 1 + 2", new String[0]),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeUpdate("select 1 + 2"),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeUpdate("select 1 + 2", Statement.NO_GENERATED_KEYS),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeUpdate("select 1 + 2", new int[0]),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.executeUpdate("select 1 + 2", new String[0]),
                    "PreparedStatement cannot execute custom SQL");

            assertThrowsMsg(SQLException.class,
                    () -> statement.addBatch("select 1 + 2"),
                    "PreparedStatement cannot execute custom SQL");

        });
    }

    @SuppressWarnings("deprecation")
    @Test
    void testIndexed() throws SQLException {
        retry(connection -> {
            if (supportIndexedParameters()) {
                return; // ---
            }

            YdbPreparedStatement statement = getTestStatementIndexed(connection, "c_Utf8", "Utf8?");

            testIndexedAccess(() -> statement.setNull(1, 1));
            testIndexedAccess(() -> statement.setBoolean(1, true));
            testIndexedAccess(() -> statement.setByte(1, (byte) 1));
            testIndexedAccess(() -> statement.setShort(1, (short) 1));
            testIndexedAccess(() -> statement.setInt(1, 1));
            testIndexedAccess(() -> statement.setLong(1, 1));
            testIndexedAccess(() -> statement.setFloat(1, 1f));
            testIndexedAccess(() -> statement.setDouble(1, 1d));
            testIndexedAccess(() -> statement.setBigDecimal(1, new BigDecimal(1)));
            testIndexedAccess(() -> statement.setString(1, "1"));
            testIndexedAccess(() -> statement.setBytes(1, "1".getBytes()));
            testIndexedAccess(() -> statement.setDate(1, new Date(0)));
            testIndexedAccess(() -> statement.setTime(1, new Time(0)));
            testIndexedAccess(() -> statement.setTimestamp(1, new Timestamp(0)));
            testIndexedAccess(() -> statement.setAsciiStream(1, stream(""), 1));
            testIndexedAccess(() -> statement.setUnicodeStream(1, stream(""), 1));
            testIndexedAccess(() -> statement.setBinaryStream(1, stream(""), 1));
            testIndexedAccess(() -> statement.setObject(1, "1", 1));
            testIndexedAccess(() -> statement.setCharacterStream(1, reader(""), 1));
            testIndexedAccess(() -> statement.setRef(1, new RefImpl()));
            testIndexedAccess(() -> statement.setBlob(1, new SerialBlob("".getBytes())));
            testIndexedAccess(() -> statement.setClob(1, new NClobImpl("".toCharArray())));
            testIndexedAccess(() -> statement.setArray(1, new ArrayImpl()));
            testIndexedAccess(() -> statement.setDate(1, new Date(0), Calendar.getInstance()));
            testIndexedAccess(() -> statement.setTime(1, new Time(0), Calendar.getInstance()));
            testIndexedAccess(() -> statement.setTimestamp(1, new Timestamp(0), Calendar.getInstance()));
            testIndexedAccess(() -> statement.setNull(1, 1, ""));
            testIndexedAccess(() -> statement.setURL(1, new URL("http://localhost")));
            testIndexedAccess(() -> statement.setNString(1, "1"));
            testIndexedAccess(() -> statement.setNCharacterStream(1, reader(""), 1L));
            testIndexedAccess(() -> statement.setNClob(1, new NClobImpl("".toCharArray())));
            testIndexedAccess(() -> statement.setClob(1, reader(""), 1L));
            testIndexedAccess(() -> statement.setBlob(1, stream(""), 1L));
            testIndexedAccess(() -> statement.setNClob(1, reader(""), 1L));
            testIndexedAccess(() -> statement.setSQLXML(1, new SQLXMLImpl()));
            testIndexedAccess(() -> statement.setObject(1, "1", 1, 1));
            testIndexedAccess(() -> statement.setAsciiStream(1, stream(""), 1L));
            testIndexedAccess(() -> statement.setBinaryStream(1, stream(""), 1L));
            testIndexedAccess(() -> statement.setCharacterStream(1, reader(""), 1L));
            testIndexedAccess(() -> statement.setAsciiStream(1, stream("")));
            testIndexedAccess(() -> statement.setBinaryStream(1, stream("")));
            testIndexedAccess(() -> statement.setCharacterStream(1, reader("")));
            testIndexedAccess(() -> statement.setNCharacterStream(1, reader("")));
            testIndexedAccess(() -> statement.setClob(1, reader("")));
            testIndexedAccess(() -> statement.setBlob(1, stream("")));
            testIndexedAccess(() -> statement.setNClob(1, reader("")));
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setNull(boolean modeJdbcType) throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getTestAllValuesStatement(connection);
            statement.setInt("key", 1);
            if (sqlTypeRequired()) {
                YdbTypes types = connection.getYdbTypes();
                if (modeJdbcType) {
                    statement.setNull("c_Bool", types.wrapYdbJdbcType(PrimitiveType.bool()));
                    statement.setNull("c_Int32", types.wrapYdbJdbcType(PrimitiveType.int32()));
                    statement.setNull("c_Int64", types.wrapYdbJdbcType(PrimitiveType.int64()));
                    statement.setNull("c_Uint8", types.wrapYdbJdbcType(PrimitiveType.uint8()));
                    statement.setNull("c_Uint32", types.wrapYdbJdbcType(PrimitiveType.uint32()));
                    statement.setNull("c_Uint64", types.wrapYdbJdbcType(PrimitiveType.uint64()));
                    statement.setNull("c_Float", types.wrapYdbJdbcType(PrimitiveType.float32()));
                    statement.setNull("c_Double", types.wrapYdbJdbcType(PrimitiveType.float64()));
                    statement.setNull("c_String", types.wrapYdbJdbcType(PrimitiveType.string()));
                    statement.setNull("c_Utf8", types.wrapYdbJdbcType(PrimitiveType.utf8()));
                    statement.setNull("c_Json", types.wrapYdbJdbcType(PrimitiveType.json()));
                    statement.setNull("c_JsonDocument", types.wrapYdbJdbcType(PrimitiveType.jsonDocument()));
                    statement.setNull("c_Yson", types.wrapYdbJdbcType(PrimitiveType.yson()));
                    statement.setNull("c_Date", types.wrapYdbJdbcType(PrimitiveType.date()));
                    statement.setNull("c_Datetime", types.wrapYdbJdbcType(PrimitiveType.datetime()));
                    statement.setNull("c_Timestamp", types.wrapYdbJdbcType(PrimitiveType.timestamp()));
                    statement.setNull("c_Interval", types.wrapYdbJdbcType(PrimitiveType.interval()));
                    statement.setNull("c_Decimal", types.wrapYdbJdbcType(YdbTypes.DEFAULT_DECIMAL_TYPE));
                } else {
                    statement.setNull("c_Bool", -1, "Bool");
                    statement.setNull("c_Int32", -1, "Int32");
                    statement.setNull("c_Int64", -1, "Int64");
                    statement.setNull("c_Uint8", -1, "Uint8");
                    statement.setNull("c_Uint32", -1, "Uint32");
                    statement.setNull("c_Uint64", -1, "Uint64");
                    statement.setNull("c_Float", -1, "Float");
                    statement.setNull("c_Double", -1, "Double");
                    statement.setNull("c_String", -1, "String");
                    statement.setNull("c_Utf8", -1, "Utf8");
                    statement.setNull("c_Json", -1, "Json");
                    statement.setNull("c_JsonDocument", -1, "JsonDocument");
                    statement.setNull("c_Yson", -1, "Yson");
                    statement.setNull("c_Date", -1, "Date");
                    statement.setNull("c_Datetime", -1, "Datetime");
                    statement.setNull("c_Timestamp", -1, "Timestamp");
                    statement.setNull("c_Interval", -1, "Interval");
                    statement.setNull("c_Decimal", -1, "Decimal(22, 9)");
                }
            } else {
                statement.setNull("c_Bool", -1);
                statement.setNull("c_Int32", -1);
                statement.setNull("c_Int64", -1);
                statement.setNull("c_Uint8", -1);
                statement.setNull("c_Uint32", -1);
                statement.setNull("c_Uint64", -1);
                statement.setNull("c_Float", -1);
                statement.setNull("c_Double", -1);
                statement.setNull("c_String", -1);
                statement.setNull("c_Utf8", -1);
                statement.setNull("c_Json", -1);
                statement.setNull("c_JsonDocument", -1);
                statement.setNull("c_Yson", -1);
                statement.setNull("c_Date", -1);
                statement.setNull("c_Datetime", -1);
                statement.setNull("c_Timestamp", -1);
                statement.setNull("c_Interval", -1);
                statement.setNull("c_Decimal", -1);
            }
            statement.executeUpdate();
            connection.commit();

            PreparedStatement statementSelect =
                    connection.prepareStatement(subst("unit_2", YdbResultSetImplTest.SELECT_ALL_VALUES));
            ResultSet resultSet = statementSelect.executeQuery();
            assertTrue(resultSet.next());

            ResultSetMetaData metaData = resultSet.getMetaData();
            assertEquals(19, metaData.getColumnCount());
            assertEquals(1, resultSet.getObject(1)); // key
            for (int i = 2; i <= metaData.getColumnCount(); i++) {
                assertNull(resultSet.getObject(i)); // everything else
            }

            assertFalse(resultSet.next());
        });
    }

    @Test
    void testParametersMeta() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getTestAllValuesStatement(connection);
            if (sqlTypeRequired()) {
                assertEquals(0, statement.getParameterMetaData().getParameterCount());

                YdbTypes types = connection.getYdbTypes();
                statement.setInt("key", 1);
                statement.setNull("c_Bool", types.wrapYdbJdbcType(PrimitiveType.bool()));
                statement.setNull("c_Int32", types.wrapYdbJdbcType(PrimitiveType.int32()));
                statement.setNull("c_Int64", types.wrapYdbJdbcType(PrimitiveType.int64()));
                statement.setNull("c_Uint8", types.wrapYdbJdbcType(PrimitiveType.uint8()));
                statement.setNull("c_Uint32", types.wrapYdbJdbcType(PrimitiveType.uint32()));
                statement.setNull("c_Uint64", types.wrapYdbJdbcType(PrimitiveType.uint64()));
                statement.setNull("c_Float", types.wrapYdbJdbcType(PrimitiveType.float32()));
                statement.setNull("c_Double", types.wrapYdbJdbcType(PrimitiveType.float64()));
                statement.setNull("c_String", types.wrapYdbJdbcType(PrimitiveType.string()));
                statement.setNull("c_Utf8", types.wrapYdbJdbcType(PrimitiveType.utf8()));
                statement.setNull("c_Json", types.wrapYdbJdbcType(PrimitiveType.json()));
                statement.setNull("c_JsonDocument", types.wrapYdbJdbcType(PrimitiveType.jsonDocument()));
                statement.setNull("c_Yson", types.wrapYdbJdbcType(PrimitiveType.yson()));
                statement.setNull("c_Date", types.wrapYdbJdbcType(PrimitiveType.date()));
                statement.setNull("c_Datetime", types.wrapYdbJdbcType(PrimitiveType.datetime()));
                statement.setNull("c_Timestamp", types.wrapYdbJdbcType(PrimitiveType.timestamp()));
                statement.setNull("c_Interval", types.wrapYdbJdbcType(PrimitiveType.interval()));
                statement.setNull("c_Decimal", types.wrapYdbJdbcType(YdbTypes.DEFAULT_DECIMAL_TYPE));
            }

            YdbParameterMetaData metadata = statement.getParameterMetaData();

            assertThrowsMsg(SQLException.class,
                    () -> metadata.getParameterIndex("some-param"),
                    "Parameter not found: some-param");
            assertThrowsMsg(SQLException.class,
                    () -> metadata.getParameterType(335),
                    "Parameter is out of range: 335");

            assertEquals(19, metadata.getParameterCount());
            for (int param = 1; param <= metadata.getParameterCount(); param++) {
                String name = metadata.getParameterName(param);
                assertEquals(param, metadata.getParameterIndex(name), "Names and indexes are matched");

                assertFalse(metadata.isSigned(param), "All params are not isSigned");
                assertEquals(0, metadata.getPrecision(param), "No precision available");
                assertEquals(0, metadata.getScale(param), "No scale available");
                assertEquals(ParameterMetaData.parameterModeIn, metadata.getParameterMode(param),
                        "All params are in");

                int type = metadata.getParameterType(param);
                assertTrue(type != 0, "All params have sql type, including " + name);

                String cleanParamName;
                if (expectParameterPrefixed()) {
                    assertTrue(name.startsWith("$"), "Parameters name must start from $s");
                    cleanParamName = name.substring(1);
                } else {
                    cleanParamName = name;
                }
                if (cleanParamName.startsWith("c_")) {
                    assertEquals(ParameterMetaData.parameterNullable, metadata.isNullable(param),
                            "All parameters expect primary key defined as nullable");

                    String expectType = cleanParamName.substring("c_".length()).toLowerCase();
                    if (expectType.equals("decimal")) {
                        expectType += "(22, 9)";
                    }

                    String actualType = metadata.getParameterTypeName(param);
                    assertNotNull(actualType, "All parameters have database types");
                    assertEquals(expectType, actualType.toLowerCase(),
                            "All parameter names are similar to types");
                } else {
                    if (sqlTypeRequired()) {
                        assertEquals(ParameterMetaData.parameterNullable, metadata.isNullable(param),
                                "Primary key must be defined as nullable");
                    } else {
                        assertEquals(ParameterMetaData.parameterNoNulls, metadata.isNullable(param),
                                "Primary key must be defined as non nullable");
                    }
                }

                String expectClassName;
                switch (cleanParamName) {
                    case "key":
                    case "c_Int32":
                    case "c_Uint8":
                        expectClassName = Integer.class.getName();
                        break;
                    case "c_Bool":
                        expectClassName = Boolean.class.getName();
                        break;
                    case "c_Int64":
                    case "c_Uint64":
                    case "c_Uint32":
                        expectClassName = Long.class.getName();
                        break;
                    case "c_Float":
                        expectClassName = Float.class.getName();
                        break;
                    case "c_Double":
                        expectClassName = Double.class.getName();
                        break;
                    case "c_String":
                    case "c_Utf8":
                    case "c_Json":
                    case "c_JsonDocument":
                        expectClassName = String.class.getName();
                        break;
                    case "c_Yson":
                        expectClassName = byte[].class.getName();
                        break;
                    case "c_Date":
                        expectClassName = LocalDate.class.getName();
                        break;
                    case "c_Datetime":
                        expectClassName = LocalDateTime.class.getName();
                        break;
                    case "c_Timestamp":
                        expectClassName = Instant.class.getName();
                        break;
                    case "c_Interval":
                        expectClassName = Duration.class.getName();
                        break;
                    case "c_Decimal":
                        expectClassName = DecimalValue.class.getName();
                        break;
                    default:
                        throw new IllegalStateException("Unknown param: " + cleanParamName);
                }
                assertEquals(expectClassName, metadata.getParameterClassName(param),
                        "Check class name for parameter: " + name);
            }
        });
    }

    @Test
    void setBoolean() throws SQLException {
        checkInsert("c_Bool", "Bool",
                YdbPreparedStatement::setBoolean,
                YdbPreparedStatement::setBoolean,
                ResultSet::getBoolean,
                Arrays.asList(
                        pair(true, true),
                        pair(false, false)
                ),
                Arrays.asList(
                        pair(true, true),
                        pair(false, false),
                        pair(1, true),
                        pair(0, false),
                        pair(-1, false),
                        pair(2, true),
                        pair(0.1, false), // round to 0
                        pair(1.1, true),
                        pair(-0.1, false),
                        pair((byte) 1, true),
                        pair((byte) 0, false),
                        pair((short) 1, true),
                        pair((short) 0, false),
                        pair(1, true),
                        pair(0, false),
                        pair(1L, true),
                        pair(0L, false),
                        pair(PrimitiveValue.bool(true), true),
                        pair(PrimitiveValue.bool(false), false)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        PrimitiveValue.int32(1),
                        PrimitiveValue.int32(1).makeOptional()
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Uint8"})
    void setByte(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setByte,
                YdbPreparedStatement::setByte,
                ResultSet::getByte,
                Arrays.asList(
                        pair((byte) 1, (byte) 1),
                        pair((byte) 0, (byte) 0),
                        pair((byte) -1, (byte) -1),
                        pair((byte) 127, (byte) 127),
                        pair((byte) 4, (byte) 4)
                ),
                Arrays.asList(
                        pair(true, (byte) 1),
                        pair(false, (byte) 0),
                        pair(PrimitiveValue.uint8((byte) 1), (byte) 1),
                        pair(PrimitiveValue.uint8((byte) 0), (byte) 0)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        (short) 5,
                        6,
                        7L,
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional()
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int32", "Uint32"})
    void setByteToInt(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setByte,
                YdbPreparedStatement::setByte,
                ResultSet::getInt,
                Arrays.asList(
                        pair((byte) 1, 1),
                        pair((byte) 0, 0),
                        pair((byte) -1, -1),
                        pair((byte) 127, 127),
                        pair((byte) 4, 4)
                ),
                Arrays.asList(
                        pair((short) 5, 5),
                        pair(6, 6),
                        pair(true, 1),
                        pair(false, 0)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        7L,
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.uint8((byte) 1)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int64", "Uint64"})
    void setByteToLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setByte,
                YdbPreparedStatement::setByte,
                ResultSet::getLong,
                Arrays.asList(
                        pair((byte) 1, 1L),
                        pair((byte) 0, 0L),
                        pair((byte) -1, -1L),
                        pair((byte) 127, 127L),
                        pair((byte) 4, 4L)
                ),
                Arrays.asList(
                        pair((short) 5, 5L),
                        pair(6, 6L),
                        pair(7L, 7L),
                        pair(true, 1L),
                        pair(false, 0L),
                        pair(new BigInteger("10"), 10L)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.uint8((byte) 1),
                        new BigDecimal("10")
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int32", "Uint32"})
    void setShortToInt(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setShort,
                YdbPreparedStatement::setShort,
                ResultSet::getInt,
                Arrays.asList(
                        pair((short) 1, 1),
                        pair((short) 0, 0),
                        pair((short) -1, -1),
                        pair((short) 127, 127),
                        pair((short) 5, 5)
                ),
                Arrays.asList(
                        pair((byte) 4, 4),
                        pair(6, 6),
                        pair(true, 1),
                        pair(false, 0)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        7L,
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.int16((short) 1)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int64", "Uint64"})
    void setShortToLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setShort,
                YdbPreparedStatement::setShort,
                ResultSet::getLong,
                Arrays.asList(
                        pair((short) 1, 1L),
                        pair((short) 0, 0L),
                        pair((short) -1, -1L),
                        pair((short) 127, 127L),
                        pair((short) 5, 5L)
                ),
                Arrays.asList(
                        pair((byte) 4, 4L),
                        pair(6, 6L),
                        pair(7L, 7L),
                        pair(true, 1L),
                        pair(false, 0L)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.int16((short) 1)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int32", "Uint32"})
    void setInt(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setInt,
                YdbPreparedStatement::setInt,
                ResultSet::getInt,
                Arrays.asList(
                        pair(1, 1),
                        pair(0, 0),
                        pair(-1, -1),
                        pair(127, 127),
                        pair(6, 6)
                ),
                Arrays.asList(
                        pair((byte) 4, 4),
                        pair((short) 5, 5),
                        pair(true, 1),
                        pair(false, 0)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        7L,
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.int16((short) 1)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int64", "Uint64"})
    void setIntToLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setInt,
                YdbPreparedStatement::setInt,
                ResultSet::getLong,
                Arrays.asList(
                        pair(1, 1L),
                        pair(0, 0L),
                        pair(-1, -1L),
                        pair(127, 127L),
                        pair(6, 6L)
                ),
                Arrays.asList(
                        pair((byte) 4, 4L),
                        pair((short) 5, 5L),
                        pair(7L, 7L),
                        pair(true, 1L),
                        pair(false, 0L)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.int32(1)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int64", "Uint64"})
    void setLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setLong,
                YdbPreparedStatement::setLong,
                ResultSet::getLong,
                Arrays.asList(
                        pair(1L, 1L),
                        pair(0L, 0L),
                        pair(-1L, -1L),
                        pair(127L, 127L),
                        pair(7L, 7L)
                ),
                Arrays.asList(
                        pair((byte) 4, 4L),
                        pair((short) 5, 5L),
                        pair(6, 6L),
                        pair(true, 1L),
                        pair(false, 0L),
                        pair(new BigInteger("1234567890123"), 1234567890123L)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        8f,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.int32(1),
                        new BigDecimal("123")
                )
        );
    }

    @Test
    void setFloat() throws SQLException {
        checkInsert("c_Float", "Float",
                YdbPreparedStatement::setFloat,
                YdbPreparedStatement::setFloat,
                ResultSet::getFloat,
                Arrays.asList(
                        pair(1f, 1f),
                        pair(0f, 0f),
                        pair(-1f, -1f),
                        pair(127f, 127f),
                        pair(8f, 8f)
                ),
                Arrays.asList(
                        pair((byte) 4, 4f),
                        pair((short) 5, 5f),
                        pair(6, 6f),
                        pair(true, 1f),
                        pair(false, 0f),
                        pair(PrimitiveValue.float32(1.1f), 1.1f)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        7L,
                        9d,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1)
                )
        );
    }

    @Test
    void setFloatToDouble() throws SQLException {
        checkInsert("c_Double", "Double",
                YdbPreparedStatement::setFloat,
                YdbPreparedStatement::setFloat,
                ResultSet::getDouble,
                Arrays.asList(
                        pair(1f, 1d),
                        pair(0f, 0d),
                        pair(-1f, -1d),
                        pair(127f, 127d),
                        pair(8f, 8d)
                ),
                Arrays.asList(
                        pair((byte) 4, 4d),
                        pair((short) 5, 5d),
                        pair(6, 6d),
                        pair(7L, 7d),
                        pair(9d, 9d),
                        pair(true, 1d),
                        pair(false, 0d),
                        pair(PrimitiveValue.float64(1.1f), (double) 1.1f) // lost double precision
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float32(1)
                )
        );
    }

    @Test
    void setDouble() throws SQLException {
        checkInsert("c_Double", "Double",
                YdbPreparedStatement::setDouble,
                YdbPreparedStatement::setDouble,
                ResultSet::getDouble,
                Arrays.asList(
                        pair(1d, 1d),
                        pair(0d, 0d),
                        pair(-1d, -1d),
                        pair(127d, 127d),
                        pair(9d, 9d)
                ),
                Arrays.asList(
                        pair((byte) 4, 4d),
                        pair((short) 5, 5d),
                        pair(6, 6d),
                        pair(7L, 7d),
                        pair(8f, 8d),
                        pair(true, 1d),
                        pair(false, 0d),
                        pair(PrimitiveValue.float64(1.1d), 1.1d)
                ),
                Arrays.asList(
                        "",
                        "".getBytes(),
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float32(1)
                )
        );
    }

    @Test
    void setBigDecimalDirect() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement insert = getTestStatement(connection, "c_Decimal", "Decimal(22,9)?");
            insert.setInt("key", 1);
            insert.setObject("c_Decimal", new BigDecimal(0)); // Make sure this type is converted to Decimal(22,9) type
            insert.executeUpdate();
        });
    }

    @Test
    void setBigDecimal() throws SQLException {
        checkInsert("c_Decimal", "Decimal(22,9)",
                YdbPreparedStatement::setBigDecimal,
                YdbPreparedStatement::setBigDecimal,
                ResultSet::getBigDecimal,
                Arrays.asList(
                        pair(new BigDecimal("0.0"), new BigDecimal("0.000000000")),
                        pair(new BigDecimal("1.3"), new BigDecimal("1.300000000"))
                ),
                Arrays.asList(
                        pair(1, new BigDecimal("1.000000000")),
                        pair(0, new BigDecimal("0.000000000")),
                        pair(-1, new BigDecimal("-1.000000000")),
                        pair(127, new BigDecimal("127.000000000")),
                        pair((byte) 4, new BigDecimal("4.000000000")),
                        pair((short) 5, new BigDecimal("5.000000000")),
                        pair(6, new BigDecimal("6.000000000")),
                        pair(7L, new BigDecimal("7.000000000")),
                        pair("1", new BigDecimal("1.000000000")),
                        pair("1.1", new BigDecimal("1.100000000")),
                        pair(DecimalType.of(22, 9).fromString("1.2"), new BigDecimal("1.200000000")),
                        pair(new BigInteger("2"), new BigDecimal("2.000000000"))
                ),
                Arrays.asList(
                        true,
                        false,
                        8f,
                        9d,
                        "".getBytes(),
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("stringAndUtf8")
    void setString(String type, List<Pair<Object, String>> callSetObject, List<Object> unsupported) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setString,
                YdbPreparedStatement::setString,
                ResultSet::getString,
                Arrays.asList(
                        pair("", ""),
                        pair("test1", "test1")
                ),
                merge(callSetObject,
                        Arrays.asList(
                                pair(1d, "1.0"),
                                pair(0d, "0.0"),
                                pair(-1d, "-1.0"),
                                pair(127d, "127.0"),
                                pair((byte) 4, "4"),
                                pair((short) 5, "5"),
                                pair(6, "6"),
                                pair(7L, "7"),
                                pair(8f, "8.0"),
                                pair(9d, "9.0"),
                                pair(true, "true"),
                                pair(false, "false"),
                                pair("".getBytes(), ""),
                                pair("test2".getBytes(), "test2"),
                                pair(stream("test3"), "test3"),
                                pair(reader("test4"), "test4")
                        )),
                merge(unsupported,
                        Arrays.asList(
                                PrimitiveValue.bool(true),
                                PrimitiveValue.bool(true).makeOptional(),
                                PrimitiveValue.float64(1.1d),
                                PrimitiveValue.json("test")
                        ))
        );
    }

    @ParameterizedTest
    @MethodSource("jsonAndJsonDocumentAndYson")
    void setStringJson(String type, List<Pair<Object, String>> callSetObject, List<Object> unsupported)
            throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setString,
                YdbPreparedStatement::setString,
                ResultSet::getString,
                Arrays.asList(
                        pair("[1]", "[1]")
                ),
                merge(callSetObject,
                        Arrays.asList(
                                pair("[2]".getBytes(), "[2]"),
                                pair(stream("[3]"), "[3]"),
                                pair(reader("[4]"), "[4]")
                                // No empty values supported
                        )),
                merge(unsupported,
                        Arrays.asList(
                                6,
                                7L,
                                8f,
                                9d,
                                true,
                                false,
                                PrimitiveValue.bool(true),
                                PrimitiveValue.bool(true).makeOptional(),
                                PrimitiveValue.float64(1.1d),
                                PrimitiveValue.utf8("test")
                        ))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8"})
    void setBytes(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setBytes,
                YdbPreparedStatement::setBytes,
                ResultSet::getBytes,
                Arrays.asList(
                        pair("".getBytes(), "".getBytes()),
                        pair("test2".getBytes(), "test2".getBytes())
                ),
                Arrays.asList(
                        pair(1d, "1.0".getBytes()),
                        pair(0d, "0.0".getBytes()),
                        pair(-1d, "-1.0".getBytes()),
                        pair(127d, "127.0".getBytes()),
                        pair((byte) 4, "4".getBytes()),
                        pair((short) 5, "5".getBytes()),
                        pair(6, "6".getBytes()),
                        pair(7L, "7".getBytes()),
                        pair(8f, "8.0".getBytes()),
                        pair(9d, "9.0".getBytes()),
                        pair(true, "true".getBytes()),
                        pair(false, "false".getBytes()),
                        pair("", "".getBytes()),
                        pair("test1", "test1".getBytes()),
                        pair(stream("test3"), "test3".getBytes()),
                        pair(reader("test4"), "test4".getBytes())
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.json("test")
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Json", "JsonDocument", "Yson"})
    void setBytesJson(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setBytes,
                YdbPreparedStatement::setBytes,
                ResultSet::getBytes,
                Arrays.asList(
                        pair("[2]".getBytes(), "[2]".getBytes())
                ),
                Arrays.asList(
                        pair("[1]", "[1]".getBytes()),
                        pair(stream("[3]"), "[3]".getBytes()),
                        pair(reader("[4]"), "[4]".getBytes())
                        // No empty values supported
                ),
                Arrays.asList(
                        6,
                        7L,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test")
                )
        );
    }

    @Test
    void setDateToDate() throws SQLException {
        checkInsert("c_Date", "Date",
                YdbPreparedStatement::setDate,
                YdbPreparedStatement::setDate,
                ResultSet::getDate,
                Arrays.asList(
                        pair(new Date(1), new Date(0)),
                        pair(new Date(0), new Date(0)),
                        pair(new Date(MILLIS_IN_DAY), new Date(MILLIS_IN_DAY)),
                        pair(new Date(MILLIS_IN_DAY * 2), new Date(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Date(0)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Date(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Date(0)),
                        pair("1970-01-01T00:00:03.111112Z", new Date(0)),
                        pair(3L, new Date(0)),
                        pair(MILLIS_IN_DAY * 3, new Date(MILLIS_IN_DAY * 3)),
                        pair(LocalDate.of(1970, 1, 2), new Date(MILLIS_IN_DAY)),
                        pair(new Timestamp(4), new Date(0)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Date(MILLIS_IN_DAY * 3)),
                        pair(new java.util.Date(5), new Date(0)),
                        pair(new java.util.Date(6999), new Date(0)),
                        pair(new java.util.Date(MILLIS_IN_DAY * 7), new Date(MILLIS_IN_DAY * 7))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test")
                )
        );
    }

    @Test
    void setDateToDatetime() throws SQLException {
        // precision - seconds
        checkInsert("c_Datetime", "Datetime",
                YdbPreparedStatement::setDate,
                YdbPreparedStatement::setDate,
                ResultSet::getDate,
                Arrays.asList(
                        pair(new Date(1), new Date(0)),
                        pair(new Date(0), new Date(0)),
                        pair(new Date(1000), new Date(1000)),
                        pair(new Date(1999), new Date(1000)),
                        pair(new Date(MILLIS_IN_DAY), new Date(MILLIS_IN_DAY)),
                        pair(new Date(MILLIS_IN_DAY * 2), new Date(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Date(0)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Date(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Date(3000)),
                        pair("1970-01-01T00:00:03.111112Z", new Date(3000)),
                        pair(3L, new Date(0)),
                        pair(2000L, new Date(2000L)),
                        pair(2999L, new Date(2000L)),
                        pair(MILLIS_IN_DAY * 3, new Date(MILLIS_IN_DAY * 3)),
                        pair(new Timestamp(4), new Date(0)),
                        pair(new Timestamp(4000), new Date(4000)),
                        pair(new Timestamp(4999), new Date(4000)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Date(MILLIS_IN_DAY * 3)),
                        pair(new Time(10), new Date(0)),
                        pair(new Time(5000), new Date(5000)),
                        pair(new Time(5999), new Date(5000)),
                        pair(new Time(MILLIS_IN_DAY * 4), new Date(MILLIS_IN_DAY * 4)),
                        pair(new java.util.Date(5), new Date(0)),
                        pair(new java.util.Date(6999), new Date(6000)),
                        pair(new java.util.Date(MILLIS_IN_DAY * 7), new Date(MILLIS_IN_DAY * 7))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test"),
                        LocalDate.of(1970, 1, 2)
                )
        );
    }

    @Test
    void setDateToTimestamp() throws SQLException {
        // precision - microseconds
        checkInsert("c_Timestamp", "Timestamp",
                YdbPreparedStatement::setDate,
                YdbPreparedStatement::setDate,
                ResultSet::getDate,
                Arrays.asList(
                        pair(new Date(1), new Date(1)),
                        pair(new Date(0), new Date(0)),
                        pair(new Date(1000), new Date(1000)),
                        pair(new Date(1999), new Date(1999)),
                        pair(new Date(MILLIS_IN_DAY), new Date(MILLIS_IN_DAY)),
                        pair(new Date(MILLIS_IN_DAY * 2), new Date(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Date(2)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Date(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Date(3111)),
                        pair("1970-01-01T00:00:03.111112Z", new Date(3111)),
                        pair(3L, new Date(3)),
                        pair(2000L, new Date(2000L)),
                        pair(2999L, new Date(2999L)),
                        pair(MILLIS_IN_DAY * 3, new Date(MILLIS_IN_DAY * 3)),
                        pair(new Timestamp(4), new Date(4)),
                        pair(new Timestamp(4000), new Date(4000)),
                        pair(new Timestamp(4999), new Date(4999)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Date(MILLIS_IN_DAY * 3)),
                        pair(new Time(10), new Date(10)),
                        pair(new Time(5000), new Date(5000)),
                        pair(new Time(5999), new Date(5999)),
                        pair(new Time(MILLIS_IN_DAY * 4), new Date(MILLIS_IN_DAY * 4)),
                        pair(new java.util.Date(5), new Date(5)),
                        pair(new java.util.Date(6999), new Date(6999)),
                        pair(new java.util.Date(MILLIS_IN_DAY * 7), new Date(MILLIS_IN_DAY * 7))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test"),
                        LocalDate.of(1970, 1, 2)
                )
        );
    }

    @Test
    void setTimeToDatetime() throws SQLException {
        checkInsert("c_Datetime", "Datetime",
                YdbPreparedStatement::setTime,
                YdbPreparedStatement::setTime,
                ResultSet::getTime,
                Arrays.asList(
                        pair(new Time(1), new Time(0)),
                        pair(new Time(0), new Time(0)),
                        pair(new Time(1000), new Time(1000)),
                        pair(new Time(1999), new Time(1000)),
                        pair(new Time(MILLIS_IN_DAY), new Time(MILLIS_IN_DAY)),
                        pair(new Time(MILLIS_IN_DAY * 2), new Time(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Time(0)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Time(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Date(3000)),
                        pair(3L, new Time(0)),
                        pair(2000L, new Time(2000L)),
                        pair(2999L, new Time(2000L)),
                        pair(MILLIS_IN_DAY * 3, new Time(MILLIS_IN_DAY * 3)),
                        pair(new Timestamp(4), new Time(0)),
                        pair(new Timestamp(4000), new Time(4000)),
                        pair(new Timestamp(4999), new Time(4000)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Time(MILLIS_IN_DAY * 3)),
                        pair(new Date(10), new Time(0)),
                        pair(new Date(5000), new Time(5000)),
                        pair(new Date(5999), new Time(5000)),
                        pair(new Date(MILLIS_IN_DAY * 4), new Time(MILLIS_IN_DAY * 4))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test"),
                        LocalDate.of(1970, 1, 2)
                )
        );
    }

    @Test
    void setTimeToTimestamp() throws SQLException {
        checkInsert("c_Timestamp", "Timestamp",
                YdbPreparedStatement::setTime,
                YdbPreparedStatement::setTime,
                ResultSet::getTime,
                Arrays.asList(
                        pair(new Time(1), new Time(1)),
                        pair(new Time(0), new Time(0)),
                        pair(new Time(1000), new Time(1000)),
                        pair(new Time(1999), new Time(1999)),
                        pair(new Time(MILLIS_IN_DAY), new Time(MILLIS_IN_DAY)),
                        pair(new Time(MILLIS_IN_DAY * 2), new Time(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Time(2)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Time(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Date(3111)),
                        pair(3L, new Time(3)),
                        pair(2000L, new Time(2000L)),
                        pair(2999L, new Time(2999L)),
                        pair(MILLIS_IN_DAY * 3, new Time(MILLIS_IN_DAY * 3)),
                        pair(new Timestamp(4), new Time(4)),
                        pair(new Timestamp(4000), new Time(4000)),
                        pair(new Timestamp(4999), new Time(4999)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Time(MILLIS_IN_DAY * 3)),
                        pair(new Date(10), new Time(10)),
                        pair(new Date(5000), new Time(5000)),
                        pair(new Date(5999), new Time(5999)),
                        pair(new Date(MILLIS_IN_DAY * 4), new Time(MILLIS_IN_DAY * 4))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test"),
                        LocalDate.of(1970, 1, 2)
                )
        );
    }

    @Test
    void setTimestampToDate() throws SQLException {
        checkInsert("c_Date", "Date",
                YdbPreparedStatement::setTimestamp,
                YdbPreparedStatement::setTimestamp,
                ResultSet::getTimestamp,
                Arrays.asList(
                        pair(new Timestamp(1), new Timestamp(0)),
                        pair(new Timestamp(0), new Timestamp(0)),
                        pair(new Timestamp(MILLIS_IN_DAY), new Timestamp(MILLIS_IN_DAY)),
                        pair(new Timestamp(MILLIS_IN_DAY * 2), new Timestamp(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Timestamp(0)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Timestamp(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Timestamp(0)),
                        pair(3L, new Timestamp(0)),
                        pair(MILLIS_IN_DAY * 3, new Timestamp(MILLIS_IN_DAY * 3)),
                        pair(LocalDate.of(1970, 1, 2), new Timestamp(MILLIS_IN_DAY)),
                        pair(new Timestamp(4), new Timestamp(0)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Timestamp(MILLIS_IN_DAY * 3))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test")
                )
        );
    }

    @Test
    void setTimestampToDatetime() throws SQLException {
        checkInsert("c_Datetime", "Datetime",
                YdbPreparedStatement::setTimestamp,
                YdbPreparedStatement::setTimestamp,
                ResultSet::getTimestamp,
                Arrays.asList(
                        pair(new Timestamp(1), new Timestamp(0)),
                        pair(new Timestamp(0), new Timestamp(0)),
                        pair(new Timestamp(1000), new Timestamp(1000)),
                        pair(new Timestamp(1999), new Timestamp(1000)),
                        pair(new Timestamp(MILLIS_IN_DAY), new Timestamp(MILLIS_IN_DAY)),
                        pair(new Timestamp(MILLIS_IN_DAY * 2), new Timestamp(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Timestamp(0)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Timestamp(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Timestamp(3000)),
                        pair(3L, new Timestamp(0)),
                        pair(2000L, new Timestamp(2000L)),
                        pair(2999L, new Timestamp(2000L)),
                        pair(MILLIS_IN_DAY * 3, new Timestamp(MILLIS_IN_DAY * 3)),
                        pair(new Timestamp(4), new Timestamp(0)),
                        pair(new Timestamp(4000), new Timestamp(4000)),
                        pair(new Timestamp(4999), new Timestamp(4000)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Timestamp(MILLIS_IN_DAY * 3)),
                        pair(new Date(10), new Timestamp(0)),
                        pair(new Date(5000), new Timestamp(5000)),
                        pair(new Date(5999), new Timestamp(5000)),
                        pair(new Date(MILLIS_IN_DAY * 4), new Timestamp(MILLIS_IN_DAY * 4)),
                        pair(new Time(10), new Timestamp(0)),
                        pair(new Time(5000), new Timestamp(5000)),
                        pair(new Time(5999), new Timestamp(5000)),
                        pair(new Time(MILLIS_IN_DAY * 4), new Timestamp(MILLIS_IN_DAY * 4))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test"),
                        LocalDate.of(1970, 1, 2)
                )
        );
    }

    @Test
    void setTimestampToTimestamp() throws SQLException {
        checkInsert("c_Timestamp", "Timestamp",
                YdbPreparedStatement::setTimestamp,
                YdbPreparedStatement::setTimestamp,
                ResultSet::getTimestamp,
                Arrays.asList(
                        pair(new Timestamp(1), new Timestamp(1)),
                        pair(new Timestamp(0), new Timestamp(0)),
                        pair(new Timestamp(1000), new Timestamp(1000)),
                        pair(new Timestamp(1999), new Timestamp(1999)),
                        pair(new Timestamp(MILLIS_IN_DAY), new Timestamp(MILLIS_IN_DAY)),
                        pair(new Timestamp(MILLIS_IN_DAY * 2), new Timestamp(MILLIS_IN_DAY * 2))
                ),
                Arrays.asList(
                        pair(Instant.ofEpochMilli(2), new Timestamp(2)),
                        pair(Instant.ofEpochMilli(MILLIS_IN_DAY), new Timestamp(MILLIS_IN_DAY)),
                        pair(Instant.parse("1970-01-01T00:00:03.111112Z"), new Timestamp(3111)),
                        pair(3L, new Timestamp(3)),
                        pair(2000L, new Timestamp(2000L)),
                        pair(2999L, new Timestamp(2999L)),
                        pair(MILLIS_IN_DAY * 3, new Timestamp(MILLIS_IN_DAY * 3)),
                        pair(new Timestamp(4), new Timestamp(4)),
                        pair(new Timestamp(4000), new Timestamp(4000)),
                        pair(new Timestamp(4999), new Timestamp(4999)),
                        pair(new Timestamp(MILLIS_IN_DAY * 3), new Timestamp(MILLIS_IN_DAY * 3)),
                        pair(new Date(10), new Timestamp(10)),
                        pair(new Date(5000), new Timestamp(5000)),
                        pair(new Date(5999), new Timestamp(5999)),
                        pair(new Date(MILLIS_IN_DAY * 4), new Timestamp(MILLIS_IN_DAY * 4)),
                        pair(new Time(10), new Timestamp(10)),
                        pair(new Time(5000), new Timestamp(5000)),
                        pair(new Time(5999), new Timestamp(5999)),
                        pair(new Time(MILLIS_IN_DAY * 4), new Timestamp(MILLIS_IN_DAY * 4))
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test"),
                        LocalDate.of(1970, 1, 2)
                )
        );
    }

    @Test
    void setTimestampToInterval() throws SQLException {
        checkInsert("c_Interval", "Interval",
                YdbPreparedStatement::setLong,
                YdbPreparedStatement::setLong,
                ResultSet::getLong,
                Arrays.asList(
                        pair(1L, 1L),
                        pair(0L, 0L),
                        pair(1000L, 1000L)
                ),
                Arrays.asList(
                        pair(Duration.parse("PT3.111113S"), 3111113L),
                        pair(3L, 3L),
                        pair(2000L, 2000L),
                        pair(2999L, 2999L),
                        pair(MICROS_IN_DAY, MICROS_IN_DAY),
                        pair(MICROS_IN_DAY * 3, MICROS_IN_DAY * 3)
                ),
                Arrays.asList(
                        6,
                        8f,
                        9d,
                        true,
                        false,
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.utf8("test"),
                        LocalDate.of(1970, 1, 2),
                        new Timestamp(4),
                        new Date(10),
                        new Time(10)
                )
        );
    }

    @Test
    void setAsciiStream() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setAsciiStream("value", stream("value")),
                        "AsciiStreams are not supported"));
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setAsciiStream("value", stream("value"), 1),
                        "AsciiStreams are not supported"));
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setAsciiStream("value", stream("value"), 1L),
                        "AsciiStreams are not supported"));
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setUnicodeStream(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setUnicodeStream(name, value, 3),
                (ps, name, value) -> ps.setUnicodeStream(name, value, 3),
                ResultSet::getUnicodeStream,
                Arrays.asList(
                        pair(stream("[3]-limited!"), stream("[3]"))
                ),
                Arrays.asList(
                        pair("[1]", stream("[1]")),
                        pair("[2]".getBytes(), stream("[2]")),
                        pair(reader("[4]"), stream("[4]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setBinaryStream(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setBinaryStream,
                YdbPreparedStatement::setBinaryStream,
                ResultSet::getBinaryStream,
                Arrays.asList(
                        pair(stream("[3]"), stream("[3]"))
                ),
                Arrays.asList(
                        pair("[1]", stream("[1]")),
                        pair("[2]".getBytes(), stream("[2]")),
                        pair(reader("[4]"), stream("[4]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setBinaryStreamInt(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setBinaryStream(name, value, 3),
                (ps, name, value) -> ps.setBinaryStream(name, value, 3),
                ResultSet::getBinaryStream,
                Arrays.asList(
                        pair(stream("[3]-limited!"), stream("[3]"))
                ),
                Arrays.asList(
                        pair("[1]", stream("[1]")),
                        pair("[2]".getBytes(), stream("[2]")),
                        pair(reader("[4]"), stream("[4]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setBinaryStreamLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setBinaryStream(name, value, 3L),
                (ps, name, value) -> ps.setBinaryStream(name, value, 3L),
                ResultSet::getBinaryStream,
                Arrays.asList(
                        pair(stream("[3]-limited!"), stream("[3]"))
                ),
                Arrays.asList(
                        pair("[1]", stream("[1]")),
                        pair("[2]".getBytes(), stream("[2]")),
                        pair(reader("[4]"), stream("[4]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setCharacterStream(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setCharacterStream,
                YdbPreparedStatement::setCharacterStream,
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]"), reader("[4]"))
                ),
                Arrays.asList(
                        pair("[1]", reader("[1]")),
                        pair("[2]".getBytes(), reader("[2]")),
                        pair(stream("[3]"), reader("[3]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setCharacterStreamInt(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setCharacterStream(name, value, 3),
                (ps, name, value) -> ps.setCharacterStream(name, value, 3),
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]-limited!"), reader("[4]"))
                ),
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8"})
    void setCharacterStreamIntEmpty(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setCharacterStream(name, value, 0),
                (ps, name, value) -> ps.setCharacterStream(name, value, 0),
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]-limited!"), reader(""))
                ),
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setCharacterStreamLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setCharacterStream(name, value, 3L),
                (ps, name, value) -> ps.setCharacterStream(name, value, 3L),
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]-limited!"), reader("[4]"))
                ),
                Arrays.asList(),
                Arrays.asList()
        );
    }


    @Test
    void setRef() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setRef("value", new RefImpl()),
                        "Refs are not supported"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void asBlob(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setBlob,
                YdbPreparedStatement::setBlob,
                ResultSet::getBinaryStream,
                Arrays.asList(
                        pair(stream("[3]"), stream("[3]"))
                ),
                Arrays.asList(
                        pair("[1]", stream("[1]")),
                        pair("[2]".getBytes(), stream("[2]")),
                        pair(reader("[4]"), stream("[4]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void asBlobLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setBlob(name, value, 3L),
                (ps, name, value) -> ps.setBlob(name, value, 3L),
                ResultSet::getBinaryStream,
                Arrays.asList(
                        pair(stream("[3]-limited!"), stream("[3]"))
                ),
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @Test
    void setBlobUnsupported() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setBlob("value", new SerialBlob("".getBytes())),
                        "Blobs are not supported"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void asClob(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setClob,
                YdbPreparedStatement::setClob,
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]"), reader("[4]"))
                ),
                Arrays.asList(
                        pair("[1]", reader("[1]")),
                        pair("[2]".getBytes(), reader("[2]")),
                        pair(stream("[3]"), reader("[3]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void asClobLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setClob(name, value, 3L),
                (ps, name, value) -> ps.setClob(name, value, 3L),
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]-limited!"), reader("[4]"))
                ),
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @Test
    void setClobUnsupported() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setClob("value", new NClobImpl("".toCharArray())),
                        "Clobs are not supported"));
    }

    @Test
    void setArray() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setArray("value", new ArrayImpl()),
                        "Arrays are not supported"));
    }

    @Test
    void getMetaData() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).getMetaData(),
                        "ResultSet metadata is not supported in prepared statements"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8"})
    void setURL(String type) throws SQLException, MalformedURLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setURL,
                YdbPreparedStatement::setURL,
                ResultSet::getURL,
                Arrays.asList(
                        pair(new URL("https://localhost"), new URL("https://localhost")),
                        pair(new URL("ftp://localhost"), new URL("ftp://localhost"))
                ),
                Arrays.asList(
                        pair("https://localhost", new URL("https://localhost")),
                        pair("ftp://localhost".getBytes(), new URL("ftp://localhost")),
                        pair(stream("https://localhost"), new URL("https://localhost")),
                        pair(reader("ftp://localhost"), new URL("ftp://localhost"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @Test
    void setRowId() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setRowId(1, new RowIdImpl()),
                        "RowIds are not supported"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8"})
    void setNString(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setNString,
                YdbPreparedStatement::setNString,
                ResultSet::getNString,
                Arrays.asList(
                        pair("", ""),
                        pair("test1", "test1")
                ),
                Arrays.asList(
                        pair(1d, "1.0"),
                        pair(0d, "0.0"),
                        pair(-1d, "-1.0"),
                        pair(127d, "127.0"),
                        pair((byte) 4, "4"),
                        pair((short) 5, "5"),
                        pair(6, "6"),
                        pair(7L, "7"),
                        pair(8f, "8.0"),
                        pair(9d, "9.0"),
                        pair(true, "true"),
                        pair(false, "false"),
                        pair("".getBytes(), ""),
                        pair("test2".getBytes(), "test2"),
                        pair(stream("test3"), "test3"),
                        pair(reader("test4"), "test4")
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d),
                        PrimitiveValue.json("test")
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setNCharacterStream(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setNCharacterStream,
                YdbPreparedStatement::setNCharacterStream,
                ResultSet::getNCharacterStream,
                Arrays.asList(
                        pair(reader("[4]"), reader("[4]"))
                ),
                Arrays.asList(
                        pair("[1]", reader("[1]")),
                        pair("[2]".getBytes(), reader("[2]")),
                        pair(stream("[3]"), reader("[3]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void setNCharacterStreamLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, value) -> ps.setNCharacterStream(name, value, 3L),
                (ps, name, value) -> ps.setNCharacterStream(name, value, 3L),
                ResultSet::getNCharacterStream,
                Arrays.asList(
                        pair(reader("[4]-limited!"), reader("[4]"))
                ),
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void asNClob(String type) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setNClob,
                YdbPreparedStatement::setNClob,
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]"), reader("[4]"))
                ),
                Arrays.asList(
                        pair("[1]", reader("[1]")),
                        pair("[2]".getBytes(), reader("[2]")),
                        pair(stream("[3]"), reader("[3]"))
                ),
                Arrays.asList(
                        PrimitiveValue.bool(true),
                        PrimitiveValue.bool(true).makeOptional(),
                        PrimitiveValue.float64(1.1d)
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Utf8", "Json", "JsonDocument", "Yson"})
    void asNClobLong(String type) throws SQLException {
        checkInsert("c_" + type, type,
                (ps, name, reader) -> ps.setNClob(name, reader, 3L),
                (ps, name, reader) -> ps.setNClob(name, reader, 3L),
                ResultSet::getCharacterStream,
                Arrays.asList(
                        pair(reader("[4]-limited!"), reader("[4]"))
                ),
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @Test
    void setNClobUnsupported() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setNClob("value", new NClobImpl("".toCharArray())),
                        "NClobs are not supported"));
    }

    @Test
    void setSQLXML() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLFeatureNotSupportedException.class,
                        () -> getUtf8Statement(connection).setSQLXML("value", new SQLXMLImpl()),
                        "SQLXMLs are not supported"));
    }

    @ParameterizedTest
    @MethodSource("stringAndUtf8")
    void setObject(String type, List<Pair<Object, Object>> callSetObject, List<Object> unsupported) throws SQLException {
        checkInsert("c_" + type, type,
                YdbPreparedStatement::setObject,
                YdbPreparedStatement::setObject,
                ResultSet::getObject,
                Arrays.asList(
                        pair("", ""),
                        pair("test1", "test1"),
                        pair(1d, "1.0"),
                        pair(0d, "0.0"),
                        pair(-1d, "-1.0"),
                        pair(127d, "127.0"),
                        pair((byte) 4, "4"),
                        pair((short) 5, "5"),
                        pair(6, "6"),
                        pair(7L, "7"),
                        pair(8f, "8.0"),
                        pair(9d, "9.0"),
                        pair(true, "true"),
                        pair(false, "false"),
                        pair("".getBytes(), ""),
                        pair("test2".getBytes(), "test2"),
                        pair(stream("test3"), "test3"),
                        pair(reader("test4"), "test4")
                ),
                callSetObject,
                merge(unsupported,
                        Arrays.asList(
                                PrimitiveValue.bool(true),
                                PrimitiveValue.bool(true).makeOptional(),
                                PrimitiveValue.float64(1.1d),
                                PrimitiveValue.json("test")
                        ))
        );
    }

    @Test
    void unknownColumns() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLException.class,
                        () -> {
                            YdbPreparedStatement statement = getUtf8Statement(connection);
                            statement.setObject("column0", "value");
                            statement.execute();
                        },
                        "Parameter not found: " + (expectParameterPrefixed() ? "$column0" : "column0")));
    }

    @Test
    void queryInList() throws SQLException {
        DecimalType defaultType = YdbTypes.DEFAULT_DECIMAL_TYPE;

        Set<String> skip = set("c_Bool", "c_Json", "c_JsonDocument", "c_Yson");
        List<Map<String, Object>> values = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> params = new LinkedHashMap<>();
            int prefix = 100 * i;
            params.put("c_Bool", i % 2 == 0);
            params.put("c_Int32", prefix++);
            params.put("c_Int64", (long) prefix++);
            params.put("c_Uint8", PrimitiveValue.uint8((byte) prefix++).makeOptional());
            params.put("c_Uint32", PrimitiveValue.uint32(prefix++).makeOptional());
            params.put("c_Uint64", PrimitiveValue.uint64(prefix++).makeOptional());
            params.put("c_Float", (float) prefix++);
            params.put("c_Double", (double) prefix++);
            params.put("c_String", PrimitiveValue.string(String.valueOf(prefix++).getBytes()).makeOptional());
            params.put("c_Utf8", String.valueOf(prefix++));
            params.put("c_Json", PrimitiveValue.json("[" + (prefix++) + "]").makeOptional());
            params.put("c_JsonDocument", PrimitiveValue.jsonDocument("[" + (prefix++) + "]").makeOptional());
            params.put("c_Yson", PrimitiveValue.yson(("[" + (prefix++) + "]").getBytes()).makeOptional());
            params.put("c_Date", new Date(MILLIS_IN_DAY * (prefix++)));
            params.put("c_Datetime", new Time(MILLIS_IN_DAY * (prefix++) + 111000));
            params.put("c_Timestamp", Instant.ofEpochMilli(MILLIS_IN_DAY * (prefix++) + 112112));
            params.put("c_Interval", Duration.of(prefix++, ChronoUnit.MICROS));
            params.put("c_Decimal", defaultType.fromString((prefix) + ".1").makeOptional());
            values.add(params);
        }

        retry(connection -> {
            YdbPreparedStatement statement = getTestAllValuesStatement(connection);
            int key = 0;
            for (Map<String, Object> params : values) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    statement.setObject(entry.getKey(), entry.getValue());
                }
                statement.setInt("key", ++key);
                statement.executeUpdate();
            }
            connection.commit();
        });

        for (String key : values.get(0).keySet()) {
            if (skip.contains(key)) {
                continue;
            }
            retry(false, connection -> {
                String type = key.substring("c_".length());
                if (type.equals("Decimal")) {
                    type = defaultType.toString();
                }
                YdbPreparedStatement ps = connection.prepareStatement(String.format(
                        "declare $keys as List<%s?>;\n" +
                                "select count(1) as rows from unit_2 where %s in $keys",
                        type, key));

                ps.setObject("keys", Arrays.asList());
                checkRows(0, ps.executeQuery());

                ps.setObject("keys", Arrays.asList((Object) null));
                checkRows(0, ps.executeQuery());

                ps.setObject("keys", Arrays.asList(
                        values.get(0).get(key)));
                checkRows(1, ps.executeQuery());

                ps.setObject("keys", Arrays.asList(
                        values.get(0).get(key),
                        values.get(1).get(key)));
                checkRows(2, ps.executeQuery());

                ps.setObject("keys", Arrays.asList(
                        values.get(0).get(key),
                        null,
                        values.get(1).get(key)));
                checkRows(2, ps.executeQuery());

                ps.setObject("keys", Arrays.asList(
                        values.get(0).get(key),
                        values.get(1).get(key),
                        values.get(2).get(key)));
                checkRows(3, ps.executeQuery());
            });
        }

        retry(false, connection -> {
            YdbPreparedStatement ps = connection.prepareStatement(String.format(
                    "declare $keys as List<%s?>;\n" +
                            "select count(1) as rows from unit_2 where %s in $keys",
                    "Bool", "c_Bool"));

            ps.setObject("keys", Arrays.asList());
            checkRows(0, ps.executeQuery());

            ps.setObject("keys", Arrays.asList((Object) null));
            checkRows(0, ps.executeQuery());

            ps.setObject("keys", Arrays.asList(true));
            checkRows(1, ps.executeQuery());

            ps.setObject("keys", Arrays.asList(false));
            checkRows(2, ps.executeQuery());

            ps.setObject("keys", Arrays.asList(true, false));
            checkRows(3, ps.executeQuery());

            ps.setObject("keys", Arrays.asList(true, null, false));
            checkRows(3, ps.executeQuery());
        });
    }

    @Test
    void unwrap() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            assertTrue(statement.isWrapperFor(YdbPreparedStatement.class));
            assertSame(statement, statement.unwrap(YdbPreparedStatement.class));

            assertFalse(statement.isWrapperFor(YdbConnection.class));
            assertThrowsMsg(SQLException.class,
                    () -> statement.unwrap(YdbConnection.class),
                    "Cannot unwrap to " + YdbConnection.class);
        });
    }


    //

    static Collection<Arguments> stringAndUtf8() {
        return Arrays.asList(
                Arguments.of("String",
                        Arrays.asList(
                                pair(PrimitiveValue.string("test-bytes".getBytes()),
                                        "test-bytes"),
                                pair(PrimitiveValue.string("test-bytes".getBytes()).makeOptional(),
                                        "test-bytes")
                        ),
                        Arrays.asList(
                                PrimitiveValue.utf8("test-utf8"),
                                PrimitiveValue.utf8("test-utf8").makeOptional()
                        )
                ),
                Arguments.of("Utf8",
                        Arrays.asList(
                                pair(PrimitiveValue.utf8("test-utf8"), "test-utf8"),
                                pair(PrimitiveValue.utf8("test-utf8").makeOptional(), "test-utf8")
                        ),
                        Arrays.asList(
                                PrimitiveValue.string("test-bytes".getBytes()),
                                PrimitiveValue.string("test-bytes".getBytes()).makeOptional()
                        )
                )
        );
    }

    static Collection<Arguments> jsonAndJsonDocumentAndYson() {
        return Arrays.asList(
                Arguments.of("Json",
                        Arrays.asList(
                                pair(PrimitiveValue.json("[1]"), "[1]"),
                                pair(PrimitiveValue.json("[1]").makeOptional(), "[1]")
                        ),
                        Arrays.asList(
                                PrimitiveValue.utf8("test-utf8"),
                                PrimitiveValue.utf8("test-utf8").makeOptional()
                        )
                ),
                Arguments.of("JsonDocument",
                        Arrays.asList(
                                pair(PrimitiveValue.jsonDocument("[1]"), "[1]"),
                                pair(PrimitiveValue.jsonDocument("[1]").makeOptional(), "[1]")
                        ),
                        Arrays.asList(
                                PrimitiveValue.utf8("test-utf8"),
                                PrimitiveValue.utf8("test-utf8").makeOptional()
                        )
                ),
                Arguments.of("Yson",
                        Arrays.asList(
                                pair(PrimitiveValue.yson("[1]".getBytes()), "[1]"),
                                pair(PrimitiveValue.yson("[1]".getBytes()).makeOptional(), "[1]")
                        ),
                        Arrays.asList(
                                PrimitiveValue.utf8("test-utf8"),
                                PrimitiveValue.utf8("test-utf8").makeOptional()
                        )
                )
        );
    }

    protected YdbPreparedStatement getUtf8Statement(YdbConnection connection) throws SQLException {
        return getTestStatement(connection, "c_Utf8", "Utf8?");
    }

    protected abstract YdbPreparedStatement getTestStatement(YdbConnection connection,
                                                             String column,
                                                             String type) throws SQLException;

    protected abstract YdbPreparedStatement getTestStatementIndexed(YdbConnection connection,
                                                                    String column,
                                                                    String type) throws SQLException;

    protected abstract YdbPreparedStatement getTestAllValuesStatement(YdbConnection connection) throws SQLException;

    protected abstract boolean expectParameterPrefixed();

    protected abstract boolean sqlTypeRequired();

    protected abstract boolean supportIndexedParameters();

    //

    protected void testIndexedAccess(Executable executable) {
        TestHelper.assertThrowsMsg(SQLException.class,
                executable,
                "Indexed parameters are not supported here");
    }

    private <TSet, TGet> void checkInsert(String param,
                                          String type,
                                          SQLSetter<TSet> setter,
                                          SQLIndexSetter<TSet> setterIndex,
                                          SQLGetter<TGet> getter,
                                          List<Pair<TSet, TGet>> callSetter,
                                          List<Pair<Object, TGet>> callSetObject,
                                          List<Object> unsupportedValues) throws SQLException {
        retry(connection -> {
            long time = System.currentTimeMillis();

            connection.setAutoCommit(true); // AUTO-COMMIT

            YdbPreparedStatement insert = getTestStatement(connection, param, type + "?");
            YdbPreparedStatement insertIndexed = getTestStatementIndexed(connection, param, type + "?");

            YdbPreparedStatement select = connection.prepareStatement("select " + param + " from unit_2");

            List<Executable> asserts = new ArrayList<>(1 + callSetObject.size() + callSetter.size() +
                    unsupportedValues.size());

            boolean sqlTypeRequired = sqlTypeRequired();
            int sqlType;
            if (sqlTypeRequired) {
                YdbResultSet rsNull = select.executeQuery();
                assertFalse(rsNull.next());
                sqlType = connection.getYdbTypes().wrapYdbJdbcType(rsNull.getMetaData().getYdbType(1));
            } else {
                sqlType = YdbConst.UNKNOWN_SQL_TYPE;
            }

            // test null by name
            insert.setInt("key", 1);
            insert.setNull(param, sqlType);
            insert.executeUpdate();

            YdbResultSet rsNull = select.executeQuery();
            assertTrue(rsNull.next());
            assertNull(rsNull.getObject(param), "Null value must be stored as null");
            assertFalse(rsNull.next());

            // test null by index
            if (supportIndexedParameters()) {
                insertIndexed.setInt(1, 1);
                insertIndexed.setNull(2, sqlType);
                insertIndexed.executeUpdate();

                rsNull = select.executeQuery();
                assertTrue(rsNull.next());
                assertNull(rsNull.getObject(param), "Null value with index must be stored as null");
                assertFalse(rsNull.next());
            } else {
                testIndexedAccess(() -> insertIndexed.setInt(1, 1));
            }

            for (int i = 0; i < callSetObject.size(); i++) {
                int index = i;
                asserts.add(() -> {
                    insert.setInt("key", 1);
                    Object value = callSetObject.get(index).key;
                    if (sqlTypeRequired) {
                        insert.setObject(param, value, sqlType);
                    } else {
                        insert.setObject(param, value);
                    }
                    insert.executeUpdate();
                    closeIfPossible(value);

                    ResultSet rs = select.executeQuery();
                    assertTrue(rs.next());

                    Object expect = castCompatible(callSetObject.get(index).value);
                    Object actual = castCompatible(getter.get(rs, param));
                    assertEquals(expect, actual,
                            "Expect #setObject as position " + index);
                    assertFalse(rs.next());
                });
            }

            if (supportIndexedParameters()) {
                for (int i = 0; i < callSetObject.size(); i++) {
                    int index = i;
                    asserts.add(() -> {
                        insertIndexed.setInt(1, 1);
                        Object value = callSetObject.get(index).key;
                        if (sqlTypeRequired) {
                            insertIndexed.setObject(2, value, sqlType);
                        } else {
                            insertIndexed.setObject(2, value);
                        }
                        insertIndexed.executeUpdate();
                        closeIfPossible(value);

                        ResultSet rs = select.executeQuery();
                        assertTrue(rs.next());

                        Object expect = castCompatible(callSetObject.get(index).value);
                        Object actual = castCompatible(getter.get(rs, param));
                        assertEquals(expect, actual,
                                "Expect #setObject with index as position " + index);
                        assertFalse(rs.next());
                    });
                }
            }

            for (int i = 0; i < callSetter.size(); i++) {
                int index = i;
                asserts.add(() -> {
                    Pair<TSet, TGet> pair = callSetter.get(index);

                    TSet value = pair.key;
                    insert.setInt("key", 1);
                    setter.set(insert, param, value);
                    insert.executeUpdate();
                    closeIfPossible(value);

                    ResultSet rs = select.executeQuery();
                    assertTrue(rs.next());

                    Object expect = castCompatible(pair.value);
                    Object actual = castCompatible(getter.get(rs, param));
                    assertEquals(expect, actual,
                            "Expect #setNNN as position " + index);
                    assertFalse(rs.next());
                });
            }

            if (supportIndexedParameters()) {
                for (int i = 0; i < callSetter.size(); i++) {
                    int index = i;
                    asserts.add(() -> {
                        Pair<TSet, TGet> pair = callSetter.get(index);

                        TSet value = pair.key;
                        insertIndexed.setInt(1, 1);
                        setterIndex.set(insertIndexed, 2, value);
                        insertIndexed.executeUpdate();
                        closeIfPossible(value);

                        ResultSet rs = select.executeQuery();
                        assertTrue(rs.next());

                        Object expect = castCompatible(pair.value);
                        Object actual = castCompatible(getter.get(rs, param));
                        assertEquals(expect, actual,
                                "Expect #setNNN with index as position " + index);
                        assertFalse(rs.next());
                    });
                }
            }

            for (Object unsupported : unsupportedValues) {
                asserts.add(() ->
                        assertThrowsMsgLike(
                                SQLException.class,
                                () -> {
                                    insert.clearParameters();
                                    insert.setObject(param, unsupported);
                                },
                                "Cannot cast",
                                "Column " + param + " must not support setting object " + unsupported.getClass()));
            }

            if (supportIndexedParameters()) {
                for (Object unsupported : unsupportedValues) {
                    asserts.add(() ->
                            assertThrowsMsgLike(
                                    SQLException.class,
                                    () -> {
                                        insertIndexed.clearParameters();
                                        insertIndexed.setObject(2, unsupported);
                                    },
                                    "Cannot cast",
                                    "Column " + param + " must not support setting with index object " +
                                            unsupported.getClass()));
                }
            }

            assertAll(asserts);

            logger.info("Verified within {} millis", System.currentTimeMillis() - time);
        });
    }


    protected void checkSimpleResultSet(YdbConnection connection) throws SQLException {
        ResultSet rs = connection
                .prepareStatement("select key, c_Utf8 from unit_2")
                .executeQuery();

        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertEquals("value-1", rs.getString(2));

        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertEquals("value-2", rs.getString(2));

        assertFalse(rs.next());
    }

    protected void checkNoResultSet(YdbConnection connection) throws SQLException {
        ResultSet rs = connection
                .prepareStatement("select key from unit_2")
                .executeQuery();

        assertFalse(rs.next());
    }

    private void checkRows(int rowCount, ResultSet rs) throws SQLException {
        assertTrue(rs.next());
        assertEquals(rowCount, rs.getLong("rows"));
        assertFalse(rs.next());
    }

    //


    protected void deleteRows(YdbConnection connection) throws SQLException {
        connection.createStatement().execute("delete from unit_2");
        connection.commit();
    }

    protected void retry(TestHelper.SQLRun run) throws SQLException {
        retry(true, run);
    }

    protected void retry(boolean cleanup, TestHelper.SQLRun run) throws SQLException {
        // TODO: must be external retry framework (but have to implement it first)

        int maxRetry = 10;
        int retry = 0;
        while (true) {
            YdbConnection connection = null;
            try {
                connection = getTestConnection();
                if (cleanup) {
                    deleteRows(connection);
                }
                run.run(connection);
                return; // ---
            } catch (Throwable e) {
                if (shouldRetry(e)) {
                    retry++;
                    if (retry > maxRetry) {
                        throw e;
                    }
                    logger.error("Retry #{}", retry, e);
                    if (connection != null) {
                        connection.close();
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    private static boolean shouldRetry(Throwable e) {
        if (e instanceof MultipleFailuresError) {
            return ((MultipleFailuresError) e).getFailures().stream()
                    .anyMatch(t -> t instanceof YdbRetryableException || t.getCause() instanceof YdbRetryableException);
        } else {
            return e instanceof YdbRetryableException || e.getCause() instanceof YdbRetryableException;
        }
    }

    private static String insertMode(String sql, String mode) {
        return sql.replace(
                YdbConst.PREFIX_SYNTAX_V1,
                YdbConst.PREFIX_SYNTAX_V1 + "\n" + mode);
    }


    interface SQLSetter<T> {
        void set(YdbPreparedStatement ps, String paramName, T value) throws SQLException;
    }

    interface SQLIndexSetter<T> {
        void set(YdbPreparedStatement ps, int paramIndex, T value) throws SQLException;
    }

    interface SQLGetter<T> {
        T get(ResultSet rs, String columnName) throws SQLException;
    }

    static class Pair<K, V> {
        private final K key;
        private final V value;

        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pair)) {
                return false;
            }
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(key, pair.key) &&
                    Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return key + " -> " + value;
        }


        static <K, V> Pair<K, V> pair(K key, V value) {
            return new Pair<>(key, value);
        }
    }
}
