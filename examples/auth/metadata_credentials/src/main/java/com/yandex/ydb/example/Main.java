package com.yandex.ydb.example;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;

import yandex.cloud.sdk.auth.provider.ComputeEngineCredentialProvider;

import java.time.Duration;

public final class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar example.jar <endpoint> <database>");
        }
        String endpoint = args[0];
        String database = args[1];

        // Use metadata credentials
        AuthProvider authProvider = CloudAuthProvider.newAuthProvider(
            ComputeEngineCredentialProvider.builder()
                .build()
        );

        GrpcTransport transport = GrpcTransport.forEndpoint(endpoint, database)
                .withAuthProvider(authProvider)
                .withSecureConnection()
                .build();
        TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
                .build();
        Result<Session> sessionResult = tableClient.getOrCreateSession(Duration.ofSeconds(10))
                .join();
        Session session = sessionResult.expect("ok");
        ResultSetReader rsReader = session.executeDataQuery("SELECT 1;", TxControl.serializableRw()).join()
                .expect("ok").getResultSet(0);
        assert(rsReader.getRowCount() == 1);
        System.out.println("Result:");
        while (rsReader.next()) {
            System.out.println(rsReader.getColumn(0).getInt32());
        }
    }
}
