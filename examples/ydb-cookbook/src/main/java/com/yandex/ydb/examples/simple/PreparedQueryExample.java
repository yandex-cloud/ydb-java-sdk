package com.yandex.ydb.examples.simple;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQuery;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.settings.DropTableSettings;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;


/**
 * @author Sergey Polovko
 */
public class PreparedQueryExample extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport)).build();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        session.dropTable(tablePath, new DropTableSettings())
            .join();

        TableDescription tableDescription = TableDescription.newBuilder()
            .addNullableColumn("name", PrimitiveType.utf8())
            .addNullableColumn("size", PrimitiveType.uint64())
            .setPrimaryKey("name")
            .build();

        session.createTable(tablePath, tableDescription)
            .join()
            .expect("cannot create table");

        DataQuery query1 = session.prepareDataQuery(
                "DECLARE $name AS Utf8;" +
                "DECLARE $size AS Uint32;" +
                "INSERT INTO [" + tablePath + "] (name, size) VALUES ($name, $size);")
            .join()
            .expect("cannot create prepared query");

        Params params = query1.newParams()
            .put("$name", PrimitiveValue.utf8("/etc/passwd"))
            .put("$size", PrimitiveValue.uint32(42));

        DataQueryResult result1 = query1.execute(TxControl.serializableRw().setCommitTx(true), params)
            .join()
            .expect("query failed");

        DataQueryResults.print(result1);

        String query2 = "SELECT * FROM [" + tablePath + "];";

        DataQueryResult result2 = session.executeDataQuery(query2, TxControl.serializableRw().setCommitTx(true))
            .join()
            .expect("query failed");

        DataQueryResults.print(result2);

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new PreparedQueryExample().doMain();
    }
}
