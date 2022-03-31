package com.yandex.ydb.examples.simple;

import java.time.Duration;
import java.util.concurrent.ForkJoinPool;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.result.ValueReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;


/**
 * @author Sergey Polovko
 */
public class RetryExample extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        try (TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
                .sessionPoolSize(10, 20)
                .queryCacheSize(100)
                .build()) {
            SessionRetryContext ctx = SessionRetryContext.create(tableClient)
                .executor(ForkJoinPool.commonPool())
                .maxRetries(5)
                .sessionSupplyTimeout(Duration.ofSeconds(3))
                .build();

            Result<DataQueryResult> result = ctx.supplyResult(session -> {
                TxControl txControl = TxControl.serializableRw()
                    .setCommitTx(true);
                return session.executeDataQuery("SELECT 1 + 2;", txControl);
            }).join();

            DataQueryResult dataQueryResult = result.expect("cannot execute data query");
            ResultSetReader resultSet = dataQueryResult.getResultSet(0);
            while (resultSet.next()) {
                ValueReader reader = resultSet.getColumn(0);
                System.out.println("result = " + reader.toString());
            }
        }
    }

    public static void main(String[] args) {
        new RetryExample().doMain();
    }
}
