package com.yandex.ydb.jdbc;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.yandex.ydb.table.values.DecimalType;
import com.yandex.ydb.table.values.Type;

public interface YdbTypes {
    DecimalType DEFAULT_DECIMAL_TYPE =
            DecimalType.of(YdbConst.SQL_DECIMAL_DEFAULT_PRECISION, YdbConst.SQL_DECIMAL_DEFAULT_SCALE);

    /**
     * Converts given Java class to sqlType.
     * <p>
     * WARNING - this conversion does not support all YDB types and may lost actual type
     *
     * @param type java class to convert
     * @return sqlType
     */
    int toWrappedSqlType(Class<?> type);

    /**
     * Converts given Java class to YDB type
     *
     * @param type java class to convert
     * @return YDB type
     */
    Type toYdbType(Class<?> type);

    /**
     * Converts given YDB type to custom (YDB-driver specific) sqlType, preserve original type if possible
     *
     * @param type complete YDB type to convert
     * @return sqlType
     */
    int wrapYdbJdbcType(Type type);

    /**
     * Converts given sqlType to standard JDBC type, pair operation for #wrapYdbJdbcType but still may lost actual type
     *
     * @param sqlType probably customized sql type
     * @return standard JDBC type
     */
    int unwrapYdbJdbcType(int sqlType);

    /**
     * Converts given sql type to YDB type
     *
     * @param sqlType sql type to convert
     * @return YDB type or null, of sqlType cannot be converted
     */
    @Nullable
    Type toYdbType(int sqlType);

    /**
     * Converts given YDB type name to YDB type
     *
     * @param typeName type name to convert
     * @return YDB type or null, of YDB type cannot be converted
     */
    @Nullable
    Type toYdbType(String typeName);

    /**
     * Converts given YDB type to standard SQL type
     *
     * @param type YDB type to convert
     * @return sqlType
     */
    int toSqlType(Type type);

    /**
     * Returns sql precision for given YDB type (or 0 if not applicable)
     *
     * @param type YDB type
     * @return precision
     */
    int getSqlPrecision(Type type);

    /**
     * Returns list of directly mapped sql types to YDB types
     *
     * @return list of supporter sql types
     */
    Collection<Integer> getSqlTypes();

    /**
     * Returns all types supported by database
     *
     * @return list of YDB types that supported by database (could be stored in columns)
     */
    List<Type> getAllDatabaseTypes();

}
