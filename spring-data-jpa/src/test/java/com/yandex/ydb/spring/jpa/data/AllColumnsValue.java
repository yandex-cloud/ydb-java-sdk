package com.yandex.ydb.spring.jpa.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import com.yandex.ydb.spring.jpa.YdbPersistable;
import com.yandex.ydb.spring.jpa.types.StringBinaryType;
import org.hibernate.annotations.Type;

@Entity(name = AllColumnsValue.TABLE)
public class AllColumnsValue extends YdbPersistable<Integer> {
    public static final String TABLE = "unit_all_columns";
    public static final String INDEX_STRING_VALUE = "INDEX_STRING_VALUE";

    // Standard mapping

    @Id
    int id;

    @Column(name = "customColumn")
    String someColumn;

    boolean booleanValue; // Types.BOOLEAN -> Bool
    Boolean booleanValueOpt;

    byte byteValue; // Types.TINYINT -> Int32
    Byte byteValueOpt;

    short shortValue; // Types.SMALLINT -> Int32
    Short shortValueOpt;

    int intValue; // Types.INTEGER -> Int32
    Integer intValueOpt;

    // TODO: support @YdbPrimitive(type = PrimitiveType.Id.Uint32)
    int uintValue;

    long longValue; // Types.BIGINT -> Int64
    Long longValueOpt;

    float floatValue; // Types.FLOAT -> Float32
    Float floatValueOpt;

    double doubleValue; // Types.DOUBLE -> Float64
    Double doubleValueOpt;

    String stringValue; // Types.VARCHAR -> Utf8

    @Type(type = StringBinaryType.NAME)
    String stringValueAsString;

    BigInteger bigIntegerValue; // Types.NUMERIC -> DECIMAL(22,9)
    BigDecimal bigDecimalValue; // Types.NUMERIC -> DECIMAL(22,9)

    byte[] bytesValue; // Types.BINARY/Types.VARBINARY -> String

    @Enumerated(EnumType.STRING)
    CustomEnum enumValue; // Types.VARCHAR -> Utf8

    // Custom mapping, available by default
    Timestamp dateValue; // Types.TIMESTAMP -> Timestamp

    LocalDate localDateValue; // Types.DATE -> Date (Unsupported?)
    LocalDateTime localDateTimeValue; // Types.TIME -> DateTime

    Instant instantValue; // Types.TIMESTAMP -> Timestamp

    //


    @Override
    public Integer getId() {
        return id;
    }

    //
    public enum CustomEnum {
        First, Second
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AllColumnsValue)) {
            return false;
        }
        AllColumnsValue that = (AllColumnsValue) o;
        return id == that.id &&
                booleanValue == that.booleanValue &&
                byteValue == that.byteValue &&
                shortValue == that.shortValue &&
                intValue == that.intValue &&
                uintValue == that.uintValue &&
                longValue == that.longValue &&
                Float.compare(that.floatValue, floatValue) == 0 &&
                Double.compare(that.doubleValue, doubleValue) == 0 &&
                Objects.equals(someColumn, that.someColumn) &&
                Objects.equals(booleanValueOpt, that.booleanValueOpt) &&
                Objects.equals(byteValueOpt, that.byteValueOpt) &&
                Objects.equals(shortValueOpt, that.shortValueOpt) &&
                Objects.equals(intValueOpt, that.intValueOpt) &&
                Objects.equals(longValueOpt, that.longValueOpt) &&
                Objects.equals(floatValueOpt, that.floatValueOpt) &&
                Objects.equals(doubleValueOpt, that.doubleValueOpt) &&
                Objects.equals(stringValue, that.stringValue) &&
                Objects.equals(stringValueAsString, that.stringValueAsString) &&
                Objects.equals(bigIntegerValue, that.bigIntegerValue) &&
                Objects.equals(bigDecimalValue, that.bigDecimalValue) &&
                Arrays.equals(bytesValue, that.bytesValue) &&
                enumValue == that.enumValue &&
                Objects.equals(dateValue, that.dateValue) &&
                Objects.equals(localDateValue, that.localDateValue) &&
                Objects.equals(localDateTimeValue, that.localDateTimeValue) &&
                Objects.equals(instantValue, that.instantValue);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, someColumn, booleanValue, booleanValueOpt, byteValue, byteValueOpt, shortValue,
                shortValueOpt, intValue, intValueOpt, uintValue, longValue, longValueOpt, floatValue, floatValueOpt,
                doubleValue, doubleValueOpt, stringValue, stringValueAsString, bigIntegerValue, bigDecimalValue,
                enumValue, dateValue, localDateValue, localDateTimeValue, instantValue);
        result = 31 * result + Arrays.hashCode(bytesValue);
        return result;
    }

    @Override
    public String toString() {
        return "AllColumnsValue{" +
                "id=" + id +
                ", someColumn='" + someColumn + '\'' +
                ", booleanValue=" + booleanValue +
                ", booleanValueOpt=" + booleanValueOpt +
                ", byteValue=" + byteValue +
                ", byteValueOpt=" + byteValueOpt +
                ", shortValue=" + shortValue +
                ", shortValueOpt=" + shortValueOpt +
                ", intValue=" + intValue +
                ", intValueOpt=" + intValueOpt +
                ", uintValue=" + uintValue +
                ", longValue=" + longValue +
                ", longValueOpt=" + longValueOpt +
                ", floatValue=" + floatValue +
                ", floatValueOpt=" + floatValueOpt +
                ", doubleValue=" + doubleValue +
                ", doubleValueOpt=" + doubleValueOpt +
                ", stringValue='" + stringValue + '\'' +
                ", stringValueAsString='" + stringValueAsString + '\'' +
                ", bigIntegerValue=" + bigIntegerValue +
                ", bigDecimalValue=" + bigDecimalValue +
                ", bytesValue=" + Arrays.toString(bytesValue) +
                ", enumValue=" + enumValue +
                ", dateValue=" + dateValue +
                ", localDateValue=" + localDateValue +
                ", localDateTimeValue=" + localDateTimeValue +
                ", instantValue=" + instantValue +
                '}';
    }

}
