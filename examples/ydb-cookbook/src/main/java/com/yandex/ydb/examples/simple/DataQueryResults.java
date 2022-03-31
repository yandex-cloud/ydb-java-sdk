package com.yandex.ydb.examples.simple;

import com.yandex.ydb.examples.TablePrinter;
import com.yandex.ydb.table.query.DataQueryResult;


/**
 * @author Sergey Polovko
 */
public class DataQueryResults {
    private DataQueryResults() { }

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
