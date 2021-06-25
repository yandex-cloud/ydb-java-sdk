package com.yandex.ydb.spring.jpa;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class YdbJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
        implements YdbJpaRepository<T, ID> {

    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager entityManager;
    private final boolean simplifiedDeletes;

    public YdbJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;

        EntityType<T> entity = entityManager.getMetamodel().entity(entityInformation.getJavaType());
        this.simplifiedDeletes = entity.getDeclaredPluralAttributes().isEmpty();
    }

    @Override
    public void deleteAll() {
        if (simplifiedDeletes) {
            entityManager.createQuery("delete from " + getDomainClass().getName()).executeUpdate();
        } else {
            deleteAll(findAll());
        }
    }
}
