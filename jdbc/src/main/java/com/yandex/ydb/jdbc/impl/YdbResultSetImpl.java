package com.yandex.ydb.jdbc.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.yandex.ydb.jdbc.YdbResultSet;
import com.yandex.ydb.jdbc.YdbResultSetMetaData;
import com.yandex.ydb.jdbc.YdbStatement;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.result.ValueReader;
import com.yandex.ydb.table.values.OptionalValue;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.ARRAYS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.ASCII_STREAM_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.BLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.CANNOT_UNWRAP_TO;
import static com.yandex.ydb.jdbc.YdbConst.CLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.COLUMN_NOT_FOUND;
import static com.yandex.ydb.jdbc.YdbConst.CURSOR_UPDATING_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.FORWARD_ONLY_MODE;
import static com.yandex.ydb.jdbc.YdbConst.INVALID_FETCH_DIRECTION;
import static com.yandex.ydb.jdbc.YdbConst.INVALID_ROW;
import static com.yandex.ydb.jdbc.YdbConst.NAMED_CURSORS_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.NCLOB_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.OBJECT_TYPED_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.REF_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.ROWID_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.SQLXML_UNSUPPORTED;
import static com.yandex.ydb.jdbc.YdbConst.UNABLE_TO_CONVERT_AS_URL;

public class YdbResultSetImpl implements YdbResultSet {

    private final MutableState state = new MutableState();

    private final YdbStatement statement;
    private final ResultSetReader result;
    private final TypeDescription[] types;
    private final Supplier<YdbResultSetMetaData> metaDataSupplier;

    private final int rowCount;

    public YdbResultSetImpl(YdbStatement statement, ResultSetReader result) {
        this.statement = Objects.requireNonNull(statement);
        this.result = Objects.requireNonNull(result);
        this.rowCount = result.getRowCount();
        this.types = asDescription(result);
        this.metaDataSupplier = Suppliers.memoize(() -> new YdbResultSetMetaDataImpl(result, types))::get;
    }

    @Override
    public boolean next() {
        setRowIndex(state.rowIndex + 1);
        return isRowIndexValid();
    }

    @Override
    public void close() {
        state.closed = true;
    }

    @Override
    public boolean wasNull() {
        return state.nullValue;
    }

