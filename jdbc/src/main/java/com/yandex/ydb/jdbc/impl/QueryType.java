package com.yandex.ydb.jdbc.impl;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum QueryType {
    // DDL
    SCHEME_QUERY("--jdbc:SCHEME"),

    // DML
    DATA_QUERY("--jdbc:DATA"),
    SCAN_QUERY("--jdbc:SCAN"),

    // EXPLAIN
    EXPLAIN_QUERY("--jdbc:EXPLAIN");

    private final String prefix;
    private final String alternativePrefix;

    QueryType(String prefix) {
        this.prefix = prefix;
        this.alternativePrefix = "!" + prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAlternativePrefix() {
        return alternativePrefix;
    }

    public static Collection<String> prefixes() {
        return Stream.of(QueryType.values())
                .map(QueryType::getPrefix)
                .collect(Collectors.toList());
    }
}
