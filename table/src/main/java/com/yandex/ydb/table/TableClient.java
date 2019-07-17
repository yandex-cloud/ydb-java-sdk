package com.yandex.ydb.table;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.table.settings.CreateSessionSettings;


/**
 * @author Sergey Polovko
 */
public interface TableClient extends AutoCloseable {

    /**
     * Create new session.
     */
    CompletableFuture<Result<Session>> createSession(CreateSessionSettings settings);

    default CompletableFuture<Result<Session>> createSession() {
        return createSession(new CreateSessionSettings());
    }

    /**
     * Returns session from session pool, if all sessions are occupied new session will be created.
     */
    CompletableFuture<Result<Session>> getOrCreateSession(CreateSessionSettings settings);

    default CompletableFuture<Result<Session>> getOrCreateSession() {
        return getOrCreateSession(new CreateSessionSettings());
    }

    /**
     * Release session back to this client.
     */
    CompletableFuture<Status> releaseSession(Session session);

    @Override
    void close();
}
