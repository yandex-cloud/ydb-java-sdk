package com.yandex.ydb.spring.data.values;

import java.util.Objects;

import com.yandex.ydb.spring.data.YdbPersistable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(T2Value.TABLE)
public class T2Value implements YdbPersistable<Integer> {
    public static final String TABLE = "unit_ref_t2";

    @Column("t2_id")
    @Id
    private final int id;

    private final String value;

    public T2Value(int id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof T2Value)) {
            return false;
        }
        T2Value t2Value = (T2Value) o;
        return id == t2Value.id && Objects.equals(value, t2Value.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return "T2Value{" +
                "id=" + id +
                ", value='" + value + '\'' +
                '}';
    }
}
