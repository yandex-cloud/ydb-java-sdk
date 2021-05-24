package com.yandex.ydb.spring;

import javax.sql.DataSource;

import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.jdbc.YdbDriver;
import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import com.yandex.ydb.spring.data.YdbJdbcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// Must be enabled in order to work under transaction management during tests
@EnableTransactionManagement
@Configuration
public class TestApplicationConfig extends YdbJdbcConfiguration {

    @Bean
    public DataSource dataSource() throws YdbConfigurationException {
        return new SimpleDriverDataSource(new YdbDriver(), TestHelper.getTestUrl());
    }

}
