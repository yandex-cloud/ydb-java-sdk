package com.yandex.ydb.jdbc.impl;

import java.sql.Ref;
import java.util.Map;

class RefImpl implements Ref {

    @Override
    public String getBaseTypeName() {
        return "Test Ref";
    }

    @Override
    public Object getObject(Map<String, Class<?>> map) {
        return new Object();
    }

    @Override
    public Object getObject() {
        return new Object();
    }

    @Override
    public void setObject(Object value) {

    }
}
