package ru.yandex.ydb.examples.indexes.configuration;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("indexes")
public class IndexesConfigurationProperties {
    @NotBlank
    private String endpoint;
    @NotBlank
    private String database;
    private String token;
    @NotBlank
    private String prefix;

    public String getEndpoint() {
        return endpoint;
    }

    public String getDatabase() {
        return database;
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
