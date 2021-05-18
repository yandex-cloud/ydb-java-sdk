package com.yandex.ydb.jdbc.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLXML;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

public class SQLXMLImpl implements SQLXML {

    @Override
    public void free() {
    }

    @Override
    public InputStream getBinaryStream() {
        return null;
    }

    @Override
    public OutputStream setBinaryStream() {
        return null;
    }

    @Override
    public Reader getCharacterStream() {
        return null;
    }

    @Override
    public Writer setCharacterStream() {
        return null;
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public void setString(String value) {

    }

    @Override
    public <T extends Source> T getSource(Class<T> sourceClass) {
        return null;
    }

    @Override
    public <T extends Result> T setResult(Class<T> resultClass) {
        return null;
    }
}
