package com.yandex.ydb.table.rpc;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
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
     */
    CompletableFuture<Result<MakeDirectoryResponse>> makeDirectory(MakeDirectoryRequest request);

    /**
     * Remove directory.
     */
    CompletableFuture<Result<RemoveDirectoryResponse>> removeDirectory(RemoveDirectoryRequest request);

    /**
     * Returns information about given directory and objects inside it.
     */
    CompletableFuture<Result<ListDirectoryResponse>> describeDirectory(ListDirectoryRequest request);

    /**
     * Returns information about object with given path.
     */
    CompletableFuture<Result<DescribePathResponse>> describePath(DescribePathRequest request);

}
