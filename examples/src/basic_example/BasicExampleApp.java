package ru.yandex.ydb.examples.basic_example;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.annotation.Nullable;

import ru.yandex.ydb.core.Result;
import ru.yandex.ydb.core.Status;
import ru.yandex.ydb.examples.App;
import ru.yandex.ydb.examples.AppRunner;
import ru.yandex.ydb.examples.TablePrinter;
import ru.yandex.ydb.examples.basic_example.exceptions.NonRetriableErrorException;
import ru.yandex.ydb.examples.basic_example.exceptions.TooManyRetriesException;
import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.description.TableColumn;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.query.DataQuery;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.query.Params;
import ru.yandex.ydb.table.result.ResultSetReader;
import ru.yandex.ydb.table.transaction.Transaction;
import ru.yandex.ydb.table.transaction.TransactionMode;
import ru.yandex.ydb.table.transaction.TxControl;
import ru.yandex.ydb.table.types.ListType;
import ru.yandex.ydb.table.types.PrimitiveType;

import static ru.yandex.ydb.table.values.PrimitiveValue.date;
import static ru.yandex.ydb.table.values.PrimitiveValue.uint64;


/**
 * @author Sergey Polovko
 */
public class BasicExampleApp implements App {

    private static final int MAX_RETRIES = 5;
    private static final long OVERLOAD_DELAY_MILLIS = 5000;

    private final String path;
    private final TableClient tableClient;
    @Nullable
    private Session session;
    private Map<String, DataQuery> preparedQueries = new HashMap<>();

    BasicExampleApp(TableService tableService, String path) {
        this.path = path;

        tableClient = tableService.newTableClient();
        session = tableClient.createSession()
            .join()
            .expect("cannot create session");
    }

    public void run() {
        createTables();
        describeTables();

        fillTableDataTransaction();

        selectSimple();
        upsertSimple();

        selectWithParams();

        preparedSelect(2, 3, 7);
        preparedSelect(2, 3, 8);

        multiStep();

        execute(this::explicitTcl);

        preparedSelect(2, 6, 1);
    }

    /**
     * Creates sample tables with CrateTable API.
     */
    private void createTables() {
        TableDescription seriesTable = TableDescription.newBuilder()
            .addNullableColumn("series_id", PrimitiveType.uint64())
            .addNullableColumn("title", PrimitiveType.utf8())
            .addNullableColumn("series_info", PrimitiveType.utf8())
            .addNullableColumn("release_date", PrimitiveType.uint64())
            .setPrimaryKey("series_id")
            .build();

        execute(session -> session.createTable(path + "/series", seriesTable).join());

        TableDescription seasonsTable = TableDescription.newBuilder()
            .addNullableColumn("series_id", PrimitiveType.uint64())
            .addNullableColumn("season_id", PrimitiveType.uint64())
            .addNullableColumn("title", PrimitiveType.utf8())
            .addNullableColumn("first_aired", PrimitiveType.uint64())
            .addNullableColumn("last_aired", PrimitiveType.uint64())
            .setPrimaryKeys("series_id", "season_id")
            .build();

        execute(session -> session.createTable(path + "/seasons", seasonsTable).join());

        TableDescription episodesTable = TableDescription.newBuilder()
            .addNullableColumn("series_id", PrimitiveType.uint64())
            .addNullableColumn("season_id", PrimitiveType.uint64())
            .addNullableColumn("episode_id", PrimitiveType.uint64())
            .addNullableColumn("title", PrimitiveType.utf8())
            .addNullableColumn("air_date", PrimitiveType.uint64())
            .setPrimaryKeys("series_id", "season_id", "episode_id")
            .build();

        execute(session -> session.createTable(path + "/episodes", episodesTable).join());
    }

    /**
     * Describe existing table.
     */
    private void describeTables() {
        System.out.println("\n--[ DescribeTables ]--");

        for (String tableName : new String[]{ "series", "seasons", "episodes" }) {
            String tablePath = path + '/' + tableName;
            TableDescription tableDesc = executeWithResult(session -> session.describeTable(tablePath).join());

            System.out.println(tablePath + ':');
            List<String> primaryKeys = tableDesc.getPrimaryKeys();
            for (TableColumn column : tableDesc.getColumns()) {
                boolean isPrimary = primaryKeys.contains(column.getName());
                System.out.println("    " + column.getName() + ": " + column.getType() + (isPrimary ? " (PK)" : ""));
            }
            System.out.println();
        }
    }

