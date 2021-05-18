package com.yandex.ydb.jdbc.exception;

import com.yandex.ydb.core.StatusCode;

public class YdbNonRetryableException extends YdbExecutionStatusException {

    public YdbNonRetryableException(Object response, StatusCode statusCode) {
        super(response, statusCode);
    }
}
