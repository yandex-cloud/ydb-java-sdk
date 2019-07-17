package com.yandex.ydb.table;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.table.description.DescribePathResult;
import com.yandex.ydb.table.description.ListDirectoryResult;


/**
 * @author Sergey Polovko
 */
public interface SchemeClient {

    CompletableFuture<Status> makeDirectory(String path);

    CompletableFuture<Status> removeDirectory(String path);

    CompletableFuture<Result<DescribePathResult>> describePath(String path);

    CompletableFuture<Result<ListDirectoryResult>> listDirectory(String path);

}
