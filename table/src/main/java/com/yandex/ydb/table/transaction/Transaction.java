package com.yandex.ydb.table.transaction;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Status;
import com.yandex.ydb.table.settings.CommitTxSettings;
import com.yandex.ydb.table.settings.RollbackTxSettings;


/**
 * @author Sergey Polovko
 */
public interface Transaction {

    String getId();

    default boolean isActive() {
        return !getId().isEmpty();
    }

    CompletableFuture<Status> commit(CommitTxSettings settings);

    default CompletableFuture<Status> commit() {
        return commit(new CommitTxSettings());
    }

    CompletableFuture<Status> rollback(RollbackTxSettings settings);

    default CompletableFuture<Status> rollback() {
        return rollback(new RollbackTxSettings());
    }
}
