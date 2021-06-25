package com.yandex.ydb.spring.data;

import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.yandex.ydb.jdbc.YdbConst;
import com.yandex.ydb.jdbc.YdbTypes;
import com.yandex.ydb.jdbc.impl.YdbTypesImpl;
import com.yandex.ydb.table.values.PrimitiveType;
import com.yandex.ydb.table.values.Type;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.jdbc.core.convert.BasicJdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.JdbcValue;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.lang.Nullable;

/**
 * YDB-specific JDBC converter, provides precise integration with YDB types
 */
public class YdbBasicJdbcConverter extends BasicJdbcConverter {
    private static final YdbTypes TYPES = YdbTypesImpl.getInstance();

    public YdbBasicJdbcConverter(
            MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> context,
            RelationResolver relationResolver,
            CustomConversions conversions,
            JdbcTypeFactory typeFactory,
            IdentifierProcessing identifierProcessing) {
        super(context, relationResolver, conversions, typeFactory, identifierProcessing);
    }

    @Override
    public JdbcValue writeJdbcValue(@Nullable Object value, Class<?> columnType, int sqlType) {
        /*
         * YDB Supports both Date and DateTime in addition to Timestamp.
         * No need for standard Temporal -> Timestamp transformation inside Basic JDBC Converter.
         */
        if (value instanceof LocalDate) {
            return JdbcValue.of(value, JDBCType.DATE);
        } else if (value instanceof LocalDateTime) {
            return JdbcValue.of(value, JDBCType.TIME);
        }

        int unwrappedSqlType = TYPES.unwrapYdbJdbcType(sqlType);
        JdbcValue jdbcValue = super.writeJdbcValue(value, columnType, unwrappedSqlType);

        if (unwrappedSqlType != sqlType) { // Custom SQL type, wrap value
            Type ydbType = TYPES.toYdbType(sqlType);
            if (ydbType != null) {
                return JdbcValue.of(new YdbWrappedValue(jdbcValue.getValue(), ydbType), jdbcValue.getJdbcType());
            }
        }
        return jdbcValue;
    }

    @Override
    public int getSqlType(RelationalPersistentProperty property) {
        /*
         * Well, the current implementation of Spring ORM given not much room for type specification.
         * Nevertheless we should support all database type with custom precision
         */
        YdbPrimitive primitiveType = property.findAnnotation(YdbPrimitive.class);
        if (primitiveType != null) {
            return TYPES.wrapYdbJdbcType(PrimitiveType.of(primitiveType.type()));
        }
        int sqlType = TYPES.toWrappedSqlType(property.getType());
        if (sqlType != YdbConst.UNKNOWN_SQL_TYPE) {
            return sqlType;
        }
        return super.getSqlType(property);
    }
}
