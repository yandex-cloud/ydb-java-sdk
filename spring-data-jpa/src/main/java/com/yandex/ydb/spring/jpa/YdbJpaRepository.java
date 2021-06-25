package com.yandex.ydb.spring.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
@Transactional(propagation = Propagation.MANDATORY)
public interface YdbJpaRepository<T, ID> extends JpaRepository<T, ID> {

}

