package com.yandex.ydb.jdbc;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static com.yandex.ydb.jdbc.TestHelper.SKIP_DOCKER_TESTS;
import static com.yandex.ydb.jdbc.TestHelper.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class YdbDriverExampleTest {

    @Test
    @DisabledIfSystemProperty(named = SKIP_DOCKER_TESTS, matches = TRUE)
    public void testYdb() throws SQLException {

        String url = TestHelper.getTestUrl(); // "jdbc:ydb:localhost:2135/local"
        try (YdbConnection connection = (YdbConnection) DriverManager.getConnection(url)) {
            try {
                connection.createStatement()
                        .execute("--jdbc:SCHEME\n" +
                                "drop table table_sample");
            } catch (SQLException e) {
                //
            }
            connection.createStatement()
                    .execute("--jdbc:SCHEME\n" +
                            "create table table_sample(id Int32, value Utf8, primary key (id))");

            YdbPreparedStatement ps = connection
                    .prepareStatement("" +
                            "declare $p1 as Int32;\n" +
                            "declare $p2 as Utf8;\n" +
                            "upsert into table_sample (id, value) values ($p1, $p2)");
            ps.setInt(1, 1);
            ps.setString(2, "value-1");
            ps.executeUpdate();

            ps.setInt("p1", 2);
            ps.setString("p2", "value-2");
            ps.executeUpdate();

            connection.commit();


            YdbPreparedStatement select = connection
                    .prepareStatement("select count(1) as cnt from table_sample");
            ResultSet rs = select.executeQuery();
            rs.next();
            assertEquals(2, rs.getLong("cnt"));

            YdbPreparedStatement psBatch = connection
                    .prepareStatement("" +
                            "declare $values as List<Struct<id:Int32,value:Utf8>>;\n" +
                            "upsert into table_sample select * from as_table($values)");
            psBatch.setInt("id", 3);
            psBatch.setString("value", "value-3");
            psBatch.addBatch();

            psBatch.setInt("id", 4);
            psBatch.setString("value", "value-4");
            psBatch.addBatch();

            psBatch.executeBatch();

            connection.commit();

            rs = select.executeQuery();
            rs.next();
            assertEquals(4, rs.getLong("cnt"));
        }
    }
}
