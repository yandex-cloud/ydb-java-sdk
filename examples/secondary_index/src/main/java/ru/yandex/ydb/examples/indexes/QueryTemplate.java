package ru.yandex.ydb.examples.indexes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public enum QueryTemplate {
    SERIES_INSERT("series_insert.yql"),
    SERIES_DELETE("series_delete.yql"),
    SERIES_UPDATE_VIEWS("series_update_views.yql"),
    SERIES_FIND_BY_ID("series_find_by_id.yql"),
    SERIES_FIND_ALL("series_find_all.yql"),
    SERIES_FIND_ALL_NEXT("series_find_all_next.yql"),
    SERIES_FIND_MOST_VIEWED("series_find_most_viewed.yql"),
    SERIES_FIND_MOST_VIEWED_NEXT("series_find_most_viewed_next.yql");

    private final String name;
    private final String text;

    QueryTemplate(String name) {
        this.name = name;
        this.text = loadTemplate(name);
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    private static String loadTemplate(String name) {
        try (InputStream stream = QueryTemplate.class.getResourceAsStream(name)) {
            Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load template " + name, e);
        }
    }
}
