package com.yandex.ydb.table.impl;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Status;
import com.yandex.ydb.table.settings.CommitTxSettings;
import com.yandex.ydb.table.settings.RollbackTxSettings;
import com.yandex.ydb.table.transaction.Transaction;


/**
 * @author Sergey Polovko
 */
final class TransactionImpl implements Transaction {

    private final SessionImpl session;
    private final String txId;

    TransactionImpl(SessionImpl session, String txId) {
        this.session = session;
        this.txId = txId;
    }

    @Override
    public String getId() {
        return txId;
    }

    @Override
    public CompletableFuture<Status> commit(CommitTxSettings settings) {
        return session.commitTransaction(txId, settings);
    }

    @Override
    public CompletableFuture<Status> rollback(RollbackTxSettings settings) {
        return session.rollbackTransaction(txId, settings);
    }
}
