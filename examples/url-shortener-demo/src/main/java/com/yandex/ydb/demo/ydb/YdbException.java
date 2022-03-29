package com.yandex.ydb.demo.ydb;

/**
 *
 * @author Alexandr Gorshenin
 */
public class YdbException extends Exception {
    public YdbException(String message, Throwable reason) {
        super(message, reason);
    }
}
