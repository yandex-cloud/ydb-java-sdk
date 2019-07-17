package ru.yandex.ydb.examples.pagination;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.ydb.examples.App;
import ru.yandex.ydb.examples.AppRunner;
import ru.yandex.ydb.examples.pagination.model.School;
import ru.yandex.ydb.table.Session;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.description.TableColumn;
import ru.yandex.ydb.table.description.TableDescription;
import ru.yandex.ydb.table.query.DataQueryResult;
import ru.yandex.ydb.table.query.Params;
import ru.yandex.ydb.table.result.ResultSetReader;
import ru.yandex.ydb.table.transaction.TxControl;
import ru.yandex.ydb.table.types.ListType;
import ru.yandex.ydb.table.types.PrimitiveType;

import static ru.yandex.ydb.table.values.PrimitiveValue.uint32;
import static ru.yandex.ydb.table.values.PrimitiveValue.uint64;
import static ru.yandex.ydb.table.values.PrimitiveValue.utf8;


/**
 * This app does not preform retries on failure. Please see {@link ru.yandex.ydb.examples.basic_example.BasicExampleApp#execute}
 * for example how to do that.
 *
 * @author Sergey Polovko
 */
public class PaginationApp implements App {

    private static final int MAX_PAGES = 10;

    private final String path;
    private final Session session;

    PaginationApp(TableService tableService, String path) {
        this.path = path;

        TableClient tableClient = tableService.newTableClient();
        this.session = tableClient.createSession()
            .join()
            .expect("cannot create session");
    }

    @Override
    public void run() {
        createTable();
        describeTable();
        fillTableDataTransaction();

        final long limit = 3;

        System.out.println("--[ Pagination ] -----------------------");
        System.out.println("limit: " + limit);

        // TODO: prepare query here

        int page = 0;
        School.Key lastKey = new School.Key("", 0);
        while (page <= MAX_PAGES) {
            ++page;
            List<School> schools = selectPaging(limit, lastKey);
            if (schools.isEmpty()) {
                break;
            }

            System.out.println("-[ page: " + page + " ]-");
            for (School school : schools) {
                System.out.println("    " + school);
            }

            School lastSchool = schools.get(schools.size() - 1);
            lastKey = lastSchool.toKey();
        }
    }

    /**
     * Creates sample tables with CrateTable API.
     */
    private void createTable() {
        TableDescription schoolTable = TableDescription.newBuilder()
            .addNullableColumn("city", PrimitiveType.utf8())
            .addNullableColumn("number", PrimitiveType.uint32())
            .addNullableColumn("address", PrimitiveType.utf8())
            .setPrimaryKeys("city", "number")
            .build();

        session.createTable(path + "/schools", schoolTable)
            .join()
            .expect("cannot create schools table");
    }

    /**
     * Describe existing table.
     */
    private void describeTable() {
        System.out.println("--[ DescribeTable ]---------------------------------------");

        String tablePath = path + "/schools";
        TableDescription tableDesc = session.describeTable(tablePath)
            .join()
            .expect("cannot describe schools table");

        System.out.println(tablePath + ':');
        List<String> primaryKeys = tableDesc.getPrimaryKeys();
        for (TableColumn column : tableDesc.getColumns()) {
            boolean isPrimary = primaryKeys.contains(column.getName());
            System.out.println("    " + column.getName() + ": " + column.getType() + (isPrimary ? " (PK)" : ""));
        }
        System.out.println();
    }

    /**
     * Fills sample table with data in single parameterized data query.
     */
    private void fillTableDataTransaction() {
        String query = String.format(
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $schoolsData AS \"List<Struct<\n" +
            "    city: Utf8,\n" +
            "    number: Uint32,\n" +
            "    address: Utf8>>\";\n" +
            "\n" +
            "REPLACE INTO schools\n" +
            "SELECT\n" +
            "    city,\n" +
            "    number,\n" +
            "    address\n" +
            "FROM AS_TABLE($schoolsData);",
            path);

        Params params = Params.withUnknownTypes()
            .put("$schoolsData", ListType.of(PaginationData.SCHOOL_TYPE), PaginationData.SCHOOL_DATA);

        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        session.executeDataQuery(query, txControl, params)
            .join()
            .expect("cannot insert data into schools table");
    }

    private List<School> selectPaging(long limit, School.Key lastSchool) {
        String query = String.format(
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $limit AS Uint64;\n" +
            "DECLARE $lastCity AS Utf8;\n" +
            "DECLARE $lastNumber AS Uint32;\n" +
            "\n" +
            "$Data = (\n" +
            "    SELECT * FROM schools\n" +
            "    WHERE city = $lastCity AND number > $lastNumber\n" +
            "    ORDER BY city, number LIMIT $limit\n" +
            "\n" +
            "    UNION ALL\n" +
            "\n" +
            "    SELECT * FROM schools\n" +
            "    WHERE city > $lastCity\n" +
            "    ORDER BY city, number LIMIT $limit\n" +
            ");\n" +
            "SELECT * FROM $Data ORDER BY city, number LIMIT $limit;",
            path);

        Params params = Params.withUnknownTypes()
            .put("$limit", uint64(limit))
            .put("$lastCity", utf8(lastSchool.getCity()))
            .put("$lastNumber", uint32(lastSchool.getNumber()));

        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        DataQueryResult result = session.executeDataQuery(query, txControl, params)
                .join()
                .expect("cannot execute data query");

        ResultSetReader resultSet = result.getResultSet(0);
        List<School> schools = new ArrayList<>(resultSet.getRowCount());
        while (resultSet.next()) {
            String city = resultSet.getColumn("city").getUtf8();
            int number = (int) resultSet.getColumn("number").getUint32();
            String address = resultSet.getColumn("address").getUtf8();
            schools.add(new School(city, number, address));
        }
        return schools;
    }

    @Override
    public void close() {
        session.close();
    }

    public static void main(String[] args) {
        AppRunner.run("PaginationApp", PaginationApp::new, args);
    }
}
