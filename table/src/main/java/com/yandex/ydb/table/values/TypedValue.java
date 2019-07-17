package com.yandex.ydb.table.values;

import com.yandex.ydb.ValueProtos;


/**
 * @author Sergey Polovko
 */
public class TypedValue<T extends Type> {

    private final T type;
    private final Value<T> value;

    public TypedValue(T type, Value<T> value) {
        this.type = type;
        this.value = value;
    }

    public T getType() {
        return type;
    }

    public Value<T> getValue() {
        return value;
    }

    public ValueProtos.TypedValue toPb() {
        return ValueProtos.TypedValue.newBuilder()
            .setType(type.toPb())
            .setValue(value.toPb())
            .build();
    }
}
