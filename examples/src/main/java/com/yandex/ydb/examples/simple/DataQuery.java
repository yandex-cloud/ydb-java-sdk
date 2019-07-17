package com.yandex.ydb.examples.simple;

import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.TableService;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.settings.AutoPartitioningPolicy;
import com.yandex.ydb.table.settings.CreateTableSettings;
import com.yandex.ydb.table.settings.PartitioningPolicy;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.types.PrimitiveType;


/**
 * @author Sergey Polovko
 */
public class DataQuery extends SimpleExample {

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
            TableDescription tableDescription = TableDescription.newBuilder()
                .addNullableColumn("id", PrimitiveType.uint32())
                .addNullableColumn("login", PrimitiveType.string())
                .addNullableColumn("age", PrimitiveType.uint32())
                .setPrimaryKey("id")
                .build();

            CreateTableSettings settings = new CreateTableSettings()
                .setPartitioningPolicy(new PartitioningPolicy()
                    .setAutoPartitioning(AutoPartitioningPolicy.AUTO_SPLIT)
                    .setUniformPartitions(4));

            session.createTable(tablePath, tableDescription, settings)
                .join()
                .expect("cannot create table");
        }

        {
            String query = "INSERT INTO [" + tablePath + "] (id, login, age) VALUES (1, 'Jamel', 99);";
            DataQueryResult result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
                .join()
                .expect("query failed");
            DataQueryResults.print(result);
        }

        {
            String query = "SELECT * FROM [" + tablePath + "];";
            DataQueryResult result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
                .join()
                .expect("query failed");
            DataQueryResults.print(result);
        }

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new DataQuery().doMain();
    }
}
