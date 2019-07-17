package com.yandex.ydb.examples.simple;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.DecimalValue;


/**
 * @author Sergey Polovko
 */
public class DecimalExample extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport)).build();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        String query = "SELECT CAST(\"12.345\" AS Decimal(13, 3));";
        DataQueryResult result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
            .join()
            .expect("query failed");

        ResultSetReader resultSet = result.getResultSet(0);
        resultSet.next();

        DecimalValue value = resultSet.getColumn(0).getDecimal();
        System.out.println("decimal: " + value);
        System.out.println("BigDecimal: " + value.toBigDecimal());

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new DecimalExample().doMain();
    }
}
