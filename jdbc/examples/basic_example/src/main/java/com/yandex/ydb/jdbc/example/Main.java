package com.yandex.ydb.jdbc.example;

import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
import com.yandex.ydb.core.StatusCode;
import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.jdbc.settings.YdbConnectionProperty;

import yandex.cloud.sdk.auth.provider.ApiKeyCredentialProvider;
import yandex.cloud.sdk.auth.provider.ComputeEngineCredentialProvider;
import yandex.cloud.sdk.auth.provider.CredentialProvider;
import yandex.cloud.sdk.auth.provider.IamTokenCredentialProvider;


public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar example.jar <connection_url>");
        }

        String connectionUrl = args[0];

        CredentialProvider credentialProvider;
        String accessToken = System.getenv("YDB_ACCESS_TOKEN_CREDENTIALS");
        String saKeyFile = System.getenv("YDB_SERVICE_ACCOUNT_KEY_FILE_CREDENTIALS");
        if (accessToken != null) {
            credentialProvider = IamTokenCredentialProvider.builder()
                    .token(accessToken)
                    .build();
        } else if (saKeyFile != null) {
            credentialProvider = ApiKeyCredentialProvider.builder()
                    .fromFile(Paths.get(saKeyFile))
                    .build();
        } else {
            credentialProvider = ComputeEngineCredentialProvider.builder()
                    .build();
        }
        AuthProvider authProvider = CloudAuthProvider.newAuthProvider(credentialProvider);

        Properties properties = new Properties();
        properties.put(YdbConnectionProperty.AUTH_PROVIDER.getName(), authProvider);
        properties.put(YdbConnectionProperty.SECURE_CONNECTION.getName(), "true");

        try (YdbConnection connection = (YdbConnection) DriverManager.getConnection(connectionUrl, properties)) {
            String tableName = "jdbc_table_sample";
            System.out.println(String.format("Trying to drop table %s...", tableName));
            try {
                connection.createStatement()
                        .execute("--jdbc:SCHEME\n" +
                                String.format("drop table %s", tableName));
                System.out.println(String.format("Table %s was successfully dropped.\n", tableName));
            } catch (SQLException e) {
                if (e instanceof YdbNonRetryableException
                        && ((YdbNonRetryableException) e).getStatusCode() == StatusCode.SCHEME_ERROR ) {
                    System.out.println(String.format("Failed to drop table %s because it's not yet created.\n", tableName));
                } else {
                    System.err.println(String.format("Failed to drop table %s", tableName));
                    e.printStackTrace();
                    return;
                }
            }

            System.out.println(String.format("Creating table %s...", tableName));
            connection.createStatement()
                    .execute("--jdbc:SCHEME\n" +
                            String.format("create table %s(id Int32, value Utf8, primary key (id))", tableName));
            System.out.println(String.format("Table %s was successfully created.\n", tableName));

            System.out.println("Upserting 2 rows into table...");
            YdbPreparedStatement ps = connection
                    .prepareStatement("" +
                            "declare $p1 as Int32;\n" +
                            "declare $p2 as Utf8;\n" +
                            String.format("upsert into %s (id, value) values ($p1, $p2)", tableName));
            ps.setInt(1, 1);
            ps.setString(2, "value-1");
            ps.executeUpdate();

            ps.setInt("p1", 2);
            ps.setString("p2", "value-2");
            ps.executeUpdate();

            connection.commit();
            System.out.println("Rows upserted.\n");

            YdbPreparedStatement select = connection
                    .prepareStatement(String.format("select count(1) as cnt from %s", tableName));

            {
                System.out.println("Selecting table rows count...");
                ResultSet rs = select.executeQuery();
                rs.next();
                long rowsCount = rs.getLong("cnt");
                assert(rowsCount == 2);
                System.out.println(String.format("Table has %d rows.\n", rowsCount));
            }

            System.out.println("Upserting 2 more rows into table...");
            YdbPreparedStatement psBatch = connection
                    .prepareStatement("" +
                            "declare $values as List<Struct<id:Int32,value:Utf8>>;\n" +
                            "upsert into jdbc_table_sample select * from as_table($values)");
            psBatch.setInt("id", 3);
            psBatch.setString("value", "value-3");
            psBatch.addBatch();

            psBatch.setInt("id", 4);
            psBatch.setString("value", "value-4");
            psBatch.addBatch();

            psBatch.executeBatch();

            connection.commit();
            System.out.println("Rows upserted.\n");

            {
                System.out.println("Selecting table rows count...");
                ResultSet rs = select.executeQuery();
                rs.next();
                long rowsCount = rs.getLong("cnt");
                assert(rowsCount == 4);
                System.out.println(String.format("Table has %d rows.\n", rowsCount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
