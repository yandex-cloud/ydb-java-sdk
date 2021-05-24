package com.yandex.ydb.spring;

import java.sql.SQLException;
import java.util.function.Consumer;

import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.spring.data.YdbDaoRuntimeException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

public class SpringTestHelper {

    public static void executeConn(PlatformTransactionManager transactionManager, TestHelper.SQLRun sqlRun) {
        executeRW(transactionManager, status -> {
            ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResourceMap().values()
                    .iterator()
                    .next();
            try {
                sqlRun.run((YdbConnection) holder.getConnection());
            } catch (SQLException e) {
                throw new YdbDaoRuntimeException("Unable to execute inside a transaction", e);
            }
        });
    }

    public static void executeRW(PlatformTransactionManager transactionManager, Consumer<TransactionStatus> action) {
        new TransactionTemplate(transactionManager).execute(status -> {
            action.accept(status);
            return null;
        });
    }

    public static void executeRO(PlatformTransactionManager transactionManager, Consumer<TransactionStatus> action) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(YdbConst.ONLINE_CONSISTENT_READ_ONLY);
        template.execute(status -> {
            action.accept(status);
            return null;
        });
    }

}
