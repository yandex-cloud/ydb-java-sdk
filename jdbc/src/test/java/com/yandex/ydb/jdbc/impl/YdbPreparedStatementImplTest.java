package com.yandex.ydb.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.jdbc.exception.YdbResultTruncatedException;
import com.yandex.ydb.table.values.PrimitiveType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static com.yandex.ydb.jdbc.TestHelper.TEST_TYPE;
import static com.yandex.ydb.jdbc.TestHelper.UNIVERSAL;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsgLike;
import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledIfSystemProperty(named = TEST_TYPE, matches = UNIVERSAL)
class YdbPreparedStatementImplTest extends AbstractYdbPreparedStatementImplTest {

    static final String PREPARE_ALL = stringFileReference("classpath:sql/prepare_all_values_nullable.sql");

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
                        "Parameter $c_Utf8 type mismatch, expected: Utf8, actual: Utf8?"));
    }

    @Test
    @Override
    void unknownColumns() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setObject("column0", "value");
            statement.execute();
        }); // Not an error - we don't know if this column is known or not
    }


    @Test
    void executeWithoutBatch() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.addBatch();

            statement.setInt("key", 2);
            statement.setString("c_Utf8", "value-2");

            assertThrowsMsg(YdbExecutionException.class,
                    statement::execute,
                    "Cannot call #execute method after #addBatch, must use #executeBatch");

            // clear will be called automatically
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
    void addBatchClearParameters() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8StatementAsBatch(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.addBatch();

            statement.setInt("key", 10);
            statement.setString("c_Utf8", "value-11");
            statement.clearParameters();

            statement.setInt("key", 2);
            statement.setString("c_Utf8", "value-2");
            statement.addBatch();

            statement.executeBatch();
            connection.commit();

            checkSimpleResultSet(connection);
        });
    }

    @Test
    void addBatch() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8StatementAsBatch(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.addBatch();

            statement.setInt("key", 2);
            statement.setString("c_Utf8", "value-2");
            statement.addBatch();

            // No add batch, must be skipped
            statement.setInt("key", 3);
            statement.setString("c_Utf8", "value-3");

            assertArrayEquals(new int[]{Statement.SUCCESS_NO_INFO, Statement.SUCCESS_NO_INFO},
                    statement.executeBatch());

            // does nothing
            assertArrayEquals(new int[0], statement.executeBatch());

            connection.commit();

            checkSimpleResultSet(connection);
        });
    }

    @Test
    void addAndClearBatch() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8StatementAsBatch(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.addBatch();
            statement.executeBatch();

            statement.setInt("key", 11);
            statement.setString("c_Utf8", "value-11");
            statement.addBatch();
            statement.clearBatch();

            statement.setInt("key", 2);
            statement.setString("c_Utf8", "value-2");
            statement.addBatch();
            statement.executeBatch();

            connection.commit();

            checkSimpleResultSet(connection);
        });
    }

    @Test
    void executeEmpty() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8StatementAsBatch(connection);
            assertThrowsMsgLike(YdbNonRetryableException.class,
                    statement::execute,
                    "Missing value for parameter");

            assertThrowsMsgLike(YdbNonRetryableException.class,
                    statement::executeUpdate,
                    "Missing value for parameter");

            statement.executeBatch();
            connection.commit();
            checkNoResultSet(connection);
        });
    }

    @Test
    void executeQueryBatchWithScanRead() throws SQLException {
        retry(connection -> {

            int rowCount = 5000;
            List<String> expect = new ArrayList<>();

            YdbPreparedStatement statement = getUtf8StatementAsBatch(connection);
            for (int i = 1; i <= rowCount; i++) {
                String value = "Row#" + i;
                expect.add(value);

                statement.setInt("key", i);
                statement.setString("c_Utf8", value);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();

            assertEquals(5000, expect.size());

            YdbPreparedStatement select = connection.prepareStatement("select key, c_Utf8 from unit_2");

            // Result is truncated (and we catch that)
            try {
                select.executeQuery();
            } catch (YdbResultTruncatedException e) {
                assertEquals("Result #0 was truncated to 1000 rows", e.getMessage());
            }

            // Expect reading all 5000 rows
            List<String> actual = new ArrayList<>();
            ResultSet rs = select.executeScanQuery();
            while (rs.next()) {
                actual.add(rs.getString(2));
            }

            assertEquals(expect, actual);
        });
    }

    @Test
    void testStatement() throws SQLException {
        retry(connection -> Assertions.assertTrue(
                getUtf8Statement(connection) instanceof YdbPreparedStatementImpl));
    }

    @Override
    protected YdbPreparedStatement getTestStatement(YdbConnection connection,
                                                    String column,
                                                    String type) throws SQLException {
        return connection.prepareStatement(
                String.format(
                        "declare $key as Int32?; \n" +
                                "declare $%s as %s; \n" +
                                "upsert into unit_2(key, %s) values ($key, $%s)",
                        column, type,
                        column, column),
                YdbConnection.PreparedStatementMode.IN_MEMORY);
    }

    @Override
    protected YdbPreparedStatement getTestStatementIndexed(YdbConnection connection,
                                                           String column,
                                                           String type) throws SQLException {
        return connection.prepareStatement(
                String.format(
                        "declare $p1 as Int32?; \n" +
                                "declare $p2 as %s; \n" +
                                "upsert into unit_2(key, %s) values ($p1, $p2)",
                        type, column),
                YdbConnection.PreparedStatementMode.IN_MEMORY);
    }

    protected YdbPreparedStatement getUtf8StatementAsBatch(YdbConnection connection) throws SQLException {
        return getTestStatementAsBatch(connection, "c_Utf8", "Utf8?");
    }

    protected YdbPreparedStatement getTestStatementAsBatch(YdbConnection connection,
                                                           String column,
                                                           String type) throws SQLException {
        return connection.prepareStatement(
                String.format("declare $values as List<Struct<key:Int32?,%s:%s>>; \n" +
                                "upsert into unit_2 select * from as_table($values)",
                        column, type),
                YdbConnection.PreparedStatementMode.IN_MEMORY);
    }

    @Override
    protected YdbPreparedStatement getTestAllValuesStatement(YdbConnection connection) throws SQLException {
        return connection.prepareStatement(subst("unit_2", PREPARE_ALL),
                YdbConnection.PreparedStatementMode.IN_MEMORY);
    }

    @Override
    protected boolean expectParameterPrefixed() {
        return true;
    }

    @Override
    protected boolean sqlTypeRequired() {
        return true;
    }

    @Override
    protected boolean supportIndexedParameters() {
        return true;
    }
}
