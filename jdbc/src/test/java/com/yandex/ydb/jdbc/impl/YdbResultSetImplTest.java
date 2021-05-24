package com.yandex.ydb.jdbc.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import com.google.common.base.Preconditions;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.jdbc.YdbResultSetMetaData;
import com.yandex.ydb.jdbc.YdbStatement;
import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.table.values.PrimitiveValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsgLike;
import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YdbResultSetImplTest extends AbstractTest {

    static final String SELECT_ALL_VALUES = stringFileReference("classpath:sql/select_all_values.sql");
    static final String UPSERT_ALL_VALUES = stringFileReference("classpath:sql/upsert_all_values.sql");

    static {
        Locale.setDefault(Locale.US);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(YdbResultSetImplTest.class);

    private static boolean configured;
    private YdbResultSet resultSet;

    @BeforeEach
    void beforeEach() throws SQLException {
        configureOnce(() -> {
            recreateSimpleTestTable();

            YdbConnection connection = getTestConnection();
            YdbStatement statement = connection.createStatement();
            statement.execute("delete from unit_1");
            statement.execute(subst("unit_1", UPSERT_ALL_VALUES));
            connection.commit();
        });
        resultSet = getTestConnection().createStatement().executeQuery(subst("unit_1", SELECT_ALL_VALUES));
    }

    // TODO: check reading other database types, not supported by table storage itself?
    // See YdbConnectionImplSet#unsupportedTypes

    @Test
    void next() throws SQLException {
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getRow());

        assertTrue(resultSet.next());
        assertEquals(2, resultSet.getRow());

        assertTrue(resultSet.next());
        assertEquals(3, resultSet.getRow());

        assertTrue(resultSet.next());
        assertEquals(4, resultSet.getRow());

        assertTrue(resultSet.next());
        assertEquals(5, resultSet.getRow());

        assertFalse(resultSet.next());
        assertFalse(resultSet.next());
        assertFalse(resultSet.next());

        assertEquals(6, resultSet.getRow());
    }

    @Test
    void close() throws SQLException {
        assertFalse(resultSet.isClosed());
        resultSet.close();
        assertTrue(resultSet.isClosed());
    }

    @Test
    void columnAtInvalidLocation() {
        assertThrowsMsg(SQLException.class,
                () -> resultSet.getString(2),
                "Current row index is out of bounds: 0");

        assertThrowsMsg(SQLException.class,
                () -> resultSet.getString("c_Utf8"),
                "Current row index is out of bounds: 0");
    }

    @Test
    void columnWithInvalidCase() {
        assertThrowsMsg(SQLException.class,
                () -> resultSet.getString("c_utf8"),
                "Column not found: c_utf8");

        assertThrowsMsg(SQLException.class,
                () -> resultSet.getString("C_UTF8"),
                "Column not found: C_UTF8");

        assertThrowsMsg(SQLException.class,
                () -> resultSet.getString("c_Utf8"),
                "Current row index is out of bounds: 0"); // And this is pretty normal column name
    }

    @Test
    void columnWithInvalidName() {
        assertThrowsMsg(SQLException.class,
                () -> resultSet.getString("value0"),
                "Column not found: value0");
    }

    @Test
    void getString() throws SQLException {
        checkRows(
                resultSet::getString,
                resultSet::getString,
                map(
                        "key", "1",
                        "c_Bool", "true",
                        "c_Int32", "2000000001",
                        "c_Int64", "2000000000001",
                        "c_Uint8", "100",
                        "c_Uint32", "2000000002",
                        "c_Uint64", "2000000000002",
                        "c_Float", "123456.78",
                        "c_Double", "1.2345678912345679E8",
                        "c_String", "https://string",
                        "c_Utf8", "file://utf8",
                        "c_Json", "{\"key\": \"value Json\"}",
                        "c_JsonDocument", "{\"key\":\"value JsonDocument\"}",
                        "c_Yson", "{key=\"value yson\"}",
                        "c_Date", "1978-07-09",
                        "c_Datetime", "1970-02-06T00:11:51",
                        "c_Timestamp", "1970-01-01T00:00:03.111112Z",
                        "c_Interval", "PT3.111113S",
                        "c_Decimal", "3.335000000"
                ),
                map(
                        "key", "2",
                        "c_Bool", "false",
                        "c_Int32", "-2000000001",
                        "c_Int64", "-2000000000001",
                        "c_Uint8", "200",
                        "c_Uint32", "4000000002",
                        "c_Uint64", "4000000000002",
                        "c_Float", "-123456.78",
                        "c_Double", "-1.2345678912345679E8",
                        "c_String", "",
                        "c_Utf8", "",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "\"\"",
                        "c_Date", "1978-07-10",
                        "c_Datetime", "1970-02-06T00:28:31",
                        "c_Timestamp", "1970-01-01T00:00:03.112112Z",
                        "c_Interval", "PT3.112113S",
                        "c_Decimal", "-3.335000000"
                ),
                map(
                        "key", "3",
                        "c_Bool", "false",
                        "c_Int32", "0",
                        "c_Int64", "0",
                        "c_Uint8", "0",
                        "c_Uint32", "0",
                        "c_Uint64", "0",
                        "c_Float", "0.0",
                        "c_Double", "0.0",
                        "c_String", "0",
                        "c_Utf8", "0",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "0",
                        "c_Date", "1970-01-01",
                        "c_Datetime", "1970-01-01T00:00",
                        "c_Timestamp", "1970-01-01T00:00:00Z",
                        "c_Interval", "PT0S",
                        "c_Decimal", "0.000000000"
                ),
                map(
                        "key", "4",
                        "c_Bool", "true",
                        "c_Int32", "1",
                        "c_Int64", "1",
                        "c_Uint8", "1",
                        "c_Uint32", "1",
                        "c_Uint64", "1",
                        "c_Float", "1.0",
                        "c_Double", "1.0",
                        "c_String", "1",
                        "c_Utf8", "1",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "1",
                        "c_Date", "1970-01-02",
                        "c_Datetime", "1970-01-01T00:00:01",
                        "c_Timestamp", "1970-01-01T00:00:00.000001Z",
                        "c_Interval", "PT0.000001S",
                        "c_Decimal", "1.000000000"
                ),
                map(
                        "key", "5",
                        "c_Bool", null,
                        "c_Int32", null,
                        "c_Int64", null,
                        "c_Uint8", null,
                        "c_Uint32", null,
                        "c_Uint64", null,
                        "c_Float", null,
                        "c_Double", null,
                        "c_String", null,
                        "c_Utf8", null,
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", null,
                        "c_Date", null,
                        "c_Datetime", null,
                        "c_Timestamp", null,
                        "c_Interval", null,
                        "c_Decimal", null
                ));
    }

    @Test
    void getBoolean() throws SQLException {
        checkRows(
                resultSet::getBoolean,
                resultSet::getBoolean,
                map(
                        "key", true,
                        "c_Bool", true,
                        "c_Int32", true,
                        "c_Int64", true,
                        "c_Uint8", true,
                        "c_Uint32", true,
                        "c_Uint64", true
                ),
                map(
                        "key", true,
                        "c_Bool", false,
                        "c_Int32", false,
                        "c_Int64", false,
                        "c_Uint8", true,
                        "c_Uint32", true,
                        "c_Uint64", true,
                        "c_String", false,
                        "c_Utf8", false
                ),
                map(
                        "key", true,
                        "c_Bool", false,
                        "c_Int32", false,
                        "c_Int64", false,
                        "c_Uint8", false,
                        "c_Uint32", false,
                        "c_Uint64", false,
                        "c_String", false,
                        "c_Utf8", false
                ),
                map(
                        "key", true,
                        "c_Bool", true,
                        "c_Int32", true,
                        "c_Int64", true,
                        "c_Uint8", true,
                        "c_Uint32", true,
                        "c_Uint64", true,
                        "c_String", true,
                        "c_Utf8", true
                ),
                map(
                        "key", true,
                        "c_Bool", NullType.of(false),
                        "c_Int32", NullType.of(false),
                        "c_Int64", NullType.of(false),
                        "c_Uint8", NullType.of(false),
                        "c_Uint32", NullType.of(false),
                        "c_Uint64", NullType.of(false),
                        "c_String", NullType.of(false),
                        "c_Utf8", NullType.of(false)
                ));
    }

    @Test
    void getByte() throws SQLException {
        checkRows(
                resultSet::getByte,
                resultSet::getByte,
                map(
                        "c_Bool", (byte) 1,
                        "c_Uint8", (byte) 100
                ),
                map(
                        "c_Bool", (byte) 0,
                        "c_Uint8", (byte) 200
                ),
                map(
                        "c_Bool", (byte) 0,
                        "c_Uint8", (byte) 0
                ),
                map(
                        "c_Bool", (byte) 1,
                        "c_Uint8", (byte) 1
                ),
                map(
                        "c_Bool", NullType.of((byte) 0),
                        "c_Uint8", NullType.of((byte) 0)
                ));
    }

    @Test
    void getShort() throws SQLException {
        checkRows(
                resultSet::getShort,
                resultSet::getShort,
                map(
                        "c_Bool", (short) 1,
                        "c_Uint8", (short) 100
                ),
                map(
                        "c_Bool", (short) 0,
                        "c_Uint8", (short) 200
                ),
                map(
                        "c_Bool", (short) 0,
                        "c_Uint8", (short) 0
                ),
                map(
                        "c_Bool", (short) 1,
                        "c_Uint8", (short) 1
                ),
                map(
                        "c_Bool", NullType.of((short) 0),
                        "c_Uint8", NullType.of((short) 0)
                ));
    }

    @Test
    void getInt() throws SQLException {
        checkRows(
                resultSet::getInt,
                resultSet::getInt,
                map(
                        "key", 1,
                        "c_Bool", 1,
                        "c_Int32", 2000000001,
                        "c_Uint8", 100,
                        "c_Uint32", 2000000002
                ),
                map(
                        "key", 2,
                        "c_Bool", 0,
                        "c_Int32", -2000000001,
                        "c_Uint8", 200,
                        "c_Uint32", -294967294 // TODO: cannot be casted without loosing precision
                ),
                map(
                        "key", 3,
                        "c_Bool", 0,
                        "c_Int32", 0,
                        "c_Uint8", 0,
                        "c_Uint32", 0
                ),
                map(
                        "key", 4,
                        "c_Bool", 1,
                        "c_Int32", 1,
                        "c_Uint8", 1,
                        "c_Uint32", 1
                ),
                map(
                        "key", 5,
                        "c_Bool", NullType.of(0),
                        "c_Int32", NullType.of(0),
                        "c_Uint8", NullType.of(0),
                        "c_Uint32", NullType.of(0)
                ));
    }

    @Test
    void getLong() throws SQLException {
        checkRows(
                resultSet::getLong,
                resultSet::getLong,
                map(
                        "key", 1L,
                        "c_Bool", 1L,
                        "c_Int32", 2000000001L,
                        "c_Int64", 2000000000001L,
                        "c_Uint8", 100L,
                        "c_Uint32", 2000000002L,
                        "c_Uint64", 2000000000002L,
                        "c_Decimal", 3335000000L,
                        "c_Date", 3111L * 86400 * 1000,
                        "c_Datetime", 3111111L * 1000,
                        "c_Timestamp", 3111L,
                        "c_Interval", 3111113L
                ),
                map(
                        "key", 2L,
                        "c_Bool", 0L,
                        "c_Int32", -2000000001L,
                        "c_Int64", -2000000000001L,
                        "c_Uint8", 200L,
                        "c_Uint32", 4000000002L,
                        "c_Uint64", 4000000000002L,
                        "c_Decimal", -3335000000L,
                        "c_Date", 3112L * 86400 * 1000,
                        "c_Datetime", 3112111L * 1000,
                        "c_Timestamp", 3112L,
                        "c_Interval", 3112113L
                ),
                map(
                        "key", 3L,
                        "c_Bool", 0L,
                        "c_Int32", 0L,
                        "c_Int64", 0L,
                        "c_Uint8", 0L,
                        "c_Uint32", 0L,
                        "c_Uint64", 0L,
                        "c_Decimal", 0L,
                        "c_Date", 0L,
                        "c_Datetime", 0L,
                        "c_Timestamp", 0L,
                        "c_Interval", 0L
                ),
                map(
                        "key", 4L,
                        "c_Bool", 1L,
                        "c_Int32", 1L,
                        "c_Int64", 1L,
                        "c_Uint8", 1L,
                        "c_Uint32", 1L,
                        "c_Uint64", 1L,
                        "c_Decimal", 1000000000L,
                        "c_Date", 86400000L,
                        "c_Datetime", 1000L,
                        "c_Timestamp", 0L,
                        "c_Interval", 1L
                ),
                map(
                        "key", 5L,
                        "c_Bool", NullType.of(0L),
                        "c_Int32", NullType.of(0L),
                        "c_Int64", NullType.of(0L),
                        "c_Uint8", NullType.of(0L),
                        "c_Uint32", NullType.of(0L),
                        "c_Uint64", NullType.of(0L),
                        "c_Decimal", NullType.of(0L),
                        "c_Date", NullType.of(0L),
                        "c_Datetime", NullType.of(0L),
                        "c_Timestamp", NullType.of(0L),
                        "c_Interval", NullType.of(0L)
                ));
    }

    @Test
    void getFloat() throws SQLException {
        checkRows(
                resultSet::getFloat,
                resultSet::getFloat,
                map(
                        "key", 1f,
                        "c_Bool", 1f,
                        "c_Int32", 2000000001f,
                        "c_Uint8", 100f,
                        "c_Uint32", 2000000002f,
                        "c_Float", 123456.78f,
                        "c_Decimal", 3.335f
                ),
                map(
                        "key", 2f,
                        "c_Bool", 0f,
                        "c_Int32", -2000000001f,
                        "c_Uint8", 200f,
                        "c_Uint32", 4000000002f,
                        "c_Float", -123456.78f,
                        "c_Decimal", -3.335f
                ),
                map(
                        "key", 3f,
                        "c_Bool", 0f,
                        "c_Int32", 0f,
                        "c_Uint8", 0f,
                        "c_Uint32", 0f,
                        "c_Float", 0f,
                        "c_Decimal", 0f
                ),
                map(
                        "key", 4f,
                        "c_Bool", 1f,
                        "c_Int32", 1f,
                        "c_Uint8", 1f,
                        "c_Uint32", 1f,
                        "c_Float", 1f,
                        "c_Decimal", 1f
                ),
                map(
                        "key", 5f,
                        "c_Bool", NullType.of(0f),
                        "c_Int32", NullType.of(0f),
                        "c_Uint8", NullType.of(0f),
                        "c_Uint32", NullType.of(0f),
                        "c_Float", NullType.of(0f),
                        "c_Decimal", NullType.of(0f)
                ));
    }

    @Test
    void getDouble() throws SQLException {
        checkRows(
                resultSet::getDouble,
                resultSet::getDouble,
                map(
                        "key", 1d,
                        "c_Bool", 1d,
                        "c_Int32", 2000000001d,
                        "c_Int64", 2000000000001d,
                        "c_Uint8", 100d,
                        "c_Uint32", 2000000002d,
                        "c_Uint64", 2000000000002d,
                        "c_Float", 123456.78125d, // TODO: cannot be casted from float without loosing precision
                        "c_Double", 123456789.123456789d,
                        "c_Decimal", 3.335d
                ),
                map(
                        "key", 2d,
                        "c_Bool", 0d,
                        "c_Int32", -2000000001d,
                        "c_Int64", -2000000000001d,
                        "c_Uint8", 200d,
                        "c_Uint32", 4000000002d,
                        "c_Uint64", 4000000000002d,
                        "c_Float", -123456.78125d,
                        "c_Double", -123456789.123456789d,
                        "c_Decimal", -3.335d
                ),
                map(
                        "key", 3d,
                        "c_Bool", 0d,
                        "c_Int32", 0d,
                        "c_Int64", 0d,
                        "c_Uint8", 0d,
                        "c_Uint32", 0d,
                        "c_Uint64", 0d,
                        "c_Float", 0d,
                        "c_Double", 0d,
                        "c_Decimal", 0d
                ),
                map(
                        "key", 4d,
                        "c_Bool", 1d,
                        "c_Int32", 1d,
                        "c_Int64", 1d,
                        "c_Uint8", 1d,
                        "c_Uint32", 1d,
                        "c_Uint64", 1d,
                        "c_Float", 1d,
                        "c_Double", 1d,
                        "c_Decimal", 1d
                ),
                map(
                        "key", 5d,
                        "c_Bool", NullType.of(0d),
                        "c_Int32", NullType.of(0d),
                        "c_Int64", NullType.of(0d),
                        "c_Uint8", NullType.of(0d),
                        "c_Uint32", NullType.of(0d),
                        "c_Uint64", NullType.of(0d),
                        "c_Float", NullType.of(0d),
                        "c_Double", NullType.of(0d),
                        "c_Decimal", NullType.of(0d)
                ));
    }

    @Test
    void getBigDecimal() throws SQLException {
        checkRows(
                resultSet::getBigDecimal,
                resultSet::getBigDecimal,
                map(
                        "key", new BigDecimal(1),
                        "c_Bool", new BigDecimal(1),
                        "c_Int32", new BigDecimal(2000000001),
                        "c_Int64", new BigDecimal(2000000000001L),
                        "c_Uint8", new BigDecimal(100),
                        "c_Uint32", new BigDecimal(2000000002),
                        "c_Uint64", new BigDecimal(2000000000002L),

                        // TODO: cannot be casted from float without loosing precision
                        "c_Float", new BigDecimal("123456.78125"),
                        "c_Double", new BigDecimal("123456789.12345679"),
                        "c_Decimal", new BigDecimal("3.335000000")
                ),
                map(
                        "key", new BigDecimal(2),
                        "c_Bool", new BigDecimal(0),
                        "c_Int32", new BigDecimal(-2000000001),
                        "c_Int64", new BigDecimal(-2000000000001L),
                        "c_Uint8", new BigDecimal(200),
                        "c_Uint32", new BigDecimal(4000000002L),
                        "c_Uint64", new BigDecimal(4000000000002L),
                        "c_Float", new BigDecimal("-123456.78125"),
                        "c_Double", new BigDecimal("-123456789.12345679"),
                        "c_Decimal", new BigDecimal("-3.335000000")
                ),
                map(
                        "key", new BigDecimal(3),
                        "c_Bool", new BigDecimal(0),
                        "c_Int32", new BigDecimal(0),
                        "c_Int64", new BigDecimal(0),
                        "c_Uint8", new BigDecimal(0),
                        "c_Uint32", new BigDecimal(0),
                        "c_Uint64", new BigDecimal(0),
                        "c_Float", new BigDecimal("0.0"),
                        "c_Double", new BigDecimal("0.0"),
                        "c_Decimal", new BigDecimal("0E-9")
                ),
                map(
                        "key", new BigDecimal(4),
                        "c_Bool", new BigDecimal(1),
                        "c_Int32", new BigDecimal(1),
                        "c_Int64", new BigDecimal(1),
                        "c_Uint8", new BigDecimal(1),
                        "c_Uint32", new BigDecimal(1),
                        "c_Uint64", new BigDecimal(1),
                        "c_Float", new BigDecimal("1.0"),
                        "c_Double", new BigDecimal("1.0"),
                        "c_Decimal", new BigDecimal("1.000000000")
                ),
                map(
                        "key", new BigDecimal(5),
                        "c_Bool", null,
                        "c_Int32", null,
                        "c_Int64", null,
                        "c_Uint8", null,
                        "c_Uint32", null,
                        "c_Uint64", null,
                        "c_Float", null,
                        "c_Double", null,
                        "c_Decimal", null
                ));
    }

    @Test
    void getBytes() throws SQLException {
        checkRows(
                resultSet::getBytes,
                resultSet::getBytes,
                map(
                        "c_String", "https://string".getBytes(),
                        "c_Utf8", "file://utf8".getBytes(),
                        "c_Json", "{\"key\": \"value Json\"}".getBytes(),
                        "c_JsonDocument", "{\"key\":\"value JsonDocument\"}".getBytes(),
                        "c_Yson", "{key=\"value yson\"}".getBytes()
                ),
                map(
                        "c_String", "".getBytes(),
                        "c_Utf8", "".getBytes(),
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "\"\"".getBytes()
                ),
                map(
                        "c_String", "0".getBytes(),
                        "c_Utf8", "0".getBytes(),
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "0".getBytes()
                ),
                map(
                        "c_String", "1".getBytes(),
                        "c_Utf8", "1".getBytes(),
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "1".getBytes()
                ),
                map(
                        "c_String", null,
                        "c_Utf8", null,
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", null
                ));
    }

    @Test
    void getDate() throws SQLException {
        checkRows(
                resultSet::getDate,
                resultSet::getDate,
                map(
                        "c_Int64", new Date(2000000000001L),
                        "c_Uint64", new Date(2000000000002L),
                        "c_Date", new Date(3111L * 86400 * 1000),
                        "c_Datetime", new Date(3111111L * 1000),
                        "c_Timestamp", new Date(3111)
                ),
                map(
                        "c_Int64", new Date(-2000000000001L),
                        "c_Uint64", new Date(4000000000002L),
                        "c_Date", new Date(3112L * 86400 * 1000),
                        "c_Datetime", new Date(3112111L * 1000),
                        "c_Timestamp", new Date(3112)
                ),
                map(
                        "c_Int64", new Date(0),
                        "c_Uint64", new Date(0),
                        "c_Date", new Date(0),
                        "c_Datetime", new Date(0),
                        "c_Timestamp", new Date(0)
                ),
                map(
                        "c_Int64", new Date(1),
                        "c_Uint64", new Date(1),
                        "c_Date", new Date(86400000L),
                        "c_Datetime", new Date(1000),
                        "c_Timestamp", new Date(0)
                ),
                map(
                        "c_Int64", null,
                        "c_Uint64", null,
                        "c_Date", null,
                        "c_Datetime", null,
                        "c_Timestamp", null
                ));
    }

    @Test
    void getTime() throws SQLException {
        checkRows(
                resultSet::getTime,
                resultSet::getTime,
                map(
                        "c_Int64", new Time(2000000000001L),
                        "c_Uint64", new Time(2000000000002L),
                        "c_Date", new Time(3111L * 86400 * 1000),
                        "c_Datetime", new Time(3111111L * 1000),
                        "c_Timestamp", new Time(3111)
                ),
                map(
                        "c_Int64", new Time(-2000000000001L),
                        "c_Uint64", new Time(4000000000002L),
                        "c_Date", new Time(3112L * 86400 * 1000),
                        "c_Datetime", new Time(3112111L * 1000),
                        "c_Timestamp", new Time(3112)
                ),
                map(
                        "c_Int64", new Time(0),
                        "c_Uint64", new Time(0),
                        "c_Date", new Time(0),
                        "c_Datetime", new Time(0),
                        "c_Timestamp", new Time(0)
                ),
                map(
                        "c_Int64", new Time(1),
                        "c_Uint64", new Time(1),
                        "c_Date", new Time(86400000L),
                        "c_Datetime", new Time(1000),
                        "c_Timestamp", new Time(0)
                ),
                map(
                        "c_Int64", null,
                        "c_Uint64", null,
                        "c_Date", null,
                        "c_Datetime", null,
                        "c_Timestamp", null
                ));
    }

    @Test
    void getTimestamp() throws SQLException {
        checkRows(
                resultSet::getTimestamp,
                resultSet::getTimestamp,
                map(
                        "c_Int64", new Timestamp(2000000000001L),
                        "c_Uint64", new Timestamp(2000000000002L),
                        "c_Date", new Timestamp(3111L * 86400 * 1000),
                        "c_Datetime", new Timestamp(3111111L * 1000),
                        "c_Timestamp", new Timestamp(3111)
                ),
                map(
                        "c_Int64", new Timestamp(-2000000000001L),
                        "c_Uint64", new Timestamp(4000000000002L),
                        "c_Date", new Timestamp(3112L * 86400 * 1000),
                        "c_Datetime", new Timestamp(3112111L * 1000),
                        "c_Timestamp", new Timestamp(3112)
                ),
                map(
                        "c_Int64", new Timestamp(0),
                        "c_Uint64", new Timestamp(0),
                        "c_Date", new Timestamp(0),
                        "c_Datetime", new Timestamp(0),
                        "c_Timestamp", new Timestamp(0)
                ),
                map(
                        "c_Int64", new Timestamp(1),
                        "c_Uint64", new Timestamp(1),
                        "c_Date", new Timestamp(86400000L),
                        "c_Datetime", new Timestamp(1000),
                        "c_Timestamp", new Timestamp(0)
                ),
                map(
                        "c_Int64", null,
                        "c_Uint64", null,
                        "c_Date", null,
                        "c_Datetime", null,
                        "c_Timestamp", null
                ));
    }

    @Test
    void getAsciiStream() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getAsciiStream(1),
                "AsciiStreams are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getAsciiStream("Stream"),
                "AsciiStreams are not supported");
    }

    @SuppressWarnings("deprecation")
    @Test
    void getUnicodeStream() throws SQLException {
        checkRows(
                resultSet::getUnicodeStream,
                resultSet::getUnicodeStream,
                map(
                        "c_String", stream("https://string"),
                        "c_Utf8", stream("file://utf8"),
                        "c_Json", stream("{\"key\": \"value Json\"}"),
                        "c_JsonDocument", stream("{\"key\":\"value JsonDocument\"}"),
                        "c_Yson", stream("{key=\"value yson\"}")
                ),
                map(
                        "c_String", stream(""),
                        "c_Utf8", stream(""),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream("\"\"")
                ),
                map(
                        "c_String", stream("0"),
                        "c_Utf8", stream("0"),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream("0")
                ),
                map(
                        "c_String", stream("1"),
                        "c_Utf8", stream("1"),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream("1")
                ),
                map(
                        "c_String", stream(null),
                        "c_Utf8", stream(null),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream(null)
                ));
    }

    @Test
    void getBinaryStream() throws SQLException {
        checkRows(
                resultSet::getBinaryStream,
                resultSet::getBinaryStream,
                map(
                        "c_String", stream("https://string"),
                        "c_Utf8", stream("file://utf8"),
                        "c_Json", stream("{\"key\": \"value Json\"}"),
                        "c_JsonDocument", stream("{\"key\":\"value JsonDocument\"}"),
                        "c_Yson", stream("{key=\"value yson\"}")
                ),
                map(
                        "c_String", stream(""),
                        "c_Utf8", stream(""),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream("\"\"")
                ),
                map(
                        "c_String", stream("0"),
                        "c_Utf8", stream("0"),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream("0")
                ),
                map(
                        "c_String", stream("1"),
                        "c_Utf8", stream("1"),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream("1")
                ),
                map(
                        "c_String", stream(null),
                        "c_Utf8", stream(null),
                        "c_Json", stream(null),
                        "c_JsonDocument", stream(null),
                        "c_Yson", stream(null)
                ));
    }

    @Test
    void warnings() throws SQLException {
        assertNull(resultSet.getWarnings());
        resultSet.clearWarnings();
        assertNull(resultSet.getWarnings());
    }

    @Test
    void getCursorName() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getCursorName(),
                "Named cursors are not supported");
    }

    @Test
    void getMetaData() throws SQLException {
        YdbResultSetMetaData metadata = resultSet.getMetaData();
        assertSame(metadata, resultSet.getMetaData(), "Metadata is cached");

        assertTrue(metadata.isWrapperFor(YdbResultSetMetaData.class));
        assertSame(metadata, metadata.unwrap(YdbResultSetMetaData.class));

        assertFalse(metadata.isWrapperFor(YdbStatement.class));
        assertThrowsMsg(SQLException.class,
                () -> metadata.unwrap(YdbStatement.class),
                "Cannot unwrap to " + YdbStatement.class);

        assertThrowsMsg(SQLException.class,
                () -> metadata.getColumnName(995),
                "Column is out of range: 995");

        assertThrowsMsg(SQLException.class,
                () -> metadata.getColumnIndex("column0"),
                "Column not found: column0");

        assertEquals(19, metadata.getColumnCount());

        for (int index = 0; index < metadata.getColumnCount(); index++) {
            int column = index + 1;
            String name = metadata.getColumnName(column);
            assertNotNull(name);
            assertEquals(name, metadata.getColumnLabel(column));
            assertEquals(column, metadata.getColumnIndex(name));

            assertFalse(metadata.isAutoIncrement(column), "All columns are not isAutoIncrement");
            assertTrue(metadata.isCaseSensitive(column), "All columns are isCaseSensitive");
            assertFalse(metadata.isSearchable(column), "All columns are not isSearchable");
            assertFalse(metadata.isCurrency(column), "All columns are not isCurrency");
            assertEquals(ResultSetMetaData.columnNullable, metadata.isNullable(column),
                    "All columns in table are nullable, but pseudo-columns are not");
            assertFalse(metadata.isSigned(column), "All columns are not isSigned");
            assertEquals(0, metadata.getColumnDisplaySize(column), "No display size available");
            assertEquals("", metadata.getSchemaName(column), "No schema available");
            assertEquals(0, metadata.getPrecision(column), "No precision available");
            assertEquals(0, metadata.getScale(column), "No scale available");
            assertEquals("", metadata.getTableName(column), "No table name available");
            assertEquals("", metadata.getCatalogName(column), "No catalog name available");
            assertTrue(metadata.isReadOnly(column), "All columns are isReadOnly");
            assertFalse(metadata.isWritable(column), "All columns are not isWritable");
            assertFalse(metadata.isDefinitelyWritable(column), "All columns are not isDefinitelyWritable");

            if (name.startsWith("c_")) {
                String expectType = name.substring("c_".length()).toLowerCase();
                if (expectType.equals("decimal")) {
                    expectType += "(22, 9)";
                }

                String actualType = metadata.getColumnTypeName(column);
                assertNotNull(actualType, "All columns have database types");
                assertEquals(expectType, actualType.toLowerCase(),
                        "All column names are similar to types");
            }

            assertTrue(metadata.getColumnType(column) != 0,
                    "All columns have sql type, including " + name);
            // getColumnClassName is checking already
        }

    }

    @Test
    void getObject() throws SQLException {
        checkRows(
                resultSet::getObject,
                resultSet::getObject,
                map(
                        "key", 1,
                        "c_Bool", true,
                        "c_Int32", 2000000001,
                        "c_Int64", 2000000000001L,
                        "c_Uint8", 100,
                        "c_Uint32", 2000000002L,
                        "c_Uint64", 2000000000002L,
                        "c_Float", 123456.78f,
                        "c_Double", 123456789.123456789d,
                        "c_String", "https://string",
                        "c_Utf8", "file://utf8",
                        "c_Json", "{\"key\": \"value Json\"}",
                        "c_JsonDocument", "{\"key\":\"value JsonDocument\"}",
                        "c_Yson", "{key=\"value yson\"}".getBytes(),
                        "c_Date", LocalDate.parse("1978-07-09"),
                        "c_Datetime", LocalDateTime.parse("1970-02-06T00:11:51"),
                        "c_Timestamp", Instant.parse("1970-01-01T00:00:03.111112Z"),
                        "c_Interval", Duration.parse("PT3.111113S"),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue("3.335000000")
                ),
                map(
                        "key", 2,
                        "c_Bool", false,
                        "c_Int32", -2000000001,
                        "c_Int64", -2000000000001L,
                        "c_Uint8", 200,
                        "c_Uint32", 4000000002L,
                        "c_Uint64", 4000000000002L,
                        "c_Float", -123456.78f,
                        "c_Double", -123456789.123456789d,
                        "c_String", "",
                        "c_Utf8", "",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "\"\"".getBytes(),
                        "c_Date", LocalDate.parse("1978-07-10"),
                        "c_Datetime", LocalDateTime.parse("1970-02-06T00:28:31"),
                        "c_Timestamp", Instant.parse("1970-01-01T00:00:03.112112Z"),
                        "c_Interval", Duration.parse("PT3.112113S"),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue("-3.335000000")
                ),
                map(
                        "key", 3,
                        "c_Bool", false,
                        "c_Int32", 0,
                        "c_Int64", 0L,
                        "c_Uint8", 0,
                        "c_Uint32", 0L,
                        "c_Uint64", 0L,
                        "c_Float", 0.0f,
                        "c_Double", 0.0d,
                        "c_String", "0",
                        "c_Utf8", "0",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "0".getBytes(),
                        "c_Date", LocalDate.parse("1970-01-01"),
                        "c_Datetime", LocalDateTime.parse("1970-01-01T00:00"),
                        "c_Timestamp", Instant.parse("1970-01-01T00:00:00Z"),
                        "c_Interval", Duration.parse("PT0S"),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue(0)
                ),
                map(
                        "key", 4,
                        "c_Bool", true,
                        "c_Int32", 1,
                        "c_Int64", 1L,
                        "c_Uint8", 1,
                        "c_Uint32", 1L,
                        "c_Uint64", 1L,
                        "c_Float", 1.0f,
                        "c_Double", 1.0d,
                        "c_String", "1",
                        "c_Utf8", "1",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "1".getBytes(),
                        "c_Date", LocalDate.parse("1970-01-02"),
                        "c_Datetime", LocalDateTime.parse("1970-01-01T00:00:01"),
                        "c_Timestamp", Instant.parse("1970-01-01T00:00:00.000001Z"),
                        "c_Interval", Duration.parse("PT0.000001S"),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue("1.000000000")
                ),
                map(
                        "key", 5,
                        "c_Bool", null,
                        "c_Int32", null,
                        "c_Int64", null,
                        "c_Uint8", null,
                        "c_Uint32", null,
                        "c_Uint64", null,
                        "c_Float", null,
                        "c_Double", null,
                        "c_String", null,
                        "c_Utf8", null,
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", null,
                        "c_Date", null,
                        "c_Datetime", null,
                        "c_Timestamp", null,
                        "c_Interval", null,
                        "c_Decimal", null
                ),
                true); // check types
    }

    @Test
    void getObjectUnsupported() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getObject(1, Integer.class),
                "Object with type conversion is not supported yet");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getObject("Column", Integer.class),
                "Object with type conversion is not supported yet");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getObject(1, Collections.emptyMap()),
                "Object with type conversion is not supported yet");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getObject("Column", Collections.emptyMap()),
                "Object with type conversion is not supported yet");
    }

    @Test
    void getNativeColumn() throws SQLException {
        checkRows(
                columnIndex -> resultSet.getNativeColumn(columnIndex).orElse(null),
                columnLabel -> resultSet.getNativeColumn(columnLabel).orElse(null),
                map(
                        "key", PrimitiveValue.int32(1),
                        "c_Bool", PrimitiveValue.bool(true),
                        "c_Int32", PrimitiveValue.int32(2000000001),
                        "c_Int64", PrimitiveValue.int64(2000000000001L),
                        "c_Uint8", PrimitiveValue.uint8((byte) 100),
                        "c_Uint32", PrimitiveValue.uint32(2000000002),
                        "c_Uint64", PrimitiveValue.uint64(2000000000002L),
                        "c_Float", PrimitiveValue.float32(123456.78f),
                        "c_Double", PrimitiveValue.float64(123456789.123456789d),
                        "c_String", PrimitiveValue.string("https://string".getBytes()),
                        "c_Utf8", PrimitiveValue.utf8("file://utf8"),
                        "c_Json", PrimitiveValue.json("{\"key\": \"value Json\"}"),
                        "c_JsonDocument", PrimitiveValue.jsonDocument("{\"key\":\"value JsonDocument\"}"),
                        "c_Yson", PrimitiveValue.yson("{key=\"value yson\"}".getBytes()),
                        "c_Date", PrimitiveValue.date(LocalDate.parse("1978-07-09")),
                        "c_Datetime", PrimitiveValue.datetime(Instant.parse("1970-02-06T00:11:51Z")),
                        "c_Timestamp", PrimitiveValue.timestamp(Instant.parse("1970-01-01T00:00:03.111112Z")),
                        "c_Interval", PrimitiveValue.interval(Duration.parse("PT3.111113S")),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue("3.335000000")
                ),
                map(
                        "key", PrimitiveValue.int32(2),
                        "c_Bool", PrimitiveValue.bool(false),
                        "c_Int32", PrimitiveValue.int32(-2000000001),
                        "c_Int64", PrimitiveValue.int64(-2000000000001L),
                        "c_Uint8", PrimitiveValue.uint8((byte) 200),
                        "c_Uint32", PrimitiveValue.uint32((int) 4000000002L),
                        "c_Uint64", PrimitiveValue.uint64(4000000000002L),
                        "c_Float", PrimitiveValue.float32(-123456.78f),
                        "c_Double", PrimitiveValue.float64(-123456789.123456789d),
                        "c_String", PrimitiveValue.string("".getBytes()),
                        "c_Utf8", PrimitiveValue.utf8(""),
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", PrimitiveValue.yson("\"\"".getBytes()),
                        "c_Date", PrimitiveValue.date(LocalDate.parse("1978-07-10")),
                        "c_Datetime", PrimitiveValue.datetime(Instant.parse("1970-02-06T00:28:31Z")),
                        "c_Timestamp", PrimitiveValue.timestamp(Instant.parse("1970-01-01T00:00:03.112112Z")),
                        "c_Interval", PrimitiveValue.interval(Duration.parse("PT3.112113S")),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue("-3.335000000")
                ),
                map(
                        "key", PrimitiveValue.int32(3),
                        "c_Bool", PrimitiveValue.bool(false),
                        "c_Int32", PrimitiveValue.int32(0),
                        "c_Int64", PrimitiveValue.int64(0),
                        "c_Uint8", PrimitiveValue.uint8((byte) 0),
                        "c_Uint32", PrimitiveValue.uint32(0),
                        "c_Uint64", PrimitiveValue.uint64(0),
                        "c_Float", PrimitiveValue.float32(0),
                        "c_Double", PrimitiveValue.float64(0),
                        "c_String", PrimitiveValue.string("0".getBytes()),
                        "c_Utf8", PrimitiveValue.utf8("0"),
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", PrimitiveValue.yson("0".getBytes()),
                        "c_Date", PrimitiveValue.date(LocalDate.parse("1970-01-01")),
                        "c_Datetime", PrimitiveValue.datetime(Instant.parse("1970-01-01T00:00:00Z")),
                        "c_Timestamp", PrimitiveValue.timestamp(Instant.parse("1970-01-01T00:00:00Z")),
                        "c_Interval", PrimitiveValue.interval(Duration.parse("PT0S")),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue(0)
                ),
                map(
                        "key", PrimitiveValue.int32(4),
                        "c_Bool", PrimitiveValue.bool(true),
                        "c_Int32", PrimitiveValue.int32(1),
                        "c_Int64", PrimitiveValue.int64(1),
                        "c_Uint8", PrimitiveValue.uint8((byte) 1),
                        "c_Uint32", PrimitiveValue.uint32(1),
                        "c_Uint64", PrimitiveValue.uint64(1),
                        "c_Float", PrimitiveValue.float32(1),
                        "c_Double", PrimitiveValue.float64(1),
                        "c_String", PrimitiveValue.string("1".getBytes()),
                        "c_Utf8", PrimitiveValue.utf8("1"),
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", PrimitiveValue.yson("1".getBytes()),
                        "c_Date", PrimitiveValue.date(LocalDate.parse("1970-01-02")),
                        "c_Datetime", PrimitiveValue.datetime(Instant.parse("1970-01-01T00:00:01Z")),
                        "c_Timestamp", PrimitiveValue.timestamp(Instant.parse("1970-01-01T00:00:00.000001Z")),
                        "c_Interval", PrimitiveValue.interval(Duration.parse("PT0.000001S")),
                        "c_Decimal", YdbTypes.DEFAULT_DECIMAL_TYPE.newValue("1.000000000")
                ),
                map(
                        "key", PrimitiveValue.int32(5),
                        "c_Bool", null,
                        "c_Int32", null,
                        "c_Int64", null,
                        "c_Uint8", null,
                        "c_Uint32", null,
                        "c_Uint64", null,
                        "c_Float", null,
                        "c_Double", null,
                        "c_String", null,
                        "c_Utf8", null,
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", null,
                        "c_Date", null,
                        "c_Datetime", null,
                        "c_Timestamp", null,
                        "c_Interval", null,
                        "c_Decimal", null
                ),
                false); // check types
    }

    @Test
    void findColumn() throws SQLException {
        assertEquals(1, resultSet.findColumn("key"));
        assertEquals(11, resultSet.findColumn("c_Utf8"));
    }

    @Test
    void findColumnUnknown() {
        assertThrowsMsg(SQLException.class,
                () -> resultSet.findColumn("value0"),
                "Column not found: value0");
    }

    @Test
    void getCharacterStream() throws SQLException {
        checkRows(
                resultSet::getCharacterStream,
                resultSet::getCharacterStream,
                map(
                        "c_String", reader("https://string"),
                        "c_Utf8", reader("file://utf8"),
                        "c_Json", reader("{\"key\": \"value Json\"}"),
                        "c_JsonDocument", reader("{\"key\":\"value JsonDocument\"}"),
                        "c_Yson", reader("{key=\"value yson\"}")
                ),
                map(
                        "c_String", reader(""),
                        "c_Utf8", reader(""),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader("\"\"")
                ),
                map(
                        "c_String", reader("0"),
                        "c_Utf8", reader("0"),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader("0")
                ),
                map(
                        "c_String", reader("1"),
                        "c_Utf8", reader("1"),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader("1")
                ),
                map(
                        "c_String", reader(null),
                        "c_Utf8", reader(null),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader(null)
                ));
    }

    @Test
    void first() throws SQLException {
        assertFalse(resultSet.isFirst());
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.next());
        assertTrue(resultSet.isFirst());
        assertEquals(1, resultSet.getRow());

        assertTrue(resultSet.next());
        assertFalse(resultSet.isFirst());
        assertEquals(2, resultSet.getRow());
    }

    @Test
    void last() throws SQLException {
        assertFalse(resultSet.isLast());
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.last());
        assertTrue(resultSet.isLast());
        assertEquals(5, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertFalse(resultSet.isLast());
        assertEquals(4, resultSet.getRow());

        assertTrue(resultSet.next());
        assertTrue(resultSet.isLast());
        assertEquals(5, resultSet.getRow());

        assertFalse(resultSet.next());
        assertFalse(resultSet.isLast());
        assertEquals(6, resultSet.getRow());

        assertFalse(resultSet.next());
        assertEquals(6, resultSet.getRow());
    }

    @Test
    void beforeFirst() throws SQLException {
        assertTrue(resultSet.isBeforeFirst());
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.next());
        assertFalse(resultSet.isBeforeFirst());
        assertEquals(1, resultSet.getRow());

        resultSet.beforeFirst();
        assertTrue(resultSet.isBeforeFirst());
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.next());
        assertFalse(resultSet.isBeforeFirst());
        assertEquals(1, resultSet.getRow());
    }

    @Test
    void afterLast() throws SQLException {
        assertFalse(resultSet.isAfterLast());
        assertEquals(0, resultSet.getRow());

        resultSet.afterLast();
        assertTrue(resultSet.isAfterLast());
        assertEquals(6, resultSet.getRow());

        assertFalse(resultSet.next());
        assertTrue(resultSet.isAfterLast());
        assertEquals(6, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertFalse(resultSet.isAfterLast());
        assertEquals(5, resultSet.getRow());

        assertFalse(resultSet.next());
        assertTrue(resultSet.isAfterLast());
        assertEquals(6, resultSet.getRow());
    }

    @Test
    void absolute() throws SQLException {
        assertEquals(0, resultSet.getRow());

        assertFalse(resultSet.absolute(0));
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.absolute(1));
        assertEquals(1, resultSet.getRow());

        assertTrue(resultSet.absolute(-1));
        assertEquals(5, resultSet.getRow());

        assertTrue(resultSet.absolute(-2));
        assertEquals(4, resultSet.getRow());

        assertTrue(resultSet.absolute(4));
        assertEquals(4, resultSet.getRow());

        assertTrue(resultSet.absolute(5));
        assertEquals(5, resultSet.getRow());

        assertFalse(resultSet.absolute(6));
        assertEquals(6, resultSet.getRow());

        assertFalse(resultSet.absolute(7));
        assertEquals(6, resultSet.getRow());
    }

    @Test
    void relative() throws SQLException {
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.relative(1));
        assertEquals(1, resultSet.getRow());

        assertTrue(resultSet.relative(2));
        assertEquals(3, resultSet.getRow());

        assertTrue(resultSet.relative(0));
        assertEquals(3, resultSet.getRow());

        assertFalse(resultSet.relative(3));
        assertEquals(6, resultSet.getRow());

        assertFalse(resultSet.relative(2));
        assertEquals(6, resultSet.getRow());

        assertTrue(resultSet.relative(-1));
        assertEquals(5, resultSet.getRow());

        assertTrue(resultSet.relative(-1));
        assertEquals(4, resultSet.getRow());

        assertFalse(resultSet.relative(-10));
        assertEquals(0, resultSet.getRow());

        assertFalse(resultSet.relative(-1));
        assertEquals(0, resultSet.getRow());
    }

    @Test
    void previous() throws SQLException {
        assertEquals(0, resultSet.getRow());

        assertTrue(resultSet.last());
        assertEquals(5, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertEquals(4, resultSet.getRow());

        assertTrue(resultSet.next());
        assertEquals(5, resultSet.getRow());

        assertFalse(resultSet.next());
        assertEquals(6, resultSet.getRow());

        assertFalse(resultSet.next());
        assertEquals(6, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertEquals(5, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertEquals(4, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertEquals(3, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertEquals(2, resultSet.getRow());

        assertTrue(resultSet.previous());
        assertEquals(1, resultSet.getRow());

        assertFalse(resultSet.previous());
        assertEquals(0, resultSet.getRow());

        assertFalse(resultSet.previous());
        assertEquals(0, resultSet.getRow());
    }

    @Test
    void moveOnEmptyResultSet() throws SQLException {
        resultSet = resultSet.getStatement().executeQuery("select * from unit_1 where 1 = 0");

        SQLRunnable sql = () -> {
            assertFalse(resultSet.isBeforeFirst());
            assertFalse(resultSet.isAfterLast());
            assertFalse(resultSet.isFirst());
            assertFalse(resultSet.isLast());
            assertEquals(0, resultSet.getRow());
        };
        sql.run();

        resultSet.beforeFirst();
        sql.run();

        resultSet.afterLast();
        sql.run();

        assertFalse(resultSet.next());
        sql.run();

        assertFalse(resultSet.previous());
        sql.run();

        assertFalse(resultSet.first());
        sql.run();

        assertFalse(resultSet.last());
        sql.run();

        assertFalse(resultSet.absolute(0));
        sql.run();

        assertFalse(resultSet.absolute(1));
        sql.run();

        assertFalse(resultSet.absolute(-1));
        sql.run();

        assertFalse(resultSet.relative(0));
        sql.run();

        assertFalse(resultSet.relative(1));
        sql.run();

        assertFalse(resultSet.relative(-1));
        sql.run();
    }

    @Test
    void fetchDirection() throws SQLException {
        assertEquals(ResultSet.FETCH_UNKNOWN, resultSet.getFetchDirection());
        resultSet.setFetchDirection(ResultSet.FETCH_FORWARD); // do nothing actually
        assertEquals(ResultSet.FETCH_FORWARD, resultSet.getFetchDirection());
    }

    @Test
    void fetchSize() throws SQLException {
        assertEquals(1000, resultSet.getFetchSize());
        resultSet.setFetchSize(99); // do nothing
        assertEquals(1000, resultSet.getFetchSize());
    }

    @Test
    void getType() throws SQLException {
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, resultSet.getType());
    }

    @Test
    void getConcurrency() throws SQLException {
        assertEquals(ResultSet.CONCUR_READ_ONLY, resultSet.getConcurrency());
    }

    @Test
    void getStatement() throws SQLException {
        assertNotNull(resultSet.getStatement());

        YdbStatement statement = getTestConnection().createStatement();
        assertSame(statement, statement.executeQuery("select 1 + 2").getStatement());
    }

    @Test
    void getRef() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getRef(1),
                "Refs are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getRef("Ref"),
                "Refs are not supported");
    }

    @Test
    void getBlob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getBlob(1),
                "Blobs are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getBlob("Blob"),
                "Blobs are not supported");
    }

    @Test
    void getClob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getClob(1),
                "Clobs are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getClob("Blob"),
                "Clobs are not supported");
    }

    @Test
    void getArray() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getArray(1),
                "Arrays are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getArray("Blob"),
                "Arrays are not supported");
    }

    @Test
    void getURL() throws MalformedURLException, SQLException {
        checkRows(
                resultSet::getURL,
                resultSet::getURL,
                map(
                        "c_String", new URL("https://string"),
                        "c_Utf8", new URL("file://utf8")
                ),
                map(),
                map(),
                map(),
                map(
                        "c_String", null,
                        "c_Utf8", null
                ));
    }

    @Test
    void getHoldability() throws SQLException {
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, resultSet.getHoldability());
    }

    @Test
    void getNClob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getNClob(1),
                "NClobs are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getNClob("Blob"),
                "NClobs are not supported");
    }

    @Test
    void getSQLXML() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getSQLXML(1),
                "SQLXMLs are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getSQLXML("Blob"),
                "SQLXMLs are not supported");
    }

    @Test
    void getNString() throws SQLException {
        checkRows(
                resultSet::getNString,
                resultSet::getNString,
                map(
                        "c_String", "https://string",
                        "c_Utf8", "file://utf8",
                        "c_Json", "{\"key\": \"value Json\"}",
                        "c_JsonDocument", "{\"key\":\"value JsonDocument\"}",
                        "c_Yson", "{key=\"value yson\"}"
                ),
                map(
                        "c_String", "",
                        "c_Utf8", "",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "\"\""
                ),
                map(
                        "c_String", "0",
                        "c_Utf8", "0",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "0"
                ),
                map(
                        "c_String", "1",
                        "c_Utf8", "1",
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", "1"
                ),
                map(
                        "c_String", null,
                        "c_Utf8", null,
                        "c_Json", null,
                        "c_JsonDocument", null,
                        "c_Yson", null
                ));
    }

    @Test
    void getNCharacterStream() throws SQLException {
        checkRows(
                resultSet::getNCharacterStream,
                resultSet::getNCharacterStream,
                map(
                        "c_String", reader("https://string"),
                        "c_Utf8", reader("file://utf8"),
                        "c_Json", reader("{\"key\": \"value Json\"}"),
                        "c_JsonDocument", reader("{\"key\":\"value JsonDocument\"}"),
                        "c_Yson", reader("{key=\"value yson\"}")
                ),
                map(
                        "c_String", reader(""),
                        "c_Utf8", reader(""),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader("\"\"")
                ),
                map(
                        "c_String", reader("0"),
                        "c_Utf8", reader("0"),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader("0")
                ),
                map(
                        "c_String", reader("1"),
                        "c_Utf8", reader("1"),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader("1")
                ),
                map(
                        "c_String", reader(null),
                        "c_Utf8", reader(null),
                        "c_Json", reader(null),
                        "c_JsonDocument", reader(null),
                        "c_Yson", reader(null)
                ));
    }

    @Test
    void rowUpdated() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.rowUpdated(),
                "Cursor updates are not supported");
    }

    @Test
    void rowInserted() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.rowInserted(),
                "Cursor updates are not supported");
    }

    @Test
    void rowDeleted() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.rowDeleted(),
                "Cursor updates are not supported");
    }

    @Test
    void updateNull() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNull(1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNull("value"),
                "Cursor updates are not supported");
    }

    @Test
    void updateBoolean() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBoolean(1, true),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBoolean("value", true),
                "Cursor updates are not supported");
    }

    @Test
    void updateByte() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateByte(1, (byte) 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateByte("value", (byte) 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateShort() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateShort(1, (short) 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateShort("value", (short) 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateInt() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateInt(1, 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateInt("value", 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateLong() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateLong(1, 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateLong("value", 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateFloat() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateFloat(1, 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateFloat("value", 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateDouble() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateDouble(1, 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateDouble("value", 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateBigDecimal() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBigDecimal(1, BigDecimal.ONE),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBigDecimal("value", BigDecimal.ONE),
                "Cursor updates are not supported");
    }

    @Test
    void updateString() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBoolean(1, true),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBoolean("value", true),
                "Cursor updates are not supported");
    }

    @Test
    void updateBytes() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBytes(1, new byte[0]),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBytes("value", new byte[0]),
                "Cursor updates are not supported");
    }

    @Test
    void updateDate() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateDate(1, new Date(0)),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateDate("value", new Date(0)),
                "Cursor updates are not supported");
    }

    @Test
    void updateTime() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateTime(1, new Time(0)),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateTime("value", new Time(0)),
                "Cursor updates are not supported");
    }

    @Test
    void updateTimestamp() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateTimestamp(1, new Timestamp(0)),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateTimestamp("value", new Timestamp(0)),
                "Cursor updates are not supported");
    }

    @Test
    void updateAsciiStream() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateAsciiStream(1, new ByteArrayInputStream(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateAsciiStream(1, new ByteArrayInputStream(new byte[0]), 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateAsciiStream(1, new ByteArrayInputStream(new byte[0]), 1L),
                "Cursor updates are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateAsciiStream("value", new ByteArrayInputStream(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateAsciiStream("value", new ByteArrayInputStream(new byte[0]), 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateAsciiStream("value", new ByteArrayInputStream(new byte[0]), 1L),
                "Cursor updates are not supported");
    }

    @Test
    void updateBinaryStream() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBinaryStream(1, new ByteArrayInputStream(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBinaryStream(1, new ByteArrayInputStream(new byte[0]), 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBinaryStream(1, new ByteArrayInputStream(new byte[0]), 1L),
                "Cursor updates are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBinaryStream("value", new ByteArrayInputStream(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBinaryStream("value", new ByteArrayInputStream(new byte[0]), 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBinaryStream("value", new ByteArrayInputStream(new byte[0]), 1L),
                "Cursor updates are not supported");
    }

    @Test
    void updateCharacterStream() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateCharacterStream(1, new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateCharacterStream(1, new StringReader(""), 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateCharacterStream(1, new StringReader(""), 1L),
                "Cursor updates are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateCharacterStream("value", new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateCharacterStream("value", new StringReader(""), 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateCharacterStream("value", new StringReader(""), 1L),
                "Cursor updates are not supported");
    }

    @Test
    void updateObject() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject(1, true),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject(1, true, 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject(1, true, JDBCType.INTEGER),
                "updateObject not implemented");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject(1, true, JDBCType.INTEGER, 1),
                "updateObject not implemented");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject("value", true),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject("value", true, 1),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject("value", true, JDBCType.INTEGER),
                "updateObject not implemented");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateObject("value", true, JDBCType.INTEGER, 1),
                "updateObject not implemented");
    }

    @Test
    void insertRow() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.insertRow(),
                "Cursor updates are not supported");
    }

    @Test
    void updateRow() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateRow(),
                "Cursor updates are not supported");
    }

    @Test
    void deleteRow() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.deleteRow(),
                "Cursor updates are not supported");
    }

    @Test
    void refreshRow() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.refreshRow(),
                "Cursor updates are not supported");
    }

    @Test
    void cancelRowUpdates() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.cancelRowUpdates(),
                "Cursor updates are not supported");
    }

    @Test
    void moveToInsertRow() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.moveToInsertRow(),
                "Cursor updates are not supported");
    }

    @Test
    void moveToCurrentRow() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.moveToCurrentRow(),
                "Cursor updates are not supported");
    }

    @Test
    void updateRef() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateRef(1, new RefImpl()),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateRef("value", new RefImpl()),
                "Cursor updates are not supported");
    }

    @Test
    void updateBlob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBlob(1, new SerialBlob(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBlob(1, new ByteArrayInputStream(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBlob(1, new ByteArrayInputStream(new byte[0]), 1),
                "Cursor updates are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBlob("value", new SerialBlob(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBlob("value", new ByteArrayInputStream(new byte[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateBlob("value", new ByteArrayInputStream(new byte[0]), 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateClob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateClob(1, new SerialClob(new char[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateClob(1, new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateClob(1, new StringReader(""), 1),
                "Cursor updates are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateClob("value", new SerialClob(new char[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateClob("value", new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateClob("value", new StringReader(""), 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateArray() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateArray(1, new ArrayImpl()),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateArray("value", new ArrayImpl()),
                "Cursor updates are not supported");
    }

    @Test
    void getRowId() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getRowId(1),
                "RowIds are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.getRowId("value"),
                "RowIds are not supported");
    }

    @Test
    void updateRowId() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateRowId(1, new RowIdImpl()),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateRowId("value", new RowIdImpl()),
                "Cursor updates are not supported");
    }

    @Test
    void updateNCharacterStream() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNCharacterStream(1, new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNCharacterStream(1, new StringReader(""), 1),
                "Cursor updates are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNCharacterStream("value", new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNCharacterStream("value", new StringReader(""), 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateNClob() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNClob(1, new NClobImpl(new char[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNClob(1, new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNClob(1, new StringReader(""), 1),
                "Cursor updates are not supported");

        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNClob("value", new NClobImpl(new char[0])),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNClob("value", new StringReader("")),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNClob("value", new StringReader(""), 1),
                "Cursor updates are not supported");
    }

    @Test
    void updateNString() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNString(1, ""),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateNString("value", ""),
                "Cursor updates are not supported");
    }

    @Test
    void updateSQLXML() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateSQLXML(1, new SQLXMLImpl()),
                "Cursor updates are not supported");
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> resultSet.updateSQLXML("value", new SQLXMLImpl()),
                "Cursor updates are not supported");
    }

    @Test
    void unwrap() throws SQLException {
        assertTrue(resultSet.isWrapperFor(YdbResultSet.class));
        assertSame(resultSet, resultSet.unwrap(YdbResultSet.class));

        assertFalse(resultSet.isWrapperFor(YdbStatement.class));
        assertThrowsMsg(SQLException.class,
                () -> resultSet.unwrap(YdbStatement.class),
                "Cannot unwrap to " + YdbStatement.class);
    }

    //

    <T> void checkRows(SQLFunction<Integer, T> byIndex,
                       SQLFunction<String, T> byName,
                       Map<String, T> row1,
                       Map<String, T> row2,
                       Map<String, T> row3,
                       Map<String, T> row4,
                       Map<String, T> row5) throws SQLException {
        checkRows(byIndex, byName, row1, row2, row3, row4, row5, false);
    }

    <T> void checkRows(SQLFunction<Integer, T> byIndex,
                       SQLFunction<String, T> byName,
                       Map<String, T> row1,
                       Map<String, T> row2,
                       Map<String, T> row3,
                       Map<String, T> row4,
                       Map<String, T> row5,
                       boolean checkDataTypes) throws SQLException {
        long time = System.currentTimeMillis();

        List<Map<String, T>> rows = Arrays.asList(row1, row2, row3, row4, row5);

        for (int i = 0; i < rows.size(); i++) {
            assertTrue(resultSet.next());
            checkRow(byIndex, byName, rows.get(i), i + 1, checkDataTypes);
        }

        assertFalse(resultSet.next());

        LOGGER.info("Verified within {} millis", System.currentTimeMillis() - time);

    }

    @SuppressWarnings("unchecked")
    private <T> void checkRow(SQLFunction<Integer, T> byIndex,
                              SQLFunction<String, T> byName,
                              Map<String, T> row,
                              int rowIndex,
                              boolean checkDataTypes) throws SQLException {
        YdbResultSetMetaData metaData = resultSet.getMetaData();

        List<Executable> asserts = new ArrayList<>(1 + 2 * metaData.getColumnCount() + row.size() * 4);
        for (Map.Entry<String, T> entry : row.entrySet()) {
            String columnName = entry.getKey();
            T expectedValue;
            boolean wasNull;
            if (entry.getValue() instanceof NullType) {
                expectedValue = ((NullType<T>) entry.getValue()).value;
                wasNull = true;
            } else {
                expectedValue = entry.getValue();
                wasNull = expectedValue == null;
            }

            int index = metaData.getColumnIndex(columnName);
            asserts.add(() -> {
                assertEquals(castCompatible(expectedValue), castCompatible(byName.apply(columnName)),
                        "Row " + rowIndex + ", Checking column " + columnName);
                assertEquals(castCompatible(expectedValue), castCompatible(byIndex.apply(index)),
                        "Row " + rowIndex + ", Checking column " + columnName + " at " + index);
            });

            asserts.add(() -> assertEquals(wasNull, resultSet.wasNull(),
                    "Row " + rowIndex + ", Last value for column " + columnName + " was null"));

            asserts.add(() -> assertEquals(castType(expectedValue), castType(byIndex.apply(index)),
                    "Row " + rowIndex + ", Check class type for " + columnName));

            if (expectedValue != null) {
                if (checkDataTypes) {
                    asserts.add(() -> assertEquals(metaData.getColumnClassName(index),
                            byIndex.apply(index).getClass().getName(),
                            "Row " + rowIndex + ", Checking java type for " + columnName));
                }
            }
        }

        // Check all other columns produces exception
        Set<String> columns = new HashSet<>(metaData.getColumnNames());
        columns.removeAll(row.keySet());
        for (String columnName : columns) {
            int index = metaData.getColumnIndex(columnName);
            asserts.add(() -> assertThrowsMsgLike(
                    SQLException.class,
                    () -> byName.apply(columnName),
                    "Cannot cast",
                    "Row " + rowIndex + ", Checking casting error for " + columnName + " at " + index));
            asserts.add(() -> assertThrowsMsgLike(
                    SQLException.class,
                    () -> byIndex.apply(index),
                    "Cannot cast",
                    "Row " + rowIndex + ", Checking casting error for " + columnName));
        }


        assertAll(asserts);
    }


    //

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> map(Object... kv) {
        Preconditions.checkState((kv.length & 1) == 0, "Length must be even");

        Map<String, T> map = new LinkedHashMap<>(1 + (kv.length / 2));
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], (T) kv[i + 1]);
        }
        return map;
    }

    private static <T> Class<?> castType(T value) {
        if (value instanceof Reader) {
            return Reader.class;
        } else if (value instanceof InputStream) {
            return InputStream.class;
        } else if (value == null) {
            return null;
        } else {
            return value.getClass();
        }
    }

    private static class NullType<T> {
        private final T value;

        private NullType(T value) {
            this.value = Objects.requireNonNull(value);
        }

        static <T> NullType<T> of(T value) {
            return new NullType<>(value);
        }
    }

    interface SQLFunction<K, V> {
        V apply(K key) throws Exception;
    }

    interface SQLRunnable {
        void run() throws SQLException;
    }
}
