package com.yandex.ydb.examples.batch_upload;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.yandex.ydb.core.Status;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.examples.App;
import com.yandex.ydb.examples.AppRunner;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.ListValue;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.StructValue;
import com.yandex.ydb.table.values.Value;

import static com.yandex.ydb.table.values.PrimitiveValue.uint64;
import static com.yandex.ydb.table.values.PrimitiveValue.utf8;

public class BatchUpload implements App {
    private final String path;
    private final String tableName;
    private final TableClient tableClient;
    @Nullable
    private Session session;

    final int recordsCount = 60;

    BatchUpload(RpcTransport transport, String path) {
        this.path = path;
        this.tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
                .build();
        this.tableName = this.path + "/batch_upload";
        this.session = tableClient.createSession()
                .join()
                .expect("cannot create session");
    }

    static class Generator {
        private int remain;

        Generator(int count) {
            this.remain = count;
        }

        boolean isValid() {
            return remain > 0;
        }

        Value<?> get() {
            -- remain;

            long urlNo = remain;
            long hostNo = urlNo / 10;

            String url = String.format("http://host-%d.ru:80/path_with_id/%d", hostNo, urlNo);
            String host = String.format("host-%d.ru:80", hostNo);

            long urlUid = url.hashCode();
            long hostUid = host.hashCode();

            String page = String.format("the page were page_num='%d' Url='%s' UrlUid='%d' HostUid='%d'",
                    remain, url, urlUid, hostUid);

            return StructValue.of(
                    "HostUid", uint64(hostUid),
                    "UrlUid", uint64(urlUid),
                    "Url", utf8(url),
                    "Page", utf8(page)
            );
        }
    }

    public void run() {
        createTables();

        String query = String.format(
            "\n" +
            "DECLARE $items AS\n" +
            "List<Struct<\n" +
            "HostUid: Uint64,\n"+
                    "UrlUid: Uint64,\n"+
                    "Url: Utf8,\n"+
                    "Page: Utf8>>;\n"+

            "REPLACE INTO `%s`\n"+
            "SELECT * FROM AS_TABLE($items)\n", tableName);

        Generator input = new Generator(recordsCount);

        while (input.isValid()) {
            ArrayList<Value<?>> pack = new ArrayList<>();
            while (input.isValid() && pack.size() < 11) {
                pack.add(input.get());
            }

            executeBatch(query, pack);

            System.out.println(String.format("%d records uploaded", pack.size()));
        }
    }

    private void executeBatch(String query, ArrayList<Value<?>> pack) {
        Value values[] = new Value[pack.size()];
        pack.toArray(values);

        Params params = Params.of("$items", ListValue.of(values));

        TxControl txControl = TxControl.serializableRw().setCommitTx(true);
        DataQueryResult result = session.executeDataQuery(query, txControl, params).join().expect("expected success result");
    }

    private void createTables() {
        TableDescription seriesTable = TableDescription.newBuilder()
                .addNullableColumn("HostUid", PrimitiveType.uint64())
                .addNullableColumn("UrlUid", PrimitiveType.uint64())
                .addNullableColumn("Url", PrimitiveType.utf8())
                .addNullableColumn("Page", PrimitiveType.utf8())
                .setPrimaryKeys("HostUid", "UrlUid")
                .build();

        Status status = session.createTable(tableName, seriesTable).join();
        if (status != Status.SUCCESS) {
            System.out.println(String.format("Creation table failed with status: %s", status));
        }
    }

    @Override
    public void close() {
        if (session != null) {
            session.close()
                    .join()
                    .expect("cannot close session");
        }
        tableClient.close();
    }

    public static void main(String[] args) {
        AppRunner.run("BatchUpload", BatchUpload::new, args);
    }
}
