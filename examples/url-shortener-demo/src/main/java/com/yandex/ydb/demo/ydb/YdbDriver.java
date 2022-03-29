package com.yandex.ydb.demo.ydb;

import java.time.Duration;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;

/**
 *
 * @author Alexandr Gorshenin
 */
public class YdbDriver implements AutoCloseable {
    private final TableClient tableClient;
    private final String database;
    private final SessionRetryContext retryContext;

    public YdbDriver(RpcTransport transport, String database) throws Exception {
        this.tableClient = TableClient
                .newClient(GrpcTableRpc.useTransport(transport))
                .build();

        this.retryContext = SessionRetryContext.create(tableClient)
                .maxRetries(5)
                .sessionSupplyTimeout(Duration.ofSeconds(3))
                .build();

        this.database = database;
    }

    public String database() {
        return this.database;
    }

    public SessionRetryContext retryCtx() {
        return this.retryContext;
    }

    @Override
    public void close() {
        this.tableClient.close();
    }
}
