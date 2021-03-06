package com.yandex.ydb.core.grpc;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.yandex.ydb.core.Issue;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.StatusCode;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;

import static com.yandex.yql.proto.IssueSeverity.TSeverityIds.ESeverityId.S_ERROR;


/**
 * @author Sergey Polovko
 */
public class UnaryStreamToFuture<T> extends ClientCall.Listener<T> {

    private final CompletableFuture<Result<T>> responseFuture;
    private T value;

    public UnaryStreamToFuture(CompletableFuture<Result<T>> responseFuture) {
        this.responseFuture = responseFuture;
    }

    @Override
    public void onMessage(T value) {
        if (this.value != null) {
            Issue issue = Issue.of("More than one value received for gRPC unary call", S_ERROR);
            responseFuture.complete(Result.fail(StatusCode.CLIENT_INTERNAL_ERROR, issue));
        }
        this.value = value;
    }

    @Override
    public void onClose(Status status, @Nullable Metadata trailers) {
        if (status.isOk()) {
            if (value == null) {
                Issue issue = Issue.of("No value received for gRPC unary call", S_ERROR);
                responseFuture.complete(Result.fail(StatusCode.CLIENT_INTERNAL_ERROR, issue));
            } else {
                responseFuture.complete(Result.success(value));
            }
        } else {
            responseFuture.complete(GrpcStatuses.toResult(status));
        }
    }
}
