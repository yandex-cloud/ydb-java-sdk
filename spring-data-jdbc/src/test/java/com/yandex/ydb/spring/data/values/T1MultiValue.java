package com.yandex.ydb.spring.data.values;

import java.util.Objects;
import java.util.Set;

import com.yandex.ydb.spring.data.YdbPersistable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;


@Table(T1Value.TABLE)
public class T1MultiValue implements YdbPersistable<Integer> {

    @Column("t1_id")
    @Id
    private final int id;

    @MappedCollection(idColumn = "t1_id")
    private final Set<T2Value> t2Value;

    private final String value;

    public T1MultiValue(int id, Set<T2Value> t2Value, String value) {
        this.id = id;
        this.t2Value = Objects.requireNonNull(t2Value);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public Set<T2Value> getT2Value() {
        return t2Value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof T1MultiValue)) {
            return false;
        }
        T1MultiValue that = (T1MultiValue) o;
        return id == that.id && Objects.equals(t2Value, that.t2Value) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, t2Value, value);
    }

    @Override
    public String toString() {
        return "T1Value{" +
                "id=" + id +
                ", t2Value=" + t2Value +
                ", value='" + value + '\'' +
                '}';
    }
}
