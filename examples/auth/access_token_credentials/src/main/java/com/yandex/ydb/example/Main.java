package com.yandex.ydb.example;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.table.SessionRetryContext;
import yandex.cloud.sdk.auth.provider.IamTokenCredentialProvider;

public final class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar ydb-access-token-example.jar <connection-string> <access-token>");
            return;
        }
        String connectionString = args[0];
        String accessToken = args[1];

        // Access token credentials
        AuthProvider authProvider = CloudAuthProvider.newAuthProvider(
            IamTokenCredentialProvider.builder()
                .token(accessToken)
                .build()
        );

        GrpcTransport transport = GrpcTransport.forConnectionString(connectionString)
                .withAuthProvider(authProvider) // Or this method could not be called at all
                .build();

        try (TableClient tableClient = TableClient
                .newClient(GrpcTableRpc.ownTransport(transport))
                .build()) {

            SessionRetryContext retryCtx = SessionRetryContext.create(tableClient).build();

            retryCtx.supplyResult(session -> {
                ResultSetReader rsReader = session.executeDataQuery("SELECT 1;", TxControl.serializableRw())
                        .join().expect("ok").getResultSet(0);

                rsReader.next();
                System.out.println(rsReader.getColumn(0).getInt32());

                return CompletableFuture.completedFuture(Result.success(Boolean.TRUE));
            });
        }
    }
}
