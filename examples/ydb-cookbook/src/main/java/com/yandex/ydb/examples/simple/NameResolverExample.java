package com.yandex.ydb.examples.simple;

import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;


/**
 * @author Sergey Polovko
 */
public class NameResolverExample {
    private NameResolverExample() { }

    public static void main(String[] args) {
        GrpcTransport transport = GrpcTransport.forEndpoint("ydb-ru.yandex.net", "/ru/home/username/mydb")
            .build();

        try (TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport)).build()) {
            Session session = tableClient.createSession().join()
                .expect("create session");

            DataQueryResult dataResult = session.executeDataQuery("SELECT 1;", TxControl.serializableRw())
                .join()
                .expect("query failed");

            ResultSetReader resultSet = dataResult.getResultSet(0);
            resultSet.next();

            long value = resultSet.getColumn(0).getUint32();
            System.out.println("value=" + value);

            session.close().join()
                .expect("close session");
        }
    }

}
