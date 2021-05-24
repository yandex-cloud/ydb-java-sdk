package com.yandex.ydb.spring.data.values;

import java.util.Objects;

import com.yandex.ydb.spring.data.YdbPersistable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Table(ComplexKeyValue.TABLE)
public class ComplexKeyValue implements YdbPersistable<ComplexKeyValue.Key> {
    public static final String TABLE = "unit_complex_key";

    // TODO: unsupported
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    @Id
    Key key;

    String value;

    @Override
    public Key getId() {
        return key;
    }

    @Override
    public String toString() {
        return "ComplexKeyValue{" +
                "key=" + key +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComplexKeyValue)) {
            return false;
        }
        ComplexKeyValue that = (ComplexKeyValue) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    public static class Key {
        final int intKey;
        final String stringKey;

        public Key(int intKey, String stringKey) {
            this.intKey = intKey;
            this.stringKey = stringKey;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "intKey=" + intKey +
                    ", stringKey='" + stringKey + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key key = (Key) o;
            return intKey == key.intKey && Objects.equals(stringKey, key.stringKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(intKey, stringKey);
        }
    }
}
