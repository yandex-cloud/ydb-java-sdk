package com.yandex.ydb.table.result;

/**
 * @author Sergey Polovko
 */
public interface OptionalReader {

    boolean isOptionalItemPresent();

    ValueReader getOptionalItem();

}
