package com.yandex.ydb.spring.jpa.data;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.yandex.ydb.spring.jpa.YdbPersistable;

@Entity(name = SimpleValue.TABLE)
public class SimpleValue extends YdbPersistable<Integer> {

    public static final String TABLE = "unit_simple";

    @Id
    private int key;
    private String value;

    private SimpleValue() {
        //
    }

    public SimpleValue(int key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Integer getId() {
        return key;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        return key == that.key &&
                Objects.equals(value, that.value);
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
