package com.yandex.ydb.spring.jpa.data;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import com.yandex.ydb.spring.jpa.YdbPersistable;
import com.yandex.ydb.spring.jpa.types.StringBinaryType;
import org.hibernate.annotations.Type;

@Entity(name = ComplexKeyValue.TABLE)
public class ComplexKeyValue extends YdbPersistable<ComplexKeyValue.Key> {
    public static final String TABLE = "unit_complex_key";

    @EmbeddedId
    private Key key;
    private String value1;

    @Type(type = StringBinaryType.NAME)
    private String value2;

    private ComplexKeyValue() {
        //
    }

    public ComplexKeyValue(Key key, String value1, String value2) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public Key getId() {
        return key;
    }

    public Key getKey() {
        return key;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
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
        return Objects.equals(key, that.key) &&
                Objects.equals(value1, that.value1) &&
                Objects.equals(value2, that.value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value1, value2);
    }

    @Override
    public String toString() {
        return "ComplexKeyValue{" +
                "key=" + key +
                ", value1='" + value1 + '\'' +
                ", value2='" + value2 + '\'' +
                '}';
    }

    @Embeddable
    public static class Key implements Serializable {
        private int intKey;
        private String stringKey;

        private Key() {
            //
        }

        public Key(int intKey, String stringKey) {
            this.intKey = intKey;
            this.stringKey = stringKey;
        }

        public int getIntKey() {
            return intKey;
        }

        public String getStringKey() {
            return stringKey;
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
