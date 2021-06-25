package com.yandex.ydb.spring.jpa.data;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.yandex.ydb.spring.jpa.YdbPersistable;

@Entity(name = T2Value.TABLE)
public class T2Value extends YdbPersistable<Integer> {
    public static final String TABLE = "unit_ref_t2";

    @Column(name = "t2_id")
    @Id
    private int id;

    private String value;

    @ManyToOne
    @JoinColumn(name = "t1_id")
    private T1Value t1Value;

    private T2Value() {
        //
    }

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

    public T1Value getT1Value() {
        return t1Value;
    }

    public void setT1Value(T1Value t1Value) {
        this.t1Value = t1Value;
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
        return id == t2Value.id &&
                Objects.equals(value, t2Value.value);
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
