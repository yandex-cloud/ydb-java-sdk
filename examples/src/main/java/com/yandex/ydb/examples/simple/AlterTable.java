package com.yandex.ydb.examples.simple;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.settings.AlterTableSettings;
import com.yandex.ydb.table.values.OptionalType;
import com.yandex.ydb.table.values.PrimitiveType;


/**
 * @author Sergey Polovko
 */
public class AlterTable extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
            .build();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        session.dropTable(tablePath)
            .join();

        String query =
            "CREATE TABLE [" + tablePath + "] (" +
            "  key Uint32," +
            "  value String," +
            "  PRIMARY KEY(key)" +
            ");";
        session.executeSchemeQuery(query)
            .join()
            .expect("cannot create table");

        {
            session.alterTable(tablePath, new AlterTableSettings()
                    .setTraceId("some-trace-id")
                    .addColumn("name", OptionalType.of(PrimitiveType.string()))
                    .addColumn("age", OptionalType.of(PrimitiveType.uint32()))
                    .dropColumn("value")
                ).join()
                .expect("cannot alter table");
        }

        TableDescription description = session.describeTable(tablePath)
            .join()
            .expect("cannot describe table");

        System.out.println("--[primary keys]-------------");
        int i = 1;
        for (String primaryKey : description.getPrimaryKeys()) {
            System.out.printf("%4d. %s\n", i++, primaryKey);
        }

        System.out.println("\n--[columns]------------------");
        i = 1;
        for (TableColumn column : description.getColumns()) {
            System.out.printf("%4d. %s %s\n", i++, column.getName(), column.getType());
        }

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new AlterTable().doMain();
    }
}
