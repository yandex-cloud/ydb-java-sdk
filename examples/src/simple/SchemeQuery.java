package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;


/**
 * @author Sergey Polovko
 */
public class SchemeQuery extends SimpleExample {

    @Override
    protected void run(TableService tableService, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = tableService.newTableClient();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        String createTable =
            "CREATE TABLE [" + tablePath + "] (" +
            "  key Uint32," +
            "  value String," +
            "  PRIMARY KEY(key)" +
            ");";

        session.executeSchemeQuery(createTable)
            .join()
            .expect("cannot create table");

        session.executeSchemeQuery("DROP TABLE [" + tablePath + "];")
            .join()
            .expect("cannot drop table");

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new SchemeQuery().doMain();
    }
}
