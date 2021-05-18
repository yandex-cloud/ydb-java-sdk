package com.yandex.ydb.jdbc.exception;

import java.sql.SQLException;

public class YdbExecutionException extends SQLException {

    public YdbExecutionException(String reason) {
        super(reason);
    }

    public YdbExecutionException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public YdbExecutionException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
}
