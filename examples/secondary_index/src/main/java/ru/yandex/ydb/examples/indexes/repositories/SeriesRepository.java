package ru.yandex.ydb.examples.indexes.repositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import ru.yandex.ydb.core.Result;
import ru.yandex.ydb.examples.indexes.QueryTemplate;
import ru.yandex.ydb.examples.indexes.SessionCache;
import ru.yandex.ydb.examples.indexes.model.Series;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.query.Params;
import ru.yandex.ydb.table.result.ResultSetReader;
import ru.yandex.ydb.table.transaction.TxControl;
import ru.yandex.ydb.table.types.PrimitiveType;
import ru.yandex.ydb.table.values.PrimitiveValue;

public class SeriesRepository {
    private static final TableDescription TABLE_DESCRIPTION = TableDescription.newBuilder()
            .addNullableColumn("series_id", PrimitiveType.uint64())
            .addNullableColumn("title", PrimitiveType.utf8())
            .addNullableColumn("series_info", PrimitiveType.utf8())
            .addNullableColumn("release_date", PrimitiveType.uint32())
            .addNullableColumn("views", PrimitiveType.uint64())
            .setPrimaryKey("series_id")
            .build();

    private static final TableDescription TABLE_REV_INDEX_DESCRIPTION = TableDescription.newBuilder()
            .addNullableColumn("rev_views", PrimitiveType.uint64())
            .addNullableColumn("series_id", PrimitiveType.uint64())
            .setPrimaryKeys("rev_views", "series_id")
            .build();

    private final SessionCache sessionCache;
    private final String tablePrefix;

    public SeriesRepository(SessionCache sessionCache, String tablePrefix) {
        if (!tablePrefix.endsWith("/")) {
            tablePrefix += "/";
        }
        this.sessionCache = sessionCache;
        this.tablePrefix = tablePrefix;
    }

    public void dropTables() {
        dropTablesAsync().join();
    }

    public CompletableFuture<Void> dropTablesAsync() {
        return sessionCache.withSession(entry -> CompletableFuture.completedFuture(null)
                .thenCompose(ignored -> entry.getSession().dropTable(tablePrefix + "series")
                        .thenAccept(status -> status.expect("drop table")))
                .thenCompose(ignored -> entry.getSession().dropTable(tablePrefix + "series_rev_views")
                        .thenAccept(status -> status.expect("drop table"))));
    }

    public void createTables() {
        createTablesAsync().join();
    }

    public CompletableFuture<Void> createTablesAsync() {
        return sessionCache.withSession(entry -> CompletableFuture.completedFuture(null)
                .thenCompose(ignored -> entry.getSession().createTable(tablePrefix + "series", TABLE_DESCRIPTION)
                        .thenAccept(status -> status.expect("create table")))
                .thenCompose(ignored -> entry.getSession()
                        .createTable(tablePrefix + "series_rev_views", TABLE_REV_INDEX_DESCRIPTION)
                        .thenAccept(status -> status.expect("create table"))));
    }

    public void insert(Series series) {
        insertAsync(series).join();
    }

