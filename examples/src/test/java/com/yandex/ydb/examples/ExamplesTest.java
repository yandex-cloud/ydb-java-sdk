package com.yandex.ydb.examples;

import com.yandex.ydb.examples.batch_upload.BatchUpload;
import com.yandex.ydb.examples.bulk_upsert.BulkUpsert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexandr Gorshenin
 */
public class ExamplesTest {
    private static final Logger log = LoggerFactory.getLogger(ExamplesTest.class);

    private static String[] args = new String[] {};
    private static YdbDockerContainer container = null;

    @BeforeAll
    public static void setUpYDB() {
        String ydbDatabase = System.getenv("YDB_DATABASE");
        String ydbEndpoint = System.getenv("YDB_ENDPOINT");

        if (ydbEndpoint != null) {
            log.info("set up reciept YDB instance -e {} -d {}", ydbEndpoint, ydbDatabase);
            args = new String[] {"-e", ydbEndpoint, "-d", ydbDatabase};
        } else {
            log.info("set up YDB docker container");
            container = YdbDockerContainer.createAndStart();
            args = new String[] {"-e", container.nonSecureEndpoint(), "-d", container.database()};
        }
    }

    @AfterAll
    public static void tearDownYDB() {
        if (container != null) {
            log.info("tear down YDB docker container");
            container.stop();
        }
    }

    @Test
    public void testBatchUpload() {
        Assertions.assertEquals(0, BatchUpload.test(args), "Batch upload test");
    }

    @Test
    public void testBulkUpsert() {
        Assertions.assertEquals(0, BulkUpsert.test(args), "Bulk upsert test");
    }
}
