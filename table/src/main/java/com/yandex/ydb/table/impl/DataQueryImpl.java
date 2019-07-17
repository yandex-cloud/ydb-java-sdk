package com.yandex.ydb.table.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableMap;
import com.yandex.ydb.ValueProtos;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.table.query.DataQuery;
import com.yandex.ydb.table.query.DataQueryResult;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.settings.ExecuteDataQuerySettings;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.types.Type;
import com.yandex.ydb.table.types.proto.ProtoType;


/**
 * @author Sergey Polovko
 */
final class DataQueryImpl implements DataQuery {

    private final SessionImpl session;
    private final String queryId;
    private final ImmutableMap<String, Type> types;
    private final ImmutableMap<String, ValueProtos.Type> typesPb;

    DataQueryImpl(SessionImpl session, String queryId, Map<String, ValueProtos.Type> parametersTypes) {
        this.session = session;
        this.queryId = queryId;

        ImmutableMap.Builder<String, Type> types = new ImmutableMap.Builder<>();
        for (Map.Entry<String, ValueProtos.Type> e : parametersTypes.entrySet()) {
            types.put(e.getKey(), ProtoType.fromPb(e.getValue()));
        }
        this.types = types.build();

        this.typesPb = ImmutableMap.copyOf(parametersTypes);
    }

    @Override
    public String getId() {
        return queryId;
    }

    @Override
    public Params.KnownTypes newParams() {
        return Params.withKnownTypes(types, typesPb);
    }

    @Override
    public CompletableFuture<Result<DataQueryResult>> execute(
        TxControl txControl, Params params, ExecuteDataQuerySettings settings)
    {
        return session.executePreparedDataQuery(queryId, txControl, params, settings);
    }
}
