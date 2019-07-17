package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.result.ResultSetReader;
import ru.yandex.ydb.table.transaction.TxControl;
import ru.yandex.ydb.table.values.DecimalValue;


/**
 * @author Sergey Polovko
 */
public class DecimalExample extends SimpleExample {

    @Override
    void run(TableService tableService, String pathPrefix) {
        TableClient tableClient = tableService.newTableClient();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        String query = "SELECT CAST(\"12.345\" AS Decimal(13, 3));";
        DataQueryResult result = session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true))
            .join()
            .expect("query failed");

        ResultSetReader resultSet = result.getResultSet(0);
        resultSet.next();

        DecimalValue value = resultSet.getColumn(0).getDecimal();
        System.out.println("decimal: " + value);
        System.out.println("BigDecimal: " + value.toBigDecimal());

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new DecimalExample().doMain();
    }
}
