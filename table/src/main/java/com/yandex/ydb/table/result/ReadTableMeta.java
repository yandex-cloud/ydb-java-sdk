package com.yandex.ydb.table.result;

import com.yandex.ydb.common.CommonProtos;

/**
 * @author Ilya Udalov
 */
public class ReadTableMeta {
    public static final class VirtualTimestamp {
        private final long planStep;
        private final long txId;

        public VirtualTimestamp(long planStep, long txId) {
            this.planStep = planStep;
            this.txId = txId;
        }

        public long getPlanStep() {
            return planStep;
        }

        public long getTxId() {
            return txId;
        }
    }

    private final VirtualTimestamp timestamp;

    public ReadTableMeta(CommonProtos.VirtualTimestamp timestamp) {
        this.timestamp = new VirtualTimestamp(timestamp.getPlanStep(), timestamp.getTxId());
    }

    public VirtualTimestamp getVirtualTimestamp() {
        return timestamp;
    }
}
