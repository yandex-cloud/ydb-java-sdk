package com.yandex.ydb.spring.data.values;

import java.sql.SQLException;
import java.util.Arrays;

import com.yandex.ydb.spring.data.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class T1T2Test extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(T1T2Test.class);

    private static final String CREATE_TABLE_T1 = stringFileReference("classpath:sql/create_table_t1.sql");
    private static final String CREATE_TABLE_T2 = stringFileReference("classpath:sql/create_table_t2.sql");

    @Autowired
    T1Repository t1Repo;

    @Autowired
    T1MultiRepository t1MultiRepo;

    @Autowired
    T2Repository t2Repo;

    @BeforeEach
    void beforeEach() throws SQLException {
        configureOnce(() -> {
            createTable(T1Value.TABLE, CREATE_TABLE_T1);
            createTable(T2Value.TABLE, CREATE_TABLE_T2);
        });
        executeRW(() -> t1Repo.deleteAll());
        executeRW(() -> t2Repo.deleteAll());
    }

    @Test
    void saveReference() {

        executeRO(() -> {
            assertEquals(Arrays.asList(), t1Repo.findAll());
            assertEquals(Arrays.asList(), t2Repo.findAll());
        });

        T2Value t2 = new T2Value(2, "t2");
        T1Value t1 = new T1Value(1, t2, "t1");

        executeRW(() -> t1Repo.save(t1));

        executeRO(() -> {
            assertEquals(Arrays.asList(t1), t1Repo.findAll());
            assertEquals(Arrays.asList(t2), t2Repo.findAll());
        });

    }

    @Test
    void saveReferenceMulti() {
        T2Value t10 = new T2Value(10, "t10");
        T2Value t11 = new T2Value(11, "t11");

        T1MultiValue t1 = new T1MultiValue(1, set(t10, t11), "t1");

        T2Value t20 = new T2Value(20, "t20");
        T2Value t21 = new T2Value(21, "t21");
        T1MultiValue t2 = new T1MultiValue(2, set(t20, t21), "t1");
        executeRW(() -> t1MultiRepo.saveAll(Arrays.asList(t1, t2)));

        executeRO(() -> {
            LOGGER.info("Read t1...");
            assertEquals(t1, t1MultiRepo.findById(t1.getId()).orElse(null));

            LOGGER.info("Read t2...");
            assertEquals(t2, t1MultiRepo.findById(t2.getId()).orElse(null));

            LOGGER.info("Read unknown...");
            assertNull(t1MultiRepo.findById(335).orElse(null));

            LOGGER.info("Read All...");
            assertEquals(Arrays.asList(t1, t2), t1MultiRepo.findAll());

            LOGGER.info("Read Dependencies...");
            assertEquals(Arrays.asList(t10, t11, t20, t21), t2Repo.findAll());
        });

    }
}
