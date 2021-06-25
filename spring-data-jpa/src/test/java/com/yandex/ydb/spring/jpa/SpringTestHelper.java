package com.yandex.ydb.spring.jpa;

import java.sql.SQLException;
import java.util.function.Consumer;

import com.yandex.ydb.jdbc.TestHelper.SQLRun;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbConst;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

public class SpringTestHelper {

    public static void executeConn(PlatformTransactionManager transactionManager, SQLRun sqlRun) {
        executeRW(transactionManager, status -> {
            DefaultTransactionStatus defaultStatus = (DefaultTransactionStatus) status;
            JdbcTransactionObjectSupport txSupport = (JdbcTransactionObjectSupport) defaultStatus.getTransaction();
            ConnectionHolder holder = txSupport.getConnectionHolder();
            try {
                sqlRun.run((YdbConnection) holder.getConnection());
            } catch (SQLException e) {
                throw new RuntimeException("Unable to execute inside a transaction", e);
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
