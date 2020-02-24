package com.yandex.ydb.table.result;

import com.yandex.ydb.table.values.Type;
import com.yandex.ydb.table.values.Value;

/**
 * @author Sergey Polovko
 */
public interface ValueReader extends
    PrimitiveReader,
    OptionalReader,
    TupleReader,
    ListReader,
    DictReader,
    StructReader,
    VariantReader
{

    /**
     * Writes string representation of current value into given string builder.
     *
     * @param sb string builder
     */
    void toString(StringBuilder sb);

    /**
     * Returns value object for current cell.
     * Please note that this method will create value object for each method call.
     *
     * @return value
     */
    Value<?> getValue();

    /**
     * Returns value type for current cell.
     * Please note that this method will create value type for each method call.
     *
     * @return type
     */
    Type getType();

}
