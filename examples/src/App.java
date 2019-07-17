package ru.yandex.ydb.examples;

import ru.yandex.ydb.table.TableService;


/**
 * @author Sergey Polovko
 */
public interface App extends AutoCloseable {

    void run();

    @Override
    void close();

    interface Factory {
        App newApp(TableService tableService, String path);
    }
}
