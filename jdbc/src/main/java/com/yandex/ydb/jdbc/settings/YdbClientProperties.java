package com.yandex.ydb.jdbc.settings;

import java.util.Map;

import javax.annotation.Nullable;

import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.rpc.TableRpc;

public class YdbClientProperties {
    private final Map<YdbClientProperty<?>, ParsedProperty> params;

    public YdbClientProperties(Map<YdbClientProperty<?>, ParsedProperty> params) {
        this.params = params;
    }

    @Nullable
    public ParsedProperty getProperty(YdbClientProperty<?> property) {
        return params.get(property);
    }

    public Map<YdbClientProperty<?>, ParsedProperty> getParams() {
        return params;
    }

    public TableClient toTableClient(TableRpc rpc) {
        TableClient.Builder builder = TableClient.newClient(rpc);
        for (Map.Entry<YdbClientProperty<?>, ParsedProperty> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                entry.getKey().getSetter().accept(builder, entry.getValue().getParsedValue());
            }
        }
        ParsedProperty minSize = params.get(YdbClientProperty.SESSION_POOL_SIZE_MIN);
        ParsedProperty maxSize = params.get(YdbClientProperty.SESSION_POOL_SIZE_MAX);
        if (minSize != null && maxSize != null) {
            builder.sessionPoolSize(minSize.getParsedValue(), maxSize.getParsedValue());
        }
        return builder.build();
    }
}
