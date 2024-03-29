package com.yandex.ydb.table.rpc.grpc;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.grpc.GrpcRequestSettings;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.core.rpc.OperationTray;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.core.rpc.StreamControl;
import com.yandex.ydb.core.rpc.StreamObserver;
import com.yandex.ydb.table.YdbTable;
import com.yandex.ydb.table.YdbTable.ReadTableRequest;
import com.yandex.ydb.table.YdbTable.ReadTableResponse;
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
    public CompletableFuture<Result<CreateSessionResponse>> createSession(CreateSessionRequest request,
                                                                          GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getCreateSessionMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<DeleteSessionResponse>> deleteSession(DeleteSessionRequest request,
                                                                          GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getDeleteSessionMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<KeepAliveResponse>> keepAlive(KeepAliveRequest request,
                                                                  GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getKeepAliveMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<CreateTableResponse>> createTable(CreateTableRequest request,
                                                                      GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getCreateTableMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<DropTableResponse>> dropTable(DropTableRequest request,
                                                                  GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getDropTableMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<AlterTableResponse>> alterTable(AlterTableRequest request,
                                                                    GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getAlterTableMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<CopyTableResponse>> copyTable(CopyTableRequest request,
                                                                  GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getCopyTableMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<DescribeTableResponse>> describeTable(DescribeTableRequest request,
                                                                          GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getDescribeTableMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<ExplainDataQueryResponse>> explainDataQuery(ExplainDataQueryRequest request,
                                                                                GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getExplainDataQueryMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<PrepareDataQueryResponse>> prepareDataQuery(PrepareDataQueryRequest request,
                                                                                GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getPrepareDataQueryMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<ExecuteDataQueryResponse>> executeDataQuery(ExecuteDataQueryRequest request,
                                                                                GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getExecuteDataQueryMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<ExecuteSchemeQueryResponse>> executeSchemeQuery(ExecuteSchemeQueryRequest request,
                                                                                    GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getExecuteSchemeQueryMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<BeginTransactionResponse>> beginTransaction(BeginTransactionRequest request,
                                                                                GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getBeginTransactionMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<CommitTransactionResponse>> commitTransaction(CommitTransactionRequest request,
                                                                                  GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getCommitTransactionMethod(), request, settings);
    }

    @Override
    public CompletableFuture<Result<RollbackTransactionResponse>> rollbackTransaction(
            RollbackTransactionRequest request, GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getRollbackTransactionMethod(), request, settings);
    }

    @Override
    public StreamControl streamReadTable(ReadTableRequest request, StreamObserver<ReadTableResponse> observer,
                                         GrpcRequestSettings settings) {
        return transport.serverStreamCall(TableServiceGrpc.getStreamReadTableMethod(), request, observer, settings);
    }

    @Override
    public StreamControl streamExecuteScanQuery(YdbTable.ExecuteScanQueryRequest request,
            StreamObserver<YdbTable.ExecuteScanQueryPartialResponse> observer, GrpcRequestSettings settings) {
        return transport.serverStreamCall(TableServiceGrpc.getStreamExecuteScanQueryMethod(), request, observer,
                settings);
    }

    @Override
    public CompletableFuture<Result<YdbTable.BulkUpsertResponse>> bulkUpsert(YdbTable.BulkUpsertRequest request,
                                                                             GrpcRequestSettings settings) {
        return transport.unaryCall(TableServiceGrpc.getBulkUpsertMethod(), request, settings);
    }

    @Override
    public String getDatabase() {
        return transport.getDatabase();
    }

    @Override
    @Nullable
    public String getEndpointByNodeId(int nodeId) {
        return transport.getEndpointByNodeId(nodeId);
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
