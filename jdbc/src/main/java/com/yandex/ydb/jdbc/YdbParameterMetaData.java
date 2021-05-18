package com.yandex.ydb.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;

public interface YdbParameterMetaData extends ParameterMetaData {

    /**
     * Returns parameter index by it's name, could be useful sometimes; basically because in YDB you should always
     * work with names (columns or parameters), indexes are just internal details
     *
     * @param parameterName column name to find
     * @return parameter (1..N)
     * @throws SQLException if parameter is unknown
     */
    int getParameterIndex(String parameterName) throws SQLException;

    /**
     * Returns parameter name by it's index
     *
     * @param param parameter (1..N)
     * @return parameter name
     * @throws SQLException if parameter index is invalid
     */
    String getParameterName(int param) throws SQLException;
}
