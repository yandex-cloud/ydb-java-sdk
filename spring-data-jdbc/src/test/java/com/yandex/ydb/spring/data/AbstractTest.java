package com.yandex.ydb.spring.data;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.spring.SpringTestHelper;
import com.yandex.ydb.spring.TestApplicationConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

@Rollback(false)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestApplicationConfig.class)
public abstract class AbstractTest {

    @Autowired
    protected PlatformTransactionManager transactionManager;

    protected void executeRW(Runnable action) {
        SpringTestHelper.executeRW(transactionManager, status -> action.run());
    }

    protected void executeRO(Runnable action) {
        SpringTestHelper.executeRO(transactionManager, status -> action.run());
    }

    protected void createTable(String tableName, String sql) {
        SpringTestHelper.executeConn(transactionManager, connection ->
                TestHelper.initTable(connection, tableName, sql));
    }

    protected void configureOnce(TestHelper.SQLSimpleRun run) throws SQLException {
        TestHelper.configureOnce(this.getClass(), run);
    }

    protected static <T> Set<T> set(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