    /**
     * Fills sample tables with data in single parameterized data query.
     */
    void fillTableDataTransaction() {
        String query = String.format(
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $seriesData AS \"List<Struct<\n" +
            "    series_id: Uint64,\n" +
            "    title: Utf8,\n" +
            "    series_info: Utf8,\n" +
            "    release_date: Date>>\";\n" +
            "\n" +
            "DECLARE $seasonsData AS \"List<Struct<\n" +
            "    series_id: Uint64,\n" +
            "    season_id: Uint64,\n" +
            "    title: Utf8,\n" +
            "    first_aired: Date,\n" +
            "    last_aired: Date>>\";\n" +
            "\n" +
            "DECLARE $episodesData AS \"List<Struct<\n" +
            "    series_id: Uint64,\n" +
            "    season_id: Uint64,\n" +
            "    episode_id: Uint64,\n" +
            "    title: Utf8,\n" +
            "    air_date: Date>>\";\n" +
            "\n" +
            "REPLACE INTO series\n" +
            "SELECT\n" +
            "    series_id,\n" +
            "    title,\n" +
            "    series_info,\n" +
            "    DateTime::ToDays(release_date) AS release_date\n" +
            "FROM AS_TABLE($seriesData);\n" +
            "\n" +
            "REPLACE INTO seasons\n" +
            "SELECT\n" +
            "    series_id,\n" +
            "    season_id,\n" +
            "    title,\n" +
            "    DateTime::ToDays(first_aired) AS first_aired,\n" +
            "    DateTime::ToDays(last_aired) AS last_aired\n" +
            "FROM AS_TABLE($seasonsData);\n" +
            "\n" +
            "REPLACE INTO episodes\n" +
            "SELECT\n" +
            "    series_id,\n" +
            "    season_id,\n" +
            "    episode_id,\n" +
            "    title,\n" +
            "    DateTime::ToDays(air_date) AS air_date\n" +
            "FROM AS_TABLE($episodesData);",
            path);

        Params params = Params.withUnknownTypes()
            .put("$seriesData", ListType.of(SeriesData.SERIES_TYPE), SeriesData.SERIES_DATA)
            .put("$seasonsData", ListType.of(SeriesData.SEASON_TYPE), SeriesData.SEASON_DATA)
            .put("$episodesData", ListType.of(SeriesData.EPISODE_TYPE), SeriesData.EPISODE_DATA);

        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        execute(session -> session.executeDataQuery(query, txControl, params)
            .join()
            .toStatus());
    }

    /**
     * Shows basic usage of YDB data queries and transactions.
     */
    private void selectSimple() {
        String query = String.format(
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "SELECT series_id, title, DateTime::ToDate(DateTime::FromDays(release_date)) AS release_date\n" +
            "FROM series\n" +
            "WHERE series_id = 1;",
            path);

        // Begin new transaction with SerializableRW mode
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        // Executes data query with specified transaction control settings.
        DataQueryResult result = executeWithResult(session -> session.executeDataQuery(query, txControl).join());

        System.out.println("\n--[ SelectSimple ]--");
        // Index of result set corresponds to its order in YQL query
        new TablePrinter(result.getResultSet(0)).print();
    }

    /**
     * Shows basic usage of mutating operations.
     */
    private void upsertSimple() {
        String query = String.format(
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "UPSERT INTO episodes (series_id, season_id, episode_id, title) VALUES\n" +
            "(2, 6, 1, \"TBD\");",
            path);

        // Begin new transaction with SerializableRW mode
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        // Executes data query with specified transaction control settings.
        execute(session -> session.executeDataQuery(query, txControl)
            .join()
            .toStatus());
    }

