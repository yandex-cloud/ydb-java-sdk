package com.yandex.ydb.jdbc;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import com.yandex.ydb.jdbc.settings.YdbProperties;
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

    private static ContainerDescription ydbContainer() {
        String customImage = System.getProperty("YDB_IMAGE", "cr.yandex/yc/yandex-docker-local-ydb:latest");

        GenericContainer<?> container = new GenericContainer<>(customImage);
        container.withExposedPorts(GRPC_PORT, WEB_UI_PORT);
        container.withCreateContainerCmdModifier(modifier -> modifier.withName("ydb-" + UUID.randomUUID()));

        String ydbCertFile;
        if (SECURED) {
            String ydbCerts;
            try {
                ydbCerts = Files.createTempDirectory("ydb-jdbc").toAbsolutePath().toString();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create temporary directory for YDB certificates", e);
            }
            container.withFileSystemBind(ydbCerts, "/ydb_certs");
            ydbCertFile = ydbCerts + "/ca.pem";
        } else {
            ydbCertFile = null;
        }
        container.waitingFor(new YdbCanCreateTableWaitStrategy(ydbCertFile));
        return new ContainerDescription(container, ydbCertFile);
    }

    static synchronized String getContainerUrl() {
        if (DOCKER_URL == null) {
            ContainerDescription description = ydbContainer();
            GenericContainer<?> container = description.ydbContainer;
            container.start();

            String suffix;
            if (SECURED) {
                String ydbCertFile = description.ydbCertFile;
                if (ydbCertFile != null) {
                    suffix = "?secureConnectionCertificate=file:" + ydbCertFile;
                } else {
                    suffix = "?secureConnection=true";
                }
            } else {
                suffix = "";
            }

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

        @Nullable
        private final String ydbCertFile;

        private YdbCanCreateTableWaitStrategy(@Nullable String ydbCertFile) {
            this.ydbCertFile = ydbCertFile;
        }

        @Override
        protected void waitUntilReady() {
            String host = waitStrategyTarget.getContainerIpAddress();
            int mappedPort = waitStrategyTarget.getMappedPort(GRPC_PORT);

            retryUntilSuccess(3600, TimeUnit.SECONDS, () -> {
                getRateLimiter().doWhenReady(() -> {
                    try {
                        GrpcTransport.Builder transportBuilder = GrpcTransport.forHost(host, mappedPort);
                        if (SECURED) {
                            transportBuilder.withSecureConnection();
                            if (ydbCertFile != null) {
                                try {
                                    // Docker machine? No viable solution yet
                                    transportBuilder.withSecureConnection(
                                            YdbProperties.byteFileReference("file:" + ydbCertFile));
                                } catch (YdbConfigurationException e) {
                                    log.error("YDB Certificate file is not available", e);
                                }
                            }
                        }
                        try (GrpcTransport transport = transportBuilder.build()) {
                            GrpcTableRpc grpcTableRpc = GrpcTableRpc.useTransport(transport);
                            try (TableClient tableClient = TableClient.newClient(grpcTableRpc).build()) {

                                log.info("Getting session");
                                Result<Session> sessionResult = tableClient.createSession().get();
                                if (!sessionResult.isSuccess()) {
                                    throw new RuntimeException("Session not ready: " + sessionResult);
                                }

                                Session session = sessionResult.ok()
                                        .orElseThrow(() ->
                                                new RuntimeException("Internal error when checking session"));

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
                            }
                        }
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

    private static class ContainerDescription {
        private final GenericContainer<?> ydbContainer;
        @Nullable
        private final String ydbCertFile;

        private ContainerDescription(GenericContainer<?> ydbContainer, @Nullable String ydbCertFile) {
            this.ydbContainer = ydbContainer;
            this.ydbCertFile = ydbCertFile;
        }
    }
}
