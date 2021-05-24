package com.yandex.ydb.spring.data;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(SimpleValue.TABLE)
public class SimpleValue implements YdbPersistable<Integer> {
    public static final String TABLE = "unit_simple";

    @Id
    private final int key;

    private final String value;

    public SimpleValue(int key, String value) {
        this.key = key;
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public Integer getId() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SimpleValue)) {
            return false;
        }
        SimpleValue that = (SimpleValue) o;
        return key == that.key && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "SimpleValue{" +
                "key=" + key +
                ", value='" + value + '\'' +
                '}';
    }
}
