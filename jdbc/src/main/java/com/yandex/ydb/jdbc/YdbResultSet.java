package com.yandex.ydb.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.values.Value;

public interface YdbResultSet extends ResultSet {

    /**
     * Return YDB result set reader for direct processing
     *
     * @return YDB original result set reader
     */
    ResultSetReader getYdbResultSetReader();

    //

    /**
     * Returns native YDB value, extracted from optional value.
     * Please note that this method will create value object for each method call.
     *
     * @param columnIndex columnIndex column index
     * @return value if available; return empty if value is optional and no value provided
     * @throws SQLException if column cannot be read
     */
    Optional<Value<?>> getNativeColumn(int columnIndex) throws SQLException;

    /**
     * Return native YDB value.
     * Sett {@link #getNativeColumn(int)}
     *
     * @param columnLabel column label
     * @return value if available
     * @throws SQLException if column cannot be read
     */
    Optional<Value<?>> getNativeColumn(String columnLabel) throws SQLException;

    //

    @Override
    YdbResultSetMetaData getMetaData() throws SQLException;

    @Override
    YdbStatement getStatement() throws SQLException;
}
