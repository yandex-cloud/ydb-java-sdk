package com.yandex.ydb.spring.jpa;

import java.sql.CallableStatement;
import java.util.List;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.procedure.spi.CallableStatementSupport;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;
import org.hibernate.procedure.spi.ParameterStrategy;

public class YdbCallableStatementSupport implements CallableStatementSupport {
    @Override
    public void registerParameters(String procedureName, CallableStatement statement,
                                   ParameterStrategy parameterStrategy,
                                   List<ParameterRegistrationImplementor<?>> parameterRegistrations,
                                   SharedSessionContractImplementor session) {
        throw new UnsupportedOperationException("No callable statement supported by " + getClass().getName());
    }
}
