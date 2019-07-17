package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.transaction.Transaction;
import ru.yandex.ydb.table.transaction.TransactionMode;
import ru.yandex.ydb.table.transaction.TxControl;
import ru.yandex.ydb.table.types.PrimitiveType;


/**
 * @author Sergey Polovko
 */
public class ComplexTransaction extends SimpleExample {

    @Override
    void run(TableService tableService, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        String prevSessionId;

        try (TableClient tableClient = tableService.newTableClient()) {
            Session session = tableClient.getOrCreateSession()
                .join()
                .expect("cannot create session");

            prevSessionId = session.getId();

            session.dropTable(tablePath)
                .join();

            {
                TableDescription tableDescription = TableDescription.newBuilder()
                    .addNullableColumn("key", PrimitiveType.uint32())
                    .addNullableColumn("value", PrimitiveType.string())
                    .setPrimaryKey("key")
                    .build();

                session.createTable(tablePath, tableDescription)
                    .join()
                    .expect("cannot create table");
            }

            Transaction transaction = session.beginTransaction(TransactionMode.SERIALIZABLE_READ_WRITE)
                .join()
                .expect("cannot create transaction");

            {
                String query = "UPSERT INTO [" + tablePath + "] (key, value) VALUES (1, 'one');";
                DataQueryResult result = session.executeDataQuery(query, TxControl.id(transaction))
                    .join()
                    .expect("query failed");
                System.out.println("--[insert1]-------------------");
                DataQueryResults.print(result);
                System.out.println("------------------------------");
            }

            {
                String query = "UPSERT INTO [" + tablePath + "] (key, value) VALUES (2, 'two');";
                DataQueryResult result = session.executeDataQuery(query, TxControl.id(transaction))
                    .join()
                    .expect("query failed");
                System.out.println("--[insert2]-------------------");
                DataQueryResults.print(result);
                System.out.println("------------------------------");
            }

            {
                String query = "SELECT * FROM [" + tablePath + "];";
                DataQueryResult result = session.executeDataQuery(query, TxControl.onlineRo().setCommitTx(true))
                    .join()
                    .expect("query failed");
                System.out.println("--[before commit]-------------");
                DataQueryResults.print(result);
                System.out.println("------------------------------");
            }

            transaction.commit()
                .join()
                .expect("cannot commit transaction");

            {
                String query = "SELECT * FROM [" + tablePath + "];";
                DataQueryResult result = session.executeDataQuery(query, TxControl.onlineRo().setCommitTx(true))
                    .join()
                    .expect("query failed");
                System.out.println("--[after commit]-------------");
                DataQueryResults.print(result);
                System.out.println("------------------------------");
            }

            tableClient.releaseSession(session)
                .join()
                .expect("cannot release session");

            Session session2 = tableClient.getOrCreateSession()
                .join()
                .expect("cannot get or create session");

            if (!prevSessionId.equals(session2.getId())) {
                throw new IllegalStateException("get non pooled session");
            }
        }
    }

    public static void main(String[] args) {
        new ComplexTransaction().doMain();
    }
}
