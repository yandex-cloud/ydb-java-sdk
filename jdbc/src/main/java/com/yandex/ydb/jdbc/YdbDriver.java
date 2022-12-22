package com.yandex.ydb.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.jdbc.impl.Validator;
import com.yandex.ydb.jdbc.impl.YdbConnectionImpl;
import com.yandex.ydb.jdbc.settings.ParsedProperty;
import com.yandex.ydb.jdbc.settings.YdbConnectionProperties;
import com.yandex.ydb.jdbc.settings.YdbConnectionProperty;
import com.yandex.ydb.jdbc.settings.YdbOperationProperties;
import com.yandex.ydb.jdbc.settings.YdbProperties;
import com.yandex.ydb.table.SchemeClient;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.rpc.SchemeRpc;
import com.yandex.ydb.table.rpc.TableRpc;
import com.yandex.ydb.table.rpc.grpc.GrpcSchemeRpc;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.YdbConst.JDBC_YDB_PREFIX;
import static com.yandex.ydb.jdbc.YdbConst.YDB_DRIVER_USES_SL4J;

/**
 * YDB JDBC driver, basic implementation supporting {@link TableClient} and {@link SchemeClient}
 */
public class YdbDriver implements Driver {
    private static final Logger LOGGER = LoggerFactory.getLogger(YdbDriver.class);

    private static final YdbConnectionsCache CONNECTIONS = new YdbConnectionsCache();

    static {
        YdbDriver driver = new YdbDriver();
        try {
            DriverManager.registerDriver(driver);
        } catch (SQLException sql) {
            throw new RuntimeException(sql);
        }
        LOGGER.info("YDB JDBC Driver registered: {}", driver);
    }

    @Override
    public YdbConnection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        // logging should be after acceptsURL, otherwise we can log properties with secrets of another database
        LOGGER.info("About to connect to [{}] using properties {}",
                YdbProperties.hideSecrets(url), YdbProperties.hideSecrets(info));

        YdbProperties properties = YdbProperties.from(url, info);
        Clients clients = CONNECTIONS.getClients(new ConnectionConfig(url, info), properties);

        YdbOperationProperties operationProperties = properties.getOperationProperties();
        Duration sessionTimeout = operationProperties.getSessionTimeout();

        Validator validator = new Validator(operationProperties);
        Result<Session> session = validator.joinResult(
                LOGGER,
                () -> "Get or create session",
                () -> clients.tableClient.getOrCreateSession(sessionTimeout));

        // TODO: support scheme client eager initialization
        return new YdbConnectionImpl(
                clients.schemeClient,
                session.expect("New Session Created"),
                operationProperties,
                validator,
                url, // raw URL
                properties.getConnectionProperties().getDatabase());
    }

    @Override
    public boolean acceptsURL(String url) {
        return YdbProperties.isYdb(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        String targetUrl = acceptsURL(url) ? url : JDBC_YDB_PREFIX;
        return YdbProperties.from(targetUrl, info).toDriverProperties();
    }

    @Override
    public int getMajorVersion() {
        return YdbDriverInfo.DRIVER_MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return YdbDriverInfo.DRIVER_MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant() {
        return false; // YDB is non-compliant
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException(YDB_DRIVER_USES_SL4J);
    }

    public static YdbConnectionsCache getConnectionsCache() {
        return CONNECTIONS;
    }

    public static class ConnectionConfig {
        private final String url;
        private final Properties properties;

        public ConnectionConfig(String url, Properties properties) {
            this.url = Objects.requireNonNull(url);
            this.properties = new Properties();
            this.properties.putAll(Objects.requireNonNull(properties));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ConnectionConfig)) {
                return false;
            }
            ConnectionConfig that = (ConnectionConfig) o;
            return Objects.equals(url, that.url) && Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, properties);
        }
    }

    private static class Clients {
        private final TableClient tableClient;
        private final Supplier<SchemeClient> schemeClient;

        private Clients(TableClient tableClient, Supplier<SchemeClient> schemeClient) {
            this.tableClient = Objects.requireNonNull(tableClient);
            this.schemeClient = Objects.requireNonNull(schemeClient);
        }
    }

    public static class YdbConnectionsCache {
        private final Map<ConnectionConfig, Clients> cache = new HashMap<>();

        public synchronized Clients getClients(ConnectionConfig config, YdbProperties properties) {
            // TODO: implement cache based on connection and client properties only (excluding operation properties)
            YdbConnectionProperties connProperties = properties.getConnectionProperties();

            Clients clients = properties.getOperationProperties().isCacheConnectionsInDriver() ?
                    cache.get(config) :
                    null; // not cached
            if (clients != null) {
                LOGGER.debug("Reusing YDB connection to {}{}",
                        connProperties.getAddresses(),
                        Strings.nullToEmpty(connProperties.getDatabase()));
                return clients;
            }

            ParsedProperty tokenProperty = connProperties.getProperty(YdbConnectionProperty.TOKEN);
            boolean hasAuth = tokenProperty != null && tokenProperty.getParsedValue() != null;
            LOGGER.info("Creating new YDB connection to {}{}{}",
                    connProperties.getAddresses(),
                    Strings.nullToEmpty(connProperties.getDatabase()),
                    hasAuth ? " with auth" : " without auth");

            GrpcTransport grpcTransport = connProperties.toGrpcTransport();

            TableRpc tableRpc = GrpcTableRpc.ownTransport(grpcTransport); // OWN
            Preconditions.checkState(tableRpc != null, "TableRpc must be initialized");

            TableClient tableClient = properties.getClientProperties().toTableClient(tableRpc);

            Supplier<SchemeClient> schemeClient = Suppliers.memoize(() -> {
                SchemeRpc schemeRpc = GrpcSchemeRpc.useTransport(grpcTransport); // USE
                Preconditions.checkState(schemeRpc != null, "SchemeRpc must be initialized");
                return SchemeClient.newClient(schemeRpc).build();
            })::get;

            clients = new Clients(tableClient, schemeClient);
            cache.put(config, clients);
            return clients;
        }

        public synchronized int getConnectionCount() {
            return cache.size();
        }

        public synchronized void close() {
            LOGGER.info("Closing {} cached connection(s)...", cache.size());
            for (Clients clients : cache.values()) {
                try {
                    clients.tableClient.close();
                } catch (Exception e) {
                    LOGGER.error("Unable to close client: " + e.getMessage(), e);
                }
            }
            cache.clear();
        }
    }
}
