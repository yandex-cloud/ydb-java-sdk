package com.yandex.ydb.examples;

import com.yandex.ydb.core.rpc.RpcTransport;


/**
 * @author Sergey Polovko
 */
public interface App extends AutoCloseable {

    void run();

    @Override
    void close();

    interface Factory {
        App newApp(RpcTransport transport, String path);
    }
}
