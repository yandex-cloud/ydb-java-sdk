/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yandex.ydb.spring.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.yandex.ydb.jdbc.YdbConnection;
import com.yandex.ydb.jdbc.YdbPreparedStatement;
import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.jdbc.impl.YdbPreparedStatementInMemoryImpl;
import com.yandex.ydb.table.values.ListType;
import com.yandex.ydb.table.values.Type;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;

import static com.yandex.ydb.jdbc.YdbConst.PARAMETER_TYPE_UNKNOWN;

/**
 * Implementation supports YDB-compatible prepared statements
 */
public class YdbPreparedStatementCreatorFactory extends PreparedStatementCreatorFactory {

    private final List<SqlParameter> declaredParameters;

    public YdbPreparedStatementCreatorFactory(String sql, List<SqlParameter> declaredParameters) {
        super(sql, declaredParameters);
        this.declaredParameters = declaredParameters;
    }

    @Override
    public void setUpdatableResults(boolean updatableResults) {
        if (updatableResults) {
            throw new YdbDaoRuntimeException("Updatable results are not supported in YDB");
        }
    }

    @Override
    public PreparedStatementCreator newPreparedStatementCreator(@Nullable List<?> params) {
        return new PreparedStatementCreatorImpl(getSql(),
                params != null ? params : Collections.emptyList());
    }

    @Override
    public PreparedStatementCreator newPreparedStatementCreator(@Nullable Object[] params) {
        return new PreparedStatementCreatorImpl(getSql(),
                params != null ? Arrays.asList(params) : Collections.emptyList());
    }

    @Override
    public PreparedStatementCreator newPreparedStatementCreator(String sqlToUse, @Nullable Object[] params) {
        return new PreparedStatementCreatorImpl(sqlToUse,
                params != null ? Arrays.asList(params) : Collections.emptyList());
    }

    @Override
    public PreparedStatementSetter newPreparedStatementSetter(@Nullable List<?> params) {
        return new PreparedStatementCreatorImpl(getSql(),
                params != null ? params : Collections.emptyList());
    }

    @Override
    public PreparedStatementSetter newPreparedStatementSetter(@Nullable Object[] params) {
        return new PreparedStatementCreatorImpl(getSql(),
                params != null ? Arrays.asList(params) : Collections.emptyList());
    }


    private class PreparedStatementCreatorImpl
            implements PreparedStatementCreator, PreparedStatementSetter, SqlProvider, ParameterDisposer {

        private final String actualSql;

        private final List<?> parameters;

        public PreparedStatementCreatorImpl(String actualSql, List<?> parameters) {
            this.actualSql = actualSql;
            this.parameters = parameters;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = ((YdbConnection) con).prepareStatementInMemory(getSql());
            setValues(ps);
            return ps;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            Preconditions.checkArgument(ps instanceof YdbPreparedStatement, "Accept only YdbPreparedStatements");
            YdbPreparedStatement ydbPs = (YdbPreparedStatement) ps;

            int len = this.parameters.size();
            for (int i = 0; i < len; i++) {
                Object in = this.parameters.get(i);
                if (declaredParameters.size() <= i) {
                    throw new InvalidDataAccessApiUsageException(
                            "SQL [" + getSql() + "]: unable to access parameter number " + (i + 1) +
                                    " given only " + declaredParameters.size() + " parameters");

                }
                SqlParameter declaredParameter = declaredParameters.get(i);
                String paramName = declaredParameter.getName();
                if (paramName == null) {
                    throw new YdbDaoRuntimeException("Cannot find parameter name in " + declaredParameter);
                }

                int sqlType = declaredParameter.getSqlType();
                if (sqlType == SqlParameterSource.TYPE_UNKNOWN) {
                    throw new YdbDaoRuntimeException("Unable to detect parameter type: [" + paramName + "]");
                }

                // TODO: Use type from SqlParameterValue?
                if (in instanceof SqlParameterValue) {
                    SqlParameterValue paramValue = (SqlParameterValue) in;
                    in = paramValue.getValue();
                }

                // TODO: make better - find a way to pass Type through ORM
                if (ydbPs instanceof YdbPreparedStatementInMemoryImpl) {
                    YdbPreparedStatementInMemoryImpl ydbPsInMem = (YdbPreparedStatementInMemoryImpl) ydbPs;
                    Type type = ydbPsInMem.getConnection().getYdbTypes().toYdbType(sqlType);
                    if (type == null) {
                        throw new YdbExecutionException(String.format(PARAMETER_TYPE_UNKNOWN, sqlType, paramName));
                    }
                    if (in instanceof Iterable) {
                        ydbPsInMem.setObject(paramName, in, ListType.of(type.makeOptional()));
                    } else {
                        if (in == null) {
                            ydbPsInMem.setObject(paramName, in, type.makeOptional());
                        } else {
                            ydbPsInMem.setObject(paramName, in, type);
                        }
                    }
                } else {
                    ydbPs.setObject(paramName, in);
                }
            }
        }

        @Override
        public String getSql() {
            return actualSql;
        }

        @Override
        public void cleanupParameters() {
            StatementCreatorUtils.cleanupParameters(this.parameters);
        }

        @Override
        public String toString() {
            return "PreparedStatementCreator: sql=[" + getSql() + "]; parameters=" + this.parameters;
        }
    }
}
