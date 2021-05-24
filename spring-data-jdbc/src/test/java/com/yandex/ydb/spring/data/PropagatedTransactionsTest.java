package com.yandex.ydb.spring.data;

import java.sql.SQLException;

import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
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
        calls.beanActionDefaultRO();
    }

    @Test
    void beanActionDefaultTx() {
        calls.beanActionDefaultTx();
    }

    @Test
    void beanActionDefaultTxRO() {
        calls.beanActionDefaultTxRO();
    }

    @Test
    void beanActionDefaultTxRW() {
        calls.beanActionDefaultTxRW();
    }

    @Test
    void beanActionDefaultTxRWRO() {
        calls.beanActionDefaultTxRWRO();
    }

    @Test
    void beanActionNestedTx() {
        calls.beanActionNestedTx();
    }

    @Test
    void beanActionNestedTxRO() {
        calls.beanActionNestedTxRO();
    }

    @Test
    void beanActionNestedTxRW() {
        calls.beanActionNestedTxRW();
    }

    @Test
    void beanActionNestedTxRWRO() {
        calls.beanActionNestedTxRWRO();
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

    static void checkYdbException(DbActionExecutionException e) {
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
            assertThrowsMsg(DbActionExecutionException.class,
                    () -> actions.beanActionDefault(new SimpleValue(1, "test")),
                    PropagatedTransactionsTest::checkYdbException,
                    "Must not modify table in RO transaction");
        }

        @Transactional
        public void beanActionDefaultTx() {
            actions.beanActionDefaultTx(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionDefaultTxRO() {
            assertThrowsMsg(DbActionExecutionException.class,
                    () -> actions.beanActionDefaultTx(new SimpleValue(1, "test")),
                    PropagatedTransactionsTest::checkYdbException,
                    "Must not modify table in RO transaction");
        }

        @Transactional
        public void beanActionDefaultTxRW() {
            actions.beanActionDefaultTxRW(new SimpleValue(1, "test"));
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionDefaultTxRWRO() {
            assertThrowsMsg(DbActionExecutionException.class,
                    () -> actions.beanActionDefaultTxRW(new SimpleValue(1, "test")),
                    PropagatedTransactionsTest::checkYdbException,
                    "Must not modify table in RO transaction");
        }

        @Transactional
        public void beanActionNestedTx() {
            assertThrowsMsg(NestedTransactionNotSupportedException.class,
                    () -> actions.beanActionNestedTx(new SimpleValue(1, "test")),
                    "Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionNestedTxRO() {
            assertThrowsMsg(NestedTransactionNotSupportedException.class,
                    () -> actions.beanActionNestedTx(new SimpleValue(1, "test")),
                    "Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
        }

        @Transactional
        public void beanActionNestedTxRW() {
            assertThrowsMsg(NestedTransactionNotSupportedException.class,
                    () -> actions.beanActionNestedTxRW(new SimpleValue(1, "test")),
                    "Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
        }

        @Transactional(isolation = Isolation.REPEATABLE_READ)
        public void beanActionNestedTxRWRO() {
            assertThrowsMsg(NestedTransactionNotSupportedException.class,
                    () -> actions.beanActionNestedTxRW(new SimpleValue(1, "test")),
                    "Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
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
