package com.yandex.ydb.table.values;

import com.yandex.ydb.ValueProtos;


/**
 * @author Sergey Polovko
 */
public interface Value<T extends Type> {

    Value[] EMPTY_ARRAY = {};

    T getType();

    ValueProtos.Value toPb();

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
}
