package com.yandex.ydb.table;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.protobuf.Message;
import com.yandex.ydb.OperationProtos.Operation;
import com.yandex.ydb.core.Issue;
import com.yandex.ydb.core.Operations;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.core.StatusCode;
import com.yandex.ydb.core.rpc.OperationTray;
import com.yandex.ydb.core.rpc.StreamObserver;
import com.yandex.ydb.table.YdbTable.AlterTableRequest;
import com.yandex.ydb.table.YdbTable.AlterTableResponse;
import com.yandex.ydb.table.YdbTable.BeginTransactionRequest;
import com.yandex.ydb.table.YdbTable.BeginTransactionResponse;
import com.yandex.ydb.table.YdbTable.CommitTransactionRequest;
import com.yandex.ydb.table.YdbTable.CommitTransactionResponse;
import com.yandex.ydb.table.YdbTable.CopyTableRequest;
import com.yandex.ydb.table.YdbTable.CopyTableResponse;
import com.yandex.ydb.table.YdbTable.CreateSessionRequest;
import com.yandex.ydb.table.YdbTable.CreateSessionResponse;
import com.yandex.ydb.table.YdbTable.CreateTableRequest;
import com.yandex.ydb.table.YdbTable.CreateTableResponse;
import com.yandex.ydb.table.YdbTable.DeleteSessionRequest;
import com.yandex.ydb.table.YdbTable.DeleteSessionResponse;
import com.yandex.ydb.table.YdbTable.DescribeTableRequest;
import com.yandex.ydb.table.YdbTable.DescribeTableResponse;
import com.yandex.ydb.table.YdbTable.DropTableRequest;
import com.yandex.ydb.table.YdbTable.DropTableResponse;
import com.yandex.ydb.table.YdbTable.ExecuteDataQueryRequest;
import com.yandex.ydb.table.YdbTable.ExecuteDataQueryResponse;
import com.yandex.ydb.table.YdbTable.ExecuteSchemeQueryRequest;
import com.yandex.ydb.table.YdbTable.ExecuteSchemeQueryResponse;
import com.yandex.ydb.table.YdbTable.ExplainDataQueryRequest;
import com.yandex.ydb.table.YdbTable.ExplainDataQueryResponse;
import com.yandex.ydb.table.YdbTable.KeepAliveRequest;
import com.yandex.ydb.table.YdbTable.KeepAliveResponse;
import com.yandex.ydb.table.YdbTable.PrepareDataQueryRequest;
import com.yandex.ydb.table.YdbTable.PrepareDataQueryResponse;
import com.yandex.ydb.table.YdbTable.ReadTableRequest;
import com.yandex.ydb.table.YdbTable.ReadTableResponse;
import com.yandex.ydb.table.YdbTable.RollbackTransactionRequest;
import com.yandex.ydb.table.YdbTable.RollbackTransactionResponse;
import com.yandex.ydb.table.rpc.TableRpc;
import com.yandex.ydb.table.utils.Async;
import com.yandex.yql.proto.IssueSeverity.TSeverityIds.ESeverityId;

import static java.util.concurrent.CompletableFuture.completedFuture;


/**
 * @author Sergey Polovko
 */
public class TableRpcStub implements TableRpc {

    @Override
    public CompletableFuture<Result<CreateSessionResponse>> createSession(CreateSessionRequest request, long deadlineAfter) {
        return notImplemented("createSession()");
    }

    @Override
    public CompletableFuture<Result<DeleteSessionResponse>> deleteSession(DeleteSessionRequest request, long deadlineAfter) {
        return notImplemented("deleteSession()");
    }

    @Override
    public CompletableFuture<Result<KeepAliveResponse>> keepAlive(KeepAliveRequest request, long deadlineAfter) {
        return notImplemented("keepAlive()");
    }

    @Override
    public CompletableFuture<Result<CreateTableResponse>> createTable(CreateTableRequest request, long deadlineAfter) {
        return notImplemented("createTable()");
    }

    @Override
    public CompletableFuture<Result<DropTableResponse>> dropTable(DropTableRequest request, long deadlineAfter) {
        return notImplemented("dropTable()");
    }

    @Override
    public CompletableFuture<Result<AlterTableResponse>> alterTable(AlterTableRequest request, long deadlineAfter) {
        return notImplemented("alterTable()");
    }

    @Override
    public CompletableFuture<Result<CopyTableResponse>> copyTable(CopyTableRequest request, long deadlineAfter) {
        return notImplemented("copyTable()");
    }

    @Override
    public CompletableFuture<Result<DescribeTableResponse>> describeTable(DescribeTableRequest request, long deadlineAfter) {
        return notImplemented("describeTable()");
    }

    @Override
    public CompletableFuture<Result<ExplainDataQueryResponse>> explainDataQuery(ExplainDataQueryRequest request, long deadlineAfter) {
        return notImplemented("explainDataQuery()");
    }

    @Override
    public CompletableFuture<Result<PrepareDataQueryResponse>> prepareDataQuery(PrepareDataQueryRequest request, long deadlineAfter) {
        return notImplemented("prepareDataQuery()");
    }

    @Override
    public CompletableFuture<Result<ExecuteDataQueryResponse>> executeDataQuery(ExecuteDataQueryRequest request, long deadlineAfter) {
        return notImplemented("executeDataQuery()");
    }

    @Override
    public CompletableFuture<Result<ExecuteSchemeQueryResponse>> executeSchemeQuery(ExecuteSchemeQueryRequest request, long deadlineAfter) {
        return notImplemented("executeSchemeQuery()");
    }

    @Override
    public CompletableFuture<Result<BeginTransactionResponse>> beginTransaction(BeginTransactionRequest request, long deadlineAfter) {
        return notImplemented("beginTransaction()");
    }

    @Override
    public CompletableFuture<Result<CommitTransactionResponse>> commitTransaction(CommitTransactionRequest request, long deadlineAfter) {
        return notImplemented("commitTransaction()");
    }

    @Override
    public CompletableFuture<Result<RollbackTransactionResponse>> rollbackTransaction(RollbackTransactionRequest request, long deadlineAfter) {
        return notImplemented("rollbackTransaction()");
    }

    @Override
    public void streamReadTable(ReadTableRequest request, StreamObserver<ReadTableResponse> observer, long deadlineAfter) {
        Issue issue = Issue.of("streamReadTable() is not implemented", ESeverityId.S_ERROR);
        observer.onError(Status.of(StatusCode.CLIENT_INTERNAL_ERROR, issue));
    }

    @Override
    public String getDatabase() {
        return "";
    }

    @Override
    public OperationTray getOperationTray() {
        return new ImmediateOperationTray();
    }

    @Override
    public void close() {
        // nop
    }

    private static <U> CompletableFuture<U> notImplemented(String method) {
        return Async.failedFuture(new UnsupportedOperationException(method + " not implemented"));
    }

    /**
     * IMMEDIATE OPERATION TRAY
     */
    private static final class ImmediateOperationTray implements OperationTray {
        @Override
        public CompletableFuture<Status> waitStatus(Operation operation, long deadlineAfter) {
            return completedFuture(Operations.status(operation));
        }

        @Override
        public <M extends Message, R> CompletableFuture<Result<R>> waitResult(
            Operation operation, Class<M> resultClass, Function<M, R> mapper, long deadlineAfter)
        {
            Status status = Operations.status(operation);
            if (status.isSuccess()) {
                M resultMessage = Operations.unpackResult(operation, resultClass);
                return completedFuture(Result.success(mapper.apply(resultMessage)));
            }
            return completedFuture(Result.fail(status));
        }

        @Override
        public void close() {
            // nop
        }
    }
}
