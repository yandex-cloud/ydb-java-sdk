package com.yandex.ydb.examples.pagination;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.yandex.ydb.examples.pagination.model.School;
import com.yandex.ydb.table.values.ListType;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import com.yandex.ydb.table.values.StructType;
import com.yandex.ydb.table.values.Value;


/**
 * @author Sergey Polovko
 */
final class PaginationData {
    private PaginationData() {}

    private static final School[] SCHOOLS = {
        new School("Орлов", 1, "Ст.Халтурина, 2"),
        new School("Орлов", 2, "Свободы, 4"),
        new School("Яранск", 1, "Гоголя, 25"),
        new School("Яранск", 2, "Кирова, 18"),
        new School("Яранск", 3, "Некрасова, 59"),
        new School("Кирс", 3, "Кирова, 6"),
        new School("Нолинск", 1, "Коммуны, 4"),
        new School("Нолинск", 2, "Федосеева, 2Б"),
        new School("Котельнич", 1, "Урицкого, 21"),
        new School("Котельнич", 2, "Октябрьская, 109"),
        new School("Котельнич", 3, "Советская, 153"),
        new School("Котельнич", 5, "Школьная, 2"),
        new School("Котельнич", 15, "Октябрьская, 91"),
    };

    public static final StructType SCHOOL_TYPE = StructType.of(
        "city", PrimitiveType.utf8(),
        "number", PrimitiveType.uint32(),
        "address", PrimitiveType.utf8()
    );

    public static final ListValue SCHOOL_DATA = toListValue(SCHOOLS, SCHOOL_TYPE, s -> ImmutableMap.of(
        "city", PrimitiveValue.utf8(s.getCity()),
        "number", PrimitiveValue.uint32(s.getNumber()),
        "address", PrimitiveValue.utf8(s.getAddress())
    ));

    private static <T> ListValue toListValue(T[] items, StructType type, Function<T, Map<String, Value>> mapper) {
        ListType listType = ListType.of(type);
        return listType.newValue(Arrays.stream(items)
            .map(e -> type.newValue(mapper.apply(e)))
            .collect(Collectors.toList()));
    }
}
