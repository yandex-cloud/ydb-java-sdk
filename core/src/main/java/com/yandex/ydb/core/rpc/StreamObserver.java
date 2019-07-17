package com.yandex.ydb.core.rpc;

import com.yandex.ydb.core.Status;


/**
 * @author Sergey Polovko
 */
public interface StreamObserver<V> {

    void onNext(V value);

    void onError(Status status);

    void onCompleted();
}
