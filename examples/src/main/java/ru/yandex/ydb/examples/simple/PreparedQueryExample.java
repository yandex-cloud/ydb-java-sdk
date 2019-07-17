package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.query.DataQuery;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.query.Params;
import ru.yandex.ydb.table.settings.DropTableSettings;
import ru.yandex.ydb.table.transaction.TxControl;
import ru.yandex.ydb.table.types.PrimitiveType;
import ru.yandex.ydb.table.values.PrimitiveValue;


/**
 * @author Sergey Polovko
 */
public class PreparedQueryExample extends SimpleExample {

    @Override
    void run(TableService tableService, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = tableService.newTableClient();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        session.dropTable(tablePath, new DropTableSettings())
            .join();

        {
            TableDescription tableDescription = TableDescription.newBuilder()
                .addNullableColumn("name", PrimitiveType.utf8())
                .addNullableColumn("size", PrimitiveType.uint64())
                .setPrimaryKey("name")
                .build();

            session.createTable(tablePath, tableDescription)
                .join()
                .expect("cannot create table");
        }

        {
            DataQuery query = session.prepareDataQuery(
                    "DECLARE $name AS Utf8;" +
                    "DECLARE $size AS Uint32;" +
                    "INSERT INTO [" + tablePath + "] (name, size) VALUES ($name, $size);")
                .join()
                .expect("cannot create prepared query");

            Params params = query.newParams()
                .put("$name", PrimitiveValue.utf8("/etc/passwd"))
                .put("$size", PrimitiveValue.uint32(42));

            DataQueryResult result = query.execute(TxControl.serializableRw().setCommitTx(true), params)
                .join()
                .expect("query failed");

            DataQueryResults.print(result);
        }

        {
            String query = "SELECT * FROM [" + tablePath + "];";

            DataQueryResult result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
                .join()
                .expect("query failed");

            DataQueryResults.print(result);
        }

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new PreparedQueryExample().doMain();
    }
}
