package com.yandex.ydb.spring.data;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface SimpleRepository extends YdbMandatoryRepository<SimpleValue, Integer> {
}
