package com.yandex.ydb.spring.data;

import javax.annotation.Nonnull;

import com.yandex.ydb.table.values.Type;

public class YdbWrappedValue {

    private final Object value;
    private final Type type;

    public YdbWrappedValue(@Nonnull Object value, @Nonnull Type type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}
