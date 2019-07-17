package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.description.TableColumn;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.settings.AlterTableSettings;
import ru.yandex.ydb.table.types.OptionalType;
import ru.yandex.ydb.table.types.PrimitiveType;


/**
 * @author Sergey Polovko
 */
public class AlterTable extends SimpleExample {

    @Override
    void run(TableService tableService, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = tableService.newTableClient();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        session.dropTable(tablePath)
            .join();

        String query =
            "CREATE TABLE [" + tablePath + "] (" +
            "  key Uint32," +
            "  value String," +
            "  PRIMARY KEY(key)" +
            ");";
        session.executeSchemeQuery(query)
            .join()
            .expect("cannot create table");

        {
            session.alterTable(tablePath, new AlterTableSettings()
                    .setTraceId("some-trace-id")
                    .addColumn("name", OptionalType.of(PrimitiveType.string()))
                    .addColumn("age", OptionalType.of(PrimitiveType.uint32()))
                    .dropColumn("value")
                ).join()
                .expect("cannot alter table");
        }

        TableDescription description = session.describeTable(tablePath)
            .join()
            .expect("cannot describe table");

        System.out.println("--[primary keys]-------------");
        int i = 1;
        for (String primaryKey : description.getPrimaryKeys()) {
            System.out.printf("%4d. %s\n", i++, primaryKey);
        }

        System.out.println("\n--[columns]------------------");
        i = 1;
        for (TableColumn column : description.getColumns()) {
            System.out.printf("%4d. %s %s\n", i++, column.getName(), column.getType());
        }

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new AlterTable().doMain();
    }
}
