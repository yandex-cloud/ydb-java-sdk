package com.yandex.ydb.spring.jpa.data;

import com.yandex.ydb.spring.jpa.YdbJpaRepository;
import com.yandex.ydb.spring.jpa.data.ComplexKeyValue.Key;

public interface ComplexKeyRepository extends YdbJpaRepository<ComplexKeyValue, Key> {
}
