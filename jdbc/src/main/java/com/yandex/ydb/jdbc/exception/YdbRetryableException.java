package com.yandex.ydb.jdbc.exception;

import com.yandex.ydb.core.StatusCode;

public class YdbRetryableException extends YdbExecutionStatusException {

    public YdbRetryableException(Object response, StatusCode statusCode) {
        super(response, statusCode);
    }
}
