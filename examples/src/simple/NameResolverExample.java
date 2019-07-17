package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.core.grpc.GrpcTransport;
import ru.yandex.ydb.core.grpc.GrpcTransportBuilder;
import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.TableServiceBuilder;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.result.ResultSetReader;
import ru.yandex.ydb.table.transaction.TxControl;


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
