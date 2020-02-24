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

    void toString(StringBuilder sb);

    Value<?> getGenericValue();

    Type getValueType();

}
