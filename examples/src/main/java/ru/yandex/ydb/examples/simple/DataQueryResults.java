package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.examples.TablePrinter;
import ru.yandex.ydb.table.query.DataQueryResult;


/**
 * @author Sergey Polovko
 */
public class DataQueryResults {

    static void print(DataQueryResult result) {
        if (result.isEmpty()) {
            System.out.println("results <empty>");
            return;
        }

        System.out.println("result txId: " + result.getTxId());

        for (int i = 0; i < result.getResultSetCount(); i++) {
            System.out.printf("-- [ result set #%d ] --\n", i);
            new TablePrinter(result.getResultSet(i)).print();
        }
    }
}
