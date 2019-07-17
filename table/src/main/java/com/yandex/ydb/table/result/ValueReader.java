package com.yandex.ydb.table.result;

import com.yandex.ydb.table.values.Type;

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

    void toString(StringBuilder sb);

    Type getValueType();

}
