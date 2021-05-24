package com.yandex.ydb.spring.data;

import com.yandex.ydb.jdbc.YdbDatabaseMetaData;
import org.springframework.data.relational.core.dialect.ArrayColumns;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.Escaper;
import org.springframework.data.relational.core.dialect.IdGeneration;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.IdentifierProcessing.LetterCasing;
import org.springframework.data.relational.core.sql.IdentifierProcessing.Quoting;
import org.springframework.data.relational.core.sql.LockOptions;
import org.springframework.data.relational.core.sql.render.SelectRenderContext;

public class YdbDialect implements Dialect {
    private static final IdentifierProcessing IDENTIFIERS =
            IdentifierProcessing.create(new Quoting(YdbDatabaseMetaData.QUOTE_IDENTIFIER), LetterCasing.AS_IS);
    private static final SelectRenderContext SELECT_RENDER_CONTEXT = new SelectRenderContext() {
    };
    public static final YdbDialect INSTANCE = new YdbDialect();

    @Override
    public LimitClause limit() {
        return YdbLimitClause.INSTANCE;
    }

    @Override
    public LockClause lock() {
        return YdbLockClause.INSTANCE;
    }

    @Override
    public ArrayColumns getArraySupport() {
        return ArrayColumns.Unsupported.INSTANCE;
    }

    @Override
    public SelectRenderContext getSelectContext() {
        return SELECT_RENDER_CONTEXT;
    }

    @Override
    public IdentifierProcessing getIdentifierProcessing() {
        return IDENTIFIERS;
    }

    @Override
    public Escaper getLikeEscaper() {
        return Escaper.DEFAULT;
    }

    @Override
    public IdGeneration getIdGeneration() {
        return IdGeneration.DEFAULT; // TODO: make unsupported?
    }

    static class YdbLockClause implements LockClause {
        private static final YdbLockClause INSTANCE = new YdbLockClause();

        @Override
        public String getLock(LockOptions lockOptions) {
            if (lockOptions.getFrom().getTables().isEmpty()) {
                return "";
            }
            throw new YdbDaoRuntimeException("Locks are not supported in YDB");
        }

        @Override
        public Position getClausePosition() {
            return Position.AFTER_ORDER_BY;
        }
    }

    static class YdbLimitClause implements LimitClause {
        private static final YdbLimitClause INSTANCE = new YdbLimitClause();

        @Override
        public String getLimit(long limit) {
            return "LIMIT " + limit;
        }

        @Override
        public String getOffset(long offset) {
            return "OFFSET " + offset;
        }

        @Override
        public String getLimitOffset(long limit, long offset) {
            return "LIMIT " + limit + "OFFSET " + offset;
        }

        @Override
        public Position getClausePosition() {
            return Position.AFTER_ORDER_BY;
        }
    }
}
