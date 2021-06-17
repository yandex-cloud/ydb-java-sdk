package com.yandex.ydb.jdbc.impl;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.OptionalValue;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.ARRAYS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.ASCII_STREAM_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.BLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.CANNOT_UNWRAP_TO;
import static com.yandex.ydb.jdbc.YdbConst.CLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.CUSTOM_SQL_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.INVALID_PARAMETER_TYPE;
import static com.yandex.ydb.jdbc.YdbConst.METADATA_RS_UNSUPPORTED_IN_PS;
import static com.yandex.ydb.jdbc.YdbConst.MISSING_REQUIRED_VALUE;
import static com.yandex.ydb.jdbc.YdbConst.NCLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.QUERY_EXPECT_RESULT_SET;
import static com.yandex.ydb.jdbc.YdbConst.QUERY_EXPECT_UPDATE;
import static com.yandex.ydb.jdbc.YdbConst.REF_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.ROWID_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.SQLXML_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.UNABLE_TO_SET_NULL_VALUE;
import static com.yandex.ydb.jdbc.YdbConst.UNSUPPORTED_QUERY_TYPE_IN_PS;

public abstract class AbstractYdbPreparedStatementImpl extends YdbStatementImpl implements YdbPreparedStatement {
    private final String query;
    private final QueryType queryType;