    /**
     * Shows usage of parameters in data queries.
     */
    public void selectWithParams() {
        String query = String.format(
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $seriesId AS Uint64;\n" +
            "DECLARE $seasonId AS Uint64;\n" +
            "\n" +
            "SELECT sa.title AS season_title, sr.title AS series_title\n" +
            "FROM seasons AS sa\n" +
            "INNER JOIN series AS sr\n" +
            "ON sa.series_id = sr.series_id\n" +
            "WHERE sa.series_id = $seriesId AND sa.season_id = $seasonId;",
            path);

        // Begin new transaction with SerializableRW mode
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        // Type of parameter values should be exactly the same as in DECLARE statements.
        Params params = Params.withUnknownTypes()
            .put("$seriesId", uint64(2))
            .put("$seasonId", uint64(3));

        DataQueryResult result = executeWithResult(session -> session.executeDataQuery(query, txControl, params).join());

        System.out.println("\n--[ SelectWithParams ]--");
        // Index of result set corresponds to its order in YQL query
        new TablePrinter(result.getResultSet(0)).print();
    }

    /**
     * Shows usage of prepared queries.
     */
    private void preparedSelect(long seriesId, long seasonId, long episodeId) {
        final String queryId = "PreparedSelectTransaction";

        // Once prepared, query data is stored in the session and identified by QueryId.
        // We keep a track of prepared queries available in current session to reuse them in
        // consecutive calls.

        DataQuery query = preparedQueries.get(queryId);
        if (query == null) {
            String queryText = String.format(
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                "\n" +
                "DECLARE $seriesId AS Uint64;\n" +
                "DECLARE $seasonId AS Uint64;\n" +
                "DECLARE $episodeId AS Uint64;\n" +
                "\n" +
                "SELECT *\n" +
                "FROM episodes\n" +
                "WHERE series_id = $seriesId AND season_id = $seasonId AND episode_id = $episodeId;",
                path);

            // Prepare query and and store it's QueryId for current session
            query = executeWithResult(session -> session.prepareDataQuery(queryText).join());
            System.out.println("Finished preparing query: " + queryId);

            preparedQueries.put(queryId, query);
        }

        Params params = query.newParams()
            .put("$seriesId", uint64(seriesId))
            .put("$seasonId", uint64(seasonId))
            .put("$episodeId", uint64(episodeId));

        DataQueryResult result = query.execute(TxControl.serializableRw().setCommitTx(true), params)
            .join()
            .expect("prepared query failed");

        System.out.println("\n--[ PreparedSelect ]--");
        new TablePrinter(result.getResultSet(0)).print();
    }

    public void multiStep() {
        final long seriesId = 2;
        final long seasonId = 5;

        final String txId;
        final Instant fromDate;
        final Instant toDate;

        {
            String query = String.format(
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                "\n" +
                "DECLARE $seriesId AS Uint64;\n" +
                "DECLARE $seasonId AS Uint64;\n" +
                "\n" +
                "SELECT first_aired AS from_date FROM seasons\n" +
                "WHERE series_id = $seriesId AND season_id = $seasonId;",
                path);

            Params params = Params.withUnknownTypes()
                .put("$seriesId", uint64(seriesId))
                .put("$seasonId", uint64(seasonId));

            // Execute first query to get the required values to the client.
            // Transaction control settings don't set CommitTx flag to keep transaction active
            // after query execution.
            TxControl txControl = TxControl.serializableRw();
            DataQueryResult result = executeWithResult(session -> session.executeDataQuery(query, txControl, params)
                .join());

            if (result.isEmpty()) {
                throw new IllegalStateException("empty result set");
            }

            ResultSetReader resultSet = result.getResultSet(0);
            resultSet.next();
            long firstAired = resultSet.getColumn(0).getUint64();

            // Perform some client logic on returned values
            fromDate = Instant.EPOCH.plus(firstAired, ChronoUnit.DAYS);
            toDate = fromDate.plus(15, ChronoUnit.DAYS);

            // Get active transaction id
            txId = result.getTxId();
        }
        {
            // Construct next query based on the results of client logic
            String query = String.format(
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                "\n" +
                "DECLARE $seriesId AS Uint64;\n" +
                "DECLARE $fromDate AS Uint64;\n" +
                "DECLARE $toDate AS Uint64;\n" +
                "\n" +
                "SELECT season_id, episode_id, title, air_date FROM episodes\n" +
                "WHERE series_id = $seriesId AND air_date >= $fromDate AND air_date <= $toDate;",
                path);

            Params params = Params.withUnknownTypes()
                .put("$seriesId", uint64(seriesId))
                .put("$fromDate", uint64(Duration.between(Instant.EPOCH, fromDate).toDays()))
                .put("$toDate", uint64(Duration.between(Instant.EPOCH, toDate).toDays()));

            // Execute second query.
            // Transaction control settings continues active transaction (tx) and
            // commits it at the end of second query execution.
            TxControl txControl = TxControl.id(txId).setCommitTx(true);
            DataQueryResult result = executeWithResult(session -> session.executeDataQuery(query, txControl, params)
                .join());

            System.out.println("\n--[ MultiStep ]--");
            // Index of result set corresponds to its order in YQL query
            new TablePrinter(result.getResultSet(0)).print();
        }
    }

