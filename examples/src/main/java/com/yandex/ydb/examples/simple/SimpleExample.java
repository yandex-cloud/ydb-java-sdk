package com.yandex.ydb.examples.simple;

import java.util.concurrent.Executors;

import com.yandex.ydb.core.grpc.GrpcTransportBuilder;
import com.yandex.ydb.core.rpc.RpcTransport;


/**
 * @author Sergey Polovko
 */
public abstract class SimpleExample {

    protected void doMain() {
        String host = System.getProperty("HOST", "some.host.name.com");
        int port = Integer.parseInt(System.getProperty("PORT", "2135"));

        String path = System.getProperty("PATH", "/Root/");
        if (!path.endsWith("/")) {
            path += "/";
        }

        System.err.println("Run with -DHOST=<hostname> and -DPORT=<port> to override connection settings");
        System.err.println();
        System.err.println("HOST=" + host);
        System.err.println("PORT=" + port);
        System.err.println("PATH=" + path);
        System.err.println();

        try (RpcTransport transport = GrpcTransportBuilder.singleHost(host, port)
            .withCallExecutor(Executors.newFixedThreadPool(3))
            .build())
        {
            run(transport, path);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    abstract void run(RpcTransport transport, String pathPrefix);
}
