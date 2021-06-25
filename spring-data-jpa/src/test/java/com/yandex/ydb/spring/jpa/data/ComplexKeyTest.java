package com.yandex.ydb.spring.jpa.data;

import com.yandex.ydb.spring.jpa.YdbJpaRepository;
import com.yandex.ydb.spring.jpa.data.ComplexKeyValue.Key;
import org.springframework.beans.factory.annotation.Autowired;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;

public class ComplexKeyTest extends AbstractValueTest<ComplexKeyValue, Key> {

    private static final String CREATE_TABLE = stringFileReference("classpath:sql/create_table_complex_key.sql");

    @Autowired
    ComplexKeyRepository repository;

    @Override
    protected String getTableName() {
        return ComplexKeyValue.TABLE;
    }

    @Override
    protected String getCreateTableSql() {
        return CREATE_TABLE;
    }

    @Override
    protected Key generateKey() {
        int next = nextIndex();
        return new Key(next, nextString(next));
    }

    @Override
    protected ComplexKeyValue generateValue(Key key) {
        return new ComplexKeyValue(key, nextString(), nextString());
    }

    @Override
    protected void updateValue(ComplexKeyValue value) {
        value.setValue1(nextString());
        value.setValue2(nextString());
    }

    @Override
    protected YdbJpaRepository<ComplexKeyValue, Key> getRepository() {
        return repository;
    }

    @Override
    protected String getSortByColumn() {
        return "value1";
    }
}
