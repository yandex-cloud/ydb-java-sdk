package com.yandex.ydb.jdbc.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.yandex.ydb.table.result.PrimitiveReader;
import com.yandex.ydb.table.result.ValueReader;
import com.yandex.ydb.table.values.DecimalValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.jdbc.YdbConst.UNABLE_TO_CAST;
import static com.yandex.ydb.jdbc.YdbConst.UNABLE_TO_CONVERT;
import static com.yandex.ydb.jdbc.impl.MappingSetters.DEFAULT_CHARSET;

public class MappingGetters {

    static Getters buildGetters(Type type) {
        Type.Kind kind = type.getKind();
        @Nullable PrimitiveType.Id id = type.getKind() == Type.Kind.PRIMITIVE ? ((PrimitiveType) type).getId() : null;
        return new Getters(
                valueToString(kind, id),
                valueToBoolean(kind, id),
                valueToByte(kind, id),
                valueToShort(kind, id),
                valueToInt(kind, id),
                valueToLong(kind, id),
                valueToFloat(kind, id),
                valueToDouble(kind, id),
                valueToBytes(kind, id),
                valueToObject(kind, id),
                valueToDateMillis(kind, id),
                valueToNString(kind, id),
                valueToURL(kind, id),
                valueToBigDecimal(kind, id),
                valueToReader(kind, id));
    }

    private static ValueToString valueToString(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = String.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case String:
                    return value -> new String(value.getString(), DEFAULT_CHARSET);
                case Utf8:
                    return PrimitiveReader::getUtf8;
                case Json:
                    return PrimitiveReader::getJson;
                case JsonDocument:
                    return PrimitiveReader::getJsonDocument;
                case Yson:
                    return value -> new String(value.getYson(), DEFAULT_CHARSET);
                case Uuid:
                    return value -> String.valueOf(value.getUuid());
                case Bool:
                    return value -> String.valueOf(value.getBool());
                case Int8:
                    return value -> String.valueOf(value.getInt8());
                case Uint8:
                    return value -> String.valueOf(value.getUint8());
                case Int16:
                    return value -> String.valueOf(value.getInt16());
                case Uint16:
                    return value -> String.valueOf(value.getUint16());
                case Int32:
                    return value -> String.valueOf(value.getInt32());
                case Uint32:
                    return value -> String.valueOf(value.getUint32());
                case Int64:
                    return value -> String.valueOf(value.getInt64());
                case Uint64:
                    return value -> String.valueOf(value.getUint64());
                case Float32:
                    return value -> String.valueOf(value.getFloat32());
                case Float64:
                    return value -> String.valueOf(value.getFloat64());
                case Date:
                    return value -> String.valueOf(value.getDate());
                case Datetime:
                    return value -> String.valueOf(value.getDatetime());
                case Timestamp:
                    return value -> String.valueOf(value.getTimestamp());
                case Interval:
                    return value -> String.valueOf(value.getInterval());
                case TzDate:
                    return value -> String.valueOf(value.getTzDate());
                case TzDatetime:
                    return value -> String.valueOf(value.getTzDatetime());
                case TzTimestamp:
                    return value -> String.valueOf(value.getTzTimestamp());
                default:
                    // DyNumber
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else if (kind == Type.Kind.DECIMAL) {
            return value -> String.valueOf(value.getDecimal());
        } else {
            return value -> String.valueOf(value.getValue());
        }
    }

