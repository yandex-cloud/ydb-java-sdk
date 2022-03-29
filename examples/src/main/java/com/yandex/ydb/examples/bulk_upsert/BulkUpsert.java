package com.yandex.ydb.examples.bulk_upsert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.examples.App;
import com.yandex.ydb.examples.AppRunner;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.settings.BulkUpsertSettings;
import com.yandex.ydb.table.values.ListValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkUpsert implements App {
    private static final Logger log = LoggerFactory.getLogger(BulkUpsert.class);

    private static final String TABLE_NAME = "bulk_upsert";
    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_COUNT = 50;

    private final String path;
    private final String tablePath;

    private final TableClient tableClient;

    @Nullable
    private final Session session;

    private final Instant now = Instant.now();

    BulkUpsert(RpcTransport transport, String path) {
        this.path = path;
        this.tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
                .build();
        this.tablePath = this.path + "/" + TABLE_NAME;

        this.session = tableClient.createSession()
                .join()
                .expect("cannot create session");
    }

    private List<LogRecord> getLogBatch(int offset) {
        List<LogRecord> list = new ArrayList<>(BATCH_SIZE);
        for (int idx = 0; idx < BATCH_SIZE; idx +=1 ) {
            String app = "App_" + String.valueOf(idx / 256);
            Instant timestamp = now.plusSeconds(offset * BATCH_SIZE + idx);
            String host = "192.168.0." + offset % 256;
            int httpCode = idx % 113 == 0 ? 404 : 200;
            String message = idx % 3 == 0 ? "GET / HTTP/1.1" : "GET /images/logo.png HTTP/1.1";

            list.add(new LogRecord(app, timestamp, host, httpCode, message));
        }
        return list;
    }

    private void createTables() {
        log.info("drop table {} if exist", tablePath);
        if (!session.dropTable(tablePath).join().isSuccess()) {
            log.info("drop table failed");
        }

        TableDescription.Builder table = TableDescription.newBuilder();
        for (int idx = 0; idx < LogRecord.COLUMNS.getMembersCount(); idx += 1) {
            table.addNullableColumn(
                    LogRecord.COLUMNS.getMemberName(idx),
                    LogRecord.COLUMNS.getMemberType(idx));
        }
        table.setPrimaryKeys(LogRecord.PRIMARY_KEYS);

        log.info("create table {}", tablePath);
        session.createTable(tablePath, table.build()).join()
                .expect("create table fail");
    }

    private void writeLogBatch(List<LogRecord> items) {
        ListValue rows = LogRecord.toListValue(items);
        session.executeBulkUpsert(tablePath, rows, new BulkUpsertSettings())
                .join()
                .expect("bulk upsert problem");
    }

    @Override
    public void run() {
        createTables();

        for (int idx = 0; idx < BATCH_COUNT; idx += 1) {
            List<LogRecord> batch = getLogBatch(idx);
            log.info("write blog batch with offset {}", idx);
            writeLogBatch(batch);
        }
    }

    @Override
    public void close() {
        if (session != null) {
            session.close().join().expect("cannot close session");
        }
        tableClient.close();
    }

    public static int test(String[] args) {
        return AppRunner.safeRun("BulkUpsert", BulkUpsert::new, args);
    }

    public static void main(String[] args) {
        AppRunner.run("BulkUpsert", BulkUpsert::new, args);
    }
}
