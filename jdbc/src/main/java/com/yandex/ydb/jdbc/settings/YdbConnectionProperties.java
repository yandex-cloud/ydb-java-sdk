package com.yandex.ydb.jdbc.settings;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import com.yandex.ydb.core.grpc.GrpcTransport;

@SuppressWarnings("UnstableApiUsage")
public class YdbConnectionProperties {
    private final List<HostAndPort> addresses;
    private final Map<YdbConnectionProperty<?>, ParsedProperty> params;

    public YdbConnectionProperties(List<HostAndPort> addresses,
                                   Map<YdbConnectionProperty<?>, ParsedProperty> params) {
        this.addresses = Objects.requireNonNull(addresses);
        Preconditions.checkArgument(!addresses.isEmpty(), "Address list cannot be empty");
        this.params = Objects.requireNonNull(params);
    }

    public List<HostAndPort> getAddresses() {
        return addresses;
    }

    @Nullable
    public String getDatabase() {
        ParsedProperty databaseProperty = getProperty(YdbConnectionProperty.DATABASE);
        return databaseProperty != null ? databaseProperty.getParsedValue() : null;
    }

    @Nullable
    public ParsedProperty getProperty(YdbConnectionProperty<?> property) {
        return params.get(property);
    }

    public Map<YdbConnectionProperty<?>, ParsedProperty> getParams() {
        return params;
    }

    public GrpcTransport toGrpcTransport() {
        GrpcTransport.Builder builder = GrpcTransport.forHosts(addresses);
        for (Map.Entry<YdbConnectionProperty<?>, ParsedProperty> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                entry.getKey().getSetter().accept(builder, entry.getValue().getParsedValue());
            }
        }
        return builder.build();
    }

}
