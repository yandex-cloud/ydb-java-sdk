package com.yandex.ydb.spring.jpa.data;

import com.yandex.ydb.spring.jpa.YdbJpaRepository;

public interface SimpleRepository extends YdbJpaRepository<SimpleValue, Integer> {
}
