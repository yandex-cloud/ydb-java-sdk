package com.yandex.ydb.table.impl.pool;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.table.SessionStatus;

/**
 * @author Sergey Polovko
 */
public interface PooledObjectHandler<T> {

    CompletableFuture<T> create(long deadlineAfter);

    CompletableFuture<Void> destroy(T object);

    boolean isValid(T object);

    CompletableFuture<Result<SessionStatus>> keepAlive(T object);
}
