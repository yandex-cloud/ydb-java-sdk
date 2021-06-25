package com.yandex.ydb.jdbc.impl;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.values.Value;

public class YdbStandardSqlTranslator {

    private static final int REGEX_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;

    // ... from [table] [alias] ... -> ... from [table] as [alias] ...
    // "$1$2$3 as $4$5"
    private static final Pattern PATTERN_TABLE_AS =
            Pattern.compile("(.*?\\s*select\\s.+?)(from)(\\s+\\w+)(\\s+\\w+)(.*?)", REGEX_FLAGS);

    // ... insert into ... -> ... upsert into ...
    // "$1upsert$2$3"
    private static final Pattern PATTERN_TABLE_UPSERT =
            Pattern.compile("(.*?\\s*)insert(\\s+into\\s+)(.+?)", REGEX_FLAGS);

    // ... select [table.column] as [alias], ... order by [table.column] ... ->
    // ... select [table.column] as [alias], ... order by [alias] ...
    private static final Pattern PATTERN_ORDER_BY_ALIAS =
            Pattern.compile("(.+?\\s+order\\s+by\\s+.*?\\s*)(\\w+\\.\\w+)(.*?)", REGEX_FLAGS);

    /**
     * Translates standard JDBC-like query into fully functional YQL
     *
     * @param sql    jdbc query
     * @param params query params
     * @return translated query
     * @throws SQLException when some parameters are invalid
     */
    String translate(String sql, Params params) throws SQLException {
        sql = PATTERN_TABLE_UPSERT.matcher(sql).replaceAll("$1upsert$2$3");

        Matcher tableAsMatcher = PATTERN_TABLE_AS.matcher(sql);
        if (tableAsMatcher.matches()) {
            String keyword = tableAsMatcher.group(4).trim();
            if (!keyword.equalsIgnoreCase("view") && !keyword.equals("from")) {
                sql = tableAsMatcher.replaceFirst("$1$2$3 as $4$5");
            }
        }

        while (true) {
            Matcher matcher = PATTERN_ORDER_BY_ALIAS.matcher(sql);
            if (matcher.find()) {
                String column = matcher.group(2);
                Matcher aliasMatcher = Pattern
                        .compile(String.format(".+?\\s+(%s)\\s+as\\s+(\\w+)(\\s+|,).+?", column),
                                Pattern.CASE_INSENSITIVE)
                        .matcher(sql);
                if (aliasMatcher.find()) {
                    String alias = aliasMatcher.group(2);
                    sql = matcher.replaceFirst("$1 " + alias + " $3");
                    continue; // ---
                }
            }
            break;
        }

        Map<String, Value<?>> paramMap = params.values();

        StringBuilder header = new StringBuilder(sql.length() * 2);
        StringBuilder buffer = new StringBuilder(sql.length() + 10 * 4);

        int start = sql.indexOf(YdbConst.PREFIX_SYNTAX_V1);
        if (start >= 0) {
            start += YdbConst.PREFIX_SYNTAX_V1.length();
            header.append(YdbConst.PREFIX_SYNTAX_V1).append("\n");
        } else {
            start = 0;
        }

        int index = 0;
        int length = sql.length();
        for (int i = start; i < length; i++) {
            char c = sql.charAt(i);
            if (c == '?') {
                index++;
                String param = "$p" + index;
                Value<?> value = paramMap.get(param);
                if (value == null) {
                    throw new SQLException("Unable to match parameter with index " + index +
                            " to parameters, " + param + " not found in " + paramMap.keySet());
                }
                header.append("declare ").append(param).append(" as ").append(value.getType()).append(";\n");
                buffer.append(param);
            } else {
                buffer.append(c);
            }
        }
        header.append(buffer);
        return header.toString();
    }
}
