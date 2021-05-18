package com.yandex.ydb.jdbc.impl;

import java.sql.Array;
import java.sql.ResultSet;
import java.util.Map;

class ArrayImpl implements Array {

    @Override
    public String getBaseTypeName() {
        return "Test Array";
    }

    @Override
    public int getBaseType() {
        return 0;
    }

    @Override
    public Object getArray() {
        return new Object[0];
    }

    @Override
    public Object getArray(Map<String, Class<?>> map) {
        return new Object[0];
    }

    @Override
    public Object getArray(long index, int count) {
        return new Object[0];
    }

    @Override
    public Object getArray(long index, int count, Map<String, Class<?>> map) {
        return new Object[0];
    }

    @Override
    public ResultSet getResultSet() {
        return null;
    }

    @Override
    public ResultSet getResultSet(Map<String, Class<?>> map) {
        return null;
    }

    @Override
    public ResultSet getResultSet(long index, int count) {
        return null;
    }

    @Override
    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) {
        return null;
    }

    @Override
    public void free() {

    }
}
