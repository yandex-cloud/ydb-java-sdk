package ru.yandex.ydb.examples.indexes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ru.yandex.ydb.core.grpc.GrpcTransportBuilder;
import ru.yandex.ydb.core.rpc.RpcTransport;
import ru.yandex.ydb.examples.indexes.configuration.IndexesConfigurationProperties;
import ru.yandex.ydb.examples.indexes.repositories.SeriesRepository;
import ru.yandex.ydb.table.TableClient;
import ru.yandex.ydb.table.TableService;
import ru.yandex.ydb.table.TableServiceBuilder;

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

    @Bean
    RpcTransport rpcTransport(IndexesConfigurationProperties properties, ExecutorService grpcExecutor) {
        String endpoint = properties.getEndpoint();
        String database = properties.getDatabase();
        String token = properties.getToken();
        if (token == null || token.isEmpty()) {
            token = System.getenv("YDB_TOKEN");
        }
        logger.info("Creating rpc transport for endpoint={} database={}", endpoint, database);
        GrpcTransportBuilder builder = GrpcTransportBuilder.forEndpoint(endpoint, database)
                .withExecutorService(grpcExecutor);
        if (token != null && !token.isEmpty()) {
            builder.withAuthToken(token);
        }
        return builder.build();
    }

    @Bean
    TableService tableService(RpcTransport rpcTransport) {
        return TableServiceBuilder.useTransport(rpcTransport).build();
    }

    @Bean
    TableClient tableClient(TableService tableService) {
        return tableService.newTableClient();
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
