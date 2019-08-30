package com.yandex.ydb.examples.simple;

import com.google.common.collect.ImmutableList;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.description.TableIndex;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.settings.CreateTableSettings;
import com.yandex.ydb.table.settings.PartitioningPolicy;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import com.yandex.ydb.table.values.TupleValue;


/**
 * @author Sergey Polovko
 */
public class CreateTable extends SimpleExample {

    @Override
    void run(RpcTransport transport, String pathPrefix) {
        try (TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport)).build()) {
            Session session = tableClient.createSession()
                .join()
                .expect("cannot create session");

            {
                String tablePath = pathPrefix + "UniformPartitionedTable";
                session.dropTable(tablePath).join();
                createUniformPartitionedTable(tablePath, session);
                printTableScheme(tablePath, session);
            }

            {
                String tablePath = pathPrefix + "ManuallyPartitionedTable";
                session.dropTable(tablePath).join();
                createManuallyPartitionedTable(tablePath, session);
                printTableScheme(tablePath, session);
            }

            {
                String tablePath = pathPrefix + "TableWithIndexes";
                session.dropTable(tablePath).join();
                createTableWithIndexes(tablePath, session);
                printTableScheme(tablePath, session);
            }

            session.close()
                .join()
                .expect("cannot close session");
        }
    }

    private void createTableWithIndexes(String tablePath, Session session) {
        TableDescription description = TableDescription.newBuilder()
            .addNullableColumn("uid", PrimitiveType.uint64())
            .addNullableColumn("login", PrimitiveType.utf8())
            .addNullableColumn("firstName", PrimitiveType.utf8())
            .addNullableColumn("lastName", PrimitiveType.utf8())
            .setPrimaryKey("uid")
            .addGlobalIndex("loginIdx", ImmutableList.of("login"))
            .addGlobalIndex("nameIdx", ImmutableList.of("firstName", "lastName"))
            .build();

        session.createTable(tablePath, description)
            .join()
            .expect("cannot create table " + tablePath);
    }

    /**
     * Will create table with 4 partitions (split by values of "hash" column)
     *      1: [0x00000000, 0x3fffffff]
     *      2: [0x40000000, 0x7fffffff]
     *      3: [0x80000000, 0xbfffffff]
     *      4: [0xc0000000, 0xffffffff]
     */
    private void createUniformPartitionedTable(String tablePath, Session session) {
        TableDescription description = TableDescription.newBuilder()
            .addNullableColumn("hash", PrimitiveType.uint32())
            .addNullableColumn("name", PrimitiveType.utf8())
            .addNullableColumn("salary", PrimitiveType.float64())
            .setPrimaryKeys("hash", "name")  // uniform partitioning requires Uint32 / Uint64 as a first key column
            .build();

        CreateTableSettings settings = new CreateTableSettings()
            .setPartitioningPolicy(new PartitioningPolicy().setUniformPartitions(4));

        session.createTable(tablePath, description, settings)
            .join()
            .expect("cannot create table " + tablePath);
    }

    /**
     * Will create table with 3 partitions (split by values of "name" column)
     *      1: [empty(), "a"]
     *      2: [next("a"), "n"]
     *      3: [next("n"), empty()]
     */
    private void createManuallyPartitionedTable(String tablePath, Session session) {
        TableDescription description = TableDescription.newBuilder()
            .addNullableColumn("name", PrimitiveType.utf8())
            .addNullableColumn("salary", PrimitiveType.float64())
            .setPrimaryKey("name")
            .build();

        CreateTableSettings settings = new CreateTableSettings()
            .setPartitioningPolicy(new PartitioningPolicy()
                .addExplicitPartitioningPoint(makeKey("a"))
                .addExplicitPartitioningPoint(makeKey("n")));

        session.createTable(tablePath, description, settings)
            .join()
            .expect("cannot create table " + tablePath);
    }

    private static TupleValue makeKey(String value) {
        return TupleValue.of(PrimitiveValue.utf8(value).makeOptional());
    }

    private void printTableScheme(String tablePath, Session session) {
        TableDescription description = session.describeTable(tablePath)
            .join()
            .expect("cannot describe table " + tablePath);

        System.out.println("--[" + tablePath + "]-----------");
        System.out.println("primary keys:");
        int i = 1;
        for (String primaryKey : description.getPrimaryKeys()) {
            System.out.printf("%4d. %s\n", i++, primaryKey);
        }

        System.out.println("columns:");
        i = 1;
        for (TableColumn column : description.getColumns()) {
            System.out.printf("%4d. %s %s\n", i++, column.getName(), column.getType());
        }

        if (!description.getIndexes().isEmpty()) {
            System.out.println("indexes:");
            i = 1;
            for (TableIndex index : description.getIndexes()) {
                System.out.printf("%4d. %s %s %s\n", i++, index.getType(), index.getName(), index.getColumns());
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        new CreateTable().doMain();
    }
}
