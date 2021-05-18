package com.yandex.ydb.jdbc.impl;

import java.util.Map;

import com.yandex.ydb.ValueProtos;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.result.impl.ProtoValueReaders;

public class MappingResultSets {

    private MappingResultSets() {
        //
    }

    static ResultSetReader readerFromMap(Map<String, String> map) {
        ValueProtos.ResultSet.Builder resultSet = ValueProtos.ResultSet.newBuilder();

        ValueProtos.Value.Builder row = resultSet.addRowsBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            resultSet.addColumnsBuilder()
                    .setName(entry.getKey())
                    .getTypeBuilder()
                    .setTypeId(ValueProtos.Type.PrimitiveTypeId.UTF8);
            row.addItemsBuilder()
                    .setTextValue(entry.getValue());
        }
        return ProtoValueReaders.forResultSet(resultSet.build());
    }

}
