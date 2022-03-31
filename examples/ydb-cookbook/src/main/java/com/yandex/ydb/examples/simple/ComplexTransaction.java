package com.yandex.ydb.examples.simple;

import java.time.Duration;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.Transaction;
import com.yandex.ydb.table.transaction.TransactionMode;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;


/**
 * @author Sergey Polovko
 */
public class ComplexTransaction extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        String prevSessionId;

        try (TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport)).build()) {
            Session session = tableClient.getOrCreateSession(Duration.ofSeconds(2))
                .join()
                .expect("cannot create session");

            prevSessionId = session.getId();

            session.dropTable(tablePath)
                .join();

            TableDescription tableDescription = TableDescription.newBuilder()
                .addNullableColumn("key", PrimitiveType.uint32())
                .addNullableColumn("value", PrimitiveType.string())
                .setPrimaryKey("key")
                .build();

            session.createTable(tablePath, tableDescription)
                .join()
                .expect("cannot create table");

            Transaction transaction = session.beginTransaction(TransactionMode.SERIALIZABLE_READ_WRITE)
                .join()
                .expect("cannot create transaction");

            String query1 = "UPSERT INTO [" + tablePath + "] (key, value) VALUES (1, 'one');";
            DataQueryResult result1 = session.executeDataQuery(query1, TxControl.id(transaction))
                .join()
                .expect("query failed");
            System.out.println("--[insert1]-------------------");
            DataQueryResults.print(result1);
            System.out.println("------------------------------");

            String query2 = "UPSERT INTO [" + tablePath + "] (key, value) VALUES (2, 'two');";
            DataQueryResult result2 = session.executeDataQuery(query2, TxControl.id(transaction))
                .join()
                .expect("query failed");
            System.out.println("--[insert2]-------------------");
            DataQueryResults.print(result2);
            System.out.println("------------------------------");

            String query3 = "SELECT * FROM [" + tablePath + "];";
            DataQueryResult result3 = session.executeDataQuery(query3, TxControl.onlineRo().setCommitTx(true))
                .join()
                .expect("query failed");
            System.out.println("--[before commit]-------------");
            DataQueryResults.print(result3);
            System.out.println("------------------------------");

            transaction.commit()
                .join()
                .expect("cannot commit transaction");

            String query = "SELECT * FROM [" + tablePath + "];";
            DataQueryResult result = session.executeDataQuery(query, TxControl.onlineRo().setCommitTx(true))
                .join()
                .expect("query failed");
            System.out.println("--[after commit]-------------");
            DataQueryResults.print(result);
            System.out.println("------------------------------");

            boolean released = session.release();
            if (released) {
                Session session2 = tableClient.getOrCreateSession(Duration.ofSeconds(2))
                    .join()
                    .expect("cannot get or create session");

                if (!prevSessionId.equals(session2.getId())) {
                    throw new IllegalStateException("get non pooled session");
                }

                session2.release();
            } else {
                session.close()
                    .join()
                    .expect("cannot close session");
            }
        }
    }

    public static void main(String[] args) {
        new ComplexTransaction().doMain();
    }
}
