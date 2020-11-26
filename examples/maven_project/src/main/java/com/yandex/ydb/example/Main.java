package com.yandex.ydb.example;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;
import yandex.cloud.sdk.auth.provider.ComputeEngineCredentialProvider;
import yandex.cloud.sdk.auth.provider.CredentialProvider;
import yandex.cloud.sdk.auth.provider.IamTokenCredentialProvider;
import yandex.cloud.sdk.auth.provider.OauthCredentialProvider;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Collectors;

import static com.yandex.ydb.table.values.PrimitiveValue.float32;
import static com.yandex.ydb.table.values.PrimitiveValue.utf8;

public final class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java -jar example.jar endpoint database");
        }
        var endpoint = args[0];
        var database = args[1];

        CredentialProvider credentialProvider;
        var oauthToken = System.getenv("OAUTH_TOKEN");
        var iamToken = System.getenv("IAM_TOKEN");
        if (oauthToken != null) {
            credentialProvider = OauthCredentialProvider.builder()
                    .oauth(oauthToken)
                    .build();
        } else if (iamToken != null) {
            credentialProvider = IamTokenCredentialProvider.builder()
                    .token(iamToken)
                    .build();
        } else {
            credentialProvider = ComputeEngineCredentialProvider.builder()
                    .build();
        }
        AuthProvider authProvider = CloudAuthProvider.newAuthProvider(credentialProvider);
        GrpcTransport transport = GrpcTransport.forEndpoint(endpoint, database)
                .withAuthProvider(authProvider)
                .withSecureConnection()
                .build();
        TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
                .build();
        Result<Session> sessionResult = tableClient.getOrCreateSession(Duration.ofSeconds(10))
                .join();
        Session session = sessionResult.expect("ok");

        TableDescription pets = TableDescription.newBuilder()
                .addNullableColumn("species", PrimitiveType.utf8())
                .addNullableColumn("name", PrimitiveType.utf8())
                .addNullableColumn("color", PrimitiveType.utf8())
                .addNullableColumn("price", PrimitiveType.float32())
                .setPrimaryKeys("species", "name")
                .build();

        Status createStatus = session.createTable(database + "/" + "java_example", pets).join();
        createStatus.expect("ok");

        TableDescription desc = session.describeTable(database + "/java_example").join().expect("ok");
        System.out.println("Columns reported by describeTable: " + desc.getColumns().stream().map(TableColumn::getName).collect(Collectors.toList()));

        String upsertQuery = String.format(
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                        "\n" +
                        "DECLARE $species AS Utf8;\n" +
                        "DECLARE $name AS Utf8;\n" +
                        "DECLARE $color AS Utf8;\n" +
                        "DECLARE $price AS Float;\n" +
                        "UPSERT INTO java_example (species, name, color, price) VALUES\n" +
                        "($species, $name, $color, $price);",
                database);

        Params params = Params.of("$species", utf8("cat"), "$name", utf8("Tom"), "$color", utf8("black"), "$price", float32(10.0f));
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);
        session.executeDataQuery(upsertQuery, txControl, params).join().expect("ok");

        String selectQuery = String.format(
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                        "\n" +
                        "DECLARE $species AS Utf8;\n" +
                        "DECLARE $name AS Utf8;\n" +
                        "SELECT * FROM java_example\n" +
                        "WHERE species = $species AND name = $name;",
                database);
        txControl = TxControl.onlineRo();
        ResultSetReader rsReader = session.executeDataQuery(selectQuery, txControl, params).join().expect("ok").getResultSet(0);
        System.out.println();
        System.out.println("Result of select query:");
        while (rsReader.next()) {
            System.out.printf("species: %s%n", rsReader.getColumn("species").getUtf8());
            System.out.printf("name: %s%n", rsReader.getColumn("name").getUtf8());
            System.out.printf("color: %s%n", rsReader.getColumn("color").getUtf8());
            System.out.printf("price: %f%n", rsReader.getColumn("price").getFloat32());
            System.out.println();
        }

        session.dropTable(database + "/java_example").join().expect("ok");
    }
}
