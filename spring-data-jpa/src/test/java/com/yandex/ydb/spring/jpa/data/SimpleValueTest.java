package com.yandex.ydb.spring.jpa.data;

import com.yandex.ydb.spring.jpa.YdbJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;

public class SimpleValueTest extends AbstractValueTest<SimpleValue, Integer> {

    private static final String CREATE_TABLE = stringFileReference("classpath:sql/create_table_simple.sql");

    @Autowired
    SimpleRepository simpleRepository;

    @Override
    protected String getTableName() {
        return SimpleValue.TABLE;
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
    protected SimpleValue generateValue(Integer key) {
        return new SimpleValue(key, nextString());
    }

    @Override
    protected void updateValue(SimpleValue value) {
        value.setValue(nextString());
    }

    @Override
    protected YdbJpaRepository<SimpleValue, Integer> getRepository() {
        return simpleRepository;
    }

    @Override
    protected String getSortByColumn() {
        return "value";
    }
}
