package com.yandex.ydb.spring.data.values;

import com.yandex.ydb.spring.data.YdbMandatoryRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface T1Repository extends YdbMandatoryRepository<T1Value, Integer> {
}
