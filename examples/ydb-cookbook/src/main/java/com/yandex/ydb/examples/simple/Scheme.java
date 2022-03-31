package com.yandex.ydb.examples.simple;

import java.util.UUID;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.SchemeClient;
import com.yandex.ydb.table.description.DescribePathResult;
import com.yandex.ydb.table.description.ListDirectoryResult;
import com.yandex.ydb.table.rpc.grpc.GrpcSchemeRpc;


/**
 * @author Sergey Polovko
 */
public class Scheme extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        String directoryPath = pathPrefix + UUID.randomUUID().toString();
        SchemeClient schemeClient = SchemeClient.newClient(GrpcSchemeRpc.useTransport(transport)).build();

        schemeClient.makeDirectory(directoryPath)
            .join()
            .expect("cannot make directory: " + directoryPath);

        for (int i = 0; i < 3; i++) {
            String subDirectory = directoryPath + '/' + i;
            schemeClient.makeDirectory(subDirectory)
                .join()
                .expect("cannot make directory: " + subDirectory);
        }

        DescribePathResult result1 = schemeClient.describePath(directoryPath)
            .join()
            .expect("cannot describe path: " + directoryPath);

        System.out.println("--[self]---------------------------");
        System.out.println(result1.getSelf());
        System.out.println("-----------------------------------");

        ListDirectoryResult result2 = schemeClient.listDirectory(directoryPath)
            .join()
            .expect("cannot describe directory: " + directoryPath);

        System.out.println("--[self]---------------------------");
        System.out.println(result2.getSelf());
        System.out.println("--[children]-----------------------");
        System.out.println(result2.getChildren());
        System.out.println("-----------------------------------");

        for (int i = 0; i < 3; i++) {
            String subDirectory = directoryPath + '/' + i;
            schemeClient.removeDirectory(subDirectory)
                .join()
                .expect("cannot remove directory: " + subDirectory);
        }

        schemeClient.removeDirectory(directoryPath)
            .join()
            .expect("cannot remove directory: " + directoryPath);

    }

    public static void main(String[] args) {
        new Scheme().doMain();
    }
}
