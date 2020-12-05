package com.yandex.ydb.table.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.yandex.ydb.table.SessionStatus;
import com.yandex.ydb.table.impl.SessionImpl.State;
import com.yandex.ydb.table.impl.pool.FixedAsyncPool;
import com.yandex.ydb.table.impl.pool.PooledObjectHandler;
import com.yandex.ydb.table.impl.pool.SettlersPool;
import com.yandex.ydb.table.settings.CreateSessionSettings;
import com.yandex.ydb.table.stats.SessionPoolStats;


/**
 * @author Sergey Polovko
 */
final class SessionPool implements PooledObjectHandler<SessionImpl> {
    private static final Logger logger = Logger.getLogger(SessionPool.class.getName());

    private final TableClientImpl tableClient;

    /**
     * Pool to store sessions which are ready but ide right now
     */
    private final FixedAsyncPool<SessionImpl> idlePool;

    /**
     * Pool to store sessions with unknown status due to some transport errors.
     */
    private final SettlersPool<SessionImpl> settlersPool;

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
        this.settlersPool = new SettlersPool<>(this, idlePool, 10, 5_000);
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
        return s.close()
            .thenAccept(r -> r.expect("cannot close session: " + s.getId()));
    }

    @Override
    public boolean isValid(SessionImpl s) {
        return s.switchState(State.ACTIVE, State.IDLE);
    }

    @Override
    public CompletableFuture<Boolean> keepAlive(SessionImpl s) {
        return s.keepAlive()
            .thenApply(r -> {
                if (!r.isSuccess()) {
                    return Boolean.FALSE;
                }
                SessionStatus status = r.expect("cannot keep alive session: " + s.getId());
                return status == SessionStatus.READY;
            });
    }

    CompletableFuture<SessionImpl> acquire(Duration timeout) {
        final Instant startTime = Instant.now();
        return idlePool.acquire(timeout)
            .thenCompose(s -> {
                if (s.switchState(State.IDLE, State.ACTIVE)) {
                    logger.log(Level.FINEST, "session `{0}' acquired", s);
                    return CompletableFuture.completedFuture(s);
                } else {
                    release(s);
                    Duration duration = Duration.between(startTime, Instant.now());
                    return acquire(timeout.minus(Duration.ZERO.compareTo(duration) < 0
                        ? duration
                        : Duration.ZERO));
                }
            });
    }

    void release(SessionImpl session) {
        if (session.switchState(State.DISCONNECTED, State.IDLE)) {
            if (!settlersPool.offerIfHaveSpace(session)) {
                logger.log(Level.FINE, "Destroy {0} because settlers pool overflow", session);
                session.close(); // do not await session to be closed
                idlePool.release(session);
            }
        } else {
            idlePool.release(session);
            logger.log(Level.FINEST, "session `{0}' released", session);
        }
    }

    void close() {
        idlePool.close();
        settlersPool.close();
    }

    public SessionPoolStats getStats() {
        return new SessionPoolStats(
            minSize,
            maxSize,
            idlePool.getIdleCount(),
            settlersPool.size(),
            idlePool.getAcquiredCount(),
            idlePool.getPendingAcquireCount());
    }
}
