package com.yandex.ydb.jdbc.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.table.values.DecimalValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.Type;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

public class YdbTypesImpl implements YdbTypes {

    private static final YdbTypesImpl INSTANCE = new YdbTypesImpl();

    private final IntObjectMap<PrimitiveType> primitiveTypeByNumId;
    private final IntObjectMap<Type> typeBySqlType;
    private final IntObjectMap<Integer> sqlTypeByPrimitiveNumId;
    private final Map<Class<?>, Integer> sqlTypeByClass;

    private YdbTypesImpl() {
        PrimitiveType.Id[] values = PrimitiveType.Id.values();
        primitiveTypeByNumId = new IntObjectHashMap<>(values.length);
        for (PrimitiveType.Id id : values) {
            primitiveTypeByNumId.put(id.getNumId(), PrimitiveType.of(id));
        }

        typeBySqlType = new IntObjectHashMap<>(16);
        typeBySqlType.put(Types.VARCHAR, PrimitiveType.utf8());
        typeBySqlType.put(Types.BIGINT, PrimitiveType.int64());
        typeBySqlType.put(Types.TINYINT, PrimitiveType.int32());
        typeBySqlType.put(Types.SMALLINT, PrimitiveType.int32());
        typeBySqlType.put(Types.INTEGER, PrimitiveType.int32());
        typeBySqlType.put(Types.REAL, PrimitiveType.float32());
        typeBySqlType.put(Types.FLOAT, PrimitiveType.float32());
        typeBySqlType.put(Types.DOUBLE, PrimitiveType.float64());
        typeBySqlType.put(Types.BIT, PrimitiveType.bool());
        typeBySqlType.put(Types.BOOLEAN, PrimitiveType.bool());
        typeBySqlType.put(Types.BINARY, PrimitiveType.string());
        typeBySqlType.put(Types.DATE, PrimitiveType.date());
        typeBySqlType.put(Types.TIME, PrimitiveType.datetime());
        typeBySqlType.put(Types.TIMESTAMP, PrimitiveType.timestamp());
        typeBySqlType.put(Types.DECIMAL, DEFAULT_DECIMAL_TYPE);

        sqlTypeByClass = new HashMap<>(32);
        sqlTypeByClass.put(String.class, Types.VARCHAR);
        sqlTypeByClass.put(long.class, Types.BIGINT);
        sqlTypeByClass.put(Long.class, Types.BIGINT);
        sqlTypeByClass.put(BigInteger.class, Types.BIGINT);
        sqlTypeByClass.put(byte.class, Types.TINYINT);
        sqlTypeByClass.put(Byte.class, Types.TINYINT);
        sqlTypeByClass.put(short.class, Types.SMALLINT);
        sqlTypeByClass.put(Short.class, Types.SMALLINT);
        sqlTypeByClass.put(int.class, Types.INTEGER);
        sqlTypeByClass.put(Integer.class, Types.INTEGER);
        sqlTypeByClass.put(float.class, Types.FLOAT);
        sqlTypeByClass.put(Float.class, Types.FLOAT);
        sqlTypeByClass.put(double.class, Types.DOUBLE);
        sqlTypeByClass.put(Double.class, Types.DOUBLE);
        sqlTypeByClass.put(boolean.class, Types.BOOLEAN);
        sqlTypeByClass.put(Boolean.class, Types.BOOLEAN);
        sqlTypeByClass.put(byte[].class, Types.BINARY);
        sqlTypeByClass.put(Date.class, Types.TIMESTAMP);
        sqlTypeByClass.put(LocalDate.class, Types.DATE);
        sqlTypeByClass.put(LocalDateTime.class, Types.TIME);
        sqlTypeByClass.put(Time.class, Types.TIME);
        sqlTypeByClass.put(LocalTime.class, Types.TIME);
        sqlTypeByClass.put(Timestamp.class, Types.TIMESTAMP);
        sqlTypeByClass.put(Instant.class, Types.TIMESTAMP);
        sqlTypeByClass.put(DecimalValue.class, Types.DECIMAL);
        sqlTypeByClass.put(BigDecimal.class, Types.DECIMAL);

        sqlTypeByPrimitiveNumId = new IntObjectHashMap<>(values.length);
        for (PrimitiveType.Id id : values) {
            final int sqlType;
            switch (id) {
                case String:
                case Utf8:
                case Json:
                case JsonDocument:
                case Uuid:
                    sqlType = Types.VARCHAR;
                    break;
                case Yson:
                    sqlType = Types.BINARY;
                    break;
                case Bool:
                    sqlType = Types.BOOLEAN;
                    break;
                case Int8:
                case Int16:
                    sqlType = Types.SMALLINT;
                    break;
                case Uint8:
                case Int32:
                case Uint16:
                    sqlType = Types.INTEGER;
                    break;
                case Uint32:
                case Int64:
                case Uint64:
                case Interval:
                    sqlType = Types.BIGINT;
                    break;
                case Float32:
                    sqlType = Types.FLOAT;
                    break;
                case Float64:
                    sqlType = Types.DOUBLE;
                    break;
                case Date:
                    sqlType = Types.DATE;
                    break;
                case Datetime:
                case Timestamp:
                    sqlType = Types.TIMESTAMP;
                    break;
                case TzDate:
                case TzDatetime:
                case TzTimestamp:
                    sqlType = Types.TIMESTAMP_WITH_TIMEZONE;
                    break;
                default:
                    sqlType = Types.JAVA_OBJECT;
            }
            sqlTypeByPrimitiveNumId.put(id.getNumId(), sqlType);
        }
    }

