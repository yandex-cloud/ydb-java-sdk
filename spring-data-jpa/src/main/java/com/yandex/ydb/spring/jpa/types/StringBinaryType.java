package com.yandex.ydb.spring.jpa.types;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;

public class StringBinaryType
        extends AbstractSingleColumnStandardBasicType<String>
        implements DiscriminatorType<byte[]> {
    public static final String NAME = "com.yandex.ydb.spring.jpa.types.StringBinaryType";
    public static final StringBinaryType INSTANCE = new StringBinaryType();

    public StringBinaryType() {
        super(BinaryTypeDescriptor.INSTANCE, StringBinaryTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "string";
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    public String objectToSQLString(String value, Dialect dialect) {
        return '\'' + value + '\'';
    }

    @Override
    public byte[] stringToObject(String xml) {
        return xml == null ? null : xml.getBytes();
    }

    @Override
    public String objectToSQLString(byte[] value, Dialect dialect) {
        return value == null ? null : new String(value);
    }

    @Override
    public String toString(String value) {
        return value;
    }
}