    //
    @Override
    public String getString(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        if (state.nullValue) {
            return null; // getString supports all types, it's safe to check nullability here
        }
        return state.description.fromValue.toString.fromValue(state.value);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        return state.description.fromValue.toBoolean.fromValue(state.value);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        return state.description.fromValue.toByte.fromValue(state.value);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        return state.description.fromValue.toShort.fromValue(state.value);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        return state.description.fromValue.toInt.fromValue(state.value);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        return state.description.fromValue.toLong.fromValue(state.value);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        return state.description.fromValue.toFloat.fromValue(state.value);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        return state.description.fromValue.toDouble.fromValue(state.value);
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        BigDecimal bigDecimal = getBigDecimal(columnIndex);
        if (bigDecimal != null) {
            return bigDecimal.setScale(scale, RoundingMode.HALF_EVEN); // TODO: not sure what to do here
        } else {
            return null;
        }
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        byte[] result = state.description.fromValue.toBytes.fromValue(state.value);
        if (state.nullValue) { // TODO: do not parse empty value when optional and no value present
            return null;
        }
        return result;
    }


    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return getDateImpl(columnIndex, Date::new);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return getDateImpl(columnIndex, Time::new);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return getDateImpl(columnIndex, Timestamp::new);
    }

    @Deprecated
    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        // TODO: implement Unicode stream?
        return getBinaryStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        byte[] bytes = getBytes(columnIndex);
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

    //

    @Override
    public String getString(String columnLabel) throws SQLException {
        return getString(getColumnIndex(columnLabel));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return getBoolean(getColumnIndex(columnLabel));
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return getByte(getColumnIndex(columnLabel));
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return getShort(getColumnIndex(columnLabel));
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return getInt(getColumnIndex(columnLabel));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return getLong(getColumnIndex(columnLabel));
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return getFloat(getColumnIndex(columnLabel));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return getDouble(getColumnIndex(columnLabel));
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return getBigDecimal(getColumnIndex(columnLabel), scale);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return getBytes(getColumnIndex(columnLabel));
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return getDate(getColumnIndex(columnLabel));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return getTime(getColumnIndex(columnLabel));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return getTimestamp(getColumnIndex(columnLabel));
    }

    @Deprecated
    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return getUnicodeStream(getColumnIndex(columnLabel));
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return getBinaryStream(getColumnIndex(columnLabel));
    }

    @Override
    public SQLWarning getWarnings() {
        return null; // TODO: Support warning
    }

    @Override
    public void clearWarnings() {
        // do nothing
    }

    @Override
    public YdbResultSetMetaData getMetaData() {
        return metaDataSupplier.get();
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        if (state.nullValue) {
            return null; // getObject supports all types, it's safe to check nullability here
        }
        return state.description.fromValue.toObject.fromValue(state.value);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return getObject(getColumnIndex(columnLabel));
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return getColumnIndex(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        Reader result = state.description.fromValue.toReader.fromValue(state.value);
        if (state.nullValue) {
            return null;
        }
        return result;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return getCharacterStream(getColumnIndex(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        BigDecimal result = state.description.fromValue.toBigDecimal.fromValue(state.value);
        if (state.nullValue) {
            return null;
        }
        return result;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return getBigDecimal(getColumnIndex(columnLabel));
    }

    @Override
    public boolean isBeforeFirst() {
        return rowCount != 0 && state.rowIndex <= 0;
    }

    @Override
    public boolean isAfterLast() {
        return rowCount != 0 && state.rowIndex > rowCount;
    }

    @Override
    public boolean isFirst() {
        return rowCount != 0 && state.rowIndex == 1;
    }

    @Override
    public boolean isLast() {
        return rowCount != 0 && state.rowIndex == rowCount;
    }

    @Override
    public void beforeFirst() throws SQLException {
        checkScroll();
        setRowIndex(0);
    }

    @Override
    public void afterLast() throws SQLException {
        checkScroll();
        setRowIndex(rowCount + 1);
    }

    @Override
    public boolean first() throws SQLException {
        checkScroll();
        setRowIndex(1);
        return isRowIndexValid();
    }

    @Override
    public boolean last() throws SQLException {
        checkScroll();
        setRowIndex(rowCount);
        return isRowIndexValid();
    }

    @Override
    public int getRow() {
        return state.rowIndex;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        checkScroll();
        if (row >= 0) {
            setRowIndex(row);
        } else {
            setRowIndex(rowCount + 1 + row);
        }
        return isRowIndexValid();
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        checkScroll();
        if (rows != 0) {
            setRowIndex(state.rowIndex + rows);
        }
        return isRowIndexValid();
    }

    @Override
    public boolean previous() throws SQLException {
        checkScroll();
        setRowIndex(state.rowIndex - 1);
        return isRowIndexValid();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        int resultSetType = getType();
        if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && direction != ResultSet.FETCH_FORWARD) {
            throw new SQLException(String.format(INVALID_FETCH_DIRECTION, direction, resultSetType));
        }
        state.direction = direction;
    }

    @Override
    public int getFetchDirection() {
        return state.direction;
    }

    @Override
    public void setFetchSize(int rows) {
        // do nothing
    }

    @Override
    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return statement.getResultSetType();
    }

    @Override
    public int getConcurrency() {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public YdbStatement getStatement() {
        return statement;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return getDateImpl(columnIndex, Date::new); // TODO: use cal
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return getDate(getColumnIndex(columnLabel), cal);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        // TODO: use cal
        return getDateImpl(columnIndex, Time::new);
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return getTime(getColumnIndex(columnLabel), cal);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        // TODO: use cal
        return getDateImpl(columnIndex, Timestamp::new);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return getTimestamp(getColumnIndex(columnLabel), cal);
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        String result = state.description.fromValue.toURL.fromValue(state.value);
        if (state.nullValue) {
            return null;
        }
        try {
            return new URL(result);
        } catch (MalformedURLException e) {
            throw new SQLException(UNABLE_TO_CONVERT_AS_URL + result, e);
        }
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return getURL(getColumnIndex(columnLabel));
    }

    @Override
    public int getHoldability() {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public boolean isClosed() {
        return state.closed;
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        String result = state.description.fromValue.toNString.fromValue(state.value);
        if (state.nullValue) {
            return null;
        }
        return result;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return getNString(getColumnIndex(columnLabel));
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return getCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return getNCharacterStream(getColumnIndex(columnLabel));
    }

    @Override
    public Optional<Value<?>> getNativeColumn(int columnIndex) throws SQLException {
        initValueReader(columnIndex);
        if (state.nullValue) {
            return Optional.empty();
        }
        // It's just a bug in implementation - actualValue could be optional, but without flag
        // I.e. the optional object looks like empty, not null
        Value<?> actualValue = state.value.getValue();
        if (actualValue instanceof OptionalValue) {
            return Optional.of((Value<?>) ((OptionalValue) actualValue).get());
        }
        return Optional.of(actualValue);
    }

    @Override
    public Optional<Value<?>> getNativeColumn(String columnLabel) throws SQLException {
        return getNativeColumn(getColumnIndex(columnLabel));
    }

    //

    @Override
    public ResultSetReader getYdbResultSetReader() {
        return result;
    }

    //

    private <T> T getDateImpl(int columnIndex, LongFunction<T> fromMillis) throws SQLException {
        initValueReader(columnIndex);
        long longValue = state.description.fromValue.toDateMillis.fromValue(state.value);
        if (state.nullValue) {
            return null;
        }
        return fromMillis.apply(longValue);
    }


    private TypeDescription getDescription(int columnIndex) {
        return types[columnIndex - 1];
    }

    private boolean isNullValue(TypeDescription description, ValueReader value) {
        return description.optional && !value.isOptionalItemPresent();
    }

    private void initValueReader(int columnIndex) throws SQLException {
        try {
            ValueReader value = result.getColumn(columnIndex - 1);
            TypeDescription description = getDescription(columnIndex);
            state.value = value;
            state.description = description;
            state.nullValue = isNullValue(description, value);
        } catch (IllegalStateException e) {
            throw new SQLException(INVALID_ROW + state.rowIndex);
        }
    }

    private int getColumnIndex(String columnLabel) throws SQLException {
        int index = result.getColumnIndex(columnLabel);
        if (index < 0) {
            throw new SQLException(COLUMN_NOT_FOUND + columnLabel);
        }
        return index + 1;
    }

    private void checkScroll() throws SQLException {
        if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(FORWARD_ONLY_MODE);
        }
    }

    private void setRowIndex(int rowIndex) {
        if (rowCount > 0) {
            int actualIndex = Math.max(Math.min(rowIndex, rowCount + 1), 0);
            state.rowIndex = actualIndex;
            result.setRowIndex(actualIndex - 1);
        }
    }

    private boolean isRowIndexValid() {
        return state.rowIndex > 0 && state.rowIndex <= rowCount;
    }

    private static TypeDescription[] asDescription(ResultSetReader result) {
        // TODO: cache?
        TypeDescription[] descriptions = new TypeDescription[result.getColumnCount()];
        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = TypeDescription.of(result.getColumnType(i));
        }
        return descriptions;
    }

    private static class MutableState {
        private int rowIndex; // 1..rowCount, inclusive (first row is 1, second is 2 and so on)

        // last column reader
        private ValueReader value;
        private TypeDescription description;
        boolean nullValue;

        private int direction = ResultSet.FETCH_UNKNOWN;
        private boolean closed;
    }


    // UNSUPPORTED

    @Override
    public String getCursorName() throws SQLException {
        throw new SQLFeatureNotSupportedException(NAMED_CURSORS_UNSUPPORTED);
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public boolean rowInserted() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void insertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void refreshRow() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }


    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(ROWID_UNSUPPORTED);
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(ROWID_UNSUPPORTED);
    }


    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }


    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }


    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }


    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException(CURSOR_UPDATING_UNSUPPORTED);
    }


    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException(OBJECT_TYPED_UNSUPPORTED);
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(REF_UNSUPPORTED);
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(BLOB_UNSUPPORTED);
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(CLOB_UNSUPPORTED);
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(ARRAYS_UNSUPPORTED);
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException(OBJECT_TYPED_UNSUPPORTED);
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(REF_UNSUPPORTED);
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(BLOB_UNSUPPORTED);
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(CLOB_UNSUPPORTED);
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(ARRAYS_UNSUPPORTED);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(NCLOB_UNSUPPORTED);
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(NCLOB_UNSUPPORTED);
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(SQLXML_UNSUPPORTED);
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(SQLXML_UNSUPPORTED);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(ASCII_STREAM_UNSUPPORTED);
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException(ASCII_STREAM_UNSUPPORTED);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException(OBJECT_TYPED_UNSUPPORTED);
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException(OBJECT_TYPED_UNSUPPORTED);
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
