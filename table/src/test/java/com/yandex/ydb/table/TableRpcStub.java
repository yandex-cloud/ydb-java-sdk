package com.yandex.ydb.table;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.protobuf.Message;
import com.yandex.ydb.OperationProtos.Operation;
import com.yandex.ydb.core.Issue;
import com.yandex.ydb.core.Operations;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.core.StatusCode;
import com.yandex.ydb.core.grpc.GrpcRequestSettings;
import com.yandex.ydb.core.rpc.OperationTray;
import com.yandex.ydb.core.rpc.StreamControl;
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


/**
 * @author Sergey Polovko
 */
public class TableRpcStub implements TableRpc {

    @Override
    public CompletableFuture<Result<CreateSessionResponse>> createSession(CreateSessionRequest request,
                                                                          GrpcRequestSettings settings) {
        return notImplemented("createSession()");
    }

    @Override
    public CompletableFuture<Result<DeleteSessionResponse>> deleteSession(DeleteSessionRequest request,
                                                                          GrpcRequestSettings settings) {
        return notImplemented("deleteSession()");
    }

    @Override
    public CompletableFuture<Result<KeepAliveResponse>> keepAlive(KeepAliveRequest request,
                                                                  GrpcRequestSettings settings) {
        return notImplemented("keepAlive()");
    }

    @Override
    public CompletableFuture<Result<CreateTableResponse>> createTable(CreateTableRequest request,
                                                                      GrpcRequestSettings settings) {
        return notImplemented("createTable()");
    }

    @Override
    public CompletableFuture<Result<DropTableResponse>> dropTable(DropTableRequest request,
                                                                  GrpcRequestSettings settings) {
        return notImplemented("dropTable()");
    }

    @Override
    public CompletableFuture<Result<AlterTableResponse>> alterTable(AlterTableRequest request,
                                                                    GrpcRequestSettings settings) {
        return notImplemented("alterTable()");
    }

    @Override
    public CompletableFuture<Result<CopyTableResponse>> copyTable(CopyTableRequest request,
                                                                  GrpcRequestSettings settings) {
        return notImplemented("copyTable()");
    }

    @Override
    public CompletableFuture<Result<DescribeTableResponse>> describeTable(DescribeTableRequest request,
                                                                          GrpcRequestSettings settings) {
        return notImplemented("describeTable()");
    }

    @Override
    public CompletableFuture<Result<ExplainDataQueryResponse>> explainDataQuery(ExplainDataQueryRequest request,
                                                                                GrpcRequestSettings settings) {
        return notImplemented("explainDataQuery()");
    }

    @Override
    public CompletableFuture<Result<PrepareDataQueryResponse>> prepareDataQuery(PrepareDataQueryRequest request,
                                                                                GrpcRequestSettings settings) {
        return notImplemented("prepareDataQuery()");
    }

    @Override
    public CompletableFuture<Result<ExecuteDataQueryResponse>> executeDataQuery(ExecuteDataQueryRequest request,
                                                                                GrpcRequestSettings settings) {
        return notImplemented("executeDataQuery()");
    }

    @Override
    public CompletableFuture<Result<ExecuteSchemeQueryResponse>> executeSchemeQuery(ExecuteSchemeQueryRequest request,
                                                                                    GrpcRequestSettings settings) {
        return notImplemented("executeSchemeQuery()");
    }

    @Override
    public CompletableFuture<Result<BeginTransactionResponse>> beginTransaction(BeginTransactionRequest request,
                                                                                GrpcRequestSettings settings) {
        return notImplemented("beginTransaction()");
    }

    @Override
    public CompletableFuture<Result<CommitTransactionResponse>> commitTransaction(CommitTransactionRequest request,
                                                                                  GrpcRequestSettings settings) {
        return notImplemented("commitTransaction()");
    }

    @Override
    public CompletableFuture<Result<RollbackTransactionResponse>> rollbackTransaction(
            RollbackTransactionRequest request, GrpcRequestSettings settings) {
        return notImplemented("rollbackTransaction()");
    }

    @Override
    public StreamControl streamReadTable(ReadTableRequest request, StreamObserver<ReadTableResponse> observer,
                                         GrpcRequestSettings settings) {
        Issue issue = Issue.of("streamReadTable() is not implemented", Issue.Severity.ERROR);
        observer.onError(Status.of(StatusCode.CLIENT_INTERNAL_ERROR, issue));
        return () -> {};
    }

    @Override
    public StreamControl streamExecuteScanQuery(YdbTable.ExecuteScanQueryRequest request,
            StreamObserver<YdbTable.ExecuteScanQueryPartialResponse> observer, GrpcRequestSettings settings) {
        Issue issue = Issue.of("streamExecuteScanQuery() is not implemented", Issue.Severity.ERROR);
        observer.onError(Status.of(StatusCode.CLIENT_INTERNAL_ERROR, issue));
        return () -> {};
    }

    @Override
    public CompletableFuture<Result<YdbTable.BulkUpsertResponse>> bulkUpsert(YdbTable.BulkUpsertRequest request,
                                                                             GrpcRequestSettings settings) {
        return notImplemented("bulkUpsert()");
    }

    @Override
    public String getDatabase() {
        return "";
    }

    @Override
    @Nullable
    public String getEndpointByNodeId(int nodeId) {
        return null;
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
        public CompletableFuture<Status> waitStatus(Operation operation, GrpcRequestSettings settings) {
            return CompletableFuture.completedFuture(Operations.status(operation));
        }

        @Override
        public <M extends Message, R> CompletableFuture<Result<R>> waitResult(
            Operation operation, Class<M> resultClass, Function<M, R> mapper, GrpcRequestSettings settings)
        {
            Status status = Operations.status(operation);
            if (status.isSuccess()) {
                double comsumedRu = operation.getCostInfo().getConsumedUnits();
                M resultMessage = Operations.unpackResult(operation, resultClass);
                return CompletableFuture.completedFuture(Result.success(
                        mapper.apply(resultMessage),
                        comsumedRu,
                        status.getIssues()));
            }
            return CompletableFuture.completedFuture(Result.fail(status));
        }

        @Override
        public void close() {
            // nop
        }
    }
}
