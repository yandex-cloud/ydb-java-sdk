package com.yandex.ydb.jdbc.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.jdbc.exception.YdbResultTruncatedException;
import org.junit.jupiter.api.Test;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertEquals;

class YdbPreparedStatementBatchedImplTest extends AbstractYdbPreparedStatementImplTest {

    static final String PREPARE_ALL = stringFileReference("classpath:sql/prepare_all_values_batched.sql");

    @Test
    void executeWithoutBatch() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.addBatch();

            statement.setInt("key", 2);
            statement.setString("c_Utf8", "value-2");

            statement.execute(); // Just added silently
            connection.commit();

            checkSimpleResultSet(connection);
        });
    }

    @Test
    void addBatchClearParameters() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
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
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.setInt("key", 1);
            statement.setString("c_Utf8", "value-1");
            statement.addBatch();

            statement.setInt("key", 2);
            statement.setString("c_Utf8", "value-2");
            statement.addBatch();

            // No add batch, must be skipped
            statement.setInt("key", 3);
            statement.setString("c_Utf8", "value-3");

            statement.executeBatch();
            connection.commit();

            checkSimpleResultSet(connection);
        });
    }

    @Test
    void addAndClearBatch() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
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
    void testInvalidStruct() throws SQLException {
        retry(connection ->
                TestHelper.assertThrowsMsgLike(YdbNonRetryableException.class,
                        () -> connection.prepareStatement(
                                "declare $values as List<Struct<key:Int32,c_Utf8:Utf8,key:Int32>>; \n" +
                                        "upsert into unit_2 select * from as_table($values)"),
                        "Duplicated member: key"));
    }

    @Test
    void executeEmpty() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);
            statement.execute();
            connection.commit();
            checkNoResultSet(connection);

            statement.executeUpdate();
            connection.commit();
            checkNoResultSet(connection);
        });
    }

    @Test
    void executeEmptyNoResultSet() throws SQLException {
        retry(connection ->
                assertThrowsMsg(SQLException.class,
                        () -> getUtf8Statement(connection).executeQuery(),
                        "Query must return ResultSet"));
    }

    @Test
    void executeQueryBatchWithScanRead() throws SQLException {
        retry(connection -> {

            int rowCount = 5000;
            List<String> expect = new ArrayList<>();

            YdbPreparedStatement statement = getUtf8Statement(connection);
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

    @Override
    protected YdbPreparedStatement getTestStatement(YdbConnection connection,
                                                    String column,
                                                    String type) throws SQLException {
        return connection.prepareStatement(
                String.format("declare $values as List<Struct<key:Int32,%s:%s>>; \n" +
                                "upsert into unit_2 select * from as_table($values)",
                        column, type));
    }

    @Override
    protected YdbPreparedStatement getTestAllValuesStatement(YdbConnection connection) throws SQLException {
        return connection.prepareStatement(subst(PREPARE_ALL, "unit_2"));
    }

    @Override
    protected boolean expectParameterPrefixed() {
        return false;
    }
}
