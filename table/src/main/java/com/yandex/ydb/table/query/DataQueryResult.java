package com.yandex.ydb.table.query;

import java.util.List;

import com.yandex.ydb.ValueProtos;
import com.yandex.ydb.table.result.ResultSetReader;
import com.yandex.ydb.table.result.impl.ProtoValueReaders;


/**
 * @author Sergey Polovko
 */
public class DataQueryResult {

    private final String txId;
    private final List<ValueProtos.ResultSet> resultSets;

    public DataQueryResult(String txId, List<ValueProtos.ResultSet> resultSets) {
        this.txId = txId;
        this.resultSets = resultSets;
    }

    public String getTxId() {
        return txId;
    }

    public int getResultSetCount() {
        return resultSets.size();
    }

    public ResultSetReader getResultSet(int index) {
        return ProtoValueReaders.forResultSet(resultSets.get(index));
    }

    public boolean isTruncated(int index) {
        return resultSets.get(index).getTruncated();
    }

    public int getRowCount(int index) {
        return resultSets.get(index).getRowsCount();
    }

    public boolean isEmpty() {
        return txId.isEmpty() && resultSets.isEmpty();
    }
}
