package com.yandex.ydb.table.rpc;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.grpc.GrpcRequestSettings;
import com.yandex.ydb.core.rpc.Rpc;
import com.yandex.ydb.scheme.SchemeOperationProtos.DescribePathRequest;
import com.yandex.ydb.scheme.SchemeOperationProtos.DescribePathResponse;
import com.yandex.ydb.scheme.SchemeOperationProtos.ListDirectoryRequest;
import com.yandex.ydb.scheme.SchemeOperationProtos.ListDirectoryResponse;
import com.yandex.ydb.scheme.SchemeOperationProtos.MakeDirectoryRequest;
import com.yandex.ydb.scheme.SchemeOperationProtos.MakeDirectoryResponse;
import com.yandex.ydb.scheme.SchemeOperationProtos.RemoveDirectoryRequest;
import com.yandex.ydb.scheme.SchemeOperationProtos.RemoveDirectoryResponse;


/**
 * @author Sergey Polovko
 */
public interface SchemeRpc extends Rpc {

    /**
     * Make directory.
     * @param request request proto
     * @param settings rpc call settings
     * @return completable future with result of operation
     */
    CompletableFuture<Result<MakeDirectoryResponse>> makeDirectory(MakeDirectoryRequest request,
                                                                   GrpcRequestSettings settings);

    /**
     * Remove directory.
     * @param request request proto
     * @param settings rpc call settings
     * @return completable future with result of operation
     */
    CompletableFuture<Result<RemoveDirectoryResponse>> removeDirectory(RemoveDirectoryRequest request,
                                                                       GrpcRequestSettings settings);

    /**
     * Returns information about given directory and objects inside it.
     * @param request request proto
     * @param settings rpc call settings
     * @return completable future with result of operation
     */
    CompletableFuture<Result<ListDirectoryResponse>> describeDirectory(ListDirectoryRequest request,
                                                                       GrpcRequestSettings settings);

    /**
     * Returns information about object with given path.
     * @param request request proto
     * @param settings rpc call settings
     * @return completable future with result of operation
     */
    CompletableFuture<Result<DescribePathResponse>> describePath(DescribePathRequest request,
                                                                 GrpcRequestSettings settings);

}
