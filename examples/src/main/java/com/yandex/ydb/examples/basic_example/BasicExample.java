package com.yandex.ydb.examples.basic_example;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.annotation.WillNotClose;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.examples.App;
import com.yandex.ydb.examples.AppRunner;
import com.yandex.ydb.examples.TablePrinter;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.settings.ExecuteScanQuerySettings;
import com.yandex.ydb.table.transaction.Transaction;
import com.yandex.ydb.table.transaction.TransactionMode;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;

import static com.yandex.ydb.table.values.PrimitiveValue.uint64;


/**
 * @author Sergey Polovko
 */
public class BasicExample implements App {
    private final String database;
    private final TableClient tableClient;
    private final SessionRetryContext retryCtx;

    BasicExample(@WillNotClose RpcTransport transport, String database) {
        this.database = database;
        this.tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
            .build();
        this.retryCtx = SessionRetryContext.create(tableClient).build();
    }

    @Override
    public void run() {
        createTables();
        describeTables();

        fillTableDataTransaction();

        selectSimple();
        upsertSimple();

        selectWithParams(2, 3);

        executeScanQueryWithParams(2, 3);

        selectWithParams(2, 3, 7);
        selectWithParams(2, 3, 8);

        multiStep();

        retryCtx.supplyStatus(this::explicitTcl).join();

        selectWithParams(2, 6, 1);
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

        retryCtx.supplyStatus(session -> session.createTable(database + "/series", seriesTable))
                .join();

        TableDescription seasonsTable = TableDescription.newBuilder()
            .addNullableColumn("series_id", PrimitiveType.uint64())
            .addNullableColumn("season_id", PrimitiveType.uint64())
            .addNullableColumn("title", PrimitiveType.utf8())
            .addNullableColumn("first_aired", PrimitiveType.uint64())
            .addNullableColumn("last_aired", PrimitiveType.uint64())
            .setPrimaryKeys("series_id", "season_id")
            .build();

        retryCtx.supplyStatus(session -> session.createTable(database + "/seasons", seasonsTable))
                .join();

        TableDescription episodesTable = TableDescription.newBuilder()
            .addNullableColumn("series_id", PrimitiveType.uint64())
            .addNullableColumn("season_id", PrimitiveType.uint64())
            .addNullableColumn("episode_id", PrimitiveType.uint64())
            .addNullableColumn("title", PrimitiveType.utf8())
            .addNullableColumn("air_date", PrimitiveType.uint64())
            .setPrimaryKeys("series_id", "season_id", "episode_id")
            .build();

        retryCtx.supplyStatus(session -> session.createTable(database + "/episodes", episodesTable))
                .join();
    }

    /**
     * Describe existing table.
     */
    private void describeTables() {
        System.out.println("\n--[ DescribeTables ]--");

        for (String tableName : new String[]{ "series", "seasons", "episodes" }) {
            String tablePath = database + '/' + tableName;
            TableDescription tableDesc = retryCtx.supplyResult(session -> session.describeTable(tablePath))
                    .join().expect("describeTable");

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
            "\n" +
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $seriesData AS List<Struct<\n" +
            "    series_id: Uint64,\n" +
            "    title: Utf8,\n" +
            "    series_info: Utf8,\n" +
            "    release_date: Date>>;\n" +
            "\n" +
            "DECLARE $seasonsData AS List<Struct<\n" +
            "    series_id: Uint64,\n" +
            "    season_id: Uint64,\n" +
            "    title: Utf8,\n" +
            "    first_aired: Date,\n" +
            "    last_aired: Date>>;\n" +
            "\n" +
            "DECLARE $episodesData AS List<Struct<\n" +
            "    series_id: Uint64,\n" +
            "    season_id: Uint64,\n" +
            "    episode_id: Uint64,\n" +
            "    title: Utf8,\n" +
            "    air_date: Date>>;\n" +
            "\n" +
            "REPLACE INTO series\n" +
            "SELECT\n" +
            "    series_id,\n" +
            "    title,\n" +
            "    series_info,\n" +
            "    CAST(release_date AS Uint64) AS release_date\n" +
            "FROM AS_TABLE($seriesData);\n" +
            "\n" +
            "REPLACE INTO seasons\n" +
            "SELECT\n" +
            "    series_id,\n" +
            "    season_id,\n" +
            "    title,\n" +
            "    CAST(first_aired AS Uint64) AS first_aired,\n" +
            "    CAST(last_aired AS Uint64) AS last_aired\n" +
            "FROM AS_TABLE($seasonsData);\n" +
            "\n" +
            "REPLACE INTO episodes\n" +
            "SELECT\n" +
            "    series_id,\n" +
            "    season_id,\n" +
            "    episode_id,\n" +
            "    title,\n" +
            "    CAST(air_date AS Uint64) AS air_date\n" +
            "FROM AS_TABLE($episodesData);",
            database);

        Params params = Params.of(
            "$seriesData", SeriesData.SERIES_DATA,
            "$seasonsData", SeriesData.SEASON_DATA,
            "$episodesData", SeriesData.EPISODE_DATA);

        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        System.out.println(query);
        retryCtx.supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().toStatus();
    }

