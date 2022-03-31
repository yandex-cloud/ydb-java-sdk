package com.yandex.ydb.examples.simple;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.query.ExplainDataQueryResult;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;


/**
 * @author Sergey Polovko
 */
public class ExplainDataQuery extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport)).build();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        session.dropTable(tablePath)
            .join();

        String query1 =
            "CREATE TABLE [" + tablePath + "] (" +
                "  key Uint32," +
                "  value String," +
                "  PRIMARY KEY(key)" +
                ");";
        session.executeSchemeQuery(query1)
            .join()
            .expect("cannot create table");


        String query2 = "SELECT * FROM [" + tablePath + "];";
        ExplainDataQueryResult result = session.explainDataQuery(query2)
            .join()
            .expect("cannot explain query");

        System.out.println("--[ast]----------------------\n" + result.getQueryAst());
        System.out.println();
        System.out.println("--[plan]---------------------\n" + result.getQueryPlan());

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new ExplainDataQuery().doMain();
    }
}
