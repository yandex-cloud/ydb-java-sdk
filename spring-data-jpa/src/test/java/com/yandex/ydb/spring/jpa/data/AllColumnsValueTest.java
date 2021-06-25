package com.yandex.ydb.spring.jpa.data;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;

import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.spring.jpa.YdbJpaRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class AllColumnsValueTest extends AbstractValueTest<AllColumnsValue, Integer> {
    private static final String CREATE_TABLE = stringFileReference("classpath:sql/create_table_all_columns.sql");

    @Autowired
    AllColumnsRepository repository;


    @Disabled("Does not work without batches: " +
            "Multiple modification of table with secondary indexes is not supported yet")
    @Override
    void crudMulti() {
        super.crudMulti();
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
        rec.dateValue = new Timestamp(System.currentTimeMillis());
//        rec.localDateValue = LocalDate.of(1971, 3, 5); TODO: support
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
        executeRO(() -> {
            logger.info("Custom query");
            assertEquals(Arrays.asList(), repository.findByCustomQuery("%username%"));

            logger.info("Custom query with index native");
            assertEquals(Arrays.asList(), repository.findByCustomQueryIndexNative("my-username"));
        });

        AllColumnsValue rec = generateValue(generateKey());
        rec.stringValue = "my-username";

        AllColumnsValue rec2 = generateValue(generateKey());
        executeRW(() -> repository.save(rec));
        executeRW(() -> repository.save(rec2));

        executeRO(() -> {
            logger.info("Custom query");
            assertEquals(Arrays.asList(rec), repository.findByCustomQuery("%username%"));

            logger.info("Custom query with index native");
            assertEquals(Arrays.asList(rec), repository.findByCustomQueryIndexNative("my-username"));

            logger.info("Custom query, not");
            assertEquals(Arrays.asList(), repository.findByCustomQuery("%not-username%"));

            logger.info("Custom query with index native, not");
            assertEquals(Arrays.asList(), repository.findByCustomQueryIndexNative("not-my-username"));
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

    @Override
    protected String getTableName() {
        return AllColumnsValue.TABLE;
    }

    @Override
    protected String getCreateTableSql() {
        return CREATE_TABLE;
    }

    @Override
    protected Integer generateKey() {
        return nextIndex();
    }

    @Override
    protected AllColumnsValue generateValue(Integer key) {
        AllColumnsValue value = new AllColumnsValue();
        value.id = key;
        value.stringValue = nextString();
        return value;
    }

    @Override
    protected void updateValue(AllColumnsValue value) {
        value.stringValue = nextString();
    }

    @Override
    protected YdbJpaRepository<AllColumnsValue, Integer> getRepository() {
        return repository;
    }

    @Override
    protected String getSortByColumn() {
        return "stringValue";
    }
}
