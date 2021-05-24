package com.yandex.ydb.jdbc.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.protobuf.NullValue;
import com.yandex.ydb.ValueProtos;
import com.yandex.ydb.ValueProtos.Type.PrimitiveTypeId;
import com.yandex.ydb.jdbc.exception.YdbRuntimeException;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.result.impl.ProtoValueReaders;
import com.yandex.ydb.table.values.PrimitiveType;

public class MappingResultSets {

    private MappingResultSets() {
        //
    }

    @SuppressWarnings("unchecked")
    static <K> LinkedHashMap<K, Object> stableMap(K key, Object value, Object... kv) {
        Preconditions.checkArgument((kv.length & 1) == 0, "KeyValue list must be even");

        LinkedHashMap<K, Object> map = new LinkedHashMap<>((kv.length / 2) + 1);
        map.put(key, value);
        for (int i = 0; i < kv.length; i += 2) {
            map.put((K) kv[i], kv[i + 1]);
        }

        return map;
    }

    static ResultSetReader readerFromList(List<Map<String, Object>> list) {
        if (list.isEmpty()) {
            return emptyReader(Collections.emptyMap());
        }
        ValueProtos.ResultSet.Builder resultSet = ValueProtos.ResultSet.newBuilder();
        for (Map.Entry<String, Object> entry : list.iterator().next().entrySet()) {
            ValueProtos.Type.Builder itemBuilder = resultSet.addColumnsBuilder()
                    .setName(entry.getKey())
                    .getTypeBuilder()
                    .getOptionalTypeBuilder()
                    .getItemBuilder();
            Object value = entry.getValue();
            if (value == null || value instanceof String) {
                itemBuilder.setTypeId(PrimitiveTypeId.UTF8);
            } else if (value instanceof Integer) {
                itemBuilder.setTypeId(PrimitiveTypeId.INT32);
            } else if (value instanceof Short) {
                itemBuilder.setTypeId(PrimitiveTypeId.INT16);
            } else if (value instanceof Boolean) {
                itemBuilder.setTypeId(PrimitiveTypeId.BOOL);
            } else {
                throw new YdbRuntimeException("Internal error. Unsupported YDB type: " + value);
            }
        }

        for (Map<String, Object> map : list) {
            ValueProtos.Value.Builder row = resultSet.addRowsBuilder();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                ValueProtos.Value.Builder item = row.addItemsBuilder();
                Object value = entry.getValue();
                if (value == null) {
                    item.setNullFlagValue(NullValue.NULL_VALUE);
                } else if (value instanceof String) {
                    item.setTextValue((String) value);
                } else if (value instanceof Integer) {
                    item.setInt32Value((Integer) value);
                } else if (value instanceof Short) {
                    item.setInt32Value((Short) value);
                } else if (value instanceof Boolean) {
                    item.setBoolValue((Boolean) value);
                } else {
                    throw new YdbRuntimeException("Internal error. Unsupported YDB type: " + value);
                }
            }
        }
        return ProtoValueReaders.forResultSet(resultSet.build());

    }

    static ResultSetReader readerFromMap(Map<String, Object> map) {
        return readerFromList(Collections.singletonList(map));
    }

    static ResultSetReader emptyReader(Map<String, PrimitiveType.Id> keys) {
        ValueProtos.ResultSet.Builder resultSet = ValueProtos.ResultSet.newBuilder();

        for (Map.Entry<String, PrimitiveType.Id> entry : keys.entrySet()) {
            PrimitiveTypeId typeId = PrimitiveTypeId.forNumber(entry.getValue().getNumId());
            if (typeId == null) {
                throw new RuntimeException("Internal error. Unable to convert primitive type " + entry.getValue() +
                        " to proto type");
            }

            resultSet.addColumnsBuilder()
                    .setName(entry.getKey())
                    .getTypeBuilder()
                    .setTypeId(typeId);
        }
        return ProtoValueReaders.forResultSet(resultSet.build());
    }

}
