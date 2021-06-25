package com.yandex.ydb.spring.jpa;

import java.sql.SQLException;

import javax.persistence.EntityManager;

import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.spring.jpa.data.SimpleRepository;
import com.yandex.ydb.spring.jpa.data.SimpleValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ContextConfiguration(classes = PropagatedTransactionsTest.TestConfig.class)
public class PropagatedTransactionsTest extends AbstractTest {
    private static final String CREATE_TABLE = stringFileReference("classpath:sql/create_table_simple.sql");

    @Autowired
    RepositoryCalls calls;

    @BeforeEach
    void beforeEach() throws SQLException {
        configureOnce(() -> createTable(SimpleValue.TABLE, CREATE_TABLE));
    }

    @Test
    void beanActionDefault() {
        calls.beanActionDefault();
    }

    @Test
    void beanActionDefaultRO() {
        assertThrowsMsg(Exception.class,
                () -> calls.beanActionDefaultRO(),
                PropagatedTransactionsTest::checkYdbException,
                "Must not modify table in RO transaction");
    }

    @Test
    void beanActionDefaultTx() {
        calls.beanActionDefaultTx();
    }

    @Test
    void beanActionDefaultTxRO() {
        assertThrowsMsg(Exception.class,
                () -> calls.beanActionDefaultTxRO(),
                PropagatedTransactionsTest::checkYdbException,
                "Must not modify table in RO transaction");
    }

    @Test
    void beanActionDefaultTxRW() {
        calls.beanActionDefaultTxRW();
    }

    @Test
    void beanActionDefaultTxRWRO() {
        assertThrowsMsg(Exception.class,
                () -> calls.beanActionDefaultTxRWRO(),
                PropagatedTransactionsTest::checkYdbException,
                "Must not modify table in RO transaction");
    }

    @Test
    void beanActionNestedTx() {
        assertThrowsMsg(RuntimeException.class,
                () -> calls.beanActionNestedTx(),
                "JpaDialect does not support savepoints - check your JPA provider's capabilities");
    }

    @Test
    void beanActionNestedTxRO() {
        assertThrowsMsg(RuntimeException.class,
                () -> calls.beanActionNestedTxRO(),
                "JpaDialect does not support savepoints - check your JPA provider's capabilities");
    }

    @Test
    void beanActionNestedTxRW() {
        assertThrowsMsg(RuntimeException.class,
                () -> calls.beanActionNestedTxRW(),
                "JpaDialect does not support savepoints - check your JPA provider's capabilities");
    }

    @Test
    void beanActionNestedTxRWRO() {
        assertThrowsMsg(NestedTransactionNotSupportedException.class,
                () -> calls.beanActionNestedTxRWRO(),
                "JpaDialect does not support savepoints - check your JPA provider's capabilities");
    }

    @Test
    void beanActionRequiresNew() {
        calls.beanActionRequiresNew();
    }

    @Test
    void beanActionRequiresNewRO() {
        calls.beanActionRequiresNewRO();
    }

    @Test
    void beanActionRequiresNewRW() {
        calls.beanActionRequiresNewRW();
    }

    @Test
    void beanActionRequiresNewRWRO() {
        calls.beanActionRequiresNewRWRO();
    }

    static void checkYdbException(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof YdbNonRetryableException) {
                assertTrue(t.getMessage().contains("Operation 'Upsert' can't be performed in read only transaction "),
                        String.format("Unexpected error message: [%s]", t.getMessage()));
                return;
            }
            t = t.getCause();
        }
        fail("No " + YdbNonRetryableException.class + " exception found");
    }

    //

    @Configuration
    static class TestConfig {
        @Bean
        public RepositoryActions actions() {
            return new RepositoryActions();
        }

        @Bean
        public RepositoryCalls calls() {
            return new RepositoryCalls();
        }
    }

    @Transactional
    static class RepositoryActions {

        @Autowired
        private SimpleRepository repository;

        @Autowired
        EntityManager entityManager;

        public void beanActionDefault(SimpleValue value) {
            repository.save(value);
        }

        @Transactional
        public void beanActionDefaultTx(SimpleValue value) {
            repository.save(value);
        }

        @Transactional(isolation = Isolation.SERIALIZABLE)
        public void beanActionDefaultTxRW(SimpleValue value) {
            repository.save(value);
        }

        @Transactional(propagation = Propagation.NESTED)
        public void beanActionNestedTx(SimpleValue value) {
            repository.save(value);
        }

        @Transactional(propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
        public void beanActionNestedTxRW(SimpleValue value) {
            repository.save(value);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void beanActionRequiresNew(SimpleValue value) {
            repository.save(value);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
        public void beanActionRequiresNewRW(SimpleValue value) {
            repository.save(value);
        }
    }


    static class RepositoryCalls {
        @Autowired
        RepositoryActions actions;

        @Transactional
        public void beanActionDefault() {
            actions.beanActionDefault(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionDefaultRO() {
            actions.beanActionDefault(new SimpleValue(1, "test"));
        }

        @Transactional
        public void beanActionDefaultTx() {
            actions.beanActionDefaultTx(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionDefaultTxRO() {
            actions.beanActionDefaultTx(new SimpleValue(1, "test"));
        }

        @Transactional
        public void beanActionDefaultTxRW() {
            actions.beanActionDefaultTxRW(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionDefaultTxRWRO() {
            actions.beanActionDefaultTxRW(new SimpleValue(1, "test"));
        }

        @Transactional
        public void beanActionNestedTx() {
            actions.beanActionNestedTx(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionNestedTxRO() {
            actions.beanActionNestedTx(new SimpleValue(1, "test"));
        }

        @Transactional
        public void beanActionNestedTxRW() {
            actions.beanActionNestedTxRW(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionNestedTxRWRO() {
            actions.beanActionNestedTxRW(new SimpleValue(1, "test"));
        }

        @Transactional
        public void beanActionRequiresNew() {
            actions.beanActionRequiresNew(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionRequiresNewRO() {
            actions.beanActionRequiresNew(new SimpleValue(1, "test"));
        }

        @Transactional
        public void beanActionRequiresNewRW() {
            actions.beanActionRequiresNewRW(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionRequiresNewRWRO() {
            actions.beanActionRequiresNewRW(new SimpleValue(1, "test"));
        }
    }

}
