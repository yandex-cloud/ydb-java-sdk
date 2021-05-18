package com.yandex.ydb.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public interface YdbPreparedStatement extends YdbStatement, PreparedStatement {

    // TODO: add future calls

    /**
     * Returns query text
     *
     * @return text
     */
    String getQuery();

    /**
     * Explicitly execute this query as a scan query
     *
     * @return result set
     * @throws SQLException if query cannot be executed
     */
    YdbResultSet executeScanQuery() throws SQLException;

    /**
     * Explicitly explain this query
     *
     * @return result set with explained query, see {@link #executeExplainQuery(String)}
     * @throws SQLException if query cannot be explained
     */
    YdbResultSet executeExplainQuery() throws SQLException;

    //

    void setObject(String parameterName, Object x) throws SQLException;

    void setNull(String parameterName) throws SQLException;

    void setNull(String parameterName, int sqlType) throws SQLException;

    void setBoolean(String parameterName, boolean x) throws SQLException;

    void setByte(String parameterName, byte x) throws SQLException;

    void setShort(String parameterName, short x) throws SQLException;

    void setInt(String parameterName, int x) throws SQLException;

    void setLong(String parameterName, long x) throws SQLException;

    void setFloat(String parameterName, float x) throws SQLException;

    void setDouble(String parameterName, double x) throws SQLException;

    void setBigDecimal(String parameterName, BigDecimal x) throws SQLException;

    void setString(String parameterName, String x) throws SQLException;

    void setBytes(String parameterName, byte[] x) throws SQLException;

    void setDate(String parameterName, Date x) throws SQLException;

    void setTime(String parameterName, Time x) throws SQLException;

    void setTimestamp(String parameterName, Timestamp x) throws SQLException;

    void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException;

    void setUnicodeStream(String parameterName, InputStream x, int length) throws SQLException;

    void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException;

    void setObject(String parameterName, Object x, int targetSqlType) throws SQLException;

    void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException;

    void setRef(String parameterName, Ref x) throws SQLException;

    void setBlob(String parameterName, Blob x) throws SQLException;

    void setClob(String parameterName, Clob x) throws SQLException;

    void setArray(String parameterName, Array x) throws SQLException;

    void setDate(String parameterName, Date x, Calendar cal) throws SQLException;

    void setTime(String parameterName, Time x, Calendar cal) throws SQLException;

    void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException;

    void setNull(String parameterName, int sqlType, String typeName) throws SQLException;

    void setURL(String parameterName, URL x) throws SQLException;

    void setNString(String parameterName, String value) throws SQLException;

    void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException;

    void setNClob(String parameterName, NClob value) throws SQLException;

    void setClob(String parameterName, Reader reader, long length) throws SQLException;

    void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException;

    void setNClob(String parameterName, Reader reader, long length) throws SQLException;

    void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException;

    void setObject(String parameterName, Object x, int targetSqlType, int scaleOrLength) throws SQLException;

    void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException;

    void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException;

    void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException;

    void setAsciiStream(String parameterName, InputStream x) throws SQLException;

    void setBinaryStream(String parameterName, InputStream x) throws SQLException;

    void setCharacterStream(String parameterName, Reader reader) throws SQLException;

    void setNCharacterStream(String parameterName, Reader value) throws SQLException;

    void setClob(String parameterName, Reader reader) throws SQLException;

    void setBlob(String parameterName, InputStream inputStream) throws SQLException;

    void setNClob(String parameterName, Reader reader) throws SQLException;

    //


    @Override
    YdbResultSet executeQuery() throws SQLException;

    @Override
    YdbParameterMetaData getParameterMetaData() throws SQLException;
}
