package com.yandex.ydb.spring.data;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.JdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionManager;

@Configuration
@EnableJdbcRepositories
public class YdbJdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) {
        return new YdbNamedParametersJdbcTemplate(dataSource);
    }

    @Bean
    TransactionManager transactionManager(DataSource dataSource) {
        return new YdbDataSourceTransactionManager(dataSource);
    }

    @Bean
    NamingStrategy namingStrategy() {
        return YdbNamingStrategy.INSTANCE;
    }

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(YdbConverters.StringToBytesConverter.INSTANCE));
    }

    @Bean
    public JdbcConverter jdbcConverter(JdbcMappingContext mappingContext,
                                       NamedParameterJdbcOperations operations,
                                       @Lazy RelationResolver relationResolver,
                                       JdbcCustomConversions conversions,
                                       Dialect dialect) {
        JdbcTypeFactory jdbcTypeFactory = JdbcTypeFactory.unsupported();
        return new YdbBasicJdbcConverter(mappingContext, relationResolver, conversions, jdbcTypeFactory,
                dialect.getIdentifierProcessing());
    }

    @Bean
    @Override
    public Dialect jdbcDialect(NamedParameterJdbcOperations operations) {
        if (operations instanceof YdbNamedParametersJdbcTemplate) {
            return YdbDialect.INSTANCE;
        } else {
            return super.jdbcDialect(operations);
        }
    }
}
