package com.yandex.ydb.examples.suites;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.description.TableColumn;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.settings.AlterTableSettings;
import com.yandex.ydb.table.settings.CreateTableSettings;
import com.yandex.ydb.table.settings.DescribeTableSettings;
import com.yandex.ydb.table.settings.PartitioningSettings;
import com.yandex.ydb.table.values.PrimitiveType;
import org.junit.jupiter.api.Assertions;

/**
 *
 * @author Alexandr Gorshenin
 */
public class TableWithPartitioningSettings {
    private static final String TABLE_NAME = "test1_table";
    private final SessionRetryContext ctx;
    private final String tablePath;

    public TableWithPartitioningSettings(SessionRetryContext ctx, String path) {
        this.ctx = ctx;
        this.tablePath = path + "/" + TABLE_NAME;

    }

    public void run() {
        TableDescription tableDescription = TableDescription.newBuilder()
                .addNullableColumn("id", PrimitiveType.uint64())
                .addNullableColumn("code", PrimitiveType.utf8())
                .addNullableColumn("size", PrimitiveType.float64())
                .addNullableColumn("created", PrimitiveType.timestamp())
                .addNullableColumn("data", PrimitiveType.json())
                .setPrimaryKey("id")
                .build();

        PartitioningSettings initSettings = new PartitioningSettings();
        initSettings.setPartitionSize(2500);       // 2000 by default
        initSettings.setMinPartitionsCount(5);     // 1 by default
        initSettings.setMaxPartitionsCount(500);   // 50 by default
        initSettings.setPartitioningByLoad(true);  // false by default
        initSettings.setPartitioningBySize(true);  // true by default

        PartitioningSettings updateSettings = new PartitioningSettings();
        updateSettings.setMinPartitionsCount(2);
        updateSettings.setMaxPartitionsCount(300);
        updateSettings.setPartitioningByLoad(false);

        PartitioningSettings mergedSettings = new PartitioningSettings();
        mergedSettings.setPartitionSize(2500);       // init value
        mergedSettings.setMinPartitionsCount(2);     // updated value
        mergedSettings.setMaxPartitionsCount(300);   // updated value
        mergedSettings.setPartitioningBySize(true);  // init value
        mergedSettings.setPartitioningByLoad(false); // updated value

        createTable(tableDescription, initSettings);
        describeTable(tableDescription, initSettings, false);

        alterTable(updateSettings);
        describeTable(tableDescription, mergedSettings, true);

        dropTable();
    }

    private void createTable(TableDescription tableDescription, PartitioningSettings partitioning) {
        CreateTableSettings settings = new CreateTableSettings();
        settings.setPartitioningSettings(partitioning);

        Status status = ctx.supplyStatus(
                session -> session.createTable(tablePath, tableDescription, settings)
        ).join();

        Assertions.assertTrue(status.isSuccess(), "Create table with PartitioningSettings");
    }

    private void describeTable(
            TableDescription tableDescription,
            PartitioningSettings partitioning,
            boolean fetchStats) {

        Result<TableDescription> describeResult = ctx.supplyResult(session -> {
            if (!fetchStats) {
                return session.describeTable(tablePath);
            }

            DescribeTableSettings settings = new DescribeTableSettings();
            settings.setIncludeTableStats(true);
            settings.setIncludePartitionStats(true);
            return session.describeTable(tablePath, settings);
        }).join();
        Assertions.assertTrue(describeResult.isSuccess(), "Describe table");

        TableDescription description = describeResult.expect("ok");

        Assertions.assertEquals(
                tableDescription.getColumns().size(),
                description.getColumns().size(),
                "Table description columns size"
        );

        Assertions.assertEquals(
                tableDescription.getPrimaryKeys().size(),
                description.getPrimaryKeys().size(),
                "Table description primary keys size"
        );

        for (int idx = 0; idx < tableDescription.getColumns().size(); idx += 1) {
            TableColumn one = tableDescription.getColumns().get(idx);
            TableColumn two = description.getColumns().get(idx);

            Assertions.assertEquals(one.getName(), two.getName(), "Table column name " + idx);
            Assertions.assertEquals(one.getType(), two.getType(), "Table column type " + idx);
        }

        for (int idx = 0; idx < tableDescription.getPrimaryKeys().size(); idx += 1) {
            String one = tableDescription.getPrimaryKeys().get(idx);
            String two = description.getPrimaryKeys().get(idx);
            Assertions.assertEquals(one, two, "Table primary key " + idx);
        }

        PartitioningSettings settings = description.getPartitioningSettings();
        Assertions.assertNotNull(settings, "Table partitioning settings");

        Assertions.assertEquals(partitioning.getPartitionSizeMb(),
                settings.getPartitionSizeMb(), "Partition Size Mb");
        Assertions.assertEquals(partitioning.getMinPartitionsCount(),
                settings.getMinPartitionsCount(), "Min Partitions Count");
        Assertions.assertEquals(partitioning.getMaxPartitionsCount(),
                settings.getMaxPartitionsCount(), "Max Partitions Count");
        Assertions.assertEquals(partitioning.getPartitioningByLoad(),
                settings.getPartitioningByLoad(), "Partitioning By Load");
        Assertions.assertEquals(partitioning.getPartitioningBySize(),
                settings.getPartitioningBySize(), "Partitioning By Size");

        if (fetchStats) {
            Assertions.assertNotNull(description.getTableStats(),
                    "Table description table stats are not null");
            Assertions.assertFalse(description.getPartitionStats().isEmpty(),
                    "Table description partition stats are not empty");
        } else {
            Assertions.assertNull(description.getTableStats(),
                    "Table description table stats are null");
            Assertions.assertTrue(description.getPartitionStats().isEmpty(),
                    "Table description partition stats are empty");
        }
    }

    private void alterTable(PartitioningSettings partitioning) {
        AlterTableSettings settings = new AlterTableSettings();
        settings.setPartitioningSettings(partitioning);

        Status status = ctx.supplyStatus(
                session -> session.alterTable(tablePath, settings)
        ).join();

        Assertions.assertTrue(status.isSuccess(), "Alter table with PartitioningSettings");
    }

    private void dropTable() {
        Status status = ctx.supplyStatus(
                session -> session.dropTable(tablePath)
        ).join();

        Assertions.assertTrue(status.isSuccess(), "Drop table with PartitioningSettings");
    }

}
