package com.yandex.ydb.examples;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.values.PrimitiveType;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

/**
 *
 * @author Alexandr Gorshenin
 */
public class YdbDockerContainer extends GenericContainer<YdbDockerContainer> {
    private static final Logger log = LoggerFactory.getLogger(YdbDockerContainer.class);

    private static final String DEFAULT_YDB_IMAGE = "cr.yandex/yc/yandex-docker-local-ydb:latest";
    private static final String DOCKER_DATABASE = "/local";

    private final int grpcsPort; // Secure connection
    private final int grpcPort;  // Non secure connection

    YdbDockerContainer(String image) {
        super(image);

        PortsGenerator gen = new PortsGenerator();
        grpcsPort = gen.findAvailablePort();
        grpcPort = gen.findAvailablePort();

        addExposedPort(grpcPort); // don't expose by default

        // Host ports and container ports MUST BE equal - ydb implementation limitation
        addFixedExposedPort(grpcsPort, grpcsPort);
        addFixedExposedPort(grpcPort, grpcPort);

        withEnv("GRPC_PORT", String.valueOf(grpcPort));
        withEnv("GRPC_TLS_PORT", String.valueOf(grpcsPort));

        withCreateContainerCmdModifier(modifier -> modifier
                .withName("ydb-" + UUID.randomUUID())
                .withHostName("localhost"));
        waitingFor(new YdbCanCreateTableWaitStrategy());
    }

    public String nonSecureEndpoint() {
        return String.format("%s:%s", getContainerIpAddress(), grpcPort);
    }

    public String secureEndpoint() {
        return String.format("%s:%s", getContainerIpAddress(), grpcsPort);
    }

    public String database() {
        return DOCKER_DATABASE;
    }

    public static YdbDockerContainer createAndStart() {
        String customImage = System.getProperty("YDB_IMAGE", DEFAULT_YDB_IMAGE);
        YdbDockerContainer container = new YdbDockerContainer(customImage);
        container.start();

        return container;
    }


    private class YdbCanCreateTableWaitStrategy extends AbstractWaitStrategy {
        private static final String DOCKER_INIT_TABLE = DOCKER_DATABASE + "/docker_init_table";

        @Override
        protected void waitUntilReady() {
            Unreliables.retryUntilSuccess(3600, TimeUnit.SECONDS, () -> {
                getRateLimiter().doWhenReady(() -> {
                    GrpcTransport.Builder transportBuilder = GrpcTransport.forEndpoint(nonSecureEndpoint(), database());
                    try (GrpcTransport transport = transportBuilder.build()) {
                        GrpcTableRpc grpcTableRpc = GrpcTableRpc.useTransport(transport);
                        try (TableClient tableClient = TableClient.newClient(grpcTableRpc).build()) {

                            log.info("Getting session");
                            Session session = tableClient.createSession()
                                    .get().expect("session not ready");

                            log.info("Creating test table");
                            session.createTable(
                                    DOCKER_INIT_TABLE,
                                    TableDescription
                                            .newBuilder()
                                            .addNullableColumn("id", PrimitiveType.utf8())
                                            .setPrimaryKey("id")
                                            .build()
                            ).get().expect("Table creation error");
                        }
                    } catch (ExecutionException e) {
                        log.info("execution problem {}", e.getMessage());
                        throw new RuntimeException("don't ready", e);
                    } catch (InterruptedException e) {
                        log.warn("execution interrupted", e);
                        throw new RuntimeException("interrupted", e);
                    }
                });
                log.info("Done");
                return true;
            });
        }
    }
}
