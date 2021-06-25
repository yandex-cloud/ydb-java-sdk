package com.yandex.ydb.jdbc.settings;

import java.util.Objects;

public class ParsedProperty {
    private final String rawValue;
    private final Object parsedValue;

    public ParsedProperty(String rawValue, Object parsedValue) {
        this.rawValue = Objects.requireNonNull(rawValue);
        this.parsedValue = Objects.requireNonNull(parsedValue);
    }

    public String getRawValue() {
        return rawValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParsedValue() {
        return (T) parsedValue;
    }
}
