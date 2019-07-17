package com.yandex.ydb.examples.simple;

import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.core.grpc.GrpcTransportBuilder;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.TableService;
import com.yandex.ydb.table.TableServiceBuilder;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.transaction.TxControl;


/**
 * @author Sergey Polovko
 */
public class NameResolverExample {

    public static void main(String[] args) {
        GrpcTransport transport = GrpcTransportBuilder.forEndpoint("ydb-ru-myt-0000.search.yandex.net", "/ru/home/dcherednik/mydb")
            .build();

        try (TableService tableService = TableServiceBuilder.ownTransport(transport).build()) {
            TableClient tableClient = tableService.newTableClient();

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
