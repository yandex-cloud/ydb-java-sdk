package com.yandex.ydb.examples;

// mkdir -p ~/.ydb && \
// wget "https://crls.yandex.net/allCAs.pem" -O ~/.crt/root.crt && \
// chmod 0600 ~/.ydb/root.crt
// export YDB_SSL_ROOT_CERTIFICATES_FILE=~/.ydb/root.crt
// yc iam key create --service-account-name satest -o  ~/.ydb/sa_name.json

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import yandex.cloud.sdk.auth.provider.ApiKeyCredentialProvider;

public class CloudConnect {
    public static void main(String[] args) throws IOException {
        AuthProvider authProvider = CloudAuthProvider.newAuthProvider(ApiKeyCredentialProvider.builder()
                .fromFile(Paths.get(System.getProperty("user.home") + "/.ydb/sa_name.json"))
                .build());

        byte[] cert = Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/.crt/root.crt"));

        GrpcTransport transport = GrpcTransport.forEndpoint(
                "lb.etn00ldc52ve3j9brsc4.ydb.mdb.yandexcloud.net:2135",
                "/ru-central1/b1grr8be1uqu58ilivmv/etn00ldc52ve3j9brsc4")
                .withAuthProvider(authProvider)
                .withSecureConnection(cert)
                .build();

        TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
                .build();

        tableClient.getOrCreateSession(Duration.ofSeconds(10))
                .join().expect("ok");
    }
}
