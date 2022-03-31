package com.yandex.ydb.example;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
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
        if (args.length != 1) {
            System.err.println("Usage: java -jar ydb-metadata-example.jar <connection-string>");
            return;
        }
        String connectionString = args[0];

        // Use metadata credentials
        AuthProvider authProvider = CloudAuthProvider.newAuthProvider(
            ComputeEngineCredentialProvider.builder()
                .build()
        );

        GrpcTransport transport = GrpcTransport.forConnectionString(connectionString)
                .withAuthProvider(authProvider)
                .build();

        try (TableClient tableClient = TableClient
                .newClient(GrpcTableRpc.ownTransport(transport))
                .build()) {

            Session session = tableClient.getOrCreateSession(Duration.ofSeconds(10))
                    .join().expect("ok");

            ResultSetReader rsReader = session.executeDataQuery("SELECT 1;", TxControl.serializableRw())
                    .join().expect("ok").getResultSet(0);

            assert (rsReader.getRowCount() == 1);
            System.out.println("Result:");
            while (rsReader.next()) {
                System.out.println(rsReader.getColumn(0).getInt32());
            }

            session.release();
        }
    }
}
