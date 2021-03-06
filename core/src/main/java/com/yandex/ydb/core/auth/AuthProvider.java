package com.yandex.ydb.core.auth;

/**
 * @author Sergey Polovko
 */
public interface AuthProvider extends AutoCloseable {

    String getToken();

    @Override
    default void close() {
    }
}