    protected AbstractYdbPreparedStatementImpl(YdbConnectionImpl connection,
                                               int resultSetType,
                                               String query) throws SQLException {
        super(connection, resultSetType);
        this.query = Objects.requireNonNull(query);
        this.queryType = checkQueryType(connection.decodeQueryType(query));
        this.setPoolable(true); // By default
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public YdbResultSet executeScanQuery() throws SQLException {
        boolean result = executeScanQueryImpl();
        if (!result) {
            throw new SQLException(QUERY_EXPECT_RESULT_SET);
        }
        return getResultSet(0);
    }

    @Override
    public YdbResultSet executeExplainQuery() throws SQLException {
        boolean result = executeExplainQueryImpl(query); // No parameters required for explain query
        if (!result) {
            throw new SQLException(QUERY_EXPECT_RESULT_SET);
        }
        return getResultSet(0);
    }

    @Override
    public void close() {
        super.close();
        clearParameters();
    }

    @Override
    public boolean execute() throws SQLException {
        return this.doExecute();
    }

    @Override
    public YdbResultSet executeQuery() throws SQLException {
        boolean result = doExecute();
        if (!result) {
            throw new SQLException(QUERY_EXPECT_RESULT_SET);
        }
        return getResultSet(0);
    }

    @Override
    public int executeUpdate() throws SQLException {
        boolean result = doExecute();
        if (result) {
            throw new SQLException(QUERY_EXPECT_UPDATE);
        }
        return getUpdateCount();
    }

    //

    @Override
    public void setObject(String parameterName, Object value, Type type) throws SQLException {
        setImpl(parameterName, value, YdbConst.UNKNOWN_SQL_TYPE, null, type);
    }

    @Override
    public void setObject(int parameterIndex, Object value, Type type) throws SQLException {
        setImpl(parameterIndex, value, YdbConst.UNKNOWN_SQL_TYPE, null, type);
    }

    //

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        setImpl(parameterName, null, sqlType, null, null);
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        setImpl(parameterName, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        setImpl(parameterName, x, Types.DECIMAL, null, null);
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        setImpl(parameterName, x, Types.VARCHAR, null, null);
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        setImpl(parameterName, x, Types.BINARY, null, null);
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        setImpl(parameterName, x, Types.DATE, null, null);
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        setImpl(parameterName, x, Types.TIME, null, null);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        setImpl(parameterName, x, Types.TIMESTAMP, null, null);
    }

    @Override
    public void setUnicodeStream(String parameterName, InputStream x, int length) throws SQLException {
        setImplStream(parameterName, x, length);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        setImplStream(parameterName, x, length);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        setImpl(parameterName, x, targetSqlType, null, null);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        setImplReader(parameterName, reader, length);
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        // TODO: check cal
        setImpl(parameterName, x, Types.DATE, null, null);
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        // TODO: check cal
        setImpl(parameterName, x, Types.TIME, null, null);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        // TODO: check cal
        setImpl(parameterName, x, Types.TIMESTAMP, null, null);
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        setImpl(parameterName, null, sqlType, typeName, null);
    }

    @Override
    public void setURL(String parameterName, URL x) throws SQLException {
        setImpl(parameterName, x, Types.VARCHAR, null, null);
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        setImpl(parameterName, value, Types.VARCHAR, null, null);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        setImplReader(parameterName, value, length);
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        setImplReader(parameterName, reader, length);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        setImplStream(parameterName, inputStream, length);
    }

    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        setImplReader(parameterName, reader, length);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        // TODO: check scaleOrLength
        setImpl(parameterName, x, targetSqlType, null, null);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        setImplStream(parameterName, x, length);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        setImplReader(parameterName, reader, length);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        setImplStream(parameterName, x, -1);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        setImplReader(parameterName, reader, -1);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        setImplReader(parameterName, value, -1);
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        setImplReader(parameterName, reader, -1);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        setImplStream(parameterName, inputStream, -1);
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        setImplReader(parameterName, reader, -1);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setImpl(parameterIndex, null, sqlType, null, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setImpl(parameterIndex, x, Types.DECIMAL, null, null);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setImpl(parameterIndex, x, Types.VARCHAR, null, null);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setImpl(parameterIndex, x, Types.BINARY, null, null);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setImpl(parameterIndex, x, Types.DATE, null, null);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setImpl(parameterIndex, x, Types.TIME, null, null);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setImpl(parameterIndex, x, Types.TIMESTAMP, null, null);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setImplStream(parameterIndex, x, length);
    }

    @Deprecated
    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setImplStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setImplStream(parameterIndex, x, length);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setImpl(parameterIndex, x, targetSqlType, null, null);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        setImplReader(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        setImpl(parameterIndex, x, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        // TODO: check cal
        setImpl(parameterIndex, x, Types.DATE, null, null);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        // TODO: check cal
        setImpl(parameterIndex, x, Types.TIME, null, null);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        // TODO: check cal
        setImpl(parameterIndex, x, Types.TIMESTAMP, null, null);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setImpl(parameterIndex, null, sqlType, typeName, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setImpl(parameterIndex, x, Types.VARCHAR, null, null);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setImpl(parameterIndex, value, Types.VARCHAR, null, null);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        setImplReader(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        setImpl(parameterIndex, value, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setImplReader(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        setImplStream(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setImplReader(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        setImpl(parameterIndex, xmlObject, YdbConst.UNKNOWN_SQL_TYPE, null, null);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        // TODO: handle scaleOrLength
        setImpl(parameterIndex, x, targetSqlType, null, null);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setImplStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setImplStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        setImplReader(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        setImplStream(parameterIndex, x, -1);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setImplStream(parameterIndex, x, -1);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setImplReader(parameterIndex, reader, -1);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setImplReader(parameterIndex, value, -1);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setImplReader(parameterIndex, reader, -1);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setImplStream(parameterIndex, inputStream, -1);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setImplReader(parameterIndex, reader, -1);
    }

    //

    private void setImplReader(String parameterName, Reader reader, long length) throws SQLException {
        setImpl(parameterName, MappingSetters.CharStream.fromReader(reader, length), Types.VARCHAR, null, null);
    }

    private void setImplStream(String parameterName, InputStream stream, long length) throws SQLException {
        setImpl(parameterName, MappingSetters.ByteStream.fromInputStream(stream, length), Types.BINARY, null, null);
    }

    private void setImplReader(int parameterIndex, Reader reader, long length) throws SQLException {
        setImpl(parameterIndex, MappingSetters.CharStream.fromReader(reader, length), Types.VARCHAR, null, null);
    }

    private void setImplStream(int parameterIndex, InputStream stream, long length) throws SQLException {
        setImpl(parameterIndex, MappingSetters.ByteStream.fromInputStream(stream, length), Types.BINARY, null, null);
    }

    @Nullable
    protected Value<?> getValue(String param, TypeDescription description, Object value) throws SQLException {
        if (value instanceof Value<?>) {
            // For all external values (passed 'as is') we have to check data types
            Value<?> targetValue = (Value<?>) value;
            if (description.optional) {
                if (targetValue instanceof OptionalValue) {
                    checkType(param, description, ((OptionalValue) targetValue).getType().getItemType());
                    return targetValue; // Could be null
                } else {
                    checkType(param, description, targetValue.getType());
                    return targetValue.makeOptional();
                }
            } else {
                if (targetValue instanceof OptionalValue) {
                    OptionalValue optionalValue = (OptionalValue) value;
                    if (!optionalValue.isPresent()) {
                        throw new SQLException(MISSING_REQUIRED_VALUE + param);
                    }
                    checkType(param, description, optionalValue.getType().getItemType());
                    return optionalValue.get();
                } else {
                    checkType(param, description, targetValue.getType());
                    return targetValue;
                }
            }
        } else if (value == null) {
            if (description.optionalValue != null) {
                return description.optionalValue;
            } else {
                throw new SQLException(UNABLE_TO_SET_NULL_VALUE + param);
            }
        } else {
            Value<?> targetValue = description.setters.toValue(value);
            if (description.optional) {
                return targetValue.makeOptional();
            } else {
                return targetValue;
            }
        }
    }

    protected void checkType(String param, TypeDescription description, Type type) throws SQLException {
        if (!description.type.equals(type)) {
            throw new SQLException(String.format(INVALID_PARAMETER_TYPE, param, type, description.type));
        }
    }

    protected QueryType getQueryType() {
        return queryType;
    }

    private QueryType checkQueryType(QueryType queryType) throws SQLException {
        switch (queryType) {
            case DATA_QUERY:
            case SCAN_QUERY:
                return queryType;
            default:
                throw new SQLException(UNSUPPORTED_QUERY_TYPE_IN_PS + queryType);
        }
    }

    private boolean doExecute() throws SQLException {
        try {
            return this.executeImpl();
        } finally {
            this.afterExecute();
        }
    }

    protected boolean executeScanQueryImpl() throws SQLException {
        return executeScanQueryImpl(
                query,
                getParams(),
                params -> QueryType.SCAN_QUERY + " >>\n" + query +
                        "\n\nParams: " + paramsToString(params));
    }

    protected String paramsToString(Params params) {
        Map<String, Value<?>> values = params.values();
        if (values.size() == 1) {
            Map.Entry<String, Value<?>> entry = values.entrySet().iterator().next();
            String key = entry.getKey();
            Value<?> value = entry.getValue();
            if (value instanceof ListValue) {
                ListValue list = (ListValue) value;
                if (list.size() > 10) {
                    String first10Elements = IntStream.range(0, 10)
                            .mapToObj(list::get)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                    return "{" + key + "=List<" + list.getType().getItemType() + ">" +
                            "[" + first10Elements + "...], and " + (list.size() - 10) + " more";
                }
            }
        }
        return String.valueOf(values);
    }
    //

    @Override
    public abstract void clearParameters();

    protected abstract void afterExecute();

    protected abstract Params getParams() throws SQLException;


    protected abstract void setImpl(String parameterName, @Nullable Object x,
                                    int sqlType, @Nullable String typeName, @Nullable Type type)
            throws SQLException;

    protected abstract void setImpl(int parameterIndex, @Nullable Object x,
                                    int sqlType, @Nullable String typeName, @Nullable Type type)
            throws SQLException;

    protected abstract boolean executeImpl() throws SQLException;

    // UNSUPPORTED

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException(METADATA_RS_UNSUPPORTED_IN_PS);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException(ROWID_UNSUPPORTED);
    }

    @Override
    public void executeSchemeQuery(String sql) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public YdbResultSet executeScanQuery(String sql) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public YdbResultSet executeExplainQuery(String sql) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public YdbResultSet executeQuery(String sql) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLException(CUSTOM_SQL_UNSUPPORTED);
    }

    @Override
    public void setArray(String parameterName, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException(ARRAYS_UNSUPPORTED);
    }


    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(ASCII_STREAM_UNSUPPORTED);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(ASCII_STREAM_UNSUPPORTED);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(ASCII_STREAM_UNSUPPORTED);
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException(BLOB_UNSUPPORTED);
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CLOB_UNSUPPORTED);
    }

    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        throw new SQLFeatureNotSupportedException(NCLOB_UNSUPPORTED);
    }

    @Override
    public void setRef(String parameterName, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException(REF_UNSUPPORTED);
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException(SQLXML_UNSUPPORTED);
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
