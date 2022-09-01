package com.yandex.ydb.jdbc.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.yandex.ydb.jdbc.impl.MappingGetters.Getters;
import com.yandex.ydb.jdbc.impl.MappingGetters.SqlTypes;
import com.yandex.ydb.jdbc.impl.MappingSetters.Setters;
import com.yandex.ydb.table.values.DecimalType;
import com.yandex.ydb.table.values.OptionalType;
import com.yandex.ydb.table.values.OptionalValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.Type;

public class TypeDescription {

    final Type type;
    final boolean optional;
    final OptionalValue optionalValue;

    final Getters fromValue;
    final Setters setters;

    final SqlTypes sqlTypes;

    private TypeDescription(Type type,
                            OptionalValue optionalValue,
                            Getters fromValue,
                            Setters setters,
                            SqlTypes sqlTypes) {
        this.type = Objects.requireNonNull(type);
        this.optional = optionalValue != null;
        this.optionalValue = optionalValue;
        this.fromValue = Objects.requireNonNull(fromValue);
        this.setters = Objects.requireNonNull(setters);
        this.sqlTypes = Objects.requireNonNull(sqlTypes);
    }

    private static final Map<Type, TypeDescription> TYPES = new ConcurrentHashMap<>();

    static {
        ofInternal(DecimalType.of(DecimalType.MAX_PRECISION)); // max
        ofInternal(DecimalType.of(22, 9)); // default for database
        for (PrimitiveType.Id id : PrimitiveType.Id.values()) {
            ofInternal(PrimitiveType.of(id)); // All primitive values
        }
    }

    private static void ofInternal(Type type) {
        of(type);
        of(type.makeOptional()); // Register both normal and optional types
    }

    private static TypeDescription buildType(Type origType) {
        Type type;
        OptionalValue optionalValue;
        if (origType.getKind() == Type.Kind.OPTIONAL) {
            OptionalType optionalType = (OptionalType) origType;
            type = optionalType.getItemType();
            optionalValue = optionalType.emptyValue();
        } else {
            type = origType;
            optionalValue = null;
        }

        Getters getters = MappingGetters.buildGetters(type);
        Setters setters = MappingSetters.buildSetters(type);
        SqlTypes sqlTypes = MappingGetters.buildDataType(type);

        return new TypeDescription(type, optionalValue, getters, setters, sqlTypes);
    }

    public static TypeDescription of(Type type) {
        // TODO: check for cache poisoning?
        return TYPES.computeIfAbsent(type, TypeDescription::buildType);
    }
}
