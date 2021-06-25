package com.yandex.ydb.spring.data;

import java.util.List;

import javax.sql.DataSource;

import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.jdbc.impl.YdbTypesImpl;
import com.yandex.ydb.table.values.Type;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.ParsedSqlAccessor;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;

/**
 * Custom JDBC template implementation, supports transforming SQL into YDB-compatible form
 */
public class YdbNamedParametersJdbcTemplate extends NamedParameterJdbcTemplate {
    private static final YdbTypes TYPES = YdbTypesImpl.getInstance();

    public YdbNamedParametersJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public YdbNamedParametersJdbcTemplate(JdbcOperations classicJdbcTemplate) {
        super(classicJdbcTemplate);
    }

    @Override
    protected PreparedStatementCreatorFactory getPreparedStatementCreatorFactory(
            ParsedSql parsedSql,
            SqlParameterSource paramSource) {
        String sqlToUse = prepareSql(parsedSql, paramSource);
        List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
        return new YdbPreparedStatementCreatorFactory(sqlToUse, declaredParameters);
    }


    static String prepareSql(ParsedSql parsedSql, @Nullable SqlParameterSource paramSource) {
        ParsedSqlAccessor accessor = new ParsedSqlAccessor(parsedSql);
        String originalSql = accessor.getOriginalSql();
        List<String> paramNames = accessor.getParameterNames();
        if (paramNames.isEmpty()) {
            return postProcessWithDirtyHacks(originalSql);
        }

        if (paramSource == null) {
            throw new YdbDaoRuntimeException("paramSource is required to prepared parameters with types");
        }

        StringBuilder declaration = new StringBuilder(512 + originalSql.length());
        StringBuilder actualSql = new StringBuilder(originalSql.length());

        int lastIndex = 0;
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);

            int sqlType = paramSource.getSqlType(paramName);
            if (sqlType == SqlParameterSource.TYPE_UNKNOWN) {
                throw new YdbDaoRuntimeException("Unable to detect parameter type: [" + paramName + "]");
            }
            Type type = null;
            declaration.append("declare $").append(paramName).append(" as ");

            Object value = paramSource.getValue(paramName);
            if (value instanceof SqlParameterValue) {
                value = ((SqlParameterValue) value).getValue();
            }
            if (value instanceof YdbWrappedValue) {
                YdbWrappedValue wrappedValue = (YdbWrappedValue) value;
                value = wrappedValue.getValue();
                type = wrappedValue.getType();
            }

            if (type == null) {
                type = TYPES.toYdbType(sqlType);
            }
            if (type == null) {
                throw new YdbDaoRuntimeException("Unable to convert parameter type: [" + paramName +
                        "] from sqlType " + sqlType + " to YDB type");
            }

            // TODO: Find a way to build query without knowing actual parameters (must be defined in paramSource)
            boolean listValue = value instanceof Iterable;
            if (listValue) {
                declaration.append("List<").append(type).append("?").append(">");
            } else {
                declaration.append(type);
                if (value == null) {
                    declaration.append("?");
                }
            }

            declaration.append(";\n");

            int[] indexes = accessor.getParameterIndexes(i);
            int startIndex = indexes[0];
            int endIndex = indexes[1];

            int skipSymbol = listValue ? 1 : 0; // `id` IN ($ids) -> `id` IN $ids

            actualSql.append(originalSql, lastIndex, startIndex - skipSymbol);
            actualSql.append("$").append(paramName);

            lastIndex = endIndex + skipSymbol;
        }
        actualSql.append(originalSql, lastIndex, originalSql.length());
        declaration.append("\n").append(actualSql);

        return postProcessWithDirtyHacks(declaration.toString());
    }

    // TODO: make better
    private static String postProcessWithDirtyHacks(String sql) {
        // INSERT INTO -> UPSERT INTO
        sql = sql.replace("INSERT INTO ", "UPSERT INTO");

        // LEFT OUTER JOIN `t` `alias` -> LEFT OUTER JOIN `t` AS `alias`
        sql = sql.replaceAll("(.+?LEFT OUTER JOIN `\\S+?`) (`\\S+?`.+)", "$1 AS $2");

        return sql;
    }
}