    /**
     * Shows basic usage of YDB data queries and transactions.
     */
    private void selectSimple() {
        String query = String.format(
            "\n" +
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "$format = DateTime::Format(\"%%Y-%%m-%%d\");\n" +
            "\n" +
            "SELECT\n" +
            "    series_id,\n" +
            "    title,\n" +
            "    $format(DateTime::FromSeconds(CAST(DateTime::ToSeconds(DateTime::IntervalFromDays(CAST(release_date AS Int16))) AS Uint32))) AS release_date\n" +
            "FROM series\n" +
            "WHERE series_id = 1;",
            database);

        // Begin new transaction with SerializableRW mode
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        // Executes data query with specified transaction control settings.
        DataQueryResult result = retryCtx.supplyResult(session -> session.executeDataQuery(query, txControl))
                .join().expect("execute data query");

        System.out.println("\n--[ SelectSimple ]--");
        // Index of result set corresponds to its order in YQL query
        new TablePrinter(result.getResultSet(0)).print();
    }

    /**
     * Shows basic usage of mutating operations.
     */
    private void upsertSimple() {
        String query = String.format(
            "\n" +
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "UPSERT INTO episodes (series_id, season_id, episode_id, title) VALUES\n" +
            "(2, 6, 1, \"TBD\");",
            database);

        // Begin new transaction with SerializableRW mode
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        // Executes data query with specified transaction control settings.
        retryCtx.supplyResult(session -> session.executeDataQuery(query, txControl))
            .join()
            .toStatus();
    }

    /*
     * Shows usage of parameters in data queries.
     */
    public void selectWithParams(long seriesId, long seasonId) {
        String query = String.format(
            "\n" +
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
            database);

        // Begin new transaction with SerializableRW mode
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        // Type of parameter values should be exactly the same as in DECLARE statements.
        Params params = Params.of("$seriesId", uint64(seriesId), "$seasonId", uint64(seasonId));

        DataQueryResult result = retryCtx.supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().expect("execute data query");

        System.out.println("\n--[ SelectWithParams ]--");
        // Index of result set corresponds to its order in YQL query
        new TablePrinter(result.getResultSet(0)).print();
    }

    public void executeScanQueryWithParams(long seriesId, long seasonId) {
        String query = String.format(
            "\n" +
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
            database);

        // Type of parameter values should be exactly the same as in DECLARE statements.
        Params params = Params.of("$seriesId", uint64(seriesId), "$seasonId", uint64(seasonId));

        ExecuteScanQuerySettings settings = ExecuteScanQuerySettings.newBuilder().build();
        Consumer<ResultSetReader> printer = (ResultSetReader result) -> {
            new TablePrinter(result).print();
        };

        System.out.println("\n--[ ExecuteScanQueryWithParams ]--");
        // Index of result set corresponds to its order in YQL query
        retryCtx.supplyStatus(session -> session.executeScanQuery(query, params, settings, printer))
                .join();
    }

