package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;

class YdbPreparedStatementWithDataQueryImplTest extends AbstractYdbPreparedStatementImplTest {

    static final String PREPARE_ALL = stringFileReference("classpath:sql/prepare_all_values.sql");

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
    void testStatement() throws SQLException {
        retry(connection -> Assertions.assertTrue(
                getUtf8Statement(connection) instanceof YdbPreparedStatementWithDataQueryImpl));
    }

    @Override
    protected YdbPreparedStatement getTestStatement(YdbConnection connection,
                                                    String column,
                                                    String type) throws SQLException {
        return connection.prepareStatement(
                String.format(
                        "declare $key as Int32; \n" +
                                "declare $%s as %s; \n" +
                                "upsert into unit_2(key, %s) values ($key, $%s)",
                        column, type,
                        column, column));
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
                        type, column));
    }

    @Override
    protected YdbPreparedStatement getTestAllValuesStatement(YdbConnection connection) throws SQLException {
        return connection.prepareStatement(subst("unit_2", PREPARE_ALL));
    }

    @Override
    protected boolean expectParameterPrefixed() {
        return true;
    }

    @Override
    protected boolean sqlTypeRequired() {
        return false;
    }

    @Override
    protected boolean supportIndexedParameters() {
        return true;
    }
}
