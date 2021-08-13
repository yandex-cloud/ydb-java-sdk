package com.yandex.ydb.jdbc;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.values.PrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import static org.rnorth.ducttape.unreliables.Unreliables.retryUntilSuccess;

public class YdbDockerHelper {
    private static final String SECURE_CONNECTION = "secured";
    private static final String LOCAL_DATABASE = "local";

    private static final int WEB_UI_PORT = 8765;
    private static final boolean SECURED;
    private static final int GRPC_PORT;
    static {
        SECURED = Boolean.getBoolean(SECURE_CONNECTION);
        GRPC_PORT = SECURED ? 2135 : 2136;
    }


    private static String DOCKER_URL;

    private static GenericContainer<?> ydbContainer() {
        String customImage = System.getProperty("YDB_IMAGE", "cr.yandex/yc/yandex-docker-local-ydb:latest");
        return new GenericContainer<>(
                customImage)
                .withExposedPorts(GRPC_PORT, WEB_UI_PORT)
                .withCreateContainerCmdModifier(modifier -> modifier.withName("ydb-" + UUID.randomUUID()))
                .waitingFor(new YdbCanCreateTableWaitStrategy());
    }

    static synchronized String getContainerUrl() {
        if (DOCKER_URL == null) {
            GenericContainer<?> container = ydbContainer();
            container.start();

            String suffix = SECURED ? "?secureConnection=true" : "";

            DOCKER_URL = String.format("jdbc:ydb:%s:%s/%s%s",
                    container.getContainerIpAddress(),
                    container.getMappedPort(GRPC_PORT),
                    LOCAL_DATABASE,
                    suffix);
        }

        return DOCKER_URL;
    }


    private static class YdbCanCreateTableWaitStrategy extends AbstractWaitStrategy {
        private static final Logger log = LoggerFactory.getLogger(YdbCanCreateTableWaitStrategy.class);

        private static final String DOCKER_INIT_TABLE = "/" + LOCAL_DATABASE + "/docker_init_table";

        @Override
        protected void waitUntilReady() {
            String host = waitStrategyTarget.getContainerIpAddress();
            int mappedPort = waitStrategyTarget.getMappedPort(GRPC_PORT);

            GrpcTransport.Builder transport = GrpcTransport.forHost(host, mappedPort);
            if (SECURED) {
                transport.withSecureConnection();
            }

            TableClient tableClient = TableClient.newClient(GrpcTableRpc.useTransport(transport.build())).build();
            retryUntilSuccess(3600, TimeUnit.SECONDS, () -> {
                getRateLimiter().doWhenReady(() -> {
                    try {
                        log.info("Getting session");
                        Result<Session> sessionResult = tableClient.createSession().get();
                        if (!sessionResult.isSuccess()) {
                            throw new RuntimeException("Session not ready: " + sessionResult);
                        }

                        Session session = sessionResult.ok()
                                .orElseThrow(() -> new RuntimeException("Internal error when checking session"));

                        log.info("Creating test table");
                        session.createTable(
                                DOCKER_INIT_TABLE,
                                TableDescription
                                        .newBuilder()
                                        .addNullableColumn("id", PrimitiveType.utf8())
                                        .setPrimaryKey("id")
                                        .build()
                        )
                                .get()
                                .expect("Table creation error");
                    } catch (Exception e) {
                        log.debug("Checking container state failed: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
                log.info("Done");
                return true;
            });
        }
    }
}