    private void selectWithParams(long seriesId, long seasonId, long episodeId) {
        String query = String.format(
            "\n" +
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $seriesId AS Uint64;\n" +
            "DECLARE $seasonId AS Uint64;\n" +
            "DECLARE $episodeId AS Uint64;\n" +
            "\n" +
            "SELECT *\n" +
            "FROM episodes\n" +
            "WHERE series_id = $seriesId AND season_id = $seasonId AND episode_id = $episodeId;",
            database);

        Params params = Params.create(3)
            .put("$seriesId", uint64(seriesId))
            .put("$seasonId", uint64(seasonId))
            .put("$episodeId", uint64(episodeId));

        ExecuteScanQuerySettings settings = ExecuteScanQuerySettings.newBuilder().build();
        Consumer<ResultSetReader> printer = (ResultSetReader result) -> {
            new TablePrinter(result).print();
        };

        retryCtx.supplyStatus(session -> session.executeScanQuery(query, params, settings, printer))
                .join();
    }

    public void multiStep() {
        final long seriesId = 2;
        final long seasonId = 5;

        final String txId;
        final Instant fromDate;
        final Instant toDate;

        {
            String query = String.format(
                "\n" +
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                "\n" +
                "DECLARE $seriesId AS Uint64;\n" +
                "DECLARE $seasonId AS Uint64;\n" +
                "\n" +
                "SELECT first_aired AS from_date FROM seasons\n" +
                "WHERE series_id = $seriesId AND season_id = $seasonId;",
                database);

            Params params = Params.of("$seriesId", uint64(seriesId), "$seasonId", uint64(seasonId));

            // Execute first query to get the required values to the client.
            // Transaction control settings don't set CommitTx flag to keep transaction active
            // after query execution.
            TxControl txControl = TxControl.serializableRw().setCommitTx(false);
            DataQueryResult result = retryCtx.supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().expect("execute data query");

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
                "\n" +
                "PRAGMA TablePathPrefix(\"%s\");\n" +
                "\n" +
                "DECLARE $seriesId AS Uint64;\n" +
                "DECLARE $fromDate AS Uint64;\n" +
                "DECLARE $toDate AS Uint64;\n" +
                "\n" +
                "SELECT season_id, episode_id, title, air_date FROM episodes\n" +
                "WHERE series_id = $seriesId AND air_date >= $fromDate AND air_date <= $toDate;",
                database);

            Params params = Params.of(
                "$seriesId", uint64(seriesId),
                "$fromDate", uint64(Duration.between(Instant.EPOCH, fromDate).toDays()),
                "$toDate", uint64(Duration.between(Instant.EPOCH, toDate).toDays()));

            // Execute second query.
            // Transaction control settings continues active transaction (tx) and
            // commits it at the end of second query execution.
            TxControl txControl = TxControl.id(txId).setCommitTx(true);
            DataQueryResult result = retryCtx.supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().expect("execute data query");

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
    private CompletableFuture<Status> explicitTcl(Session session) {
        Result<Transaction> transactionResult = session.beginTransaction(TransactionMode.SERIALIZABLE_READ_WRITE)
            .join();
        if (!transactionResult.isSuccess()) {
            return CompletableFuture.completedFuture(transactionResult.toStatus());
        }

        Transaction transaction = transactionResult.expect("cannot begin transaction");
        String query = String.format(
            "\n" +
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "DECLARE $airDate AS Uint64;\n" +
            "UPDATE episodes SET air_date = $airDate WHERE title = \"TBD\";",
            database);

        Params params = Params.of("$airDate", uint64(Duration.between(Instant.EPOCH, Instant.now()).toDays()));

        // Execute data query.
        // Transaction control settings continues active transaction (tx)
        TxControl txControl = TxControl.id(transaction).setCommitTx(false);
        Result<DataQueryResult> updateResult = session.executeDataQuery(query, txControl, params)
            .join();
        if (!updateResult.isSuccess()) {
            return CompletableFuture.completedFuture(updateResult.toStatus());
        }

        // Commit active transaction (tx)
        return transaction.commit();
    }

    @Override
    public void close() {
        tableClient.close();
    }

    public static void main(String[] args) {
        AppRunner.run("BasicExample", BasicExample::new, args);
    }
}
