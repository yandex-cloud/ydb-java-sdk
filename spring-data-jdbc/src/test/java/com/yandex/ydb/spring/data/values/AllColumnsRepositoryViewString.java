package com.yandex.ydb.spring.data.values;

import java.util.List;

import com.yandex.ydb.spring.data.YdbMandatoryRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface AllColumnsRepositoryViewString extends YdbMandatoryRepository<AllColumnsStringValue, Integer> {

    List<AllColumnsStringValue> findViewByIdIn(Integer... id);
}
