package com.yandex.ydb.table.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.UnexpectedResultException;
import com.yandex.ydb.core.utils.Async;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.SessionStatus;
import com.yandex.ydb.table.impl.SessionImpl.State;
import com.yandex.ydb.table.impl.pool.FixedAsyncPool;
import com.yandex.ydb.table.impl.pool.PooledObjectHandler;
import com.yandex.ydb.table.settings.CloseSessionSettings;
import com.yandex.ydb.table.settings.CreateSessionSettings;
import com.yandex.ydb.table.stats.SessionPoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergey Polovko
 */
final class SessionPool implements PooledObjectHandler<SessionImpl> {
    private static final Logger logger = LoggerFactory.getLogger(SessionPool.class);

    private final TableClientImpl tableClient;

    /**
     * Pool to store sessions which are ready but idle right now
     */
    private final FixedAsyncPool<SessionImpl> idlePool;

    private final int minSize;
    private final int maxSize;

    SessionPool(TableClientImpl tableClient, SessionPoolOptions options) {
        this.tableClient = tableClient;
        this.minSize = options.getMinSize();
        this.maxSize = options.getMaxSize();
        this.idlePool = new FixedAsyncPool<>(
            this,
            minSize,
            maxSize,
            maxSize * 2,
            options.getKeepAliveTimeMillis(),
            options.getMaxIdleTimeMillis());
    }

    @Override
    public CompletableFuture<SessionImpl> create(long deadlineAfter) {
        return tableClient.createSessionImpl(new CreateSessionSettings().setDeadlineAfter(deadlineAfter), this)
            .thenApply(r -> {
                SessionImpl session = (SessionImpl) r.expect("cannot create session");
                session.setState(State.IDLE);
                return session;
            });
    }

    @Override
    public CompletableFuture<Void> destroy(SessionImpl s) {
        return s.delete(new CloseSessionSettings())
            .thenAccept(r -> r.expect("cannot close session: " + s.getId()));
    }

    @Override
    public boolean isValid(SessionImpl s) {
        return s.switchState(State.ACTIVE, State.IDLE);
    }

    @Override
    public CompletableFuture<Result<SessionStatus>> keepAlive(SessionImpl s) {
        return s.keepAlive();
    }

    CompletableFuture<Result<Session>> acquire(Duration timeout) {
        CompletableFuture<Result<Session>> future = new CompletableFuture<>();
        Instant expireTime = Instant.now().plusNanos(timeout.toNanos());

        tryAcquire(future, expireTime);
        return future;
    }

    private void tryAcquire(CompletableFuture<Result<Session>> future, Instant expireTime) {
        Duration timeout = Duration.between(Instant.now(), expireTime);
        idlePool.acquire(timeout.isNegative() ? Duration.ZERO : timeout).whenComplete((session, th) -> {
            if (future.isDone()) {
                if (session != null) {
                    // Fake usage of session
                    session.switchState(State.IDLE, State.ACTIVE);
                    release(session);
                }
                return;
            }

            if (th != null) {
                Throwable unwrapped = Async.unwrapCompletionException(th);
                if (unwrapped instanceof UnexpectedResultException) {
                    future.complete(Result.fail((UnexpectedResultException) unwrapped));
                } else {
                    future.complete(Result.error("cannot acquire session from pool", unwrapped));
                }
                return;
            }

            if (session != null) {
                if (session.switchState(State.IDLE, State.ACTIVE)) {
                    logger.debug("session `{}' acquired", session);
                    future.complete(Result.success(session));
                } else {
                    release(session);
                    tryAcquire(future, expireTime);
                }
            }
        });
    }

    void release(SessionImpl session) {
        if (session.switchState(State.DISCONNECTED, State.IDLE)) {
            logger.debug("Destroy {} because disconnected", session);
            session.close(); // do not await session to be closed
            idlePool.release(session);
        } else if (session.isGracefulShutdown()) {
            logger.debug("Destroy {} because graceful shutdown hook was recived", session);
            session.close(); // do not await session to be closed
            idlePool.release(session);
        } else {
            logger.debug("session `{}' released", session);
            idlePool.release(session);
        }
    }

    void delete(SessionImpl session) {
        idlePool.delete(session);
    }

    void close() {
        idlePool.close();
    }

    public SessionPoolStats getStats() {
        return new SessionPoolStats(
            minSize,
            maxSize,
            idlePool.getIdleCount(),
            0,
            idlePool.getAcquiredCount(),
            idlePool.getPendingAcquireCount());
    }
}
