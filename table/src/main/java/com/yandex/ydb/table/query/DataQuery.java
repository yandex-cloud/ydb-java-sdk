package com.yandex.ydb.table.query;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.table.settings.ExecuteDataQuerySettings;
import com.yandex.ydb.table.transaction.TxControl;


/**
 * @author Sergey Polovko
 */
public interface DataQuery {

    String getId();

    Params newParams();

    Optional<String> getText();

    CompletableFuture<Result<DataQueryResult>> execute(
        TxControl txControl, Params params, ExecuteDataQuerySettings settings);

    default CompletableFuture<Result<DataQueryResult>> execute(TxControl txControl, Params params) {
        return execute(txControl, params, new ExecuteDataQuerySettings());
    }

    default CompletableFuture<Result<DataQueryResult>> execute(TxControl txControl) {
        return execute(txControl, Params.empty(), new ExecuteDataQuerySettings());
    }
}
