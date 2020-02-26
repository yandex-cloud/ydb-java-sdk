package com.yandex.ydb.core.rpc;

/**
 * @author Sergey Polovko
 */
public interface RpcTransport extends AutoCloseable {

    String getDatabase();

    OperationTray getOperationTray();

    @Override
    void close();
}
