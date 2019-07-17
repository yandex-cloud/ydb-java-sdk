package com.yandex.ydb.examples.indexes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.yandex.ydb.core.auth.TokenAuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.core.rpc.RpcTransport;
import com.yandex.ydb.examples.indexes.configuration.IndexesConfigurationProperties;
import com.yandex.ydb.examples.indexes.repositories.SeriesRepository;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Bean
    ExecutorService grpcExecutor() {
        return Executors.newFixedThreadPool(3);
    }

    @Bean
    ScheduledExecutorService timerScheduler() {
        return Executors.newScheduledThreadPool(1);
    }

    @Bean(destroyMethod = "close")
    RpcTransport rpcTransport(IndexesConfigurationProperties properties, ExecutorService grpcExecutor) {
        String endpoint = properties.getEndpoint();
        String database = properties.getDatabase();
        String token = properties.getToken();
        if (token == null || token.isEmpty()) {
            token = System.getenv("YDB_TOKEN");
        }
        logger.info("Creating rpc transport for endpoint={} database={}", endpoint, database);
        GrpcTransport.Builder builder = GrpcTransport.forEndpoint(endpoint, database)
                .withCallExecutor(grpcExecutor);
        if (token != null && !token.isEmpty()) {
            builder.withAuthProvider(new TokenAuthProvider(token));
        }
        return builder.build();
    }

    @Bean
    TableClient tableClient(RpcTransport transport) {
        return TableClient.newClient(GrpcTableRpc.useTransport(transport))
            .build();
    }

    @Bean
    SessionCache sessionCache(TableClient tableClient, ScheduledExecutorService timerScheduler) {
        return new SessionCache(tableClient, timerScheduler);
    }

    @Bean
    SeriesRepository seriesRepository(SessionCache sessionCache, IndexesConfigurationProperties properties) {
        return new SeriesRepository(sessionCache, properties.getPrefix());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
