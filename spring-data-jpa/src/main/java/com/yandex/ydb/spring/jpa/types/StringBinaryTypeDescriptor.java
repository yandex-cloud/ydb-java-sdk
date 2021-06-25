package com.yandex.ydb.spring.jpa.types;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;

public class StringBinaryTypeDescriptor extends StringTypeDescriptor {
    public static final StringBinaryTypeDescriptor INSTANCE = new StringBinaryTypeDescriptor();

    @Override
    public <X> X unwrap(String value, Class<X> type, WrapperOptions options) {
        if ( value == null ) {
            return null;
        }
        if (byte[].class.isAssignableFrom(type)) {
            return (X) value.getBytes();
        }
        return super.unwrap(value, type, options);
    }

    @Override
    public <X> String wrap(X value, WrapperOptions options) {
        if ( value == null ) {
            return null;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value);
        }
        return super.wrap(value, options);
    }
}
