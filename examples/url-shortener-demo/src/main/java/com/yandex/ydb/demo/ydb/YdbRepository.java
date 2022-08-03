package com.yandex.ydb.demo.ydb;

import java.util.Optional;

import com.yandex.ydb.core.Status;
import com.yandex.ydb.core.UnexpectedResultException;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.PrimitiveValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexandr Gorshenin
 */
public class YdbRepository {
    private static final Logger log = LoggerFactory.getLogger(YdbRepository.class);

    private static final String TABLE_NAME = "urls";
    private static final TableDescription TABLE_DESCRIPTION = TableDescription.newBuilder()
            .addNullableColumn("src", PrimitiveType.utf8())
            .addNullableColumn("hash", PrimitiveType.utf8())
            .setPrimaryKey("hash")
            .build();

    private final YdbDriver driver;
    private final String tablePath;

    public YdbRepository(YdbDriver driver) {
        this.driver = driver;
        this.tablePath = driver.database() + "/" + TABLE_NAME;
    }

    public void initTable() throws YdbException {
        try {
            Status dropResult = driver.retryCtx()
                    .supplyStatus(session -> session.dropTable(tablePath))
                    .join();

            if (!dropResult.isSuccess()) {
                log.info("can't drop table");
            }

            driver.retryCtx()
                    .supplyStatus(session -> session.createTable(tablePath, TABLE_DESCRIPTION))
                    .join().expect("can't create table " + tablePath);
        } catch (UnexpectedResultException e) {
            log.error("init table problem", e);
            throw new YdbException(e.getMessage(), e);
        }
    }

    public void insertRecord(UrlRecord record) throws YdbException  {
        try {
            String query = "DECLARE $url AS utf8;\n"
                    + "DECLARE $hash AS utf8;\n"
                    + "UPSERT INTO " + TABLE_NAME + "(src, hash) VALUES ($url, $hash);";

            Params params = Params.of(
                "$url", PrimitiveValue.utf8(record.url()),
                "$hash", PrimitiveValue.utf8(record.hash())
            );

            TxControl txControl = TxControl.serializableRw().setCommitTx(true);

            driver.retryCtx()
                    .supplyResult(session -> session.executeDataQuery(query, txControl, params))
                    .join().expect("can't read query result");
        } catch (UnexpectedResultException e) {
            log.error("insert record problem", e);
            throw new YdbException(e.getMessage(), e);
        }
    }

    public Optional<UrlRecord> findByHash(String hash) throws YdbException  {
        try {
            String query = "DECLARE $hash AS utf8;\n"
                    + "SELECT * FROM " + TABLE_NAME + " WHERE hash=$hash;";

            Params params = Params.of(
                "$hash", PrimitiveValue.utf8(hash)
            );

            TxControl txControl = TxControl.serializableRw();

            DataQueryResult result = driver.retryCtx()
                    .supplyResult(session -> session.executeDataQuery(query, txControl, params))
                    .join().expect("can't read query result");

            if (result.isEmpty()) {
                return Optional.empty();
            }

            // First SELECT from query
            ResultSetReader rs = result.getResultSet(0);
            if (!rs.next()) {
                return Optional.empty();
            }

            String rowHash = rs.getColumn("hash").getUtf8();
            String rowSource = rs.getColumn("src").getUtf8();

            return Optional.of(new UrlRecord(rowHash, rowSource));
        } catch (UnexpectedResultException e) {
            log.error("select record problem", e);
            throw new YdbException(e.getMessage(), e);
        }
    }
}
