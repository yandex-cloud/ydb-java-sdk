package com.yandex.ydb.spring.jpa;

import java.sql.Types;

import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.jdbc.impl.YdbTypesImpl;
import com.yandex.ydb.spring.jpa.types.BigIntegerInt64Type;
import com.yandex.ydb.spring.jpa.types.LocalDateTimeType;
import com.yandex.ydb.spring.jpa.types.LocalDateType;
import com.yandex.ydb.table.values.Type;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.procedure.spi.CallableStatementSupport;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.sql.IntegerTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class YdbDialect extends Dialect {
    public static final String MAX_BATCH_SIZE = "10000";
    public static final int IN_LIMIT = 10000;

    private static final YdbLimitHandler LIMIT_HANDLER = new YdbLimitHandler();
    private static final YdbUniqueDelegate UNIQUE_DELEGATE = new YdbUniqueDelegate();
    private static final YdbCallableStatementSupport CALLABLE_STATEMENT_SUPPORT = new YdbCallableStatementSupport();
    private static final YdbSQLExceptionConversionDelegate SQL_EXCEPTIONS_DELEGATE =
            new YdbSQLExceptionConversionDelegate();

    public YdbDialect() {
        YdbTypes types = YdbTypesImpl.getInstance();
        for (Integer sqlType : types.getSqlTypes()) {
            Type type = types.toYdbType(sqlType);
            if (type != null) {
                registerColumnType(sqlType, type.toString());
            }
        }
        getDefaultProperties().setProperty(AvailableSettings.STATEMENT_BATCH_SIZE, MAX_BATCH_SIZE);
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        typeContributions.contributeType(BigIntegerInt64Type.INSTANCE);
        typeContributions.contributeType(LocalDateTimeType.INSTANCE);
        typeContributions.contributeType(LocalDateType.INSTANCE);
    }

    @Override
    public LimitHandler getLimitHandler() {
        return LIMIT_HANDLER;
    }

    @Override
    public boolean supportsLockTimeouts() {
        return false;
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    @Override
    public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
        return SQL_EXCEPTIONS_DELEGATE;
    }

    @Override
    public boolean supportsUnionAll() {
        return true;
    }

    @Override
    public boolean supportsCaseInsensitiveLike() {
        return true;
    }

    @Override
    public String toBooleanValueString(boolean bool) {
        return String.valueOf(bool);
    }

    @Override
    public NameQualifierSupport getNameQualifierSupport() {
        return NameQualifierSupport.NONE;
    }

    @Override
    public char openQuote() {
        return '`';
    }

    @Override
    public char closeQuote() {
        return '`';
    }

    @Override
    public boolean canCreateSchema() {
        return false;
    }

    @Override
    public String[] getCreateSchemaCommand(String schemaName) {
        throw new UnsupportedOperationException("No create schema syntax supported by " + getClass().getName());
    }

    @Override
    public String[] getDropSchemaCommand(String schemaName) {
        throw new UnsupportedOperationException("No drop schema syntax supported by " + getClass().getName());
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public String getDropForeignKeyString() {
        throw new UnsupportedOperationException("No drop foreign key syntax supported by " + getClass().getName());
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable,
                                                   String[] primaryKey, boolean referencesPrimaryKey) {
        throw new UnsupportedOperationException("No add foreign key syntax supported by " + getClass().getName());
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String foreignKeyDefinition) {
        throw new UnsupportedOperationException("No add foreign key syntax supported by " + getClass().getName());
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        throw new UnsupportedOperationException("No add primary key syntax supported by " + getClass().getName());
    }

    @Override
    public String getNullColumnString() {
        return " null";
    }

    @Override
    public boolean supportsColumnCheck() {
        return false;
    }

    @Override
    public boolean supportsTableCheck() {
        return false;
    }

    @Override
    public boolean supportsRowValueConstructorSyntax() {
        return true;
    }

    @Override
    public boolean supportsRowValueConstructorSyntaxInInList() {
        return true;
    }

    @Override
    public boolean supportsCircularCascadeDeleteConstraints() {
        return false;
    }

    @Override
    public boolean supportsTupleCounts() {
        return false;
    }

    @Override
    public boolean supportsTupleDistinctCounts() {
        return false;
    }

    @Override
    public int getInExpressionCountLimit() {
        return IN_LIMIT;
    }

    @Override
    public UniqueDelegate getUniqueDelegate() {
        return UNIQUE_DELEGATE;
    }

    @Override
    public CallableStatementSupport getCallableStatementSupport() {
        return CALLABLE_STATEMENT_SUPPORT;
    }

    @Override
    public boolean supportsPartitionBy() {
        return true;
    }

    @Override
    public boolean supportsValuesList() {
        return true;
    }

    @Override
    public String inlineLiteral(String literal) {
        return "'" + escapeLiteral(literal) + "'";
    }

    @Override
    public boolean supportsSelectAliasInGroupByClause() {
        return true;
    }

    @Override
    protected SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
        switch (sqlCode) {
            case Types.TINYINT:
            case Types.SMALLINT:
                return IntegerTypeDescriptor.INSTANCE;
            default:
                return super.getSqlTypeDescriptorOverride(sqlCode);
        }
    }
}
