package com.yandex.ydb.spring.data.values;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(AllColumnsValue.TABLE)
public class AllColumnsStringValue {
    @Id
    private final int id;

    private final String stringValue; // Types.VARCHAR -> Utf8

    public AllColumnsStringValue(int id, String stringValue) {
        this.id = id;
        this.stringValue = stringValue;
    }

    public int getId() {
        return id;
    }

    public String getStringValue() {
        return stringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AllColumnsStringValue)) {
            return false;
        }
        AllColumnsStringValue that = (AllColumnsStringValue) o;
        return id == that.id &&
                Objects.equals(stringValue, that.stringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stringValue);
    }

    @Override
    public String toString() {
        return "StringColumnValue{" +
                "id=" + id +
                ", stringValue='" + stringValue + '\'' +
                '}';
    }


}
