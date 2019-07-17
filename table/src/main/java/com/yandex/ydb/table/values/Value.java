package com.yandex.ydb.table.values;

import com.yandex.ydb.ValueProtos;
import com.yandex.ydb.table.types.Type;


/**
 * @author Sergey Polovko
 */
public interface Value<T extends Type> {

    default PrimitiveValue asData() {
        return (PrimitiveValue) this;
    }

    default DictValue asDict() {
        return (DictValue) this;
    }

    default ListValue asList() {
        return (ListValue) this;
    }

    default OptionalValue asOptional() {
        return (OptionalValue) this;
    }

    default StructValue asStuct() {
        return (StructValue) this;
    }

    default VariantValue asVariant() {
        return (VariantValue) this;
    }

    default VoidValue asVoid() {
        return (VoidValue) this;
    }

    default OptionalValue makeOptional() {
        return OptionalValue.of(this);
    }

    ValueProtos.Value toPb(T type);
}
