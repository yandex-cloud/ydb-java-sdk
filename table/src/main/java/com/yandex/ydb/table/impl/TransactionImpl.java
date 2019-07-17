package com.yandex.ydb.table.impl;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Status;
import com.yandex.ydb.table.YdbTable;
import com.yandex.ydb.table.rpc.TableRpc;
import com.yandex.ydb.table.settings.CommitTxSettings;
import com.yandex.ydb.table.settings.RollbackTxSettings;
import com.yandex.ydb.table.transaction.Transaction;


/**
 * @author Sergey Polovko
 */
final class TransactionImpl implements Transaction {

    private final String sessionId;
    private final String txId;
    private final TableRpc tableRpc;

    TransactionImpl(String sessionId, String txId, TableRpc tableRpc) {
        this.sessionId = sessionId;
        this.txId = txId;
        this.tableRpc = tableRpc;
    }

    @Override
    public String getId() {
        return txId;
    }

    @Override
    public CompletableFuture<Status> commit(CommitTxSettings settings) {
        YdbTable.CommitTransactionRequest request = YdbTable.CommitTransactionRequest.newBuilder()
            .setSessionId(sessionId)
            .setTxId(txId)
            .build();

        return tableRpc.commitTransaction(request)
            .thenCompose(response -> {
                if (!response.isSuccess()) {
                    return CompletableFuture.completedFuture(response.toStatus());
                }
                return tableRpc.getOperationTray()
                    .waitStatus(response.expect("commitTransaction()").getOperation());
            });
    }

    @Override
    public CompletableFuture<Status> rollback(RollbackTxSettings settings) {
        YdbTable.RollbackTransactionRequest request = YdbTable.RollbackTransactionRequest.newBuilder()
            .setSessionId(sessionId)
            .setTxId(txId)
            .build();

        return tableRpc.rollbackTransaction(request)
            .thenCompose(response -> {
                if (!response.isSuccess()) {
                    return CompletableFuture.completedFuture(response.toStatus());
                }
                return tableRpc.getOperationTray()
                    .waitStatus(response.expect("rollbackTransaction()").getOperation());
            });
    }
}
