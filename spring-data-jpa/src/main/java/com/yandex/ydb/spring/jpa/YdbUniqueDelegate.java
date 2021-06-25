package com.yandex.ydb.spring.jpa;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

public class YdbUniqueDelegate implements UniqueDelegate {

    @Override
    public String getColumnDefinitionUniquenessFragment(Column column) {
        return "";
    }

    @Override
    public String getTableCreationUniqueConstraintsFragment(Table table) {
        return "";
    }

    @Override
    public String getAlterTableToAddUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata) {
        throw new UnsupportedOperationException("No add unique key syntax supported by " + getClass().getName());
    }

    @Override
    public String getAlterTableToDropUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata) {
        throw new UnsupportedOperationException("No drop unique key syntax supported by " + getClass().getName());
    }
}
