package com.yandex.ydb.spring.data.values;

import com.yandex.ydb.spring.data.YdbMandatoryRepository;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface ComplexKeyRepository extends YdbMandatoryRepository<ComplexKeyValue, ComplexKeyValue.Key> {

    @Modifying
    @Query("upsert into `" + ComplexKeyValue.TABLE +
            "`(`intKey`, `stringKey`, `value`) values (:intKey, :stringKey, :value)")
    void upsert(@Param("intKey") int intKey,
                @Param("stringKey") String stringKey,
                @Param("value") String value);
}
