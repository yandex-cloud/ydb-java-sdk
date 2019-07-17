package com.yandex.ydb.table.rpc;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.rpc.Rpc;
import com.yandex.ydb.core.rpc.StreamObserver;
import com.yandex.ydb.table.YdbTable.ReadTableRequest;
import com.yandex.ydb.table.YdbTable.ReadTableResponse;

import static com.yandex.ydb.table.YdbTable.AlterTableRequest;
import static com.yandex.ydb.table.YdbTable.AlterTableResponse;
import static com.yandex.ydb.table.YdbTable.BeginTransactionRequest;
import static com.yandex.ydb.table.YdbTable.BeginTransactionResponse;
import static com.yandex.ydb.table.YdbTable.CommitTransactionRequest;
import static com.yandex.ydb.table.YdbTable.CommitTransactionResponse;
import static com.yandex.ydb.table.YdbTable.CopyTableRequest;
import static com.yandex.ydb.table.YdbTable.CopyTableResponse;
import static com.yandex.ydb.table.YdbTable.CreateSessionRequest;
import static com.yandex.ydb.table.YdbTable.CreateSessionResponse;
import static com.yandex.ydb.table.YdbTable.CreateTableRequest;
import static com.yandex.ydb.table.YdbTable.CreateTableResponse;
import static com.yandex.ydb.table.YdbTable.DeleteSessionRequest;
import static com.yandex.ydb.table.YdbTable.DeleteSessionResponse;
import static com.yandex.ydb.table.YdbTable.DescribeTableRequest;
import static com.yandex.ydb.table.YdbTable.DescribeTableResponse;
import static com.yandex.ydb.table.YdbTable.DropTableRequest;
import static com.yandex.ydb.table.YdbTable.DropTableResponse;
import static com.yandex.ydb.table.YdbTable.ExecuteDataQueryRequest;
import static com.yandex.ydb.table.YdbTable.ExecuteDataQueryResponse;
import static com.yandex.ydb.table.YdbTable.ExecuteSchemeQueryRequest;
import static com.yandex.ydb.table.YdbTable.ExecuteSchemeQueryResponse;
import static com.yandex.ydb.table.YdbTable.ExplainDataQueryRequest;
import static com.yandex.ydb.table.YdbTable.ExplainDataQueryResponse;
import static com.yandex.ydb.table.YdbTable.KeepAliveRequest;
import static com.yandex.ydb.table.YdbTable.KeepAliveResponse;
import static com.yandex.ydb.table.YdbTable.PrepareDataQueryRequest;
import static com.yandex.ydb.table.YdbTable.PrepareDataQueryResponse;
import static com.yandex.ydb.table.YdbTable.RollbackTransactionRequest;
import static com.yandex.ydb.table.YdbTable.RollbackTransactionResponse;


/**
 * @author Sergey Polovko
 */
public interface TableRpc extends Rpc {

    /**
     * Create new session. Implicit session creation is forbidden, so user must create new session
     * before execute any query, otherwise BAD_SESSION status wil be returned. Simultaneous execution
     * of requests are forbidden. Sessions are volatile, can be invalidated by server, e.g. in case
     * of fatal errors. All requests with this session will fail with BAD_SESSION status.
     * So, client must be able to handle BAD_SESSION status.
     */
    CompletableFuture<Result<CreateSessionResponse>> createSession(CreateSessionRequest request);

    /**
     * Ends a session, releasing server resources associated with it.
     */
    CompletableFuture<Result<DeleteSessionResponse>> deleteSession(DeleteSessionRequest request);

    /**
     * Idle sessions can be kept alive by calling KeepAlive periodically.
     */
    CompletableFuture<Result<KeepAliveResponse>> keepAlive(KeepAliveRequest request);

    /**
     * Creates new table.
     */
    CompletableFuture<Result<CreateTableResponse>> createTable(CreateTableRequest request);

    /**
     * Drop table.
     */
    CompletableFuture<Result<DropTableResponse>> dropTable(DropTableRequest request);

    /**
     * Modifies schema of given table.
     */
    CompletableFuture<Result<AlterTableResponse>> alterTable(AlterTableRequest request);

    /**
     * Creates copy of given table.
     */
    CompletableFuture<Result<CopyTableResponse>> copyTable(CopyTableRequest request);

    /**
     * Returns information about given table (metadata).
     */
    CompletableFuture<Result<DescribeTableResponse>> describeTable(DescribeTableRequest request);

    /**
     * Explains data query.
     * SessionId of previously created session must be provided.
     */
    CompletableFuture<Result<ExplainDataQueryResponse>> explainDataQuery(ExplainDataQueryRequest request);

    /**
     * Prepares data query, returns query id.
     * SessionId of previously created session must be provided.
     */
    CompletableFuture<Result<PrepareDataQueryResponse>> prepareDataQuery(PrepareDataQueryRequest request);

    /**
     * Executes data query.
     * SessionId of previously created session must be provided.
     */
    CompletableFuture<Result<ExecuteDataQueryResponse>> executeDataQuery(ExecuteDataQueryRequest request);

    /**
     * Executes scheme query.
     * SessionId of previously created session must be provided.
     */
    CompletableFuture<Result<ExecuteSchemeQueryResponse>> executeSchemeQuery(ExecuteSchemeQueryRequest request);

    /**
     * Begins new transaction.
     */
    CompletableFuture<Result<BeginTransactionResponse>> beginTransaction(BeginTransactionRequest request);

    /**
     * Commits specified active transaction.
     */
    CompletableFuture<Result<CommitTransactionResponse>> commitTransaction(CommitTransactionRequest request);

    /**
     * Performs a rollback of the specified active transaction.
     */
    CompletableFuture<Result<RollbackTransactionResponse>> rollbackTransaction(RollbackTransactionRequest request);

    /**
     * Streaming read table.
     */
    void streamReadTable(ReadTableRequest request, StreamObserver<ReadTableResponse> observer);
}
