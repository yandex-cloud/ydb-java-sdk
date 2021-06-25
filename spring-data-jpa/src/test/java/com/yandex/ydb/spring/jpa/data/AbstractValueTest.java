package com.yandex.ydb.spring.jpa.data;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import com.yandex.ydb.spring.jpa.AbstractTest;
import com.yandex.ydb.spring.jpa.YdbJpaRepository;
import com.yandex.ydb.spring.jpa.YdbPersistable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public abstract class AbstractValueTest<T extends YdbPersistable<K>, K> extends AbstractTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    YdbJpaRepository<T, K> repository;

    @Autowired
    EntityManager entityManager;

    private int index = 0;

    @BeforeEach
    void beforeEach() throws SQLException {
        this.repository = getRepository();

        configureOnce(() -> createTable(getTableName(), getCreateTableSql()));
        executeRW(() -> repository.deleteAll());
    }
    //

    @Test
    void testCrud() {

        K key = generateKey();
        T rec = generateValue(key);

        logger.info("save rec");
        executeRW(() -> save(rec));

        executeRO(() -> {
            logger.info("find by unknown id");
            assertNull(repository.findById(generateKey()).orElse(null));

            logger.info("find by known id");
            T actual = repository.findById(key).orElse(null);
            assertNotSame(rec, actual);
            assertEquals(rec, actual);

            logger.info("get count");
            assertEquals(1, repository.count());

            logger.info("find all by single key");
            assertEquals(Arrays.asList(rec), repository.findAllById(Arrays.asList(key)));

            logger.info("find all by keys");
            assertEquals(Arrays.asList(rec), repository.findAllById(Arrays.asList(
                    generateKey(),
                    generateKey(),
                    generateKey(),
                    key)));
        });

        T rec2 = generateValue(key);
        logger.info("save rec2");
        executeRW(() -> save(rec2));

        executeRO(() -> {
            logger.info("find by known id");
            T actual = repository.findById(key).orElse(null);
            assertNotSame(rec2, actual);
            assertEquals(rec2, actual);

            logger.info("find all");
            assertEquals(Arrays.asList(rec2), repository.findAll());
        });

        logger.info("delete by key");
        executeRW(() -> repository.deleteById(key));
        executeRO(() -> {
            logger.info("find by known id, empty");
            assertEquals(0, repository.count());
            assertNull(repository.findById(key).orElse(null));
        });

        logger.info("save rec2");
        executeRW(() -> save(rec2));
        executeRO(() -> {
            logger.info("find by known id");
            T actual = repository.findById(key).orElse(null);
            assertNotSame(rec2, actual);
            assertEquals(rec2, actual);
        });

        logger.info("delete by rec2");
        executeRW(() -> repository.delete(rec2));
        executeRO(() -> {
            logger.info("find by known id, empty");
            assertEquals(0, repository.count());
            assertNull(repository.findById(key).orElse(null));
        });

        logger.info("save rec2");
        executeRW(() -> save(rec2));
        executeRO(() -> {
            logger.info("find by known id");
            T actual = repository.findById(key).orElse(null);
            assertNotSame(rec2, actual);
            assertEquals(rec2, actual);
        });

        logger.info("delete all");
        executeRW(() -> repository.deleteAll());
        executeRO(() -> {
            logger.info("find by known id, empty");
            assertEquals(0, repository.count());
            assertNull(repository.findById(key).orElse(null));
        });
    }

    @Test
    void crudMulti() {

        K key1 = generateKey();
        T rec1 = generateValue(key1);

        K key2 = generateKey();
        T rec2 = generateValue(key2);

        executeRW(() -> saveAll(Arrays.asList(rec1, rec2)));

        executeRO(() -> {
            assertNull(repository.findById(generateKey()).orElse(null));
            T actual = repository.findById(key1).orElse(null);
            assertNotSame(rec1, actual);
            assertEquals(rec1, actual);

            assertEquals(2, repository.count());
            assertEquals(Arrays.asList(rec1), repository.findAllById(Arrays.asList(key1)));
            assertEquals(Arrays.asList(rec1, rec2), repository.findAllById(Arrays.asList(
                    key2,
                    generateKey(),
                    generateKey(),
                    key1)));
        });

        T rec3 = generateValue(key1);
        T rec4 = generateValue(key2);
        executeRW(() -> {
            save(rec3);
            save(rec4);
        });

        executeRO(() -> {
            T actual = repository.findById(key1).orElse(null);
            assertNotEquals(rec1, actual);
            assertNotSame(rec3, actual);
            assertEquals(rec3, actual);

            String sortByColumn = getSortByColumn();
            assertEquals(Arrays.asList(rec3, rec4), repository.findAll());
            assertEquals(Arrays.asList(rec4, rec3), repository.findAll(Sort.by(Sort.Direction.DESC, sortByColumn)));
            assertEquals(Arrays.asList(rec3, rec4), repository.findAll(Sort.by(sortByColumn)));
        });

        executeRW(() -> repository.delete(rec1));
        executeRO(() -> {
            assertEquals(1, repository.count());
            assertNull(repository.findById(key1).orElse(null));
            assertEquals(rec4, repository.findById(key2).orElse(null));
        });

        executeRW(() -> repository.deleteAll(Arrays.asList(rec4)));
        executeRO(() -> {
            assertEquals(0, repository.count());
            assertNull(repository.findById(key2).orElse(null));
        });

        executeRW(() -> save(rec1));
        executeRW(() -> save(rec2));
        executeRO(() -> {
            assertEquals(rec1, repository.findById(key1).orElse(null));
            assertEquals(rec2, repository.findById(key2).orElse(null));
        });

        executeRW(() -> repository.deleteById(key1));
        executeRO(() -> {
            assertNull(repository.findById(key1).orElse(null));
            assertEquals(rec2, repository.findById(key2).orElse(null));
        });

        //

        executeRW(() -> repository.deleteAllById(Arrays.asList(key2)));
        executeRO(() -> {
            assertNull(repository.findById(key1).orElse(null));
            assertNull(repository.findById(key2).orElse(null));
        });
    }

    @Test
    void testSaveNewAndGet() {
        executeRW(() -> {
            K key = generateKey();
            T rec = generateValue(key);
            logger.info("Find by key");
            assertNull(repository.findById(key).orElse(null));

            logger.info("Save rec");
            save(rec);

            logger.info("Find by key");
            T value = repository.findById(key).orElse(null);
            assertSame(rec, value);

            logger.info("Save rec again");
            save(rec);

            logger.info("Find by key");
            value = repository.findById(key).orElse(null);
            assertSame(rec, value);
        });
    }

    @Test
    void testUpdateExistingAndGet() {
        K key = generateKey();
        T rec = generateValue(key);
        executeRW(() -> {
            logger.info("Save rec");
            save(rec);
        });

        executeRW(() -> {
            logger.info("Find by key");
            T value = repository.findById(key).orElse(null);
            assertNotSame(rec, value);
            assertEquals(rec, value);

            logger.info("Save rec");
            save(rec);

            logger.info("Find by key");
            value = repository.findById(key).orElse(null);
            assertNotSame(rec, value);
            assertEquals(rec, value);
        });
    }

    @Test
    void testSaveExistingAndGet() {
        K key = generateKey();
        T rec = generateValue(key);
        executeRW(() -> {
            logger.info("Save rec");
            save(rec);
        });

        executeRW(() -> {
            logger.info("Find by key");
            T value = repository.findById(key).orElse(null);
            assertNotSame(rec, value);
            assertEquals(rec, value);

            entityManager.detach(value); // THIS IS REQUIRED, OTHERWISE we'll face issues

            T rec2 = generateValue(key);
            assertNotEquals(rec, rec2);
            logger.info("Save rec2");
            save(rec2);

            logger.info("Find by key");
            value = repository.findById(key).orElse(null);
            assertSame(rec2, value);
        });
    }

    @Test
    void testSaveNewThenUpdate() {
        K key = generateKey();
        T rec = generateValue(key);

        executeRW(() -> save(rec));

        executeRW(() -> {
            logger.info("Find by key");
            T value = repository.findById(key).orElse(null);
            assertNotSame(rec, value);
            assertEquals(rec, value);

            String s1 = value.toString();
            logger.info("Before: {}", s1);

            updateValue(value);
            String s2 = value.toString();
            logger.info("After: {}", s2);
            assertNotEquals(s1, s2);

            logger.info("Save rec");
            save(value); // same object

            logger.info("Find by key");
            T value2 = repository.findById(key).orElse(null);
            assertSame(value, value2);
        });
    }


    //

    protected int nextIndex() {
        return ++index;
    }

    protected String nextString() {
        return nextString(nextIndex());
    }

    protected String nextString(int nextIndex) {
        return String.format("value-%06d", nextIndex);
    }

    //

    protected void save(T rec) {
        repository.save(rec);
    }

    protected void saveAll(List<T> rec) {
        repository.saveAll(rec);
    }

    //

    protected abstract String getTableName();

    protected abstract String getCreateTableSql();

    protected abstract K generateKey();

    protected abstract T generateValue(K key);

    protected abstract void updateValue(T value);

    protected abstract YdbJpaRepository<T, K> getRepository();

    protected abstract String getSortByColumn();
}
