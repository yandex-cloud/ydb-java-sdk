package com.yandex.ydb.table.impl;

import com.yandex.ydb.table.SchemeClient;
import com.yandex.ydb.table.rpc.SchemeRpc;


/**
 * @author Sergey Polovko
 */
public class SchemeClientBuilderImpl implements SchemeClient.Builder {

    protected final SchemeRpc schemeRpc;

    public SchemeClientBuilderImpl(SchemeRpc schemeRpc) {
        this.schemeRpc = schemeRpc;
    }

    @Override
    public SchemeClient build() {
        return new SchemeClientImpl(this);
    }
}
