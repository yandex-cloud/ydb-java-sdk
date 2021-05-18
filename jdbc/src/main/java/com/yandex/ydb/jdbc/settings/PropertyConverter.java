package com.yandex.ydb.jdbc.settings;

import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import com.yandex.ydb.jdbc.exception.YdbConfigurationException;

interface PropertyConverter<T> {
    T convert(String value) throws SQLException;

    static PropertyConverter<String> stringValue() {
        return value -> value;
    }

    static PropertyConverter<Duration> durationValue() {
        return value -> {
            String targetValue = "PT" + value.replace(" ", "").toUpperCase(Locale.ROOT);
            try {
                return Duration.parse(targetValue);
            } catch (DateTimeParseException e) {
                throw new YdbConfigurationException("Unable to parse value [" + value + "] -> [" +
                        targetValue + "] as Duration: " + e.getMessage(), e);
            }
        };
    }

    static PropertyConverter<Integer> integerValue() {
        return value -> {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                throw new YdbConfigurationException("Unable to parse value [" + value + "] as Integer: " +
                        e.getMessage(), e);
            }
        };
    }

    static PropertyConverter<Boolean> booleanValue() {
        return Boolean::valueOf;
    }

    static PropertyConverter<String> stringFileReference() {
        return YdbProperties::stringFileReference;
    }

    static PropertyConverter<byte[]> byteFileReference() {
        return YdbProperties::byteFileReference;
    }
}
