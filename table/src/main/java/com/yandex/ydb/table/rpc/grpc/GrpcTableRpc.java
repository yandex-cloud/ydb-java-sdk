package com.yandex.ydb.table.rpc.grpc;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.core.rpc.OperationTray;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.table.rpc.TableRpc;
import com.yandex.ydb.table.v1.TableServiceGrpc;

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
@ParametersAreNonnullByDefault
public final class GrpcTableRpc implements TableRpc {

    private final GrpcTransport transport;
    private final boolean transportOwned;

    private GrpcTableRpc(GrpcTransport transport, boolean transportOwned) {
        this.transport = transport;
        this.transportOwned = transportOwned;
    }

    @Nullable
    public static GrpcTableRpc useTransport(@WillNotClose RpcTransport transport) {
        if (transport instanceof GrpcTransport) {
            return new GrpcTableRpc((GrpcTransport) transport, false);
        }
        return null;
    }

    @Nullable
    public static GrpcTableRpc ownTransport(@WillClose RpcTransport transport) {
        if (transport instanceof GrpcTransport) {
            return new GrpcTableRpc((GrpcTransport) transport, true);
        }
        return null;
    }

    @Override
    public CompletableFuture<Result<CreateSessionResponse>> createSession(CreateSessionRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_CREATE_SESSION, request);
    }

    @Override
    public CompletableFuture<Result<DeleteSessionResponse>> deleteSession(DeleteSessionRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_DELETE_SESSION, request);
    }

    @Override
    public CompletableFuture<Result<KeepAliveResponse>> keepAlive(KeepAliveRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_KEEP_ALIVE, request);
    }

    @Override
    public CompletableFuture<Result<CreateTableResponse>> createTable(CreateTableRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_CREATE_TABLE, request);
    }

    @Override
    public CompletableFuture<Result<DropTableResponse>> dropTable(DropTableRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_DROP_TABLE, request);
    }

    @Override
    public CompletableFuture<Result<AlterTableResponse>> alterTable(AlterTableRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_ALTER_TABLE, request);
    }

    @Override
    public CompletableFuture<Result<CopyTableResponse>> copyTable(CopyTableRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_COPY_TABLE, request);
    }

    @Override
    public CompletableFuture<Result<DescribeTableResponse>> describeTable(DescribeTableRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_DESCRIBE_TABLE, request);
    }

    @Override
    public CompletableFuture<Result<ExplainDataQueryResponse>> explainDataQuery(ExplainDataQueryRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_EXPLAIN_DATA_QUERY, request);
    }

    @Override
    public CompletableFuture<Result<PrepareDataQueryResponse>> prepareDataQuery(PrepareDataQueryRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_PREPARE_DATA_QUERY, request);
    }

    @Override
    public CompletableFuture<Result<ExecuteDataQueryResponse>> executeDataQuery(ExecuteDataQueryRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_EXECUTE_DATA_QUERY, request);
    }

    @Override
    public CompletableFuture<Result<ExecuteSchemeQueryResponse>> executeSchemeQuery(ExecuteSchemeQueryRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_EXECUTE_SCHEME_QUERY, request);
    }

    @Override
    public CompletableFuture<Result<BeginTransactionResponse>> beginTransaction(BeginTransactionRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_BEGIN_TRANSACTION, request);
    }

    @Override
    public CompletableFuture<Result<CommitTransactionResponse>> commitTransaction(CommitTransactionRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_COMMIT_TRANSACTION, request);
    }

    @Override
    public CompletableFuture<Result<RollbackTransactionResponse>> rollbackTransaction(RollbackTransactionRequest request) {
        return transport.unaryCall(TableServiceGrpc.METHOD_ROLLBACK_TRANSACTION, request);
    }

    @Override
    public OperationTray getOperationTray() {
        return transport.getOperationTray();
    }

    @Override
    public void close() {
        if (transportOwned) {
            transport.close();
        }
    }
}
