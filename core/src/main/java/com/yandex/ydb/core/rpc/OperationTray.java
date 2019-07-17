package com.yandex.ydb.core.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.protobuf.Message;
import com.yandex.ydb.OperationProtos.Operation;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;


/**
 *
 * @author Sergey Polovko
 */
public interface OperationTray extends AutoCloseable {

    /**
     * Awaits operation completion and retrieves its status.
     *
     * @param operation     operation to await for
     * @return future which will be completed with operation status
     */
    CompletableFuture<Status> waitStatus(Operation operation);

    /**
     * Awaits operation completion and retrieves its result.
     *
     * @param operation     operation to await for
     * @param resultClass   class of operation result message
     * @param mapper        function to map result message to appropriate result*
     * @return future which will be completed with operation result
     */
    <M extends Message, R> CompletableFuture<Result<R>> waitResult(
        Operation operation, Class<M> resultClass, Function<M, R> mapper);

    /**
     * Stops active tasks and release resources.
     */
    @Override
    void close();
}
