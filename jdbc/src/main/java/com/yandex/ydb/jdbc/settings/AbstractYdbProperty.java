package com.yandex.ydb.jdbc.settings;

import java.sql.DriverPropertyInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

public abstract class AbstractYdbProperty<T, B> implements ToDriverPropertyInfo {

    private final String title;
    private final String description;
    @Nullable
    private final String defaultValue;
    private final Class<T> type;
    private final PropertyConverter<T> converter;
    private final BiConsumer<B, T> setter;

    protected AbstractYdbProperty(String title,
                                  String description,
                                  @Nullable String defaultValue,
                                  Class<T> type,
                                  PropertyConverter<T> converter,
                                  BiConsumer<B, T> setter) {
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.defaultValue = defaultValue;
        this.type = Objects.requireNonNull(type);
        this.converter = Objects.requireNonNull(converter);
        this.setter = Objects.requireNonNull(setter);
    }

    public String getTitle() {
        return title;
    }

    @Nullable
    public String getDefaultValue() {
        return defaultValue;
    }

    public PropertyConverter<T> getConverter() {
        return converter;
    }

    public Class<T> getType() {
        return type;
    }

    public BiConsumer<B, T> getSetter() {
        return setter;
    }

    @Override
    public DriverPropertyInfo toDriverPropertyInfo(@Nullable String value) {
        DriverPropertyInfo info = new DriverPropertyInfo(title,
                value != null ? value : defaultValue != null ? defaultValue : "");
        info.description = description;
        info.required = false;
        return info;
    }

    static class PropertiesCollector<T extends AbstractYdbProperty<?, ?>> {
        private final Map<String, T> properties = new LinkedHashMap<>();

        protected void register(T property) {
            if (properties.put(property.getTitle(), property) != null) {
                throw new IllegalStateException("Internal error. Unable to register property with name " +
                        property.getTitle() + " twice");
            }
        }

        protected Collection<T> properties() {
            return Collections.unmodifiableCollection(properties.values());
        }
    }
}
