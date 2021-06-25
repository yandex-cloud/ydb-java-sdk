package com.yandex.ydb.jdbc.settings;

import java.time.Duration;
import java.util.Collection;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.yandex.ydb.table.TableClient;

public class YdbClientProperty<T> extends AbstractYdbProperty<T, TableClient.Builder> {
    private static final PropertiesCollector<YdbClientProperty<?>> PROPERTIES = new PropertiesCollector<>();

    public static final YdbClientProperty<Integer> QUERY_CACHE_SIZE =
            new YdbClientProperty<>(
                    "queryCacheSize",
                    "Query cache size",
                    null,
                    Integer.class,
                    PropertyConverter.integerValue(),
                    TableClient.Builder::queryCacheSize);
    public static final YdbClientProperty<Boolean> KEEP_QUERY_TEXT =
            new YdbClientProperty<>(
                    "keepQueryText",
                    "Keep Query text",
                    null,
                    Boolean.class,
                    PropertyConverter.booleanValue(),
                    TableClient.Builder::keepQueryText);
    public static final YdbClientProperty<Duration> SESSION_KEEP_ALIVE_TIME =
            new YdbClientProperty<>(
                    "sessionKeepAliveTime",
                    "Session keep-alive timeout",
                    null,
                    Duration.class,
                    PropertyConverter.durationValue(),
                    TableClient.Builder::sessionKeepAliveTime);
    public static final YdbClientProperty<Duration> SESSION_MAX_IDLE_TIME =
            new YdbClientProperty<>(
                    "sessionMaxIdleTime",
                    "Session max idle time",
                    null,
                    Duration.class,
                    PropertyConverter.durationValue(),
                    TableClient.Builder::sessionMaxIdleTime);
    public static final YdbClientProperty<Integer> SESSION_CREATION_MAX_RETRIES =
            new YdbClientProperty<>(
                    "sessionCreationMaxRetries",
                    "Session creation max retries",
                    null,
                    Integer.class,
                    PropertyConverter.integerValue(),
                    TableClient.Builder::sessionCreationMaxRetries);
    public static final YdbClientProperty<Integer> SESSION_POOL_SIZE_MIN =
            new YdbClientProperty<>("sessionPoolSizeMin",
                    "Session pool min size (with with sessionPoolSizeMax)",
                    null,
                    Integer.class,
                    PropertyConverter.integerValue(),
                    (builder, value) -> {
                    });
    public static final YdbClientProperty<Integer> SESSION_POOL_SIZE_MAX =
            new YdbClientProperty<>(
                    "sessionPoolSizeMax",
                    "Session pool max size (with with sessionPoolSizeMin)",
                    null,
                    Integer.class,
                    PropertyConverter.integerValue(),
                    (builder, value) -> {
                    });

    protected YdbClientProperty(String name,
                                String description,
                                @Nullable String defaultValue,
                                Class<T> type,
                                PropertyConverter<T> converter,
                                BiConsumer<TableClient.Builder, T> setter) {
        super(name, description, defaultValue, type, converter, setter);
        PROPERTIES.register(this);
    }

    public static Collection<YdbClientProperty<?>> properties() {
        return PROPERTIES.properties();
    }
}
