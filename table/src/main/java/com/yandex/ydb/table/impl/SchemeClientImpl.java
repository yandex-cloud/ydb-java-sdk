package com.yandex.ydb.table.impl;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.scheme.SchemeOperationProtos;
import com.yandex.ydb.scheme.SchemeOperationProtos.DescribePathRequest;
import com.yandex.ydb.scheme.SchemeOperationProtos.ListDirectoryRequest;
import com.yandex.ydb.scheme.SchemeOperationProtos.MakeDirectoryRequest;
import com.yandex.ydb.scheme.SchemeOperationProtos.RemoveDirectoryRequest;
import com.yandex.ydb.table.SchemeClient;
import com.yandex.ydb.table.description.DescribePathResult;
import com.yandex.ydb.table.description.ListDirectoryResult;
import com.yandex.ydb.table.rpc.SchemeRpc;


/**
 * @author Sergey Polovko
 */
final class SchemeClientImpl implements SchemeClient {

    private final SchemeRpc schemeRpc;

    SchemeClientImpl(SchemeClientBuilderImpl builder) {
        this.schemeRpc = builder.schemeRpc;
    }

    @Override
    public CompletableFuture<Status> makeDirectory(String path) {
        MakeDirectoryRequest request = MakeDirectoryRequest.newBuilder()
            .setPath(path)
            .build();
        final long deadlineAfter = 0;
        return schemeRpc.makeDirectory(request, deadlineAfter)
            .thenCompose(response -> {
                if (!response.isSuccess()) {
                    return CompletableFuture.completedFuture(response.toStatus());
                }
                return schemeRpc.getOperationTray()
                    .waitStatus(response.expect("makeDirectory()").getOperation(), deadlineAfter);
            });
    }

    @Override
    public CompletableFuture<Status> removeDirectory(String path) {
        RemoveDirectoryRequest request = RemoveDirectoryRequest.newBuilder()
            .setPath(path)
            .build();
        final long deadlineAfter = 0;
        return schemeRpc.removeDirectory(request, deadlineAfter)
            .thenCompose(response -> {
                if (!response.isSuccess()) {
                    return CompletableFuture.completedFuture(response.toStatus());
                }
                return schemeRpc.getOperationTray()
                    .waitStatus(response.expect("removeDirectory()").getOperation(), deadlineAfter);
            });
    }

    @Override
    public CompletableFuture<Result<DescribePathResult>> describePath(String path) {
        DescribePathRequest request = DescribePathRequest.newBuilder()
            .setPath(path)
            .build();
        final long deadlineAfter = 0;
        return schemeRpc.describePath(request, deadlineAfter)
            .thenCompose(response -> {
                if (!response.isSuccess()) {
                    return CompletableFuture.completedFuture(response.cast());
                }
                return schemeRpc.getOperationTray().waitResult(
                    response.expect("describePath()").getOperation(),
                    SchemeOperationProtos.DescribePathResult.class,
                    result -> new DescribePathResult(result.getSelf()),
                    deadlineAfter);
            });
    }

    @Override
    public CompletableFuture<Result<ListDirectoryResult>> listDirectory(String path) {
        ListDirectoryRequest request = ListDirectoryRequest.newBuilder()
            .setPath(path)
            .build();
        final long deadlineAfter = 0;
        return schemeRpc.describeDirectory(request, deadlineAfter)
            .thenCompose(response -> {
                if (!response.isSuccess()) {
                    return CompletableFuture.completedFuture(response.cast());
                }
                return schemeRpc.getOperationTray().waitResult(
                    response.expect("describeDirectory()").getOperation(),
                    SchemeOperationProtos.ListDirectoryResult.class,
                    result -> new ListDirectoryResult(result.getSelf(), result.getChildrenList()),
                    deadlineAfter);
            });
    }

    @Override
    public void close() {
        schemeRpc.close();
    }
}
