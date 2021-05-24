package com.yandex.ydb.jdbc.settings;

import java.time.Duration;
import java.util.Collection;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.auth.TokenAuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.jdbc.exception.YdbConfigurationException;

public class YdbConnectionProperty<T> extends AbstractYdbProperty<T, GrpcTransport.Builder> {
    private static final PropertiesCollector<YdbConnectionProperty<?>> PROPERTIES = new PropertiesCollector<>();

    public static final YdbConnectionProperty<Duration> ENDPOINT_DISCOVERY_PERIOD =
            new YdbConnectionProperty<>(
                    "endpointDiscoveryPeriod",
                    "Endpoint discovery period",
                    null,
                    Duration.class,
                    PropertyConverter.durationValue(),
                    GrpcTransport.Builder::withEndpointsDiscoveryPeriod);
    public static final YdbConnectionProperty<String> LOCAL_DATACENTER =
            new YdbConnectionProperty<>(
                    "localDatacenter",
                    "Local Datacenter",
                    null,
                    String.class,
                    PropertyConverter.stringValue(),
                    GrpcTransport.Builder::withLocalDataCenter);
    public static final YdbConnectionProperty<Boolean> SECURE_CONNECTION =
            new YdbConnectionProperty<>(
                    "secureConnection",
                    "Use TLS connection",
                    null,
                    Boolean.class,
                    PropertyConverter.booleanValue(),
                    (builder, value) -> {
                        if (value) {
                            builder.withSecureConnection();
                        }
                    });
    public static final YdbConnectionProperty<byte[]> SECURE_CONNECTION_CERTIFICATE =
            new YdbConnectionProperty<>(
                    "secureConnectionCertificate",
                    "Use TLS connection with certificate from provided path",
                    null,
                    byte[].class,
                    PropertyConverter.byteFileReference(),
                    GrpcTransport.Builder::withSecureConnection);
    public static final YdbConnectionProperty<Duration> READ_TIMEOUT =
            new YdbConnectionProperty<>(
                    "readTimeout",
                    "Read Timeout",
                    null,
                    Duration.class,
                    PropertyConverter.durationValue(),
                    GrpcTransport.Builder::withReadTimeout);

    public static final YdbConnectionProperty<TokenAuthProvider> TOKEN =
            new YdbConnectionProperty<>(
                    "token",
                    "Token-based authentication",
                    null,
                    TokenAuthProvider.class,
                    value -> new TokenAuthProvider(PropertyConverter.stringFileReference().convert(value)),
                    GrpcTransport.Builder::withAuthProvider);

    public static final YdbConnectionProperty<AuthProvider> AUTH_PROVIDER =
            new YdbConnectionProperty<>(
                    "authProvider",
                    "Custom authentication provider (must be specified programmatically in properties as object)",
                    null,
                    AuthProvider.class,
                    value -> {
                        throw new YdbConfigurationException("Property authProvider must be configured with object, " +
                                "not a string");
                    },
                    GrpcTransport.Builder::withAuthProvider);

    protected YdbConnectionProperty(String title,
                                    String description,
                                    @Nullable String defaultValue,
                                    Class<T> type,
                                    PropertyConverter<T> converter,
                                    BiConsumer<GrpcTransport.Builder, T> setter) {
        super(title, description, defaultValue, type, converter, setter);
        PROPERTIES.register(this);
    }

    public static Collection<YdbConnectionProperty<?>> properties() {
        return PROPERTIES.properties();
    }
}
