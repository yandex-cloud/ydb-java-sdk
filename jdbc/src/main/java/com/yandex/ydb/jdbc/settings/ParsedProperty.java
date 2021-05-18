package com.yandex.ydb.jdbc.settings;

import java.util.Objects;

public class ParsedProperty {
    private final String value;
    private final Object parsedValue;

    public ParsedProperty(String value, Object parsedValue) {
        this.value = Objects.requireNonNull(value);
        this.parsedValue = Objects.requireNonNull(parsedValue);
    }

    public String getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParsedValue() {
        return (T) parsedValue;
    }
}