    /**
     * Show usage of explicit Begin/Commit transaction control calls.
     * In most cases it's better to use transaction control settings in executeDataQuery calls instead
     * to avoid additional hops to YDB cluster and allow more efficient execution of queries.
     */
    private Status explicitTcl(Session session) {
        Result<Transaction> transactionResult = session.beginTransaction(TransactionMode.SERIALIZABLE_READ_WRITE)
            .join();
        if (!transactionResult.isSuccess()) {
            return transactionResult.toStatus();
        }

        Transaction transaction = transactionResult.expect("cannot begin transaction");
        String query = String.format(
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "DECLARE $airDate AS Date;\n" +
            "UPDATE episodes SET air_date = DateTime::ToDays($airDate) WHERE title = \"TBD\";",
            path);

        Params params = Params.withUnknownTypes()
            .put("$airDate", date(Instant.now()));

        // Execute data query.
        // Transaction control settings continues active transaction (tx)
        Result<DataQueryResult> updateResult = session.executeDataQuery(query, TxControl.id(transaction), params)
            .join();
        if (!updateResult.isSuccess()) {
            return updateResult.toStatus();
        }

        // Commit active transaction (tx)
        return transaction.commit().join();
    }

    /**
    * Executes given function with retry logic for YDB response statuses.
    *
    * In case of data transaction we have to retry the whole transaction as YDB
    * transaction may be invalidated on query error.
    *
    * @throws NonRetriableErrorException in case of non-retriable error.
    * @throws TooManyRetriesException if number of allowed retries is exceeded.
    */
    private Status execute(Function<Session, Status> fn) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            Status status = null;

            if (session == null) {
                // Session was invalidated, create new one here.
                // In real-world applications it's better to keep a pool of active sessions to avoid
                // additional latency on session creation.
                Result<Session> sessionResult = tableClient.createSession()
                    .join();
                if (sessionResult.isSuccess()) {
                    preparedQueries.clear();
                    session = sessionResult.expect("cannot create session");
                } else {
                    status = sessionResult.toStatus();
                }
            }

            if (session != null) {
                status = fn.apply(session);
                if (status.isSuccess()) {
                    return status;
                }
            }

            assert status != null;

            switch (status.getCode()) {
                case ABORTED:
                case UNAVAILABLE:
                    // Simple retry
                    break;

                case OVERLOADED:
                case CLIENT_RESOURCE_EXHAUSTED:
                    // Wait and retry. In applications with large parallelism it's better
                    // to add some randomization to the delay to avoid congestion.
                    try {
                        Thread.sleep(OVERLOAD_DELAY_MILLIS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                case NOT_FOUND:
                    // May indicate invalidation of prepared query, clear prepared queries state
                    preparedQueries.clear();
                    break;

                case BAD_SESSION:
                    // Session is invalidated, clear session state
                    session = null;
                    break;

                default:
                    throw new NonRetriableErrorException(status);
            }
        }

        throw new TooManyRetriesException();
    }

    /**
     * Same as {@link BasicExampleApp#execute}, but extracts result.
     */
    private <T> T executeWithResult(Function<Session, Result<T>> fn) {
        AtomicReference<Result<T>> result = new AtomicReference<>();
        execute(session -> {
            Result<T> r = fn.apply(session);
            result.set(r);
            return r.toStatus();
        });
        return result.get().expect("expected success result");
    }

    @Override
    public void close() {
        if (session != null) {
            session.close()
                .join()
                .expect("cannot close session");
        }
    }

    public static void main(String[] args) {
        AppRunner.run("BasicExampleApp", BasicExampleApp::new, args);
    }
}
