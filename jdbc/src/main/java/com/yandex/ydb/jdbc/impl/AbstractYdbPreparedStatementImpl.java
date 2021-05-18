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
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;
import com.yandex.ydb.jdbc.YdbParameterMetaData;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.table.query.DataQuery;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.values.OptionalValue;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.ARRAYS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.ASCII_STREAM_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.BLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.CLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.CUSTOM_SQL_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.INVALID_PARAMETER_TYPE;
import static com.yandex.ydb.jdbc.YdbConst.METADATA_RS_UNSUPPORTED_IN_PS;
import static com.yandex.ydb.jdbc.YdbConst.MISSING_REQUIRED_VALUE;
import static com.yandex.ydb.jdbc.YdbConst.NCLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.NOTHING_TO_UNWRAP;
import static com.yandex.ydb.jdbc.YdbConst.QUERY_EXPECT_RESULT_SET;
import static com.yandex.ydb.jdbc.YdbConst.QUERY_EXPECT_UPDATE;
import static com.yandex.ydb.jdbc.YdbConst.REF_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.ROWID_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.SQLXML_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.UNABLE_TO_SET_NULL_VALUE;
import static com.yandex.ydb.jdbc.YdbConst.UNSUPPORTED_QUERY_TYPE_IN_PS;

public abstract class AbstractYdbPreparedStatementImpl extends YdbStatementImpl implements YdbPreparedStatement {

    private final Supplier<YdbParameterMetaData> metaDataSupplier;
    private final String query;
    private final QueryType queryType;
    private final DataQuery dataQuery;

    protected AbstractYdbPreparedStatementImpl(YdbConnectionImpl connection,
                                               int resultSetType,
                                               String query,
                                               DataQuery dataQuery) throws SQLException {
        super(connection, resultSetType);
        this.query = Objects.requireNonNull(query);
        this.queryType = checkQueryType(connection.decodeQueryType(query));
        this.dataQuery = Objects.requireNonNull(dataQuery);
        this.setPoolable(true); // By default

        this.metaDataSupplier = Suppliers.memoize(() ->
                new YdbParameterMetaDataImpl(getParameterTypes()))::get;
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
        return this.executeImpl();
    }

    @Override
    public YdbResultSet executeQuery() throws SQLException {
        boolean result = executeImpl();
        if (!result) {
            throw new SQLException(QUERY_EXPECT_RESULT_SET);
        }
        return getResultSet(0);
    }

    @Override
    public int executeUpdate() throws SQLException {
        boolean result = executeImpl();
        if (result) {
            throw new SQLException(QUERY_EXPECT_UPDATE);
        }
        return getUpdateCount();
    }

    //


    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setNull(String parameterName) throws SQLException {
        setImpl(parameterName, null);
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        setNull(parameterName); // TODO: check targetSqlType
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        setImpl(parameterName, x);
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
        setImpl(parameterName, x); // TODO: check targetSqlType
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        setImplReader(parameterName, reader, length);
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        setImpl(parameterName, x); // TODO: check cal
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        setImpl(parameterName, x); // TODO: check cal
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        setImpl(parameterName, x); // TODO: check cal
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        setImpl(parameterName, null); // TODO: check sqlType, typeName
    }

    @Override
    public void setURL(String parameterName, URL x) throws SQLException {
        setImpl(parameterName, x);
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        setImpl(parameterName, value);
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
        setImpl(parameterName, x); // TODO: check targetSqlType and scaleOrLength
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
        setImplStream(parameterName, x);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        setImplReader(parameterName, reader);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        setImplReader(parameterName, value);
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        setImplReader(parameterName, reader);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        setImplStream(parameterName, inputStream);
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        setImplReader(parameterName, reader);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setImpl(parameterIndex, null); // TODO: check sqlType
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setImpl(parameterIndex, x);
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
        setImpl(parameterIndex, x); // TODO: check targetSqlType
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        setImplReader(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setImpl(parameterIndex, x); // TODO: check cal
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setImpl(parameterIndex, x); // TODO: check cal
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setImpl(parameterIndex, x); // TODO: check cal
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setImpl(parameterIndex, null); // TODO: check sqlType, typeName
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setImpl(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setImpl(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        setImplReader(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        setImpl(parameterIndex, value);
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
        setImpl(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        setImpl(parameterIndex, x); // TODO: handle targetSqlType, scaleOrLength
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
        setImplStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setImplStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setImplReader(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setImplReader(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setImplReader(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setImplStream(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setImplReader(parameterIndex, reader);
    }

    @Override
    public YdbParameterMetaData getParameterMetaData() {
        return metaDataSupplier.get();
    }


    @Override
    public abstract void clearParameters();

    protected abstract void afterExecute();

    protected abstract Params getParams();

    protected abstract Map<String, Type> getParameterTypes();

    protected abstract void setImpl(String parameterName, Object x) throws SQLException;

    protected abstract void setImpl(int parameterIndex, Object x) throws SQLException;

    private void setImplReader(String parameterName, Reader reader) throws SQLException {
        setImplReader(parameterName, reader, -1);
    }

    private void setImplReader(String parameterName, Reader reader, long length) throws SQLException {
        setImpl(parameterName, MappingSetters.CharStream.fromReader(reader, length));
    }

    private void setImplStream(String parameterName, InputStream stream) throws SQLException {
        setImplStream(parameterName, stream, -1);
    }

    private void setImplStream(String parameterName, InputStream stream, long length) throws SQLException {
        setImpl(parameterName, MappingSetters.ByteStream.fromInputStream(stream, length));
    }

    private void setImplReader(int parameterIndex, Reader reader) throws SQLException {
        setImplReader(parameterIndex, reader, -1);
    }

    private void setImplReader(int parameterIndex, Reader reader, long length) throws SQLException {
        setImpl(parameterIndex, MappingSetters.CharStream.fromReader(reader, length));
    }

    private void setImplStream(int parameterIndex, InputStream stream) throws SQLException {
        setImplStream(parameterIndex, stream, -1);
    }

    private void setImplStream(int parameterIndex, InputStream stream, long length) throws SQLException {
        setImpl(parameterIndex, MappingSetters.ByteStream.fromInputStream(stream, length));
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

    protected DataQuery getDataQuery() {
        return dataQuery;
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

    private boolean executeImpl() throws SQLException {
        switch (queryType) {
            case DATA_QUERY:
                return executeDataQueryImpl(
                        getParams(),
                        params -> QueryType.DATA_QUERY + " [" + dataQuery.getId() + "] >>\n" +
                                dataQuery.getText().orElse("<empty>") +
                                "\n\nParams: " + paramsToString(params),
                        (tx, params, execParams) -> {
                            try {
                                return dataQuery.execute(tx, params, execParams);
                            } finally {
                                afterExecute();
                            }
                        });
            case SCAN_QUERY:
                return executeScanQueryImpl();
            default:
                throw new SQLException(UNSUPPORTED_QUERY_TYPE_IN_PS + queryType);
        }
    }

    private boolean executeScanQueryImpl() throws SQLException {
        return executeScanQueryImpl(
                query,
                getParams(),
                params -> QueryType.SCAN_QUERY + " >>\n" + query +
                        "\n\nParams: " + paramsToString(params));
    }

    protected String paramsToString(Params params) {
        return String.valueOf(params.values());
    }

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
        throw new SQLException(NOTHING_TO_UNWRAP);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
