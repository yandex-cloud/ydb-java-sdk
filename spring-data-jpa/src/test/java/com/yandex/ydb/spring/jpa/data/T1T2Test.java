package com.yandex.ydb.spring.jpa.data;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.yandex.ydb.spring.jpa.YdbJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;

@Disabled("Does not work proper")
public class T1T2Test extends AbstractValueTest<T1Value, Integer> {
    private static final String CREATE_TABLE_T1 = stringFileReference("classpath:sql/create_table_t1.sql");
    private static final String CREATE_TABLE_T2 = stringFileReference("classpath:sql/create_table_t2.sql");

    @Autowired
    T1Repository t1Repo;

    @Autowired
    T2Repository t2Repo;

    @Override
    @BeforeEach
    void beforeEach() throws SQLException {
        this.repository = getRepository();
        configureOnce(() -> {
            createTable(getTableName(), getCreateTableSql());
            createTable(T2Value.TABLE, CREATE_TABLE_T2);
        });
        executeRW(() -> repository.deleteAll());
    }

    @Override
    protected void save(T1Value rec) {
        super.save(rec);
        t2Repo.saveAll(rec.getT2Value());
    }

    @Override
    protected void saveAll(List<T1Value> rec) {
        super.saveAll(rec);
        t2Repo.saveAll(rec.stream()
                .map(T1Value::getT2Value)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    @Override
    protected String getTableName() {
        return T1Value.TABLE;
    }

    @Override
    protected String getCreateTableSql() {
        return CREATE_TABLE_T1;
    }

    @Override
    protected Integer generateKey() {
        return nextIndex();
    }

    @Override
    protected T1Value generateValue(Integer key) {
        return new T1Value(key,
                Arrays.asList(
                        new T2Value(nextIndex(), nextString()),
                        new T2Value(nextIndex(), nextString())),
                nextString());
    }

    @Override
    protected void updateValue(T1Value value) {
        value.setT2Value(
                Arrays.asList(
                        new T2Value(nextIndex(), nextString()),
                        new T2Value(nextIndex(), nextString()),
                        new T2Value(nextIndex(), nextString())));
        value.setValue(nextString());
    }

    @Override
    protected YdbJpaRepository<T1Value, Integer> getRepository() {
        return t1Repo;
    }

    @Override
    protected String getSortByColumn() {
        return "value";
    }
}
