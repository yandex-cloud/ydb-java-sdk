package com.yandex.ydb.spring.data.values;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.spring.data.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;


public class AllColumnsValueTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllColumnsValueTest.class);
    private static final String CREATE_TABLE = stringFileReference("classpath:sql/create_table_all_columns.sql");

    @Autowired
    AllColumnsRepository repository;

    @Autowired
    AllColumnsRepositoryViewString repositoryViewString;

    @BeforeEach
    void beforeEach() throws SQLException {
        configureOnce(() -> createTable(AllColumnsValue.TABLE, CREATE_TABLE));
        executeRW(() -> repository.deleteAll());
    }

    @Test
    void crudSingle() {

        AllColumnsValue rec = new AllColumnsValue();
        rec.id = 1;
        rec.stringValue = "example-1";

        executeRW(() -> repository.save(rec));
        executeRO(() -> {
            assertNull(repository.findById(3).orElse(null));
            AllColumnsValue actual = repository.findById(rec.id).orElse(null);
            assertNotSame(rec, actual);
            assertEquals(rec, actual);

            assertEquals(1, repository.count());
            assertEquals(Arrays.asList(rec), repository.findAllById(Arrays.asList(rec.id)));
            assertEquals(Arrays.asList(rec), repository.findAllById(Arrays.asList(2, 3, 4, rec.id)));
        });

        rec.stringValue = "example-1-1";
        executeRW(() -> repository.save(rec));

        executeRO(() -> {
            AllColumnsValue actual = repository.findById(rec.id).orElse(null);
            assertNotSame(rec, actual);
            assertEquals(rec, actual);

            assertEquals(Arrays.asList(rec), repository.findAll());
        });

        executeRW(() -> repository.deleteAll());
        executeRO(() -> {
            assertEquals(0, repository.count());
            assertNull(repository.findById(rec.id).orElse(null));
        });
    }

    @Test
    void crudMulti() {

        AllColumnsValue rec1 = new AllColumnsValue();
        rec1.id = 1;
        rec1.stringValue = "example-1";

        AllColumnsValue rec2 = new AllColumnsValue();
        rec2.id = 2;
        rec2.stringValue = "example-2";

        // TODO: multiple upsert with indexes is not supported
        // executeRW(() -> repository.saveAll(Arrays.asList(rec1, rec2)));
        executeRW(() -> repository.save(rec1));
        executeRW(() -> repository.save(rec2));

        executeRO(() -> {
            assertNull(repository.findById(3).orElse(null));
            AllColumnsValue actual = repository.findById(rec1.id).orElse(null);
            assertNotSame(rec1, actual);
            assertEquals(rec1, actual);

            assertEquals(2, repository.count());
            assertEquals(Arrays.asList(rec1), repository.findAllById(Arrays.asList(rec1.id)));
            assertEquals(Arrays.asList(rec2, rec1), repository.findAllById(Arrays.asList(rec2.id, 3, 4, rec1.id)));

            LOGGER.info("findViewByIdIn...");
            assertEquals(Arrays.asList(), repositoryViewString.findViewByIdIn());
            assertEquals(Arrays.asList(
                    new AllColumnsStringValue(rec1.id, rec1.stringValue)),
                    repositoryViewString.findViewByIdIn(rec1.id));
            assertEquals(Arrays.asList(
                    new AllColumnsStringValue(rec1.id, rec1.stringValue),
                    new AllColumnsStringValue(rec2.id, rec2.stringValue)),
                    repositoryViewString.findViewByIdIn(rec1.id, rec2.id));
        });

        rec1.stringValue = "example-1-1";
        executeRW(() -> repository.save(rec1));

        executeRO(() -> {
            AllColumnsValue actual = repository.findById(rec1.id).orElse(null);
            assertNotSame(rec1, actual);
            assertEquals(rec1, actual);

            assertEquals(Arrays.asList(rec1, rec2), repository.findAll());
            assertEquals(Arrays.asList(rec2, rec1), repository.findAll(Sort.by(Sort.Direction.DESC, "stringValue")));
            assertEquals(Arrays.asList(rec1, rec2), repository.findAll(Sort.by("stringValue")));
        });

        executeRW(() -> repository.delete(rec1));
        executeRO(() -> {
            assertEquals(1, repository.count());
            assertNull(repository.findById(rec1.id).orElse(null));
            assertEquals(rec2, repository.findById(rec2.id).orElse(null));
        });

        executeRW(() -> repository.deleteAll(Arrays.asList(rec2)));
        executeRO(() -> {
            assertEquals(0, repository.count());
            assertNull(repository.findById(rec2.id).orElse(null));
        });
    }

    @Test
    void crudAllColumns() {
        AllColumnsValue rec = new AllColumnsValue();
        rec.id = 996;
        rec.someColumn = "some-string";
        rec.booleanValue = false;
        rec.booleanValueOpt = true;
        rec.byteValue = 1;
        rec.byteValueOpt = (byte) 0;
        rec.shortValue = 1;
        rec.shortValueOpt = (short) 0;
        rec.intValue = 1;
        rec.intValueOpt = 0;
        rec.uintValue = 2;
        rec.longValue = 1;
        rec.longValueOpt = 1L;
        rec.floatValue = 1.0f;
        rec.floatValueOpt = 0.0f;
        rec.doubleValue = 1.0;
        rec.doubleValueOpt = 0.0;
        rec.stringValue = "string";
        rec.stringValueAsString = "string-as-string";
        rec.bigIntegerValue = new BigInteger("335");
        rec.bigDecimalValue = YdbTypes.DEFAULT_DECIMAL_TYPE.newValue("336.1").toBigDecimal(); // TODO: Looks weird
        rec.bytesValue = "bytes".getBytes();
        rec.dateValue = new Date(System.currentTimeMillis());
        rec.localDateValue = LocalDate.of(1971, 3, 5);
        rec.localDateTimeValue = LocalDateTime.of(1972, 4, 6, 12, 13, 14);
        rec.enumValue = AllColumnsValue.CustomEnum.Second;
        rec.instantValue = Instant.parse("1970-01-01T00:00:03.111Z");
        executeRW(() -> repository.save(rec));
        executeRO(() -> {
            AllColumnsValue actual = repository.findById(rec.id).orElse(null);
            assertEquals(rec, actual);
        });

    }

    @Test
    void testFindByCustomQuery() {
        executeRW(() -> {
            assertEquals(Arrays.asList(), repository.findByCustomQuery("%username%"));
            assertEquals(Arrays.asList(), repository.findByCustomQueryIndex("my-username"));
        });

        AllColumnsValue rec = new AllColumnsValue();
        rec.id = 1;
        rec.stringValue = "my-username";
        executeRW(() -> repository.save(rec));

        executeRO(() -> {
            assertEquals(Arrays.asList(rec), repository.findByCustomQuery("%username%"));
            assertEquals(Arrays.asList(rec), repository.findByCustomQueryIndex("my-username"));
            assertEquals(Arrays.asList(), repository.findByCustomQuery("%not-username%"));
            assertEquals(Arrays.asList(), repository.findByCustomQueryIndex("not-my-username"));
        });
    }

    @Test
    void testUpdateByCustomQuery() {
        executeRW(() -> repository.updateByCustomQuery("%username%", 4));
        executeRO(() -> assertEquals(Arrays.asList(), repository.findAll()));

        AllColumnsValue rec = new AllColumnsValue();
        rec.id = 1;
        rec.stringValue = "my-username";
        executeRW(() -> repository.save(rec));

        executeRW(() -> repository.updateByCustomQuery("%username%", 4));
        rec.intValue = 4;
        executeRO(() -> assertEquals(rec, repository.findById(rec.id).orElse(null)));

        executeRW(() -> repository.updateByCustomQuery("%not-username%", 5));
        executeRO(() -> assertEquals(rec, repository.findById(rec.id).orElse(null)));
    }
}
