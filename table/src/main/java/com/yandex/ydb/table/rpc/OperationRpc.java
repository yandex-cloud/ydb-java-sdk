package com.yandex.ydb.table.rpc;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.OperationProtos.GetOperationRequest;
import com.yandex.ydb.OperationProtos.GetOperationResponse;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.rpc.Rpc;


/**
 * @author Sergey Polovko
 */
public interface OperationRpc extends Rpc {

    /**
     * Check status for a given operation.
     */
    CompletableFuture<Result<GetOperationResponse>> getOperation(GetOperationRequest request);

}
