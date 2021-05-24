package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.table.values.PrimitiveType;
import org.junit.jupiter.api.Test;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsgLike;

class YdbPreparedStatementInMemoryImplTest extends AbstractYdbPreparedStatementImplTest {

    @Test
    void addBatch() throws SQLException {
        retry(connection -> {
            YdbPreparedStatement statement = getUtf8Statement(connection);

            assertThrowsMsg(SQLFeatureNotSupportedException.class,
                    statement::addBatch,
                    "Batches are not supported in simple prepared statements");

            assertThrowsMsg(SQLFeatureNotSupportedException.class,
                    statement::clearBatch,
                    "Batches are not supported in simple prepared statements");

            assertThrowsMsg(SQLFeatureNotSupportedException.class,
                    statement::executeBatch,
                    "Batches are not supported in simple prepared statements");
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

    @Override
    protected YdbPreparedStatement getTestStatement(YdbConnection connection,
                                                    String column,
                                                    String type) throws SQLException {
        return connection.prepareStatementInMemory(
                String.format(
                        "declare $key as Int32?; \n" +
                                "declare $%s as %s; \n" +
                                "upsert into unit_2(key, %s) values ($key, $%s)",
                        column, type,
                        column, column));
    }

    @Override
    protected YdbPreparedStatement getTestAllValuesStatement(YdbConnection connection) throws SQLException {
        return connection.prepareStatement(subst("unit_2", YdbPreparedStatementImplTest.PREPARE_ALL));
    }

    @Override
    protected boolean expectParameterPrefixed() {
        return true;
    }

    @Override
    protected boolean sqlTypeRequired() {
        return true;
    }
}