    private static ValueToBoolean valueToBoolean(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = boolean.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return PrimitiveReader::getBool;
                case Int8:
                    return value -> value.getInt8() > 0;
                case Uint8:
                    return value -> value.getUint8() > 0;
                case Int16:
                    return value -> value.getInt16() > 0;
                case Uint16:
                    return value -> value.getUint16() > 0;
                case Int32:
                    return value -> value.getInt32() > 0;
                case Uint32:
                    return value -> value.getUint32() > 0;
                case Int64:
                    return value -> value.getInt64() > 0;
                case Uint64:
                    return value -> value.getUint64() > 0;
                case String:
                    return value -> {
                        byte[] stringValue = value.getString();
                        if (stringValue.length == 0) {
                            return false;
                        } else if (stringValue.length == 1) {
                            if (stringValue[0] == '0') {
                                return false;
                            } else if (stringValue[0] == '1') {
                                return true;
                            }
                        }
                        throw cannotConvert(id, javaType, new String(stringValue, DEFAULT_CHARSET));
                    };
                case Utf8:
                    return value -> {
                        String utfValue = value.getUtf8();
                        if (utfValue.isEmpty()) {
                            return false;
                        } else if (utfValue.length() == 1) {
                            if (utfValue.charAt(0) == '0') {
                                return false;
                            } else if (utfValue.charAt(0) == '1') {
                                return true;
                            }
                        }
                        throw cannotConvert(id, javaType, utfValue);
                    };
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToByte valueToByte(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = byte.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return value -> (byte) (value.getBool() ? 1 : 0);
                case Int8:
                    return PrimitiveReader::getInt8;
                case Uint8:
                    return value -> (byte) value.getUint8();
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToShort valueToShort(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = short.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return value -> (short) (value.getBool() ? 1 : 0);
                case Int8:
                    return PrimitiveReader::getInt8;
                case Uint8:
                    return valueReader -> (short) valueReader.getUint8();
                case Int16:
                    return PrimitiveReader::getInt16;
                case Uint16:
                    return valueReader -> (short) valueReader.getUint16();
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToInt valueToInt(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = int.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return value -> value.getBool() ? 1 : 0;
                case Int8:
                    return PrimitiveReader::getInt8;
                case Uint8:
                    return PrimitiveReader::getUint8;
                case Int16:
                    return PrimitiveReader::getInt16;
                case Uint16:
                    return PrimitiveReader::getUint16;
                case Int32:
                    return PrimitiveReader::getInt32;
                case Uint32:
                    return valueReader -> (int) valueReader.getUint32();
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToLong valueToLong(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = long.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return value -> value.getBool() ? 1 : 0;
                case Int8:
                    return PrimitiveReader::getInt8;
                case Uint8:
                    return PrimitiveReader::getUint8;
                case Int16:
                    return PrimitiveReader::getInt16;
                case Uint16:
                    return PrimitiveReader::getUint16;
                case Int32:
                    return PrimitiveReader::getInt32;
                case Uint32:
                    return PrimitiveReader::getUint32;
                case Int64:
                    return PrimitiveReader::getInt64;
                case Uint64:
                    return PrimitiveReader::getUint64;
                case Date:
                case Datetime:
                case TzDate:
                case TzDatetime:
                case Timestamp:
                case TzTimestamp:
                    ValueToDateMillis delegate = valueToDateMillis(kind, id);
                    return delegate::fromValue;
                case Interval:
                    return value -> TimeUnit.NANOSECONDS.toMicros(value.getInterval().toNanos());
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else if (kind == Type.Kind.DECIMAL) {
            return value -> value.getDecimal().toBigInteger().longValue(); // TODO: Improve performance
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToFloat valueToFloat(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = float.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return value -> value.getBool() ? 1 : 0;
                case Int8:
                    return PrimitiveReader::getInt8;
                case Uint8:
                    return PrimitiveReader::getUint8;
                case Int16:
                    return PrimitiveReader::getInt16;
                case Uint16:
                    return PrimitiveReader::getUint16;
                case Int32:
                    return PrimitiveReader::getInt32;
                case Uint32:
                    return PrimitiveReader::getUint32;
                case Float32:
                    return PrimitiveReader::getFloat32;
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else if (kind == Type.Kind.DECIMAL) {
            return value -> value.getDecimal().toBigDecimal().floatValue();
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToDouble valueToDouble(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = double.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return value -> value.getBool() ? 1 : 0;
                case Int8:
                    return PrimitiveReader::getInt8;
                case Uint8:
                    return PrimitiveReader::getUint8;
                case Int16:
                    return PrimitiveReader::getInt16;
                case Uint16:
                    return PrimitiveReader::getUint16;
                case Int32:
                    return PrimitiveReader::getInt32;
                case Uint32:
                    return PrimitiveReader::getUint32;
                case Int64:
                    return PrimitiveReader::getInt64;
                case Uint64:
                    return PrimitiveReader::getUint64;
                case Float32:
                    return PrimitiveReader::getFloat32;
                case Float64:
                    return PrimitiveReader::getFloat64;
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else if (kind == Type.Kind.DECIMAL) {
            return value -> value.getDecimal().toBigDecimal().doubleValue();
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToBytes valueToBytes(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = byte[].class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case String:
                    return PrimitiveReader::getString;
                case Utf8:
                    // TODO: pretty ineffective conversion (bytes -> string -> bytes)
                    return value -> value.getUtf8().getBytes();
                case Json:
                    return value -> value.getJson().getBytes();
                case JsonDocument:
                    return value -> value.getJsonDocument().getBytes();
                case Yson:
                    return PrimitiveReader::getYson;
                case Uuid:
                    return value -> value.getUuid().toString().getBytes();
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToDateMillis valueToDateMillis(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = long.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Int64:
                    return PrimitiveReader::getInt64;
                case Uint64:
                    return PrimitiveReader::getUint64;
                case Date:
                    return value -> TimeUnit.DAYS.toMillis(value.getDate().toEpochDay());
                case Datetime:
                    return value -> TimeUnit.SECONDS.toMillis(value.getDatetime().toEpochSecond(ZoneOffset.UTC));
                case TzDate:
                    return value -> TimeUnit.SECONDS.toMillis(value.getTzDate().toEpochSecond());
                case TzDatetime:
                    return value -> TimeUnit.SECONDS.toMillis(value.getTzDatetime().toEpochSecond());
                case Timestamp:
                    return value -> value.getTimestamp().toEpochMilli();
                case TzTimestamp:
                    return value -> TimeUnit.SECONDS.toMillis(value.getTzTimestamp().toEpochSecond());
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToNString valueToNString(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = String.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case String:
                    return value -> new String(value.getString(), DEFAULT_CHARSET);
                case Utf8:
                    return PrimitiveReader::getUtf8;
                case Json:
                    return PrimitiveReader::getJson;
                case JsonDocument:
                    return PrimitiveReader::getJsonDocument;
                case Yson:
                    return value -> new String(value.getYson(), DEFAULT_CHARSET);
                case Uuid:
                    return value -> String.valueOf(value.getUuid());
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToURL valueToURL(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = URL.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case String:
                    return value -> new String(value.getString(), DEFAULT_CHARSET);
                case Utf8:
                    return PrimitiveReader::getUtf8;
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToBigDecimal valueToBigDecimal(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = BigDecimal.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case Bool:
                    return value -> BigDecimal.valueOf(value.getBool() ? 1 : 0);
                case Int8:
                    return value -> BigDecimal.valueOf(value.getInt8());
                case Uint8:
                    return value -> BigDecimal.valueOf(value.getUint8());
                case Int16:
                    return value -> BigDecimal.valueOf(value.getInt16());
                case Uint16:
                    return value -> BigDecimal.valueOf(value.getUint16());
                case Int32:
                    return value -> BigDecimal.valueOf(value.getInt32());
                case Uint32:
                    return value -> BigDecimal.valueOf(value.getUint32());
                case Int64:
                    return value -> BigDecimal.valueOf(value.getInt64());
                case Uint64:
                    return value -> BigDecimal.valueOf(value.getUint64());
                case Float32:
                    return value -> BigDecimal.valueOf(value.getFloat32());
                case Float64:
                    return value -> BigDecimal.valueOf(value.getFloat64());
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else if (kind == Type.Kind.DECIMAL) {
            return value -> value.getDecimal().toBigDecimal();
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }

    private static ValueToReader valueToReader(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = Reader.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case String:
                    return value -> new InputStreamReader(new ByteArrayInputStream(value.getString()));
                case Utf8:
                    return value -> new StringReader(value.getUtf8());
                case Json:
                    return value -> new StringReader(value.getJson());
                case JsonDocument:
                    return value -> new StringReader(value.getJsonDocument());
                case Yson:
                    return value -> new InputStreamReader(new ByteArrayInputStream(value.getYson()));
                case Uuid:
                    return value -> new StringReader(value.getUuid().toString());
                default:
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else {
            return value -> {
                throw dataTypeNotSupported(kind, javaType);
            };
        }
    }


    static SqlTypes buildDataType(Type type) {
        Type.Kind kind = type.getKind();
        // All types must be the same as for #valueToObject
        if (kind == Type.Kind.PRIMITIVE) {
            PrimitiveType.Id id = ((PrimitiveType) type).getId();
            String dbType = id.name();
            switch (id) {
                case String:
                case Utf8:
                case Json:
                case JsonDocument:
                case Uuid:
                    return new SqlTypes(dbType, Types.VARCHAR, String.class);
                case Yson:
                    return new SqlTypes(dbType, Types.BINARY, byte[].class);
                case Bool:
                    return new SqlTypes(dbType, Types.BOOLEAN, Boolean.class);
                case Int8:
                    return new SqlTypes(dbType, Types.SMALLINT, Byte.class);
                case Uint8:
                case Int32:
                case Uint16:
                    return new SqlTypes(dbType, Types.INTEGER, Integer.class);
                case Int16:
                    return new SqlTypes(dbType, Types.SMALLINT, Short.class);
                case Uint32:
                case Int64:
                case Uint64:
                    return new SqlTypes(dbType, Types.BIGINT, Long.class);
                case Float32:
                    return new SqlTypes("Float", Types.FLOAT, Float.class);
                case Float64:
                    return new SqlTypes("Double", Types.DOUBLE, Double.class);
                case Date:
                    return new SqlTypes(dbType, Types.DATE, LocalDate.class);
                case Datetime:
                    return new SqlTypes(dbType, Types.TIMESTAMP, LocalDateTime.class);
                case Timestamp:
                    return new SqlTypes(dbType, Types.TIMESTAMP, Instant.class);
                case Interval:
                    return new SqlTypes(dbType, Types.TIMESTAMP, Duration.class);
                case TzDate:
                case TzDatetime:
                case TzTimestamp:
                    return new SqlTypes(dbType, Types.TIMESTAMP_WITH_TIMEZONE, ZonedDateTime.class);
                default:
                    return new SqlTypes(dbType, Types.JAVA_OBJECT, Value.class);
            }
        } else if (kind == Type.Kind.DECIMAL) {
            // Maybe different type?
            return new SqlTypes(kind.name(), Types.DECIMAL, DecimalValue.class);
        } else {
            return new SqlTypes(kind.name(), Types.JAVA_OBJECT, Value.class);
        }
    }

    private static ValueToObject valueToObject(Type.Kind kind, @Nullable PrimitiveType.Id id) {
        Class<?> javaType = Object.class;
        if (kind == Type.Kind.PRIMITIVE) {
            Preconditions.checkState(id != null, "Primitive type must not be null when kind is %s", kind);
            switch (id) {
                case String:
                    return valueReader -> new String(valueReader.getString(), DEFAULT_CHARSET);
                case Utf8:
                    return PrimitiveReader::getUtf8;
                case Json:
                    return PrimitiveReader::getJson;
                case JsonDocument:
                    return PrimitiveReader::getJsonDocument;
                case Yson:
                    return PrimitiveReader::getYson;
                case Uuid:
                    return PrimitiveReader::getUuid;
                case Bool:
                    return PrimitiveReader::getBool;
                case Int8:
                    return PrimitiveReader::getInt8;
                case Uint8:
                    return PrimitiveReader::getUint8;
                case Int16:
                    return PrimitiveReader::getInt16;
                case Uint16:
                    return PrimitiveReader::getUint16;
                case Int32:
                    return PrimitiveReader::getInt32;
                case Uint32:
                    return PrimitiveReader::getUint32;
                case Int64:
                    return PrimitiveReader::getInt64;
                case Uint64:
                    return PrimitiveReader::getUint64;
                case Float32:
                    return PrimitiveReader::getFloat32;
                case Float64:
                    return PrimitiveReader::getFloat64;
                case Date:
                    return PrimitiveReader::getDate;
                case Datetime:
                    return PrimitiveReader::getDatetime;
                case Timestamp:
                    return PrimitiveReader::getTimestamp;
                case Interval:
                    return PrimitiveReader::getInterval;
                case TzDate:
                    return PrimitiveReader::getTzDate;
                case TzDatetime:
                    return PrimitiveReader::getTzDatetime;
                case TzTimestamp:
                    return PrimitiveReader::getTzTimestamp;
                default:
                    // DyNumber
                    return value -> {
                        throw dataTypeNotSupported(id, javaType);
                    };
            }
        } else if (kind == Type.Kind.DECIMAL) {
            return PrimitiveReader::getDecimal;
        } else {
            return ValueReader::getValue;
        }
    }


    private static SQLException cannotConvert(PrimitiveType.Id type, Class<?> javaType, Object value) {
        return new SQLException(String.format(UNABLE_TO_CONVERT, type, value, javaType));
    }

    private static SQLException dataTypeNotSupported(PrimitiveType.Id type, Class<?> javaType) {
        return new SQLException(String.format(UNABLE_TO_CAST, type, javaType));
    }

    private static SQLException dataTypeNotSupported(Type.Kind kind, Class<?> javaType) {
        return new SQLException(String.format(UNABLE_TO_CAST, kind, javaType));
    }

    static class Getters {
        final ValueToString toString;
        final ValueToBoolean toBoolean;
        final ValueToByte toByte;
        final ValueToShort toShort;
        final ValueToInt toInt;
        final ValueToLong toLong;
        final ValueToFloat toFloat;
        final ValueToDouble toDouble;
        final ValueToBytes toBytes;
        final ValueToObject toObject;
        final ValueToDateMillis toDateMillis;
        final ValueToNString toNString;
        final ValueToURL toURL;
        final ValueToBigDecimal toBigDecimal;
        final ValueToReader toReader;

        Getters(ValueToString toString,
                ValueToBoolean toBoolean,
                ValueToByte toByte,
                ValueToShort toShort,
                ValueToInt toInt,
                ValueToLong toLong,
                ValueToFloat toFloat,
                ValueToDouble toDouble,
                ValueToBytes toBytes,
                ValueToObject toObject,
                ValueToDateMillis toDateMillis,
                ValueToNString toNString,
                ValueToURL toURL,
                ValueToBigDecimal toBigDecimal,
                ValueToReader toReader) {
            this.toString = toString;
            this.toBoolean = toBoolean;
            this.toByte = toByte;
            this.toShort = toShort;
            this.toInt = toInt;
            this.toLong = toLong;
            this.toFloat = toFloat;
            this.toDouble = toDouble;
            this.toBytes = toBytes;
            this.toObject = toObject;
            this.toDateMillis = toDateMillis;
            this.toNString = toNString;
            this.toURL = toURL;
            this.toBigDecimal = toBigDecimal;
            this.toReader = toReader;
        }
    }

    interface ValueToString {
        String fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToBoolean {
        boolean fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToByte {
        byte fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToShort {
        short fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToInt {
        int fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToLong {
        long fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToFloat {
        float fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToDouble {
        double fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToBytes {
        byte[] fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToObject {
        Object fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToDateMillis {
        long fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToNString {
        String fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToURL {
        String fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToBigDecimal {
        BigDecimal fromValue(ValueReader reader) throws SQLException;
    }

    interface ValueToReader {
        Reader fromValue(ValueReader reader) throws SQLException;
    }

    //

    static class SqlTypes {
        private final String databaseType;
        private final int sqlType;
        private final Class<?> javaType;

        SqlTypes(String databaseType, int sqlType, Class<?> javaType) {
            this.databaseType = Objects.requireNonNull(databaseType);
            this.sqlType = sqlType;
            this.javaType = Objects.requireNonNull(javaType);
        }

        public String getDatabaseType() {
            return databaseType;
        }

        public int getSqlType() {
            return sqlType;
        }

        public Class<?> getJavaType() {
            return javaType;
        }
    }

}
