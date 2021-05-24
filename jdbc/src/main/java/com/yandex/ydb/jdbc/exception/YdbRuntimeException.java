package com.yandex.ydb.jdbc.exception;

public class YdbRuntimeException extends RuntimeException {
    public YdbRuntimeException(String message) {
        super(message);
    }

    public YdbRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
