package com.yandex.ydb.jdbc.exception;

import java.sql.SQLException;

public class YdbConfigurationException extends SQLException {

    public YdbConfigurationException(String message) {
        super(message);
    }

    public YdbConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
