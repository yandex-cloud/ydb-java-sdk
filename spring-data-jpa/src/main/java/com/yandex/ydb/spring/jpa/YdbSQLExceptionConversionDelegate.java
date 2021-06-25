package com.yandex.ydb.spring.jpa;

import java.sql.SQLException;

import org.hibernate.JDBCException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;

public class YdbSQLExceptionConversionDelegate implements SQLExceptionConversionDelegate {
    @Override
    public JDBCException convert(SQLException sqlException, String message, String sql) {
        return new GenericJDBCException(message, sqlException, sql);
    }
}