    public CompletableFuture<Void> insertAsync(Series series) {
        return sessionCache.withQuery(QueryTemplate.SERIES_INSERT, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$seriesId", PrimitiveValue.uint64(series.getSeriesId()))
                    .put("$title", PrimitiveValue.utf8(series.getTitle()))
                    .put("$seriesInfo", PrimitiveValue.utf8(series.getSeriesInfo()))
                    .put("$releaseDate", PrimitiveValue.uint32((int) series.getReleaseDate().toEpochDay()))
                    .put("$views", PrimitiveValue.uint64(series.getViews()));
            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply((Result<DataQueryResult> result) -> {
                        result.expect("insert failed");
                        return (Void) null;
                    });
        });
    }

    public long delete(long seriesId) {
        return deleteAsync(seriesId).join();
    }

    public CompletableFuture<Long> deleteAsync(long seriesId) {
        return sessionCache.withQuery(QueryTemplate.SERIES_DELETE, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$seriesId", PrimitiveValue.uint64(seriesId));

            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply(wrappedResult -> {
                        DataQueryResult result = wrappedResult.expect("delete failed");
                        ResultSetReader resultSet = result.getResultSet(0);
                        if (!resultSet.next()) {
                            throw new IllegalStateException("Query count was not returned");
                        }
                        return resultSet.getColumn(0).getUint64();
                    });
        });
    }

    public long updateViews(long seriesId, long newViews) {
        return updateViewsAsync(seriesId, newViews).join();
    }

    public CompletableFuture<Long> updateViewsAsync(long seriesId, long newViews) {
        return sessionCache.withQuery(QueryTemplate.SERIES_UPDATE_VIEWS, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$seriesId", PrimitiveValue.uint64(seriesId))
                    .put("$newViews", PrimitiveValue.uint64(newViews));
            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply(wrappedResult -> {
                        DataQueryResult result = wrappedResult.expect("update views");
                        ResultSetReader resultSet = result.getResultSet(0);
                        if (!resultSet.next()) {
                            throw new IllegalStateException("Query count was not returned");
                        }
                        return resultSet.getColumn(0).getUint64();
                    });
        });
    }

    public Series findById(long seriesId) {
        return findByIdAsync(seriesId).join();
    }

    public CompletableFuture<Series> findByIdAsync(long seriesId) {
        return sessionCache.withQuery(QueryTemplate.SERIES_FIND_BY_ID, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$seriesId", PrimitiveValue.uint64(seriesId));
            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply(wrappedResult -> {
                        DataQueryResult result = wrappedResult.expect("find by id");
                        ResultSetReader resultSet = result.getResultSet(0);
                        if (!resultSet.next()) {
                            return null;
                        }
                        return extractSeries(resultSet);
                    });
        });
    }

    public List<Series> findAll(int limit) {
        return findAllAsync(limit).join();
    }

    public CompletableFuture<List<Series>> findAllAsync(int limit) {
        return sessionCache.withQuery(QueryTemplate.SERIES_FIND_ALL, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$limit", PrimitiveValue.uint64(limit));
            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply(wrappedResult -> {
                        DataQueryResult result = wrappedResult.expect("find all");
                        ResultSetReader resultSet = result.getResultSet(0);
                        List<Series> results = new ArrayList<>(resultSet.getRowCount());
                        while (resultSet.next()) {
                            results.add(extractSeries(resultSet));
                        }
                        return results;
                    });
        });
    }

    public List<Series> findAll(int limit, long lastSeriesId) {
        return findAllAsync(limit, lastSeriesId).join();
    }

    public CompletableFuture<List<Series>> findAllAsync(int limit, long lastSeriesId) {
        return sessionCache.withQuery(QueryTemplate.SERIES_FIND_ALL_NEXT, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$limit", PrimitiveValue.uint64(limit))
                    .put("$lastSeriesId", PrimitiveValue.uint64(lastSeriesId));
            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply(wrappedResult -> {
                        DataQueryResult result = wrappedResult.expect("find all next");
                        ResultSetReader resultSet = result.getResultSet(0);
                        List<Series> results = new ArrayList<>(resultSet.getRowCount());
                        while (resultSet.next()) {
                            results.add(extractSeries(resultSet));
                        }
                        return results;
                    });
        });
    }

    public List<Series> findMostViewed(int limit) {
        return findMostViewedAsync(limit).join();
    }

    public CompletableFuture<List<Series>> findMostViewedAsync(int limit) {
        return sessionCache.withQuery(QueryTemplate.SERIES_FIND_MOST_VIEWED, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$limit", PrimitiveValue.uint64(limit));
            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply(wrappedResult -> {
                        DataQueryResult result = wrappedResult.expect("find most viewed");
                        ResultSetReader resultSet = result.getResultSet(0);
                        List<Series> results = new ArrayList<>(resultSet.getRowCount());
                        while (resultSet.next()) {
                            results.add(extractSeries(resultSet));
                        }
                        return results;
                    });
        });
    }

    public List<Series> findMostViewed(int limit, long lastSeriesId, long lastViews) {
        return findMostViewedAsync(limit, lastSeriesId, lastViews).join();
    }

    public CompletableFuture<List<Series>> findMostViewedAsync(int limit, long lastSeriesId, long lastViews) {
        return sessionCache.withQuery(QueryTemplate.SERIES_FIND_MOST_VIEWED_NEXT, tablePrefix, query -> {
            Params params = query.newParams()
                    .put("$limit", PrimitiveValue.uint64(limit))
                    .put("$lastSeriesId", PrimitiveValue.uint64(lastSeriesId))
                    .put("$lastViews", PrimitiveValue.uint64(lastViews));
            return query.execute(TxControl.serializableRw().setCommitTx(true), params)
                    .thenApply(wrappedResult -> {
                        DataQueryResult result = wrappedResult.expect("find most viewed next");
                        ResultSetReader resultSet = result.getResultSet(0);
                        List<Series> results = new ArrayList<>(resultSet.getRowCount());
                        while (resultSet.next()) {
                            results.add(extractSeries(resultSet));
                        }
                        return results;
                    });
        });
    }

    private static Series extractSeries(ResultSetReader resultSet) {
        long seriesId = resultSet.getColumn(0).getUint64();
        String title = resultSet.getColumn(1).getUtf8();
        String seriesInfo = resultSet.getColumn(2).getUtf8();
        LocalDate releaseDate = LocalDate.ofEpochDay(resultSet.getColumn(3).getUint32());
        long views = resultSet.getColumn(4).getUint64();
        return new Series(seriesId, title, seriesInfo, releaseDate, views);
    }
}
