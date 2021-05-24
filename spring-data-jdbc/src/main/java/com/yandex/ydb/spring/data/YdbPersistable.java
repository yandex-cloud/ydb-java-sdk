package com.yandex.ydb.spring.data;

import org.springframework.data.domain.Persistable;

public interface YdbPersistable<ID> extends Persistable<ID> {

    @Override
    default boolean isNew() {
        return true; // And always use upsert instead of insert
    }
}
