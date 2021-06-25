package com.yandex.ydb.spring.jpa.data;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.yandex.ydb.spring.jpa.YdbPersistable;


@Entity(name = T1Value.TABLE)
public class T1Value extends YdbPersistable<Integer> {
    public static final String TABLE = "unit_ref_t1";

    @Column(name = "t1_id")
    @Id
    private int id;

    @OneToMany(mappedBy = "t1Value")
    private List<T2Value> t2Value;

    private String value;

    private T1Value() {
    }

    public T1Value(int id, List<T2Value> t2Value, String value) {
        this.id = id;
        this.t2Value = t2Value;
        this.value = value;
        this.setT2Value(t2Value);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public List<T2Value> getT2Value() {
        return t2Value;
    }

    public String getValue() {
        return value;
    }

    public void setT2Value(List<T2Value> t2Value) {
        t2Value.forEach(t2 -> t2.setT1Value(this));
        this.t2Value = t2Value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof T1Value)) {
            return false;
        }
        T1Value t1Value = (T1Value) o;
        if (id == t1Value.id && Objects.equals(value, t1Value.value)) {
            if (t2Value.size() != t1Value.t2Value.size()) {
                return false;
            }
            for (int i = 0; i < t2Value.size(); i++) {
                if (!Objects.equals(t2Value.get(i), t1Value.t2Value.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
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
