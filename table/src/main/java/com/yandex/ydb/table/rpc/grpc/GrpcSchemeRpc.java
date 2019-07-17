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
import com.yandex.ydb.scheme.v1.SchemeServiceGrpc;
import com.yandex.ydb.table.rpc.SchemeRpc;

import static com.yandex.ydb.scheme.SchemeOperationProtos.DescribePathRequest;
import static com.yandex.ydb.scheme.SchemeOperationProtos.DescribePathResponse;
import static com.yandex.ydb.scheme.SchemeOperationProtos.ListDirectoryRequest;
import static com.yandex.ydb.scheme.SchemeOperationProtos.ListDirectoryResponse;
import static com.yandex.ydb.scheme.SchemeOperationProtos.MakeDirectoryRequest;
import static com.yandex.ydb.scheme.SchemeOperationProtos.MakeDirectoryResponse;
import static com.yandex.ydb.scheme.SchemeOperationProtos.RemoveDirectoryRequest;
import static com.yandex.ydb.scheme.SchemeOperationProtos.RemoveDirectoryResponse;


/**
 * @author Sergey Polovko
 */
@ParametersAreNonnullByDefault
public final class GrpcSchemeRpc implements SchemeRpc {

    private final GrpcTransport transport;
    private final boolean transportOwned;

    private GrpcSchemeRpc(GrpcTransport transport, boolean transportOwned) {
        this.transport = transport;
        this.transportOwned = transportOwned;
    }

    @Nullable
    public static GrpcSchemeRpc useTransport(@WillNotClose RpcTransport transport) {
        if (transport instanceof GrpcTransport) {
            return new GrpcSchemeRpc((GrpcTransport) transport, false);
        }
        return null;
    }

    @Nullable
    public static GrpcSchemeRpc ownTransport(@WillClose RpcTransport transport) {
        if (transport instanceof GrpcTransport) {
            return new GrpcSchemeRpc((GrpcTransport) transport, true);
        }
        return null;
    }

    @Override
    public CompletableFuture<Result<MakeDirectoryResponse>> makeDirectory(MakeDirectoryRequest request, long deadlineAfter) {
        return transport.unaryCall(SchemeServiceGrpc.METHOD_MAKE_DIRECTORY, request, deadlineAfter);
    }

    @Override
    public CompletableFuture<Result<RemoveDirectoryResponse>> removeDirectory(RemoveDirectoryRequest request, long deadlineAfter) {
        return transport.unaryCall(SchemeServiceGrpc.METHOD_REMOVE_DIRECTORY, request, deadlineAfter);
    }

    @Override
    public CompletableFuture<Result<ListDirectoryResponse>> describeDirectory(ListDirectoryRequest request, long deadlineAfter) {
        return transport.unaryCall(SchemeServiceGrpc.METHOD_LIST_DIRECTORY, request, deadlineAfter);
    }

    @Override
    public CompletableFuture<Result<DescribePathResponse>> describePath(DescribePathRequest request, long deadlineAfter) {
        return transport.unaryCall(SchemeServiceGrpc.METHOD_DESCRIBE_PATH, request, deadlineAfter);
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
