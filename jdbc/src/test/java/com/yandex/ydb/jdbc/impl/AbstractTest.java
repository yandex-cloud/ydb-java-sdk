package com.yandex.ydb.jdbc.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbDriver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;

public abstract class AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);

    static final String CREATE_TABLE = stringFileReference("classpath:sql/create_table.sql");
    static final String SIMPLE_TABLE = "unit_1";
    static final String PREPARED_TABLE = "unit_2";

    // TODO: test fast connection reuse
    private static final boolean reuseConnection = Boolean.parseBoolean(System.getProperty("REUSE_CONNECTION", "true"));
    private static YdbConnection connection;

    @Nullable
    static InputStream stream(@Nullable String value) {
        return value == null ? null : new ByteArrayInputStream(value.getBytes()) {
            @Override
            public void close() {
                this.reset();
            }
        };
    }

    @Nullable
    static Reader reader(@Nullable String value) {
        return value == null ? null : new StringReader(value) {
            @Override
            public void close() {
                try {
                    this.reset();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @AfterEach
    void afterEach() throws SQLException {
        if (connection != null) {
            if (reuseConnection) {
                if (!connection.isClosed()) {
                    try {
                        connection.rollback();
                    } catch (Exception e) {
                        LOGGER.warn("Unable to rollback", e);
                    }
                    connection.setAutoCommit(false);
                    connection.setTransactionIsolation(YdbConst.TRANSACTION_SERIALIZABLE_READ_WRITE);
                    connection.setReadOnly(false);
                }
            } else {
                connection.close();
            }
        }
    }

    @AfterAll
    static void afterAll() {
        connection = null;
        YdbDriver.getConnectionsCache().close();
    }

    protected static YdbConnection getTestConnection() throws SQLException {
        // TODO: must be session pool (but have to implement it first)
        if (connection == null || connection.isClosed()) {
            connection = (YdbConnection) DriverManager.getConnection(TestHelper.getTestUrl());
        }
        return connection;
    }

    protected void configureOnce(TestHelper.SQLSimpleRun run) throws SQLException {
        TestHelper.configureOnce(this.getClass(), run);
    }

    protected void cleanupSimpleTestTable() throws SQLException {
        cleanupTable(SIMPLE_TABLE);
    }

    protected void cleanupTable(String table) throws SQLException {
        YdbConnection connection = getTestConnection();
        connection.createStatement().executeUpdate("delete from " + table);
        connection.commit();
    }

    protected static void recreatePreparedTestTable() throws SQLException {
        createTestTable(PREPARED_TABLE, CREATE_TABLE);
    }

    protected static void recreateSimpleTestTable() throws SQLException {
        createTestTable(SIMPLE_TABLE, CREATE_TABLE);
    }

    protected static void createTestTable(String tableName, String expression) throws SQLException {
        TestHelper.initTable(getTestConnection(), tableName, expression);
    }

    protected static String subst(String tableName, String sql) {
        return TestHelper.withTableName(tableName, sql);
    }

    protected static void closeIfPossible(Object value) throws IOException {
        if (value instanceof Reader) {
            ((Reader) value).close();
        } else if (value instanceof InputStream) {
            ((InputStream) value).close();
        }
    }

    protected static <T> Object castCompatible(T value) throws SQLException {
        if (value instanceof Date) {
            return ((Date) value).getTime();
        } else if (value instanceof Time) {
            return ((Time) value).getTime();
        } else if (value instanceof byte[]) {
            return new String((byte[]) value);
        } else if (value instanceof Reader) {
            try {
                try (Reader reader = (Reader) value) {
                    return CharStreams.toString(reader);
                }
            } catch (IOException e) {
                throw new SQLException("Unable to read from reader", e);
            }
        } else if (value instanceof InputStream) {
            try {
                try (InputStream stream = (InputStream) value) {
                    return new String(ByteStreams.toByteArray(stream));
                }
            } catch (IOException e) {
                throw new SQLException("Unable to read from inputStream", e);
            }
        } else {
            return value;
        }
    }

    @SafeVarargs
    protected static <T> List<T> merge(List<T>... lists) {
        return Stream.of(lists)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    protected static <T> Set<T> set(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
