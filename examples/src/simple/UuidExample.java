package ru.yandex.ydb.examples.simple;

import java.util.UUID;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.result.ResultSetReader;
import ru.yandex.ydb.table.transaction.TxControl;


/**
 * @author Sergey Polovko
 */
public class UuidExample extends SimpleExample {

    @Override
    void run(TableService tableService, String pathPrefix) {
        TableClient tableClient = tableService.newTableClient();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        String query = "SELECT CAST(\"00112233-4455-6677-8899-aabbccddeeff\" AS Uuid);";
        DataQueryResult result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
            .join()
            .expect("query failed");

        ResultSetReader resultSet = result.getResultSet(0);
        resultSet.next();

        UUID uuid = resultSet.getColumn(0).getUuid();
        System.out.println("uuid: " + uuid);

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new UuidExample().doMain();
    }
}