    @Override
    public int toSqlType(Class<?> type) {
        Integer result = sqlTypeByClass.get(type);
        return result != null ? result : YdbConst.UNKNOWN_SQL_TYPE;
    }

    @Override
    public Type toYdbType(Class<?> type) {
        return toYdbType(toSqlType(type));
    }

    @Override
    public int wrapYdbJdbcType(Type type) {
        if (type.getKind() == Type.Kind.PRIMITIVE) {
            return YdbConst.SQL_KIND_PRIMITIVE + ((PrimitiveType) type).getId().getNumId();
        } else if (type.getKind() == Type.Kind.DECIMAL) {
            return Types.DECIMAL;
        } else if (type.getKind() == Type.Kind.OPTIONAL) {
            return wrapYdbJdbcType(type.unwrapOptional());
        } else {
            return Types.JAVA_OBJECT;
        }
    }

    @Override
    public int unwrapYdbJdbcType(int sqlType) {
        if (sqlType >= YdbConst.SQL_KIND_PRIMITIVE && sqlType < YdbConst.SQL_KIND_DECIMAL) {
            int idType = sqlType - YdbConst.SQL_KIND_PRIMITIVE;
            return sqlTypeByPrimitiveNumId.get(idType);
        } else {
            return sqlType;
        }
    }

    @Override
    @Nullable
    public Type toYdbType(int sqlType) {
        if (sqlType == YdbConst.UNKNOWN_SQL_TYPE) {
            return null;
        } else if (sqlType >= YdbConst.SQL_KIND_PRIMITIVE && sqlType < YdbConst.SQL_KIND_DECIMAL) {
            int idType = sqlType - YdbConst.SQL_KIND_PRIMITIVE;
            return primitiveTypeByNumId.get(idType);
        } else if (sqlType == YdbConst.SQL_KIND_DECIMAL || sqlType == Types.DECIMAL) {
            return DEFAULT_DECIMAL_TYPE;
        } else {
            return typeBySqlType.get(sqlType);
        }
    }

    @Override
    public int toSqlType(Type type) {
        return unwrapYdbJdbcType(wrapYdbJdbcType(type));
    }


    @Override
    public int getSqlPrecision(Type type) {
        // The <...> column specifies the column size for the given column.
        // For numeric data, this is the maximum precision.
        // For character data, this is the length in characters.
        // For datetime datatypes, this is the length in characters of the String representation
        // (assuming the maximum allowed precision of the fractional seconds component).
        // For binary data, this is the length in bytes.
        // For the ROWID datatype, this is the length in bytes.
        // Null is returned for data types where the column size is not applicable.

        switch (type.getKind()) {
            case OPTIONAL:
                return getSqlPrecision(type.unwrapOptional());
            case DECIMAL:
                return 8 + 8;
            case PRIMITIVE:
                return getSqlPrecision(((PrimitiveType) type).getId());
            default:
                return 0; // unsupported?
        }
    }

    @Override
    public List<Type> getDatabaseTypes() {
        return Arrays.asList(
                PrimitiveType.bool(),
                PrimitiveType.int32(),
                PrimitiveType.int64(),
                PrimitiveType.uint8(),
                PrimitiveType.uint32(),
                PrimitiveType.uint64(),
                PrimitiveType.float32(),
                PrimitiveType.float64(),
                PrimitiveType.string(),
                PrimitiveType.utf8(),
                PrimitiveType.json(),
                PrimitiveType.jsonDocument(),
                PrimitiveType.yson(),
                PrimitiveType.date(),
                PrimitiveType.datetime(),
                PrimitiveType.timestamp(),
                PrimitiveType.interval(),
                YdbTypes.DEFAULT_DECIMAL_TYPE);
    }

    //

    private int getSqlPrecision(PrimitiveType.Id type) {
        switch (type) {
            case Bool:
            case Int8:
            case Uint8:
                return 1;
            case Int16:
            case Uint16:
                return 2;
            case Int32:
            case Uint32:
            case Float32:
                return 4;
            case Int64:
            case Uint64:
            case Float64:
            case Interval:
                return 8;
            case String:
            case Utf8:
            case Yson:
            case Json:
            case JsonDocument:
                return YdbConst.MAX_COLUMN_SIZE;
            case Uuid:
                return 8 + 8;
            case Date:
                return "0000-00-00".length();
            case Datetime:
                return "0000-00-00 00:00:00".length();
            case Timestamp:
                return "0000-00-00T00:00:00.000000".length();
            case TzDate:
                return "0000-00-00+00:00".length();
            case TzDatetime:
                return "0000-00-00 00:00:00+00:00".length();
            case TzTimestamp:
                return "0000-00-00T00:00:00.000000+00:00".length();
            default:
                return 0;
        }
    }


    // TODO: make access from connection only
    public static YdbTypes getInstance() {
        return INSTANCE;
    }
}
