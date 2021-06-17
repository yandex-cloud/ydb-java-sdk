package com.yandex.ydb.jdbc.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.table.values.DecimalType;
import com.yandex.ydb.table.values.DecimalValue;
import com.yandex.ydb.table.values.ListType;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.OptionalType;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.CANNOT_LOAD_DATA_FROM_IS;
import static com.yandex.ydb.jdbc.YdbConst.CANNOT_LOAD_DATA_FROM_READER;
import static com.yandex.ydb.jdbc.YdbConst.UNABLE_TO_CAST;

public class MappingSetters {

    static Setters buildSetters(Type type) {
        return buildToValueImpl(type);
    }

    private static Setters buildToValueImpl(Type type) {
        Type.Kind kind = type.getKind();
        // TODO: Separate setters for primitive values?
        if (kind == Type.Kind.PRIMITIVE) {
            PrimitiveType.Id id = ((PrimitiveType) type).getId();
            switch (id) {
                case String:
                    return x -> PrimitiveValue.stringOwn(castAsBytes(id, x));
                case Utf8:
                    return x -> PrimitiveValue.utf8(castAsString(id, x));
                case Json:
                    return x -> PrimitiveValue.json(castAsJson(id, x));
                case JsonDocument:
                    return x -> PrimitiveValue.jsonDocument(castAsJson(id, x));
                case Yson:
                    return x -> PrimitiveValue.ysonOwn(castAsYson(id, x));
                case Uuid:
                    return x -> castAsUuid(id, x);
                case Bool:
                    return x -> PrimitiveValue.bool(castAsBoolean(id, x));
                case Int8:
                    return x -> PrimitiveValue.int8(castAsByte(id, x));
                case Uint8:
                    return x -> PrimitiveValue.uint8(castAsByte(id, x));
                case Int16:
                    return x -> PrimitiveValue.int16(castAsShort(id, x));
                case Uint16:
                    return x -> PrimitiveValue.uint16(castAsShort(id, x));
                case Int32:
                    return x -> PrimitiveValue.int32(castAsInt(id, x));
                case Uint32:
                    return x -> PrimitiveValue.uint32(castAsInt(id, x));
                case Int64:
                    return x -> PrimitiveValue.int64(castAsLong(id, x));
                case Uint64:
                    return x -> PrimitiveValue.uint64(castAsLong(id, x));
                case Float32:
                    return x -> PrimitiveValue.float32(castAsFloat(id, x));
                case Float64:
                    return x -> PrimitiveValue.float64(castAsDouble(id, x));
                case Date:
                    return x -> castToDate(id, x);
                case Datetime:
                    return x -> castToDateTime(id, x);
                case Timestamp:
                    return x -> castToTimestamp(id, x);
                case Interval:
                    return x -> castToInterval(id, x);
                case TzDate:
                    return x -> PrimitiveValue.tzDate(castAsZonedDateTime(id, x));
                case TzDatetime:
                    return x -> PrimitiveValue.tzDatetime(castAsZonedDateTime(id, x));
                case TzTimestamp:
                    return x -> PrimitiveValue.tzTimestamp(castAsZonedDateTime(id, x));
                default:
                    return x -> {
                        throw castNotSupported(id, x);
                    };
            }
        } else if (kind == Type.Kind.DECIMAL) {
            return x -> castToDecimalValue((DecimalType) type, x);
        } else if (kind == Type.Kind.LIST) {
            ListType listType = (ListType) type;
            Setters itemSetter = buildToValueImpl(listType.getItemType());
            return x -> castAsList(listType, itemSetter, x);
        } else if (kind == Type.Kind.OPTIONAL) {
            return buildToValueImpl(((OptionalType) type).getItemType());
        } else {
            return x -> {
                throw castNotSupported(kind, x);
            };
        }
    }

    private static String toString(Object x) {
        return x == null ? "null" : (x.getClass() + ": " + x);
    }

    private static SQLException castNotSupported(PrimitiveType.Id type, Object x) {
        return new SQLException(String.format(UNABLE_TO_CAST, toString(x), type));
    }

    private static SQLException castNotSupported(Type.Kind kind, Object x) {
        return new SQLException(String.format(UNABLE_TO_CAST, toString(x), kind));
    }

    private static ListValue castAsList(ListType type, Setters itemSetter, Object x) throws SQLException {
        if (x instanceof Collection<?>) {
            Collection<?> values = (Collection<?>) x;
            int len = values.size();
            Value<?>[] result = new Value[len];
            int index = 0;
            for (Object value : values) {
                if (value != null) {
                    if (value instanceof Value<?>) {
                        result[index++] = (Value<?>) value;
                    } else {
                        result[index++] = itemSetter.toValue(value);
                    }
                }
            }
            if (index < result.length) {
                result = Arrays.copyOf(result, index); // Some values are null
            }
            return type.newValueOwn(result);
        } else {
            throw castNotSupported(type.getKind(), x);
        }
    }

