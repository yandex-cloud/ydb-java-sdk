package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.description.TableColumn;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.settings.CreateTableSettings;
import ru.yandex.ydb.table.settings.PartitioningPolicy;
import ru.yandex.ydb.table.types.PrimitiveType;


/**
 * @author Sergey Polovko
 */
public class CreateTable extends SimpleExample {

    @Override
    void run(TableService tableService, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = tableService.newTableClient();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        session.dropTable(tablePath)
            .join();

        {
            TableDescription description = TableDescription.newBuilder()
                .addNullableColumn("hash", PrimitiveType.uint32())
                .addNullableColumn("name", PrimitiveType.utf8())
                .addNullableColumn("salary", PrimitiveType.float64())
                .setPrimaryKeys("hash", "name")  // uniform partitioning requires Uint32 / Uint64 as a first key column
                .build();

            CreateTableSettings settings = new CreateTableSettings()
                .setPartitioningPolicy(new PartitioningPolicy().setUniformPartitions(4));

            session.createTable(tablePath, description, settings)
                .join()
                .expect("cannot create table");
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
        new CreateTable().doMain();
    }
}
