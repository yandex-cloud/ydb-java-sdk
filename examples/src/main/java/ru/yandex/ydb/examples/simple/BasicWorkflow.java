package ru.yandex.ydb.examples.simple;

import java.util.UUID;

import ru.yandex.ydb.core.Result;
import ru.yandex.ydb.table.SchemeClient;
import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.query.DataQuery;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.query.Params;
import ru.yandex.ydb.table.result.ResultSetReader;
import ru.yandex.ydb.table.settings.DropTableSettings;
import ru.yandex.ydb.table.transaction.TxControl;
import ru.yandex.ydb.table.types.PrimitiveType;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ru.yandex.ydb.table.values.PrimitiveValue.uint32;


public class BasicWorkflow extends SimpleExample {

    private void makeDirectory(SchemeClient schemeClient, String directoryPath) {
        schemeClient.makeDirectory(directoryPath)
                .join()
                .expect("cannot make directory: " + directoryPath);
    }

    private void removeDir(SchemeClient schemeClient, String directoryPath) {
        schemeClient.removeDirectory(directoryPath)
                .join()
                .expect("cannot remove directory: " + directoryPath);
    }

    private Session makeSession(TableClient tableClient) {
        return tableClient.createSession()
            .join()
            .expect("cannot create session");
    }

    private void createOrdersTable(Session session, String tablePath) {
        TableDescription tableDescription = TableDescription.newBuilder()
            .addNullableColumn("id", PrimitiveType.uint32())
            .addNullableColumn("counterparty", PrimitiveType.string())
            .addNullableColumn("security", PrimitiveType.string())
            .addNullableColumn("amount", PrimitiveType.uint32())
            .setPrimaryKey("id")
            .build();

        session.createTable(tablePath, tableDescription)
                .join()
                .expect("cannot create table");
    }

    private void dropTable(Session session, String tablePath) {
        session.dropTable(tablePath, new DropTableSettings())
                .join();

    }

    DataQueryResult executeQuery(Session session, String query) {
        Result<DataQueryResult> result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
            .join();
        System.out.println("Status: " + result.getCode());
        return result.expect("query failed: " + query);
    }

    DataQuery makePreparedQuery(Session session, String query) {
        return session.prepareDataQuery(query)
            .join()
            .expect("cannot create prepared query");
    }

    void processBasicData(Session session, String tablePath) {
        {
            String query = "INSERT INTO [" + tablePath + "] (id, counterparty, security, amount) " +
                    "VALUES (1, 'Fedex', 'tmob', 5000), (2, 'Apple', 'db', 10000);";
            DataQueryResult result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
                .join()
                .expect("query failed");
            DataQueryResults.print(result);
        }

        {
            String query = "SELECT * FROM [" + tablePath + "];";
            DataQueryResult result = executeQuery(session, query);
            System.out.println("Table content:");
            DataQueryResults.print(result);
        }

        {
            DataQuery query = makePreparedQuery(session,
                    "DECLARE $id AS Uint32; SELECT id, security FROM [" + tablePath + "] where id=$id;");

            Params params = Params.withUnknownTypes()
                .put("$id", uint32(1));

            DataQueryResult result = query.execute(TxControl.serializableRw().setCommitTx(true), params)
                .join()
                .expect("Can't select");

            if (result.isEmpty()) {
                throw new IllegalStateException("empty result set");
            }

            System.out.println("Prepared query results:");
            // Index of result set corresponds to its order in YQL query
            ResultSetReader resultSet = result.getResultSet(0);
            for (int i = 0; i < resultSet.getColumnCount(); i++) {
                System.out.print(resultSet.getColumnName(i) + '\t');
            }
            System.out.println("-------------------------------------");
            while (resultSet.next()) {
                long id = resultSet.getColumn("id").getUint32();
                String security = resultSet.getColumn("security").getString(UTF_8);
                System.out.println(String.format("ID=%d, security=%s", id, security));
            }
        }
        {
            String query = "INSERT INTO [" + tablePath + "] (id, counterparty, security, amount) VALUES (1, 'Fedex', 'tmob', 'Bad Value');";
            executeQuery(session, query);
        }

    }

    @Override
    void run(TableService tableService, String pathPrefix) {
        final String rootPath = pathPrefix + UUID.randomUUID().toString();
        final String workDirPath = rootPath + "/MyData";
        final String ordersTablePath = workDirPath + "/Orders";

        final SchemeClient schemeClient = tableService.newSchemeClient();
        final TableClient tableClient = tableService.newTableClient();
        final Session session = makeSession(tableClient);

        try {
            makeDirectory(schemeClient, rootPath);
            makeDirectory(schemeClient, workDirPath);
            createOrdersTable(session, ordersTablePath);

            processBasicData(session, ordersTablePath);

            dropTable(session, ordersTablePath);
            removeDir(schemeClient, workDirPath);
            removeDir(schemeClient, rootPath);
        } finally {
            session.close();
        }
    }

    public static void main(String[] args) {
        new BasicWorkflow().doMain();
    }
}
