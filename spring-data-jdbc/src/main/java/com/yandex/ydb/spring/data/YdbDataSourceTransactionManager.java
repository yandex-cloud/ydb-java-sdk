package com.yandex.ydb.spring.data;

import java.lang.reflect.UndeclaredThrowableException;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import com.yandex.ydb.jdbc.exception.YdbRetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;

/**
 * Transaction manager implementation with retry support.
 * Support retries in TOP-LEVEL transactions only, recreating the YDB Session underneath
 */
public class YdbDataSourceTransactionManager extends DataSourceTransactionManager
        implements CallbackPreferringPlatformTransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(YdbDataSourceTransactionManager.class);

    private static final int RETRY_COUNT = 10;
    private static final int RETRY_SLEEP_MILLIS = 50;

    // TODO: implement proper retry
    private final int maxRetry = RETRY_COUNT;
    private final int retrySleepMillis = RETRY_SLEEP_MILLIS;

    public YdbDataSourceTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public <T> T execute(@Nullable TransactionDefinition definition, TransactionCallback<T> callback)
            throws TransactionException {

        int retry = 0;
        while (true) {
            TransactionStatus status = this.getTransaction(definition);
            T result;
            try {
                result = callback.doInTransaction(status);
            } catch (RuntimeException | Error ex) {
                try {
                    this.rollback(status);
                } catch (RuntimeException | Error ex2) {
                    LOGGER.error("Unexpected exception during rollback operation", ex2);
                }
                boolean newTransaction = status.isNewTransaction();
                boolean shouldRetry = shouldRetry(ex);
                if (newTransaction && shouldRetry) {
                    retry++;
                    if (retry >= maxRetry) {
                        LOGGER.error("Exception during transaction (out of retries)", ex);
                        throw ex;
                    } else {
                        LOGGER.warn("Handle retryable exception: retry {} out of {}", retry, maxRetry);
                        try {
                            //noinspection BusyWait
                            Thread.sleep(retrySleepMillis);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Transaction retry interrupted", ex);
                        }
                        continue; // ---
                    }
                }
                LOGGER.error("Exception during transaction ({}, {})",
                        newTransaction ? "tx is new" : "tx is not new",
                        shouldRetry ? "should retry" : "should not retry",
                        ex);
                throw ex;
            } catch (Throwable ex) {
                this.rollback(status);
                throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
            }
            if (status.isRollbackOnly()) {
                LOGGER.warn("Transaction marked as roll-back only and cannot be commited");
                this.rollback(status);
            } else {
                this.commit(status);
            }
            return result;
        }
    }

    private boolean shouldRetry(Throwable t) {
        while (t != null) {
            if (t instanceof YdbRetryableException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
