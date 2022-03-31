package com.yandex.ydb.examples.bulk_upsert;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.yandex.ydb.table.values.ListType;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import com.yandex.ydb.table.values.StructType;
import com.yandex.ydb.table.values.Value;

/**
 *
 * @author Alexandr Gorshenin
 */
public class LogRecord {
    public static final StructType COLUMNS = StructType.of(
        "app", PrimitiveType.utf8(),
        "timestamp", PrimitiveType.timestamp(),
        "host", PrimitiveType.utf8(),
        "http_code", PrimitiveType.uint32(),
        "message", PrimitiveType.utf8()
    );

    public static final List<String> PRIMARY_KEYS = Arrays.asList(
            "app", "timestamp", "host"
    );

    private final String app;
    private final Instant timestamp;
    private final String host;
    private final Integer httpCode;
    private final String message;

    public LogRecord(String app, Instant timestamp, String host, Integer httpCode, String message) {
        this.app = app;
        this.timestamp = timestamp;
        this.host = host;
        this.httpCode = httpCode;
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LogRecord other = (LogRecord) o;

        return Objects.equals(app, other.app)
                && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(host, other.host)
                && Objects.equals(httpCode, other.httpCode)
                && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(app, timestamp, host, httpCode, message);
    }

    @Override
    public String toString() {
        return "LogRecord{" +
            "app=" + app +
            ", timestamp=" + timestamp +
            ", host='" + host +
            ", httpCode=" + httpCode +
            ", message=" + message +
            '}';
    }

    private Map<String, Value> toValue() {
        return ImmutableMap.of(
            "app", PrimitiveValue.utf8(app),
            "timestamp", PrimitiveValue.timestamp(timestamp),
            "host", PrimitiveValue.utf8(host),
            "http_code", PrimitiveValue.uint32(httpCode),
            "message", PrimitiveValue.utf8(message)
        );
    }

    public static ListValue toListValue(List<LogRecord> items) {
        ListType listType = ListType.of(COLUMNS);
        return listType.newValue(items.stream()
            .map(e -> COLUMNS.newValue(e.toValue()))
            .collect(Collectors.toList()));
    }
}
