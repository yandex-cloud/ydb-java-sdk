package com.yandex.ydb.spring.data.values;

import java.sql.SQLException;

import com.yandex.ydb.spring.data.AbstractTest;
import com.yandex.ydb.spring.data.YdbDaoRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;

import static com.yandex.ydb.jdbc.TestHelper.stringFileReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComplexKeyValueTest extends AbstractTest {

    private static final String CREATE_TABLE = stringFileReference("classpath:sql/create_table_complex_key.sql");

    @Autowired
    ComplexKeyRepository repository;

    @BeforeEach
    void beforeEach() throws SQLException {
        configureOnce(() -> createTable(ComplexKeyValue.TABLE, CREATE_TABLE));
        executeRW(() -> repository.deleteAll());
    }

    @Test
    void crudSingle() {

        ComplexKeyValue rec = new ComplexKeyValue();
        rec.key = new ComplexKeyValue.Key(1, "key");
        rec.value = "string value";

        DbActionExecutionException e = assertThrows(DbActionExecutionException.class,
                () -> executeRW(() -> repository.save(rec)));
        Throwable cause = e.getCause();
        assertNotNull(cause);
        assertEquals(YdbDaoRuntimeException.class, cause.getClass());
        assertEquals("Unable to detect parameter type: [intKey]", cause.getMessage());

    }
}
