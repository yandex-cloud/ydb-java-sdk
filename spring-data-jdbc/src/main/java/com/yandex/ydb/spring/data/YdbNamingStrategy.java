package com.yandex.ydb.spring.data;

import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.util.Assert;

public class YdbNamingStrategy implements NamingStrategy {
    public static final YdbNamingStrategy INSTANCE = new YdbNamingStrategy();

    @Override
    public String getSchema() {
        return NamingStrategy.super.getSchema();
    }

    @Override
    public String getTableName(Class<?> type) {
        Assert.notNull(type, "Type must not be null.");
        return type.getSimpleName();
    }

    @Override
    public String getColumnName(RelationalPersistentProperty property) {
        Assert.notNull(property, "Property must not be null.");
        return property.getName();
    }

    @Override
    public String getKeyColumn(RelationalPersistentProperty property) {
        return NamingStrategy.super.getKeyColumn(property);
    }
}
