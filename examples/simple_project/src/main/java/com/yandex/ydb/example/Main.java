package com.yandex.ydb.example;

import java.io.IOException;

import com.yandex.ydb.auth.iam.CloudAuthHelper;
import com.yandex.ydb.core.UnexpectedResultException;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String TABLE_NAME = "simple_project";

    private Main() { }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java -jar ydb-simple-project.jar <connection-string>");
            return;
        }

        String connectionString = args[0];

        GrpcTransport transport = GrpcTransport.forConnectionString(connectionString)
                .withAuthProvider(CloudAuthHelper.getAuthProviderFromEnviron())
                .build();

        GrpcTableRpc rpc = GrpcTableRpc.ownTransport(transport);
        try (TableClient tableClient = TableClient.newClient(rpc).build()) {
            SessionRetryContext retryCtx = SessionRetryContext.create(tableClient).build();

            createTable(retryCtx, transport.getDatabase(), TABLE_NAME);
            describeTable(retryCtx, transport.getDatabase(), TABLE_NAME);
            upsertData(retryCtx, TABLE_NAME);
            selectData(retryCtx, TABLE_NAME);
            dropTable(retryCtx, transport.getDatabase(), TABLE_NAME);
        } catch (UnexpectedResultException e) {
            logger.error("Unexpected", e);
        }
    }

    private static void createTable(SessionRetryContext retryCtx, String database, String tableName) {
        TableDescription pets = TableDescription.newBuilder()
                .addNullableColumn("species", PrimitiveType.utf8())
                .addNullableColumn("name", PrimitiveType.utf8())
                .addNullableColumn("color", PrimitiveType.utf8())
                .addNullableColumn("price", PrimitiveType.float32())
                .setPrimaryKeys("species", "name")
                .build();

        String tablePath = database + "/" + tableName;
        retryCtx.supplyStatus(session -> session.createTable(tablePath, pets))
                .join().expect("ok");
    }

    private static void describeTable(SessionRetryContext retryCtx, String database, String tableName) {
        String tablePath = database + "/" + tableName;
        TableDescription desc = retryCtx.supplyResult(session -> session.describeTable(tablePath))
                .join().expect("ok");
        logger.info("Columns reported by describeTable:");
        desc.getColumns().forEach(column -> {
            logger.info(" {} : {}", column.getName(), column.getType());
        });
    }

    private static void upsertData(SessionRetryContext retryCtx, String tableName) {
        String upsertQuery
                = "DECLARE $species AS Utf8;"
                + "DECLARE $name AS Utf8;"
                + "DECLARE $color AS Utf8;"
                + "DECLARE $price AS Float;"
                + "UPSERT INTO " + tableName + " (species, name, color, price)"
                + "VALUES($species, $name, $color, $price);";

        Params params = Params.of(
                "$species", PrimitiveValue.utf8("cat"),
                "$name", PrimitiveValue.utf8("Tom"),
                "$color", PrimitiveValue.utf8("black"),
                "$price", PrimitiveValue.float32(10.0f)
        );
        TxControl tx = TxControl.serializableRw().setCommitTx(true);
        retryCtx.supplyResult(session -> session.executeDataQuery(upsertQuery, tx, params))
                .join().expect("ok");
    }

    private static void selectData(SessionRetryContext retryCtx, String tableName) {
        String selectQuery
                = "DECLARE $species AS Utf8;"
                + "DECLARE $name AS Utf8;"
                + "SELECT * FROM " + tableName + " "
                + "WHERE species = $species AND name = $name;";

        Params params = Params.of(
                "$species", PrimitiveValue.utf8("cat"),
                "$name", PrimitiveValue.utf8("Tom")
        );

        TxControl tx = TxControl.onlineRo();
        ResultSetReader rsReader = retryCtx
                .supplyResult(session -> session.executeDataQuery(selectQuery, tx, params))
                .join().expect("ok").getResultSet(0);

        logger.info("Result of select query:");
        while (rsReader.next()) {
            logger.info("  species: {}, name: {}, color: {}, price: {}",
                    rsReader.getColumn("species").getUtf8(),
                    rsReader.getColumn("name").getUtf8(),
                    rsReader.getColumn("color").getUtf8(),
                    rsReader.getColumn("price").getFloat32()
            );
        }
    }

    private static void dropTable(SessionRetryContext retryCtx, String database, String tableName) {
        String tablePath = database + "/" + tableName;
        retryCtx.supplyStatus(session -> session.dropTable(tablePath))
                .join().expect("ok");
    }
}
