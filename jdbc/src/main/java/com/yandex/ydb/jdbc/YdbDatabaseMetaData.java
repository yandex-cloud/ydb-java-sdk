package com.yandex.ydb.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public interface YdbDatabaseMetaData extends DatabaseMetaData {

    String QUOTE_IDENTIFIER = "`";

    @Override
    YdbConnection getConnection() throws SQLException;
}
