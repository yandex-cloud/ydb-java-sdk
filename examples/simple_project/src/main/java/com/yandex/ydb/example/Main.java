package com.yandex.ydb.example;

import java.io.IOException;
import java.util.stream.Collectors;

import com.yandex.ydb.auth.iam.CloudAuthHelper;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;


public final class Main {
    private Main() { }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java -jar simple-project.jar <endpoint>");
            return;
        }

        String endpoint = args[0];

        GrpcTransport transport = GrpcTransport.forConnectionString(endpoint)
                .withAuthProvider(CloudAuthHelper.getAuthProviderFromEnviron())
                .build();

        GrpcTableRpc rpc = GrpcTableRpc.ownTransport(transport);
        try (TableClient tableClient = TableClient.newClient(rpc).build()) {
            SessionRetryContext retryCtx = SessionRetryContext.create(tableClient).build();

            execute(retryCtx, transport.getDatabase());
        }
    }

    private static void execute(SessionRetryContext retryCtx, String database) {
        String tablePath = database + "/" + "java_example";

        TableDescription pets = TableDescription.newBuilder()
                .addNullableColumn("species", PrimitiveType.utf8())
                .addNullableColumn("name", PrimitiveType.utf8())
                .addNullableColumn("color", PrimitiveType.utf8())
                .addNullableColumn("price", PrimitiveType.float32())
                .setPrimaryKeys("species", "name")
                .build();

        retryCtx.supplyStatus(session -> session.createTable(tablePath, pets))
                .join().expect("ok");

        TableDescription desc = retryCtx.supplyResult(session -> session.describeTable(tablePath))
                .join().expect("ok");
        System.out.println("Columns reported by describeTable: " + desc.getColumns().stream()
                .map(TableColumn::getName).collect(Collectors.toList()));

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

        Params params = Params.of(
                "$species", PrimitiveValue.utf8("cat"),
                "$name", PrimitiveValue.utf8("Tom"),
                "$color", PrimitiveValue.utf8("black"),
                "$price", PrimitiveValue.float32(10.0f)
        );
        TxControl txRW = TxControl.serializableRw().setCommitTx(true);
        retryCtx.supplyResult(session -> session.executeDataQuery(upsertQuery, txRW, params))
                .join().expect("ok");

        String selectQuery = String.format(
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                        "\n" +
                        "DECLARE $species AS Utf8;\n" +
                        "DECLARE $name AS Utf8;\n" +
                        "SELECT * FROM java_example\n" +
                        "WHERE species = $species AND name = $name;",
                database);
        TxControl txRO = TxControl.onlineRo();
        ResultSetReader rsReader = retryCtx
                .supplyResult(session -> session.executeDataQuery(selectQuery, txRO, params))
                .join().expect("ok").getResultSet(0);
        System.out.println();
        System.out.println("Result of select query:");
        while (rsReader.next()) {
            System.out.printf("species: %s%n", rsReader.getColumn("species").getUtf8());
            System.out.printf("name: %s%n", rsReader.getColumn("name").getUtf8());
            System.out.printf("color: %s%n", rsReader.getColumn("color").getUtf8());
            System.out.printf("price: %f%n", rsReader.getColumn("price").getFloat32());
            System.out.println();
        }

        retryCtx.supplyStatus(session -> session.dropTable(tablePath))
                .join().expect("ok");
    }
}
