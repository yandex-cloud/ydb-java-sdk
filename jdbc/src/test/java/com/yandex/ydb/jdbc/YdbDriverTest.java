package com.yandex.ydb.jdbc;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.io.Files;
import com.google.common.net.HostAndPort;
import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.auth.TokenAuthProvider;
import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import com.yandex.ydb.jdbc.impl.YdbConnectionImpl;
import com.yandex.ydb.jdbc.settings.ParsedProperty;
import com.yandex.ydb.jdbc.settings.YdbClientProperty;
import com.yandex.ydb.jdbc.settings.YdbConnectionProperties;
import com.yandex.ydb.jdbc.settings.YdbConnectionProperty;
import com.yandex.ydb.jdbc.settings.YdbOperationProperties;
import com.yandex.ydb.jdbc.settings.YdbOperationProperty;
import com.yandex.ydb.jdbc.settings.YdbProperties;
import com.yandex.ydb.table.SchemeClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsg;
import static com.yandex.ydb.jdbc.TestHelper.assertThrowsMsgLike;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YdbDriverTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(YdbDriverTest.class);
    public static final String TOKEN_FROM_FILE = "token-from-file";
    public static final String CERTIFICATE_FROM_FILE = "certificate-from-file";

    private static String TEST_URL;
    private static File TOKEN_FILE;
    private static File CERTIFICATE_FILE;

    private YdbDriver driver;

    @BeforeAll
    static void beforeAll() throws YdbConfigurationException, IOException {
        TEST_URL = TestHelper.getTestUrl();
        TOKEN_FILE = safeCreateFile(TOKEN_FROM_FILE);
        CERTIFICATE_FILE = safeCreateFile(CERTIFICATE_FROM_FILE);
    }

    @BeforeEach
    void beforeEach() {
        driver = new YdbDriver();
    }

    @AfterAll
    static void afterAll() {
        safeDeleteFile(TOKEN_FILE);
        safeDeleteFile(CERTIFICATE_FILE);
        YdbDriver.getConnectionsCache().close();
    }

    @Test
    void connect() throws SQLException {
        try (Connection connection = driver.connect(TEST_URL, new Properties())) {
            assertTrue(connection instanceof YdbConnection);
            assertTrue(connection instanceof YdbConnectionImpl);

            YdbConnection ydbConnection = (YdbConnection) connection;
            LOGGER.info("Session opened: {}", ydbConnection.getYdbSession());

            SchemeClient schemeClient = ydbConnection.getYdbScheme().get();
            LOGGER.info("Scheme client: {}", schemeClient);

            assertSame(schemeClient, ydbConnection.getYdbScheme().get());
        }
    }

    @Test
    void connectMultipleTimes() throws SQLException {
        try (YdbConnection connection1 = (YdbConnection) driver.connect(TEST_URL, new Properties())) {
            LOGGER.info("Session 1 opened: {}", connection1.getYdbSession());
            try (YdbConnection connection2 = (YdbConnection) driver.connect(TEST_URL, new Properties())) {
                LOGGER.info("Session 2 opened: {}", connection2.getYdbSession());

                // Expect only single connection
                assertEquals(1, YdbDriver.getConnectionsCache().getConnectionCount());
            }
        }
    }

    @Test
    void connectToUnsupportedUrl() throws SQLException {
        assertNull(driver.connect("jdbc:clickhouse:localhost:123", new Properties()));
    }

    @SuppressWarnings("UnstableApiUsage")
    @ParameterizedTest
    @MethodSource("urlsToParse")
    void parseURL(String url, HostAndPort hostAndPort,
                  @Nullable String database,
                  @Nullable String localDatacenter) throws SQLException {
        YdbProperties props = YdbProperties.from(url, new Properties());
        YdbConnectionProperties connectionProperties = props.getConnectionProperties();
        assertEquals(Collections.singletonList(hostAndPort), connectionProperties.getAddresses());
        assertEquals(database, connectionProperties.getDatabase());

        ParsedProperty dcProperty = connectionProperties.getParams().get(YdbConnectionProperty.LOCAL_DATACENTER);
        assertEquals(localDatacenter, Optional.ofNullable(dcProperty)
                .map(ParsedProperty::getParsedValue)
                .orElse(null));

    }

    @ParameterizedTest
    @MethodSource("urlsToCheck")
    void acceptsURL(String url, boolean accept) {
        assertEquals(accept, driver.acceptsURL(url));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void getPropertyInfoDefault() throws SQLException {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci";

        Properties properties = new Properties();
        DriverPropertyInfo[] propertyInfo = driver.getPropertyInfo(url, properties);
        assertEquals(new Properties(), properties);

        List<String> actual = convertPropertyInfo(propertyInfo);
        actual.forEach(LOGGER::info);

        List<String> expect = convertPropertyInfo(defaultPropertyInfo());
        assertEquals(expect, actual);

        YdbProperties ydbProperties = YdbProperties.from(url, properties);
        assertEquals(Collections.singletonList(HostAndPort.fromParts("ydb-ru-prestable.yandex.net", 2135)),
                ydbProperties.getConnectionProperties().getAddresses());
        assertEquals("/ru-prestable/ci/testing/ci",
                ydbProperties.getConnectionProperties().getDatabase());
    }

    @Test
    void getPropertyInfoAllFromUrl() throws SQLException {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?" +
                customizedProperties().entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("&"));

        Properties properties = new Properties();
        DriverPropertyInfo[] propertyInfo = driver.getPropertyInfo(url, properties);
        assertEquals(new Properties(), properties);

        List<String> actual = convertPropertyInfo(propertyInfo);
        actual.forEach(LOGGER::info);

        List<String> expect = convertPropertyInfo(customizedPropertyInfo());
        assertEquals(expect, actual);

        YdbProperties ydbProperties = YdbProperties.from(url, properties);
        checkCustomizedProperties(ydbProperties);
    }

    @Test
    void getPropertyInfoFromProperties() throws SQLException {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci";

        Properties properties = customizedProperties();
        Properties copy = new Properties();
        copy.putAll(properties);

        DriverPropertyInfo[] propertyInfo = driver.getPropertyInfo(url, properties);
        assertEquals(copy, properties);

        List<String> actual = convertPropertyInfo(propertyInfo);
        actual.forEach(LOGGER::info);

        List<String> expect = convertPropertyInfo(customizedPropertyInfo());
        assertEquals(expect, actual);

        YdbProperties ydbProperties = YdbProperties.from(url, properties);
        checkCustomizedProperties(ydbProperties);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void getPropertyInfoOverwrite() throws SQLException {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?localDatacenter=sas";
        Properties properties = new Properties();
        properties.put("localDatacenter", "vla");

        Properties copy = new Properties();
        copy.putAll(properties);

        DriverPropertyInfo[] propertyInfo = driver.getPropertyInfo(url, properties);
        assertEquals(copy, properties);

        List<String> actual = convertPropertyInfo(propertyInfo);
        actual.forEach(LOGGER::info);

        // URL will always overwrite properties
        List<String> expect = convertPropertyInfo(defaultPropertyInfo("sas"));
        assertEquals(expect, actual);

        YdbProperties ydbProperties = YdbProperties.from(url, properties);
        assertEquals(Collections.singletonList(HostAndPort.fromParts("ydb-ru-prestable.yandex.net", 2135)),
                ydbProperties.getConnectionProperties().getAddresses());
        assertEquals("/ru-prestable/ci/testing/ci",
                ydbProperties.getConnectionProperties().getDatabase());
    }

    @Test
    void getPropertyInfoAuthProvider() throws SQLException {
        AuthProvider customAuthProvider = () -> "any";

        Properties properties = new Properties();
        properties.put(YdbConnectionProperty.AUTH_PROVIDER.getTitle(), customAuthProvider);

        Properties copy = new Properties();
        copy.putAll(properties);

        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci";
        DriverPropertyInfo[] propertyInfo = driver.getPropertyInfo(url, properties);

        assertEquals(copy, properties); // Provided properties were not changed

        List<String> actual = convertPropertyInfo(propertyInfo);
        actual.forEach(LOGGER::info);

        List<String> expect = convertPropertyInfo(defaultPropertyInfo());
        assertEquals(expect, actual);


        YdbProperties ydbProperties = YdbProperties.from(url, properties);
        ParsedProperty auth = ydbProperties.getConnectionProperties().getProperty(YdbConnectionProperty.AUTH_PROVIDER);
        assertNotNull(auth);
        assertEquals(customAuthProvider, auth.getParsedValue());
    }


    @ParameterizedTest
    @MethodSource("tokensToCheck")
    void getTokenAs(String token, String expectValue) throws SQLException {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?token=" + token;
        Properties properties = new Properties();
        YdbProperties ydbProperties = YdbProperties.from(url, properties);

        YdbConnectionProperties props = ydbProperties.getConnectionProperties();
        assertEquals(expectValue,
                ((TokenAuthProvider) props.getProperty(YdbConnectionProperty.TOKEN).getParsedValue()).getToken());
    }

    @ParameterizedTest
    @MethodSource("unknownFiles")
    void getTokenAsInvalid(String token, String expectException) {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?token=" + token;

        assertThrowsMsgLike(YdbConfigurationException.class,
                () -> YdbProperties.from(url, new Properties()),
                expectException);
    }

    @ParameterizedTest
    @MethodSource("certificatesToCheck")
    void getCaCertificateAs(String certificate, String expectValue) throws SQLException {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci" +
                "?secureConnectionCertificate=" + certificate;
        Properties properties = new Properties();
        YdbProperties ydbProperties = YdbProperties.from(url, properties);

        YdbConnectionProperties props = ydbProperties.getConnectionProperties();
        assertArrayEquals(expectValue.getBytes(),
                props.getProperty(YdbConnectionProperty.SECURE_CONNECTION_CERTIFICATE).getParsedValue());
    }

    @ParameterizedTest
    @MethodSource("unknownFiles")
    void getCaCertificateAsInvalid(String certificate, String expectException) {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci" +
                "?secureConnectionCertificate=" + certificate;
        assertThrowsMsgLike(YdbConfigurationException.class,
                () -> YdbProperties.from(url, new Properties()),
                expectException);
    }

    @ParameterizedTest
    @MethodSource("invalidDurationParams")
    void invalidDuration(String param) {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?" + param + "=1bc";
        assertThrowsMsg(YdbConfigurationException.class,
                () -> YdbProperties.from(url, new Properties()),
                "Unable to convert property " + param +
                        ": Unable to parse value [1bc] -> [PT1BC] as Duration: Text cannot be parsed to a Duration");
    }

    @ParameterizedTest
    @MethodSource("invalidIntegerParams")
    void invalidInteger(String param) {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?" + param + "=1bc";
        assertThrowsMsg(YdbConfigurationException.class,
                () -> YdbProperties.from(url, new Properties()),
                "Unable to convert property " + param +
                        ": Unable to parse value [1bc] as Integer: For input string: \"1bc\"");
    }

    @Test
    void invalidAuthProviderProperty() {
        String url = "jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?authProvider=test";
        assertThrowsMsg(YdbConfigurationException.class,
                () -> YdbProperties.from(url, new Properties()),
                "Unable to convert property authProvider: " +
                        "Property authProvider must be configured with object, not a string");
    }

    @Test
    void getMajorVersion() {
        assertEquals(1, driver.getMajorVersion());
    }

    @Test
    void getMinorVersion() {
        assertEquals(0, driver.getMinorVersion());
    }

    @Test
    void jdbcCompliant() {
        assertFalse(driver.jdbcCompliant());
    }

    @Test
    void getParentLogger() {
        assertThrowsMsg(SQLFeatureNotSupportedException.class,
                () -> driver.getParentLogger(),
                "YDB Driver uses SLF4j");
    }

    static List<String> convertPropertyInfo(DriverPropertyInfo[] propertyInfo) {
        return Stream.of(propertyInfo)
                .map(YdbDriverTest::asString)
                .collect(Collectors.toList());
    }

    static DriverPropertyInfo[] defaultPropertyInfo() {
        return defaultPropertyInfo(null);
    }

    static DriverPropertyInfo[] defaultPropertyInfo(@Nullable String localDatacenter) {
        return new DriverPropertyInfo[]{
                YdbConnectionProperty.ENDPOINT_DISCOVERY_PERIOD.toDriverPropertyInfo(null),
                YdbConnectionProperty.LOCAL_DATACENTER.toDriverPropertyInfo(localDatacenter),
                YdbConnectionProperty.SECURE_CONNECTION.toDriverPropertyInfo(null),
                YdbConnectionProperty.SECURE_CONNECTION_CERTIFICATE.toDriverPropertyInfo(null),
                YdbConnectionProperty.READ_TIMEOUT.toDriverPropertyInfo(null),
                YdbConnectionProperty.TOKEN.toDriverPropertyInfo(null),
                YdbConnectionProperty.AUTH_PROVIDER.toDriverPropertyInfo(null),

                YdbClientProperty.QUERY_CACHE_SIZE.toDriverPropertyInfo(null),
                YdbClientProperty.KEEP_QUERY_TEXT.toDriverPropertyInfo(null),
                YdbClientProperty.SESSION_KEEP_ALIVE_TIME.toDriverPropertyInfo(null),
                YdbClientProperty.SESSION_MAX_IDLE_TIME.toDriverPropertyInfo(null),
                YdbClientProperty.SESSION_CREATION_MAX_RETRIES.toDriverPropertyInfo(null),
                YdbClientProperty.SESSION_POOL_SIZE_MIN.toDriverPropertyInfo(null),
                YdbClientProperty.SESSION_POOL_SIZE_MAX.toDriverPropertyInfo(null),

                YdbOperationProperty.JOIN_DURATION.toDriverPropertyInfo("5m"),
                YdbOperationProperty.KEEP_IN_QUERY_CACHE.toDriverPropertyInfo("false"),
                YdbOperationProperty.QUERY_TIMEOUT.toDriverPropertyInfo("0s"),
                YdbOperationProperty.SCAN_QUERY_TIMEOUT.toDriverPropertyInfo("1m"),
                YdbOperationProperty.FAIL_ON_TRUNCATED_RESULT.toDriverPropertyInfo("true"),
                YdbOperationProperty.SESSION_TIMEOUT.toDriverPropertyInfo("5s"),
                YdbOperationProperty.DEADLINE_TIMEOUT.toDriverPropertyInfo("0s"),
                YdbOperationProperty.AUTOCOMMIT.toDriverPropertyInfo("false"),
                YdbOperationProperty.TRANSACTION_LEVEL.toDriverPropertyInfo("8"),

                YdbOperationProperty.AUTO_PREPARED_BATCHES.toDriverPropertyInfo("true"),
                YdbOperationProperty.ENFORCE_SQL_V1.toDriverPropertyInfo("true"),
                YdbOperationProperty.ENFORCE_VARIABLE_PREFIX.toDriverPropertyInfo("true"),
                YdbOperationProperty.CACHE_CONNECTIONS_IN_DRIVER.toDriverPropertyInfo("true"),
                YdbOperationProperty.DETECT_SQL_OPERATIONS.toDriverPropertyInfo("true")
        };
    }

    static Properties customizedProperties() {
        Properties properties = new Properties();
        properties.setProperty("endpointDiscoveryPeriod", "1m");
        properties.setProperty("localDatacenter", "sas");
        properties.setProperty("secureConnection", "true");
        properties.setProperty("readTimeout", "2m");
        properties.setProperty("token", "x-secured-token");

        properties.setProperty("queryCacheSize", "101");
        properties.setProperty("keepQueryText", "true");
        properties.setProperty("sessionKeepAliveTime", "15m");
        properties.setProperty("sessionMaxIdleTime", "5m");
        properties.setProperty("sessionCreationMaxRetries", "2");
        properties.setProperty("sessionPoolSizeMin", "3");
        properties.setProperty("sessionPoolSizeMax", "4");

        properties.setProperty("joinDuration", "6m");
        properties.setProperty("keepInQueryCache", "true");
        properties.setProperty("queryTimeout", "2m");
        properties.setProperty("scanQueryTimeout", "3m");
        properties.setProperty("failOnTruncatedResult", "false");
        properties.setProperty("sessionTimeout", "6s");
        properties.setProperty("deadlineTimeout", "1s");
        properties.setProperty("autoCommit", "true");
        properties.setProperty("transactionLevel", "4");

        properties.setProperty("autoPreparedBatches", "false");
        properties.setProperty("enforceSqlV1", "false");
        properties.setProperty("enforceVariablePrefix", "false");
        properties.setProperty("cacheConnectionsInDriver", "false");
        properties.setProperty("detectSqlOperations", "false");
        return properties;
    }

    static DriverPropertyInfo[] customizedPropertyInfo() {
        return new DriverPropertyInfo[]{
                YdbConnectionProperty.ENDPOINT_DISCOVERY_PERIOD.toDriverPropertyInfo("1m"),
                YdbConnectionProperty.LOCAL_DATACENTER.toDriverPropertyInfo("sas"),
                YdbConnectionProperty.SECURE_CONNECTION.toDriverPropertyInfo("true"),
                YdbConnectionProperty.SECURE_CONNECTION_CERTIFICATE.toDriverPropertyInfo(null),
                YdbConnectionProperty.READ_TIMEOUT.toDriverPropertyInfo("2m"),
                YdbConnectionProperty.TOKEN.toDriverPropertyInfo("x-secured-token"),
                YdbConnectionProperty.AUTH_PROVIDER.toDriverPropertyInfo(null),

                YdbClientProperty.QUERY_CACHE_SIZE.toDriverPropertyInfo("101"),
                YdbClientProperty.KEEP_QUERY_TEXT.toDriverPropertyInfo("true"),
                YdbClientProperty.SESSION_KEEP_ALIVE_TIME.toDriverPropertyInfo("15m"),
                YdbClientProperty.SESSION_MAX_IDLE_TIME.toDriverPropertyInfo("5m"),
                YdbClientProperty.SESSION_CREATION_MAX_RETRIES.toDriverPropertyInfo("2"),
                YdbClientProperty.SESSION_POOL_SIZE_MIN.toDriverPropertyInfo("3"),
                YdbClientProperty.SESSION_POOL_SIZE_MAX.toDriverPropertyInfo("4"),

                YdbOperationProperty.JOIN_DURATION.toDriverPropertyInfo("6m"),
                YdbOperationProperty.KEEP_IN_QUERY_CACHE.toDriverPropertyInfo("true"),
                YdbOperationProperty.QUERY_TIMEOUT.toDriverPropertyInfo("2m"),
                YdbOperationProperty.SCAN_QUERY_TIMEOUT.toDriverPropertyInfo("3m"),
                YdbOperationProperty.FAIL_ON_TRUNCATED_RESULT.toDriverPropertyInfo("false"),
                YdbOperationProperty.SESSION_TIMEOUT.toDriverPropertyInfo("6s"),
                YdbOperationProperty.DEADLINE_TIMEOUT.toDriverPropertyInfo("1s"),
                YdbOperationProperty.AUTOCOMMIT.toDriverPropertyInfo("true"),
                YdbOperationProperty.TRANSACTION_LEVEL.toDriverPropertyInfo("4"),

                YdbOperationProperty.AUTO_PREPARED_BATCHES.toDriverPropertyInfo("false"),
                YdbOperationProperty.ENFORCE_SQL_V1.toDriverPropertyInfo("false"),
                YdbOperationProperty.ENFORCE_VARIABLE_PREFIX.toDriverPropertyInfo("false"),
                YdbOperationProperty.CACHE_CONNECTIONS_IN_DRIVER.toDriverPropertyInfo("false"),
                YdbOperationProperty.DETECT_SQL_OPERATIONS.toDriverPropertyInfo("false")
        };
    }

    @SuppressWarnings("UnstableApiUsage")
    static void checkCustomizedProperties(YdbProperties properties) {
        YdbConnectionProperties conn = properties.getConnectionProperties();
        assertEquals(Collections.singletonList(HostAndPort.fromParts("ydb-ru-prestable.yandex.net", 2135)),
                conn.getAddresses());
        assertEquals("/ru-prestable/ci/testing/ci",
                conn.getDatabase());

        YdbOperationProperties ops = properties.getOperationProperties();
        assertEquals(Duration.ofMinutes(6), ops.getJoinDuration());
        assertTrue(ops.isKeepInQueryCache());
        assertEquals(Duration.ofMinutes(2), ops.getQueryTimeout());
        assertEquals(Duration.ofMinutes(3), ops.getScanQueryTimeout());
        assertFalse(ops.isFailOnTruncatedResult());
        assertEquals(Duration.ofSeconds(6), ops.getSessionTimeout());
        assertTrue(ops.isAutoCommit());
        assertEquals(YdbConst.ONLINE_CONSISTENT_READ_ONLY, ops.getTransactionLevel());
        assertFalse(ops.isAutoPreparedBatches());
        assertFalse(ops.isEnforceSqlV1());
        assertFalse(ops.isEnforceVariablePrefix());
        assertFalse(ops.isCacheConnectionsInDriver());
        assertFalse(ops.isDetectSqlOperations());
    }

    static String asString(DriverPropertyInfo info) {
        assertNull(info.choices);
        return String.format("%s=%s (%s, required = %s)", info.name, info.value, info.description, info.required);
    }

    @SuppressWarnings("UnstableApiUsage")
    static Collection<Arguments> urlsToParse() {
        return Arrays.asList(
                Arguments.of("jdbc:ydb:ydb-ru-prestable.yandex.net:2135",
                        HostAndPort.fromParts("ydb-ru-prestable.yandex.net", 2135),
                        null,
                        null),
                Arguments.of("jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci",
                        HostAndPort.fromParts("ydb-ru-prestable.yandex.net", 2135),
                        "/ru-prestable/ci/testing/ci",
                        null),
                Arguments.of("jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci?localDatacenter=man",
                        HostAndPort.fromParts("ydb-ru-prestable.yandex.net", 2135),
                        "/ru-prestable/ci/testing/ci",
                        "man"),
                Arguments.of("jdbc:ydb:ydb-ru-prestable.yandex.net:2135?localDatacenter=man",
                        HostAndPort.fromParts("ydb-ru-prestable.yandex.net", 2135),
                        null,
                        "man")
        );
    }

    static Collection<Arguments> urlsToCheck() {
        return Arrays.asList(
                Arguments.of("jdbc:ydb:", true),
                Arguments.of("jdbc:ydb:ydb-ru-prestable.yandex.net:2135", true),
                Arguments.of("jdbc:ydb:ydb-ru-prestable.yandex.net:2135/ru-prestable/ci/testing/ci", true),
                Arguments.of("jdbc:ydb:ydb-ru-prestable.yandex.net:2135" +
                        "/ru-prestable/ci/testing/ci?localDatacenter=man", true),
                Arguments.of("ydb:", false),
                Arguments.of("jdbc:ydb", false),
                Arguments.of("jdbc:clickhouse://man", false)
        );
    }

    static Collection<Arguments> tokensToCheck() {
        return Arrays.asList(
                Arguments.of("classpath:data/token.txt", "token-from-classpath"),
                Arguments.of("file:" + TOKEN_FILE.getAbsolutePath(), TOKEN_FROM_FILE));
    }

    static Collection<Arguments> certificatesToCheck() {
        return Arrays.asList(
                Arguments.of("classpath:data/certificate.txt", "certificate-from-classpath"),
                Arguments.of("file:" + CERTIFICATE_FILE.getAbsolutePath(), CERTIFICATE_FROM_FILE));
    }

    static Collection<Arguments> unknownFiles() {
        return Arrays.asList(
                Arguments.of("classpath:data/unknown-file.txt",
                        "Unable to find classpath resource: classpath:data/unknown-file.txt"),
                Arguments.of("file:data/unknown-file.txt",
                        "Unable to read resource from file:data/unknown-file.txt"));
    }

    static Collection<Arguments> invalidDurationParams() {
        return Arrays.asList(
                Arguments.of("endpointDiscoveryPeriod"),
                Arguments.of("readTimeout"),
                Arguments.of("sessionKeepAliveTime"),
                Arguments.of("sessionMaxIdleTime"),
                Arguments.of("joinDuration"),
                Arguments.of("queryTimeout"),
                Arguments.of("scanQueryTimeout"),
                Arguments.of("sessionTimeout"),
                Arguments.of("deadlineTimeout")
        );
    }

    static Collection<Arguments> invalidIntegerParams() {
        return Arrays.asList(
                Arguments.of("queryCacheSize"),
                Arguments.of("sessionCreationMaxRetries"),
                Arguments.of("sessionPoolSizeMin"),
                Arguments.of("sessionPoolSizeMax"),
                Arguments.of("transactionLevel")
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static File safeCreateFile(String content) throws IOException {
        File file = File.createTempFile("junit", "ydb");
        Files.write(content.getBytes(), file);
        return file;
    }

    private static void safeDeleteFile(@Nullable File file) {
        if (file != null) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }
}
