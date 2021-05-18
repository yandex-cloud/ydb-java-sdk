package com.yandex.ydb.jdbc.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.net.HostAndPort;
import com.yandex.ydb.jdbc.exception.YdbConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YdbProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(YdbProperties.class);

    private static final String JDBC_PREFIX = "jdbc:";
    private static final String JDBC_YDB_PREFIX = JDBC_PREFIX + "ydb:";

    private final YdbConnectionProperties connectionProperties;
    private final YdbClientProperties clientProperties;
    private final YdbOperationProperties operationProperties;

    public YdbProperties(YdbConnectionProperties connectionProperties, YdbClientProperties clientProperties,
                         YdbOperationProperties operationProperties) {
        this.connectionProperties = Objects.requireNonNull(connectionProperties);
        this.clientProperties = Objects.requireNonNull(clientProperties);
        this.operationProperties = Objects.requireNonNull(operationProperties);
    }

    public YdbConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    public YdbClientProperties getClientProperties() {
        return clientProperties;
    }

    public YdbOperationProperties getOperationProperties() {
        return operationProperties;
    }

    public DriverPropertyInfo[] toDriverProperties() {
        List<DriverPropertyInfo> properties = new ArrayList<>();
        connectionProperties.getParams().forEach((property, value) ->
                properties.add(property.toDriverPropertyInfoFrom(value)));
        clientProperties.getParams().forEach((property, value) ->
                properties.add(property.toDriverPropertyInfoFrom(value)));
        operationProperties.getParams().forEach((property, value) ->
                properties.add(property.toDriverPropertyInfoFrom(value)));
        return properties.toArray(new DriverPropertyInfo[0]);
    }

    //

    public static boolean isYdb(String url) {
        return url.startsWith(JDBC_YDB_PREFIX);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static YdbProperties from(String url, Properties origProperties) throws SQLException {
        if (!isYdb(url)) {
            throw new YdbConfigurationException("Not an YDB URL, must starts from " + JDBC_YDB_PREFIX);
        }

        url = url.substring(JDBC_YDB_PREFIX.length());

        String addressesUrl = getAddressesUrl(url);
        url = url.substring(addressesUrl.length());

        List<HostAndPort> addresses = getAddresses(addressesUrl);
        String database = getDatabase(url);
        Properties props = withQuery(url, origProperties);
        YdbConnectionProperties connectionProperties = new YdbConnectionProperties(addresses, database,
                parseProperties(props, YdbConnectionProperty.properties()));

        YdbClientProperties ydbClientProperties = new YdbClientProperties(
                parseProperties(props, YdbClientProperty.properties()));
        YdbOperationProperties ydbOperationProperties = new YdbOperationProperties(
                parseProperties(props, YdbOperationProperty.properties()));
        return new YdbProperties(connectionProperties, ydbClientProperties, ydbOperationProperties);
    }

    private static String getAddressesUrl(String url) {
        int databaseSeparator = url.indexOf('/');
        if (databaseSeparator < 0) {
            databaseSeparator = url.indexOf('?');
            if (databaseSeparator < 0) {
                return url;
            }
        }
        return url.substring(0, databaseSeparator);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static List<HostAndPort> getAddresses(String part) throws SQLException {
        String[] addresses = part.split(",");
        if (addresses.length == 0) {
            throw invalidFormatException();
        }
        return Stream.of(addresses)
                .map(HostAndPort::fromString)
                .collect(Collectors.toList());
    }

    @Nullable
    private static String getDatabase(String url) {
        String database;
        int paramsSeparator = url.indexOf('?');
        if (paramsSeparator == 0) {
            return null;
        } else if (paramsSeparator > 0) {
            database = url.substring(0, paramsSeparator);
        } else {
            database = url;
        }
        return database.isEmpty() || database.equals("/") ? null : database;
    }

    private static Properties withQuery(String url, Properties defaults) {
        int paramsSeparator = url.indexOf('?');
        if (paramsSeparator < 0) {
            return defaults;
        }

        String params = url.substring(paramsSeparator + 1);
        if (params.isEmpty()) {
            return defaults;
        }
        Properties urlProps = new Properties(defaults);
        String[] kv = params.split("&");
        for (String keyValue : kv) {
            String[] tokens = keyValue.split("=", 2);
            if (tokens.length == 2) {
                urlProps.put(tokens[0], tokens[1]);
            } else {
                LOGGER.error("Invalid property: {}", keyValue);
            }
        }
        return urlProps;
    }

    private static <T extends AbstractYdbProperty<?, ?>> Map<T, ParsedProperty> parseProperties(
            Properties properties,
            Collection<T> knownProperties) throws SQLException {
        Map<T, ParsedProperty> result = new LinkedHashMap<>(knownProperties.size());
        for (T property : knownProperties) {
            String title = property.getTitle();
            Object value = properties.get(title);

            PropertyConverter<?> converter = property.getConverter();
            ParsedProperty parsed;
            if (value != null) {
                if (value instanceof String) {
                    String stringValue = (String) value;
                    try {
                        parsed = new ParsedProperty(stringValue, converter.convert(stringValue));
                    } catch (SQLException e) {
                        throw new YdbConfigurationException("Unable to convert property " +
                                title + ": " + e.getMessage(), e);
                    }
                } else {
                    if (property.getType().isAssignableFrom(value.getClass())) {
                        parsed = new ParsedProperty("", value);
                    } else {
                        throw new SQLException("Invalid object property " + title +
                                ", must be " + property.getType() + ", got " + value.getClass());
                    }
                }
            } else {
                String stringValue = property.getDefaultValue();
                if (stringValue != null) {
                    try {
                        parsed = new ParsedProperty(stringValue, converter.convert(stringValue));
                    } catch (SQLException e) {
                        throw new YdbConfigurationException("Unable to convert property " +
                                title + ": " + e.getMessage(), e);
                    }
                } else {
                    parsed = null;
                }
            }

            result.put(property, parsed);
        }
        return Collections.unmodifiableMap(result);
    }

    private static SQLException invalidFormatException() {
        return new SQLException("URL must be provided in form " + JDBC_YDB_PREFIX +
                "host:port[/database][?param1=value1&paramN=valueN]");
    }

    //

    public static String stringFileReference(String ref) throws YdbConfigurationException {
        Optional<URL> urlOpt = YdbProperties.resolvePath(ref);
        if (urlOpt.isPresent()) {
            URL url = urlOpt.get();
            try (Reader reader = new InputStreamReader(url.openStream())) {
                return CharStreams.toString(reader).trim();
            } catch (IOException e) {
                throw new YdbConfigurationException("Unable to read resource from " + url, e);
            }
        } else {
            return ref;
        }
    }

    public static byte[] byteFileReference(String ref) throws YdbConfigurationException {
        Optional<URL> urlOpt = YdbProperties.resolvePath(ref);
        if (urlOpt.isPresent()) {
            URL url = urlOpt.get();
            try (InputStream stream = url.openStream()) {
                return ByteStreams.toByteArray(stream);
            } catch (IOException e) {
                throw new YdbConfigurationException("Unable to read resource from " + url, e);
            }
        } else {
            throw new YdbConfigurationException("Must be 'file:' or 'classpath:' reference");
        }
    }

    static Optional<URL> resolvePath(String ref) throws YdbConfigurationException {
        if (ref.startsWith("file:")) {
            try {
                return Optional.of(new URL(ref));
            } catch (MalformedURLException e) {
                throw new YdbConfigurationException("Unable to parse ref as file: " + ref, e);
            }
        } else if (ref.startsWith("classpath:")) {
            URL systemResource = ClassLoader.getSystemResource(ref.substring("classpath:".length()));
            if (systemResource == null) {
                throw new YdbConfigurationException("Unable to find classpath resource: " + ref);
            }
            return Optional.of(systemResource);
        } else {
            return Optional.empty();
        }
    }

}
