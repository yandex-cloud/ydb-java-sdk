package com.yandex.ydb.core.grpc;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;


/**
 * @author Sergey Polovko
 */
public class UnaryStreamToBiConsumer<T> extends ClientCall.Listener<T> {

    private static final AtomicIntegerFieldUpdater<UnaryStreamToBiConsumer> acceptedUpdater =
        AtomicIntegerFieldUpdater.newUpdater(UnaryStreamToBiConsumer.class, "accepted");

    private final BiConsumer<T, Status> consumer;
    private volatile int accepted = 0;
    private T value;

    public UnaryStreamToBiConsumer(BiConsumer<T, Status> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onMessage(T value) {
        if (this.value != null) {
            accept(null, Status.INTERNAL.withDescription("More than one value received for gRPC unary call"));
        } else {
            this.value = value;
        }
    }

    @Override
    public void onClose(Status status, @Nullable Metadata trailers) {
        if (status.isOk()) {
            if (value == null) {
                accept(null, Status.INTERNAL.withDescription("No value received for gRPC unary call"));
            } else {
                accept(value, status);
            }
        } else {
            accept(null, status);
        }
    }

    private void accept(T value, Status status) {
        // protect from double accept
        if (acceptedUpdater.compareAndSet(this, 0, 1)) {
            consumer.accept(value, status);
        }
    }
}
