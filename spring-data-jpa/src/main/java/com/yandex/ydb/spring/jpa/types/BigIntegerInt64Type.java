package com.yandex.ydb.spring.jpa.types;

import java.math.BigInteger;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.java.BigIntegerTypeDescriptor;
import org.hibernate.type.descriptor.sql.BigIntTypeDescriptor;

public class BigIntegerInt64Type extends AbstractSingleColumnStandardBasicType<BigInteger>
        implements DiscriminatorType<BigInteger> {

    public static final String NAME = "com.yandex.ydb.spring.jpa.types.BigIntegerInt64Type";
    public static final BigIntegerInt64Type INSTANCE = new BigIntegerInt64Type();

    public BigIntegerInt64Type() {
        super( BigIntTypeDescriptor.INSTANCE, BigIntegerTypeDescriptor.INSTANCE );
    }

    @Override
    public String getName() {
        return "big_integer";
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    @Override
    public String objectToSQLString(BigInteger value, Dialect dialect) {
        return BigIntegerTypeDescriptor.INSTANCE.toString( value );
    }

    @Override
    public BigInteger stringToObject(String string) {
        return BigIntegerTypeDescriptor.INSTANCE.fromString( string );
    }
}
