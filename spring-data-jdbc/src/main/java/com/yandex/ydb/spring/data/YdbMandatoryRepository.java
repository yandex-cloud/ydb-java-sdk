package com.yandex.ydb.spring.data;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
@Transactional(propagation = Propagation.MANDATORY)
public interface YdbMandatoryRepository<T, ID> extends PagingAndSortingRepository<T, ID> {

    @Override
    Iterable<T> findAll();

    @Override
    Iterable<T> findAll(Sort sort);

    @Override
    Page<T> findAll(Pageable pageable);

    @Override
    Optional<T> findById(ID integer);

    @Override
    boolean existsById(ID integer);

    @Override
    Iterable<T> findAllById(Iterable<ID> integers);

    @Override
    long count();

    @Override
    <S extends T> S save(S entity);

    @Override
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    @Override
    void delete(T entity);

    @Override
    void deleteAll();

    @Override
    void deleteAll(Iterable<? extends T> entities);

    @Override
    void deleteById(ID integer);
}
