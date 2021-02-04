package com.yandex.ydb.examples.pagination;

import java.util.ArrayList;
import java.util.List;

import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.examples.App;
import com.yandex.ydb.examples.AppRunner;
import com.yandex.ydb.examples.pagination.model.School;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;

import static com.yandex.ydb.table.values.PrimitiveValue.uint32;
import static com.yandex.ydb.table.values.PrimitiveValue.uint64;
import static com.yandex.ydb.table.values.PrimitiveValue.utf8;


/**
 * This app does not preform retries on failure. Please see {@link com.yandex.ydb.examples.basic_example.BasicExample#execute}
 * for example how to do that.
 *
 * @author Sergey Polovko
 */
public class PaginationApp implements App {

    private static final int MAX_PAGES = 10;

    private final String path;
    private final TableClient tableClient;
    private final Session session;

    PaginationApp(RpcTransport transport, String path) {
        this.path = path;
        this.tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport))
            .build();
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
            "\n" +
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $schoolsData AS List<Struct<\n" +
            "    city: Utf8,\n" +
            "    number: Uint32,\n" +
            "    address: Utf8>>;\n" +
            "\n" +
            "REPLACE INTO schools\n" +
            "SELECT\n" +
            "    city,\n" +
            "    number,\n" +
            "    address\n" +
            "FROM AS_TABLE($schoolsData);",
            path);

        Params params = Params.of("$schoolsData", PaginationData.SCHOOL_DATA);
        TxControl txControl = TxControl.serializableRw().setCommitTx(true);

        session.executeDataQuery(query, txControl, params)
            .join()
            .expect("cannot insert data into schools table");
    }

    private List<School> selectPaging(long limit, School.Key lastSchool) {
        String query = String.format(
            "\n" +
            "PRAGMA TablePathPrefix(\"%s\");\n" +
            "\n" +
            "DECLARE $limit AS Uint64;\n" +
            "DECLARE $lastCity AS Utf8;\n" +
            "DECLARE $lastNumber AS Uint32;\n" +
            "\n" +
            "$part1 = (\n" +
            "    SELECT * FROM schools\n" +
            "    WHERE city = $lastCity AND number > $lastNumber\n" +
            "    ORDER BY city, number LIMIT $limit\n" +
            ");\n" +
            "\n" +
            "$part2 = (\n" +
            "    SELECT * FROM schools\n" +
            "    WHERE city > $lastCity\n" +
            "    ORDER BY city, number LIMIT $limit\n" +
            ");\n" +
            "$union = (\n" +
            "    SELECT * FROM $part1\n" +
            "    UNION ALL\n" +
            "    SELECT * FROM $part2\n" +
            ");\n" +
            "\n" +
            "SELECT * FROM $union\n" +
            "ORDER BY city, number LIMIT $limit;",
            path);

        Params params = Params.of(
            "$limit", uint64(limit),
            "$lastCity", utf8(lastSchool.getCity()),
            "$lastNumber", uint32(lastSchool.getNumber()));

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
        tableClient.close();
    }

    public static void main(String[] args) {
        AppRunner.run("PaginationApp", PaginationApp::new, args);
    }
}
