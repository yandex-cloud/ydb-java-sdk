package ru.yandex.ydb.examples.simple;

import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.query.ExplainDataQueryResult;


/**
 * @author Sergey Polovko
 */
public class ExplainDataQuery extends SimpleExample {

    @Override
    void run(TableService tableService, String pathPrefix) {
        String tablePath = pathPrefix + getClass().getSimpleName();
        TableClient tableClient = tableService.newTableClient();

        Session session = tableClient.createSession()
            .join()
            .expect("cannot create session");

        session.dropTable(tablePath)
            .join();

        {
            String query =
                "CREATE TABLE [" + tablePath + "] (" +
                    "  key Uint32," +
                    "  value String," +
                    "  PRIMARY KEY(key)" +
                    ");";
            session.executeSchemeQuery(query)
                .join()
                .expect("cannot create table");
        }

        {
            String query = "SELECT * FROM [" + tablePath + "];";
            ExplainDataQueryResult result = session.explainDataQuery(query)
                .join()
                .expect("cannot explain query");

            System.out.println("--[ast]----------------------\n" + result.getQueryAst());
            System.out.println();
            System.out.println("--[plan]---------------------\n" + result.getQueryPlan());
        }

        session.close()
            .join()
            .expect("cannot close session");
    }

    public static void main(String[] args) {
        new ExplainDataQuery().doMain();
    }
}
