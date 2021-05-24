package com.yandex.ydb.spring.data;

import org.springframework.dao.NonTransientDataAccessException;

public class YdbDaoRuntimeException extends NonTransientDataAccessException {
    public YdbDaoRuntimeException(String message) {
        super(message);
    }

    public YdbDaoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
