package com.yandex.ydb.spring.jpa;

import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Transient;

import org.springframework.data.domain.Persistable;

@MappedSuperclass
public abstract class YdbPersistable<ID> implements Persistable<ID> {

    @Transient
    private boolean persisted;

    @PostLoad
    public void postLoad() {
        this.persisted = true;
    }

    @PostUpdate
    public void postUpdate() {
        this.persisted = true;
    }

    @PostPersist
    public void postPersist() {
        this.persisted = true;
    }

    public void reset() {
        this.persisted = false;
    }

    @Override
    public boolean isNew() {
        return !persisted;
    }
}
