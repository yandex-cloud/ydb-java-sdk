package com.yandex.ydb.table;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQuery;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.ExplainDataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.settings.AlterTableSettings;
import com.yandex.ydb.table.settings.BeginTxSettings;
import com.yandex.ydb.table.settings.CloseSessionSettings;
import com.yandex.ydb.table.settings.CommitTxSettings;
import com.yandex.ydb.table.settings.CopyTableSettings;
import com.yandex.ydb.table.settings.CreateTableSettings;
import com.yandex.ydb.table.settings.DescribeTableSettings;
import com.yandex.ydb.table.settings.DropTableSettings;
import com.yandex.ydb.table.settings.ExecuteDataQuerySettings;
import com.yandex.ydb.table.settings.ExecuteScanQuerySettings;
import com.yandex.ydb.table.settings.ExecuteSchemeQuerySettings;
import com.yandex.ydb.table.settings.ExplainDataQuerySettings;
import com.yandex.ydb.table.settings.KeepAliveSessionSettings;
import com.yandex.ydb.table.settings.PrepareDataQuerySettings;
import com.yandex.ydb.table.settings.ReadTableSettings;
import com.yandex.ydb.table.settings.RollbackTxSettings;
import com.yandex.ydb.table.transaction.Transaction;
import com.yandex.ydb.table.transaction.TransactionMode;
import com.yandex.ydb.table.transaction.TxControl;


/**
 * @author Sergey Polovko
 */
public interface Session {

    String getId();

    CompletableFuture<Status> createTable(String path, TableDescription tableDescriptions, CreateTableSettings settings);

    default CompletableFuture<Status> createTable(String path, TableDescription tableDescriptions) {
        return createTable(path, tableDescriptions, new CreateTableSettings());
    }

    CompletableFuture<Status> dropTable(String path, DropTableSettings settings);

    default CompletableFuture<Status> dropTable(String path) {
        return dropTable(path, new DropTableSettings());
    }

    CompletableFuture<Status> alterTable(String path, AlterTableSettings settings);

    default CompletableFuture<Status> alterTable(String path) {
        return alterTable(path, new AlterTableSettings());
    }

    CompletableFuture<Status> copyTable(String src, String dst, CopyTableSettings settings);

    default CompletableFuture<Status> copyTable(String src, String dst) {
        return copyTable(src, dst, new CopyTableSettings());
    }

    CompletableFuture<Result<TableDescription>> describeTable(String path, DescribeTableSettings settings);

    default CompletableFuture<Result<TableDescription>> describeTable(String path) {
        return describeTable(path, new DescribeTableSettings());
    }

    CompletableFuture<Result<DataQueryResult>> executeDataQuery(
        String query,
        TxControl txControl,
        Params params,
        ExecuteDataQuerySettings settings);

    default CompletableFuture<Result<DataQueryResult>> executeDataQuery(String query, TxControl txControl, Params params) {
        return executeDataQuery(query, txControl, params, new ExecuteDataQuerySettings());
    }

    default CompletableFuture<Result<DataQueryResult>> executeDataQuery(String query, TxControl txControl) {
        return executeDataQuery(query, txControl, Params.empty(), new ExecuteDataQuerySettings());
    }

    CompletableFuture<Result<DataQuery>> prepareDataQuery(String query, PrepareDataQuerySettings settings);

    default CompletableFuture<Result<DataQuery>> prepareDataQuery(String query) {
        return prepareDataQuery(query, new PrepareDataQuerySettings());
    }

    CompletableFuture<Status> executeSchemeQuery(String query, ExecuteSchemeQuerySettings settings);

    default CompletableFuture<Status> executeSchemeQuery(String query) {
        return executeSchemeQuery(query, new ExecuteSchemeQuerySettings());
    }

    CompletableFuture<Result<ExplainDataQueryResult>> explainDataQuery(String query, ExplainDataQuerySettings settings);

    default CompletableFuture<Result<ExplainDataQueryResult>> explainDataQuery(String query) {
        return explainDataQuery(query, new ExplainDataQuerySettings());
    }

    CompletableFuture<Result<Transaction>> beginTransaction(TransactionMode transactionMode, BeginTxSettings settings);

    default CompletableFuture<Result<Transaction>> beginTransaction(TransactionMode transactionMode) {
        return beginTransaction(transactionMode, new BeginTxSettings());
    }

    CompletableFuture<Status> commitTransaction(String txId, CommitTxSettings settings);

    CompletableFuture<Status> rollbackTransaction(String txId, RollbackTxSettings settings);

    CompletableFuture<Status> readTable(String tablePath, ReadTableSettings settings, Consumer<ResultSetReader> fn);

    CompletableFuture<Status> executeScanQuery(String query, Params params, ExecuteScanQuerySettings settings, Consumer<ResultSetReader> fn);

    CompletableFuture<Result<SessionStatus>> keepAlive(KeepAliveSessionSettings settings);

    default CompletableFuture<Result<SessionStatus>> keepAlive() {
        return keepAlive(new KeepAliveSessionSettings());
    }

    void invalidateQueryCache();

    boolean release();

    CompletableFuture<Status> close(CloseSessionSettings settings);

    default CompletableFuture<Status> close() {
        return close(new CloseSessionSettings());
    }
}
