package com.yandex.ydb.spring.data.values;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.yandex.ydb.spring.data.YdbMandatoryRepository;
import com.yandex.ydb.spring.data.values.AllColumnsValue.CustomEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface AllColumnsRepository extends YdbMandatoryRepository<AllColumnsValue, Integer> {
    //

    List<AllColumnsValue> findBySomeColumn(String someColumn);

    //

    List<AllColumnsValue> findByBooleanValue(boolean value);

    List<AllColumnsValue> findByBooleanValueOpt(Boolean value);

    List<AllColumnsValue> findByByteValue(byte value);

    List<AllColumnsValue> findByByteValueOpt(Byte value);

    List<AllColumnsValue> findByShortValue(short value);

    List<AllColumnsValue> findByShortValueOpt(Short value);

    List<AllColumnsValue> findByIntValue(int value);

    List<AllColumnsValue> findByIntValueOpt(Integer value);

    List<AllColumnsValue> findByLongValue(long value);

    List<AllColumnsValue> findByLongValueOpt(Long value);

    List<AllColumnsValue> findByFloatValue(float value);

    List<AllColumnsValue> findByFloatValueOpt(Float value);

    List<AllColumnsValue> findByDoubleValue(double value);

    List<AllColumnsValue> findByDoubleValueOpt(Double value);

    List<AllColumnsValue> findByStringValue(String value);

    List<AllColumnsValue> findByStringValueAsString(String value);

    List<AllColumnsValue> findByBigIntegerValue(BigInteger value);

    List<AllColumnsValue> findByBigDecimalValue(BigDecimal bigDecimalValue);

    List<AllColumnsValue> findByDateValue(Date dateValue);

    List<AllColumnsValue> findByLocalDateValue(LocalDate localDate);

    List<AllColumnsValue> findByLocalDateTimeValue(LocalDateTime localDateTime);

    List<AllColumnsValue> findByEnumValue(CustomEnum enumValue);

    List<AllColumnsValue> findByEnumValueIn(List<CustomEnum> enumValues);

    List<AllColumnsValue> findByInstantValue(Instant value);

    //

    List<AllColumnsValue> findByIntValueAndStringValue(int intValue, String stringValue);

    List<AllColumnsValue> findByIntValueOrderByStringValue(int intValue, Pageable pageable);

    List<AllColumnsValue> findByIntValueGreaterThan(int value);

    //
    @Query("select * from `" + AllColumnsValue.TABLE + "` where `stringValue` like :value")
    List<AllColumnsValue> findByCustomQuery(@Param("value") String value);

    @Query("select * from `" + AllColumnsValue.TABLE +
            "` view `" + AllColumnsValue.INDEX_STRING_VALUE +
            "` where `stringValue` = :value")
    List<AllColumnsValue> findByCustomQueryIndex(@Param("value") String value);

    @Modifying
    @Query("update `" + AllColumnsValue.TABLE + "` set `intValue` = :intValue where `stringValue` like :stringValue")
    void updateByCustomQuery(@Param("stringValue") String stringValue,
                             @Param("intValue") int intValue);

}
