package com.yandex.ydb.table.utils;

import com.google.protobuf.Duration;
import com.yandex.ydb.OperationProtos;
import com.yandex.ydb.common.CommonProtos.FeatureFlag.Status;
import com.yandex.ydb.table.settings.RequestSettings;

public class OperationParamUtils {

    private OperationParamUtils() {
    }

    public static OperationProtos.OperationParams fromRequestSettings(RequestSettings<?> requestSettings) {
        OperationProtos.OperationParams.Builder builder = OperationProtos.OperationParams.newBuilder();
        requestSettings.getOperationTimeout().ifPresent(duration -> builder.setOperationTimeout(toProto(duration)));
        requestSettings.getCancelAfter().ifPresent(duration -> builder.setCancelAfter(toProto(duration)));
        requestSettings.getReportCostInfo().ifPresent(report ->
                builder.setReportCostInfo(report ? Status.ENABLED : Status.DISABLED)
        );
        return builder.build();
    }

    private static Duration toProto(java.time.Duration duration) {
        return Duration.newBuilder()
                .setSeconds(duration.getSeconds())
                .setNanos(duration.getNano())
                .build();
    }
}
