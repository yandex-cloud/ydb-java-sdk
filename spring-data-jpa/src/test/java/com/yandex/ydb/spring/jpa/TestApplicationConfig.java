package com.yandex.ydb.spring.jpa;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.yandex.ydb.jdbc.TestHelper;
import com.yandex.ydb.jdbc.YdbDriver;
import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import com.yandex.ydb.jdbc.settings.YdbOperationProperty;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(repositoryBaseClass = YdbJpaRepositoryImpl.class)
@EnableTransactionManagement
public class TestApplicationConfig {

    @Bean
    public DataSource dataSource() throws YdbConfigurationException {
        Properties properties = new Properties();
        properties.setProperty(YdbOperationProperty.ALWAYS_PREPARE_DATAQUERY.getName(), "false");
        properties.setProperty(YdbOperationProperty.TRANSFORM_STANDARD_JDBC_QUERIES.getName(), "true");
        properties.setProperty(YdbOperationProperty.TRANSFORMED_JDBC_QUERIES_CACHE.getName(), "10000");
        properties.setProperty(YdbOperationProperty.ENFORCE_VARIABLE_PREFIX.getName(), "true");
        return new SimpleDriverDataSource(new YdbDriver(), TestHelper.getTestUrl(), properties);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setDatabasePlatform(com.yandex.ydb.spring.jpa.YdbDialect.class.getName());
        vendorAdapter.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.yandex.ydb.spring.jpa.data");
        factory.setDataSource(dataSource);

        Properties properties = new Properties();
//        properties.setProperty(AvailableSettings.STATEMENT_BATCH_SIZE, YdbDialect.MAX_BATCH_SIZE);
        properties.setProperty(AvailableSettings.ORDER_INSERTS, "true");
        properties.setProperty(AvailableSettings.ORDER_UPDATES, "true");
        properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "none");
        properties.setProperty("org.hibernate.flushMode", "COMMIT");
        factory.setJpaProperties(properties);
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new YdbJpaTransactionManager(entityManagerFactory);
    }
}
