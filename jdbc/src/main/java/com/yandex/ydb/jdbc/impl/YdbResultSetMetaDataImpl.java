package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.yandex.ydb.jdbc.YdbResultSetMetaData;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.values.Type;

import static com.yandex.ydb.jdbc.YdbConst.CANNOT_UNWRAP_TO;
import static com.yandex.ydb.jdbc.YdbConst.COLUMN_NOT_FOUND;
import static com.yandex.ydb.jdbc.YdbConst.COLUMN_NUMBER_NOT_FOUND;

public class YdbResultSetMetaDataImpl implements YdbResultSetMetaData {
    private final ResultSetReader result;
    private final TypeDescription[] descriptions;
    private final String[] names;

    YdbResultSetMetaDataImpl(ResultSetReader result, TypeDescription[] descriptions) {
        this.result = Objects.requireNonNull(result);
        this.descriptions = Objects.requireNonNull(descriptions);
        this.names = asNames(result);
        Preconditions.checkState(result.getColumnCount() == descriptions.length,
                "Internal error, column count in resultSet must be equals to extract column descriptions");
        Preconditions.checkState(descriptions.length == names.length,
                "Internal error, column description count must be equals to extract column names");
    }

    @Override
    public int getColumnCount() {
        return descriptions.length;
    }

    @Override
    public boolean isAutoIncrement(int column) {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) {
        return true;
    }

    @Override
    public boolean isSearchable(int column) {
        return false;
    }

    @Override
    public boolean isCurrency(int column) {
        return false;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return getDescription(column).optional ?
                columnNullable :
                columnNoNulls;
    }

    @Override
    public boolean isSigned(int column) {
        return false; // TODO: support?
    }

    @Override
    public int getColumnDisplaySize(int column) {
        return 0; // TODO: support?
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return getColumnName(column);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return names[getIndex(column)];
    }

    @Override
    public String getSchemaName(int column) {
        return ""; // not applicable
    }

    @Override
    public int getPrecision(int column) {
        return 0; // TODO: support?
    }

    @Override
    public int getScale(int column) {
        return 0; // TODO: support?
    }

    @Override
    public String getTableName(int column) {
        return ""; // unknown
    }

    @Override
    public String getCatalogName(int column) {
        return ""; // unknown
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return getDescription(column).sqlTypes.getSqlType();
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return getDescription(column).type.toString();
    }

    @Override
    public boolean isReadOnly(int column) {
        return true;
    }

    @Override
    public boolean isWritable(int column) {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return getDescription(column).sqlTypes.getJavaType().getName();
    }

    @Override
    public Type getYdbType(int column) throws SQLException {
        return getDescription(column).type;
    }

    @Override
    public int getColumnIndex(String columnName) throws SQLException {
        int index = result.getColumnIndex(columnName);
        if (index >= 0) {
            return index + 1;
        } else {
            throw new SQLException(COLUMN_NOT_FOUND + columnName);
        }
    }


    //

    private int getIndex(int column) throws SQLException {
        if (column <= 0 || column > descriptions.length) {
            throw new SQLException(COLUMN_NUMBER_NOT_FOUND + column);
        }
        return column - 1;
    }

    private TypeDescription getDescription(int column) throws SQLException {
        return descriptions[getIndex(column)];
    }


    private static String[] asNames(ResultSetReader result) {
        String[] names = new String[result.getColumnCount()];
        for (int i = 0; i < names.length; i++) {
            names[i] = result.getColumnName(i);
        }
        return names;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException(CANNOT_UNWRAP_TO + iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(getClass());
    }

}
