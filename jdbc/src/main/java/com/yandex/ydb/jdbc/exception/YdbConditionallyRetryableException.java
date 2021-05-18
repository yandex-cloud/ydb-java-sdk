package com.yandex.ydb.jdbc.exception;

import com.yandex.ydb.core.StatusCode;

// Treat this as non retryable exception by nature, i.e. need to handle in consciously
public class YdbConditionallyRetryableException extends YdbNonRetryableException {

    public YdbConditionallyRetryableException(Object response, StatusCode statusCode) {
        super(response, statusCode);
    }
}
