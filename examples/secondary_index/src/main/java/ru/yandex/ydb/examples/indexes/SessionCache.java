package ru.yandex.ydb.examples.indexes;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.yandex.ydb.core.Result;
import ru.yandex.ydb.core.UnexpectedResultException;
import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.query.DataQuery;

public class SessionCache {
    private static final Logger logger = LoggerFactory.getLogger(SessionCache.class);

    private static final int DEFAULT_RETRIES = 10;

    private final TableClient tableClient;
    private final ScheduledExecutorService timerScheduler;
    private final List<Entry> freeEntries = new ArrayList<>();

    public SessionCache(TableClient tableClient, ScheduledExecutorService timerScheduler) {
        this.tableClient = tableClient;
        this.timerScheduler = timerScheduler;
    }

    public TableClient getTableClient() {
        return tableClient;
    }

    public CompletableFuture<Entry> acquireSession() {
        synchronized (freeEntries) {
            if (!freeEntries.isEmpty()) {
                Entry cachedEntry = freeEntries.get(freeEntries.size() - 1);
                freeEntries.remove(freeEntries.size() - 1);
                return CompletableFuture.completedFuture(cachedEntry);
            }
        }

        logger.debug("Creating new YDB session");
        return tableClient.createSession()
                .thenApply((Result<Session> result) -> new Entry(result.expect("acquireSession")));
    }

    public void releaseSession(Entry entry) {
        synchronized (freeEntries) {
            freeEntries.add(entry);
        }
    }

    public <R> CompletableFuture<R> withSession(Function<Entry, CompletableFuture<R>> fn, int retries) {
        WithSessionAndRetries<R> context = new WithSessionAndRetries<>(fn, retries);
        context.next();
        return context.result;
    }

    public <R> CompletableFuture<R> withSession(Function<Entry, CompletableFuture<R>> fn) {
        return withSession(fn, DEFAULT_RETRIES);
    }

    public <R> CompletableFuture<R> withQuery(QueryTemplate query, String tablePrefix,
            Function<DataQuery, CompletableFuture<R>> fn, int retries)
    {
        return withSession(entry -> entry.prepareQuery(query, tablePrefix).thenCompose(fn), retries);
    }

    public <R> CompletableFuture<R> withQuery(QueryTemplate query, String tablePrefix,
            Function<DataQuery, CompletableFuture<R>> fn)
    {
        return withQuery(query, tablePrefix, fn, DEFAULT_RETRIES);
    }

    private static boolean isBadSession(Throwable ex) {
        while (ex instanceof CompletionException) {
            ex = ex.getCause();
        }
        if (ex instanceof UnexpectedResultException) {
            switch (((UnexpectedResultException) ex).getStatusCode()) {
                case BAD_SESSION:
                    return true;
            }
        }
        return false;
    }

    enum RetryType {
        NONE,
        IMMEDIATE,
        WITH_BACKOFF;
    }

    private static RetryType getRetryType(Throwable ex) {
        while (ex instanceof CompletionException) {
            ex = ex.getCause();
        }
        if (ex instanceof UnexpectedResultException) {
            switch (((UnexpectedResultException) ex).getStatusCode()) {
                case BAD_SESSION:
                case TRANSPORT_UNAVAILABLE:
                    return RetryType.IMMEDIATE;
                case OVERLOADED:
                case CLIENT_RESOURCE_EXHAUSTED:
                    return RetryType.WITH_BACKOFF;
            }
        }
        return RetryType.NONE;
    }

    private class WithSessionAndRetries<R> {
        private final CompletableFuture<R> result = new CompletableFuture<>();
        private final Function<Entry, CompletableFuture<R>> fn;
        private int retriesLeft = 0;
        private long nextBackoffMillis = 100;

        public WithSessionAndRetries(
                Function<Entry, CompletableFuture<R>> fn,
                int retries)
        {
            this.fn = fn;
            this.retriesLeft = retries;
        }

        public void next() {
            acquireSession()
                    .thenCompose(entry -> fn.apply(entry).whenComplete((R result, Throwable ex) -> {
                        if (isBadSession(ex)) {
                            entry.getSession().close();
                        } else {
                            releaseSession(entry);
                        }
                    }))
                    .whenComplete(this::processResult);
        }

        private void nextAfter(long millis) {
            timerScheduler.schedule(this::next, millis, TimeUnit.MILLISECONDS);
        }

        private long nextSleepMillis() {
            long maxBackoff = nextBackoffMillis;
            nextBackoffMillis *= 2;
            return ThreadLocalRandom.current().nextLong(1, maxBackoff + 1);
        }

        private void processException(Throwable ex) {
            try {
                RetryType retryType = getRetryType(ex);
                if (retryType != RetryType.NONE && retriesLeft > 0) {
                    retriesLeft--;
                    switch (retryType) {
                        case IMMEDIATE:
                            logger.debug("Callback failed, will retry immediately", ex);
                            next();
                            return;
                        case WITH_BACKOFF:
                            long millis = nextSleepMillis();
                            logger.debug("Callback failed, will retry in " + millis + "ms", ex);
                            nextAfter(millis);
                            return;
                    }
                }
                // We are not going to retry this request, complete with the last exception
                result.completeExceptionally(ex);
            } catch (Throwable unexpected) {
                result.completeExceptionally(unexpected);
            }
        }

        private void processResult(R value, Throwable ex) {
            if (ex != null) {
                processException(ex);
            } else {
                result.complete(value);
            }
        }
    }

    public class Entry {
        private final Session session;
        private final Map<QueryTemplate, DataQuery> queries =
                new EnumMap<>(QueryTemplate.class);

        private Entry(Session session) {
            this.session = session;
        }

        public Session getSession() {
            return session;
        }

        public CompletableFuture<DataQuery> prepareQuery(QueryTemplate query, String tablePrefix) {
            synchronized (queries) {
                DataQuery cachedQuery = queries.get(query);
                if (cachedQuery != null) {
                    return CompletableFuture.completedFuture(cachedQuery);
                }
            }
            String queryText = query.getText().replaceAll("<TABLE_PREFIX>", tablePrefix);
            return session.prepareDataQuery(queryText)
                    .thenApply((Result<DataQuery> result) -> {
                        DataQuery dataQuery = result.expect("prepareQuery");
                        synchronized (queries) {
                            queries.put(query, dataQuery);
                        }
                        return dataQuery;
                    });
        }
    }
}