    private static byte[] castAsBytes(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof byte[]) {
            return (byte[]) x;
        } else if (x instanceof String) {
            return ((String) x).getBytes();
        } else if (x instanceof InputStream) {
            return ByteStream.fromInputStream((InputStream) x, -1).asByteArray();
        } else if (x instanceof Reader) {
            return CharStream.fromReader((Reader) x, -1).asString().getBytes();
        } else if (x instanceof ByteStream) {
            return ((ByteStream) x).asByteArray();
        } else if (x instanceof CharStream) {
            return ((CharStream) x).asString().getBytes();
        } else {
            return castAsString(type, x).getBytes();
        }
    }

    private static byte[] castAsYson(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof byte[]) {
            return (byte[]) x;
        } else if (x instanceof String) {
            return ((String) x).getBytes();
        } else if (x instanceof InputStream) {
            return ByteStream.fromInputStream((InputStream) x, -1).asByteArray();
        } else if (x instanceof Reader) {
            return CharStream.fromReader((Reader) x, -1).asString().getBytes();
        } else if (x instanceof ByteStream) {
            return ((ByteStream) x).asByteArray();
        } else if (x instanceof CharStream) {
            return ((CharStream) x).asString().getBytes();
        }
        throw castNotSupported(type, x);
    }

    @SuppressWarnings("unused")
    private static String castAsString(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof String) {
            return (String) x;
        } else if (x instanceof byte[]) {
            return new String((byte[]) x);
        } else if (x instanceof InputStream) {
            return new String(ByteStream.fromInputStream((InputStream) x, -1).asByteArray());
        } else if (x instanceof Reader) {
            return CharStream.fromReader((Reader) x, -1).asString();
        } else if (x instanceof ByteStream) {
            return new String(((ByteStream) x).asByteArray());
        } else if (x instanceof CharStream) {
            return ((CharStream) x).asString();
        } else {
            return String.valueOf(x);
        }
    }

    private static String castAsJson(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof String) {
            return (String) x;
        } else if (x instanceof byte[]) {
            return new String((byte[]) x);
        } else if (x instanceof InputStream) {
            return new String(ByteStream.fromInputStream((InputStream) x, -1).asByteArray());
        } else if (x instanceof Reader) {
            return CharStream.fromReader((Reader) x, -1).asString();
        } else if (x instanceof ByteStream) {
            return new String(((ByteStream) x).asByteArray());
        } else if (x instanceof CharStream) {
            return ((CharStream) x).asString();
        }
        throw castNotSupported(type, x);
    }

    private static PrimitiveValue castAsUuid(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof String) {
            return PrimitiveValue.uuid((String) x);
        } else if (x instanceof byte[]) {
            return PrimitiveValue.uuid(new String((byte[]) x));
        } else if (x instanceof UUID) {
            return PrimitiveValue.uuid((UUID) x);
        }
        throw castNotSupported(type, x);
    }

    private static byte castAsByte(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Byte) {
            return (Byte) x;
        } else if (x instanceof Boolean) {
            return (byte) (((Boolean) x) ? 1 : 0);
        }
        throw castNotSupported(type, x);
    }

    private static short castAsShort(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Short) {
            return (Short) x;
        } else if (x instanceof Byte) {
            return (Byte) x;
        } else if (x instanceof Boolean) {
            return (short) (((Boolean) x) ? 1 : 0);
        }
        throw castNotSupported(type, x);
    }

    private static int castAsInt(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Integer) {
            return (Integer) x;
        } else if (x instanceof Short) {
            return (Short) x;
        } else if (x instanceof Byte) {
            return (Byte) x;
        } else if (x instanceof Boolean) {
            return ((Boolean) x) ? 1 : 0;
        }
        throw castNotSupported(type, x);
    }

    private static long castAsLong(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Long) {
            return (Long) x;
        } else if (x instanceof Integer) {
            return (Integer) x;
        } else if (x instanceof Short) {
            return (Short) x;
        } else if (x instanceof Byte) {
            return (Byte) x;
        } else if (x instanceof Boolean) {
            return ((Boolean) x) ? 1L : 0L;
        } else if (x instanceof BigInteger) {
            return ((BigInteger) x).longValue();
        }
        throw castNotSupported(type, x);
    }

    private static float castAsFloat(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Float) {
            return (Float) x;
        } else if (x instanceof Integer) {
            return (Integer) x;
        } else if (x instanceof Short) {
            return (Short) x;
        } else if (x instanceof Byte) {
            return (Byte) x;
        } else if (x instanceof Boolean) {
            return ((Boolean) x) ? 1f : 0f;
        }
        throw castNotSupported(type, x);
    }

    private static double castAsDouble(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Double) {
            return (Double) x;
        } else if (x instanceof Float) {
            return (Float) x;
        } else if (x instanceof Long) {
            return (Long) x;
        } else if (x instanceof Integer) {
            return (Integer) x;
        } else if (x instanceof Short) {
            return (Short) x;
        } else if (x instanceof Byte) {
            return (Byte) x;
        } else if (x instanceof Boolean) {
            return ((Boolean) x) ? 1d : 0d;
        }
        throw castNotSupported(type, x);
    }

    private static boolean castAsBoolean(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Boolean) {
            return (boolean) x;
        } else if (x instanceof Number) {
            long lValue = ((Number) x).longValue();
            return lValue > 0;
        }
        throw castNotSupported(type, x);
    }

    private static ZonedDateTime castAsZonedDateTime(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof ZonedDateTime) {
            return (ZonedDateTime) x;
        }
        throw castNotSupported(type, x);
    }

    private static PrimitiveValue castToInterval(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Duration) {
            return PrimitiveValue.interval((Duration) x);
        } else if (x instanceof Long) {
            return PrimitiveValue.interval((Long) x);
        }
        throw castNotSupported(type, x);
    }

    private static PrimitiveValue castToDate(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Instant) {
            return PrimitiveValue.date((Instant) x);
        } else if (x instanceof LocalDate) {
            return PrimitiveValue.date((LocalDate) x);
        } else if (x instanceof Long) {
            return PrimitiveValue.date(TimeUnit.MILLISECONDS.toDays((Long) x));
        } else if (x instanceof Date) {
            return PrimitiveValue.date(TimeUnit.MILLISECONDS.toDays(((Date) x).getTime()));
        } else if (x instanceof Timestamp) {
            return PrimitiveValue.date(TimeUnit.MILLISECONDS.toDays(((Timestamp) x).getTime()));
        }
        throw castNotSupported(type, x);
    }

    private static PrimitiveValue castToDateTime(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Instant) {
            return PrimitiveValue.datetime((Instant) x);
        } else if (x instanceof LocalDateTime) {
            return PrimitiveValue.datetime((LocalDateTime) x);
        } else if (x instanceof Long) {
            return PrimitiveValue.datetime(TimeUnit.MILLISECONDS.toSeconds((Long) x));
        } else if (x instanceof Date) {
            return PrimitiveValue.datetime(TimeUnit.MILLISECONDS.toSeconds(((Date) x).getTime()));
        } else if (x instanceof Timestamp) {
            return PrimitiveValue.datetime(TimeUnit.MILLISECONDS.toSeconds(((Timestamp) x).getTime()));
        }
        throw castNotSupported(type, x);
    }

    private static PrimitiveValue castToTimestamp(PrimitiveType.Id type, Object x) throws SQLException {
        if (x instanceof Instant) {
            return PrimitiveValue.timestamp((Instant) x);
        } else if (x instanceof Long) {
            return PrimitiveValue.timestamp(TimeUnit.MILLISECONDS.toMicros((Long) x));
        } else if (x instanceof Date) {
            return PrimitiveValue.timestamp(TimeUnit.MILLISECONDS.toMicros(((Date) x).getTime()));
        } else if (x instanceof Timestamp) {
            return PrimitiveValue.timestamp(TimeUnit.MILLISECONDS.toMicros(((Timestamp) x).getTime()));
        }
        throw castNotSupported(type, x);
    }

    private static DecimalValue castToDecimalValue(DecimalType type, Object x) throws SQLException {
        if (x instanceof DecimalValue) {
            return (DecimalValue) x;
        } else if (x instanceof BigDecimal) {
            return type.newValue((BigDecimal) x);
        } else if (x instanceof BigInteger) {
            return type.newValue((BigInteger) x);
        } else if (x instanceof Long) {
            return type.newValue((Long) x);
        } else if (x instanceof Integer) {
            return type.newValue((Integer) x);
        } else if (x instanceof Short) {
            return type.newValue((Short) x);
        } else if (x instanceof Byte) {
            return type.newValue((Byte) x);
        } else if (x instanceof String) {
            return type.newValue((String) x);
        }
        throw castNotSupported(type.getKind(), x);
    }

    interface Setters {
        Value<?> toValue(Object value) throws SQLException;
    }

    interface CharStream {
        String asString() throws SQLException;

        static CharStream fromReader(Reader reader, long length) {
            return () -> {
                try {
                    if (length >= 0) {
                        return CharStreams.toString(new LimitedReader(reader, length));
                    } else {
                        return CharStreams.toString(reader);
                    }
                } catch (IOException e) {
                    throw new YdbExecutionException(CANNOT_LOAD_DATA_FROM_READER + e.getMessage(), e);
                }
            };
        }
    }

    interface ByteStream {
        byte[] asByteArray() throws SQLException;

        @SuppressWarnings("UnstableApiUsage")
        static ByteStream fromInputStream(InputStream stream, long length) {
            return () -> {
                try {
                    if (length >= 0) {
                        return ByteStreams.toByteArray(ByteStreams.limit(stream, length));
                    } else {
                        return ByteStreams.toByteArray(stream);
                    }
                } catch (IOException e) {
                    throw new YdbExecutionException(CANNOT_LOAD_DATA_FROM_IS + e.getMessage(), e);
                }
            };
        }
    }
}
