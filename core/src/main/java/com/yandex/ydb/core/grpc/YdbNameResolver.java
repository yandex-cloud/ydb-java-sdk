package com.yandex.ydb.core.grpc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.auth.AuthProvider;
import com.yandex.ydb.core.utils.Async;
import com.yandex.ydb.discovery.DiscoveryProtos.EndpointInfo;
import com.yandex.ydb.discovery.DiscoveryProtos.ListEndpointsResult;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.SynchronizationContext;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sergey Polovko
 * @author Evgeniy Pshenitsin
 */
final class YdbNameResolver extends NameResolver {

    private static final Logger logger = LoggerFactory.getLogger(YdbNameResolver.class);

    private static final String SCHEME = "ydb";
    public static final Attributes.Key<String> LOCATION_ATTR = Attributes.Key.create("loc");

    private final String database;
    private final String authority;
    private final GrpcDiscoveryRpc discoveryRpc;

    private Listener listener;
    private volatile boolean shutdown = false;

    private final SynchronizationContext synchronizationContext;
    private Timeout scheduledHandle = null;
    private final Duration discoveryPeriod;

    private YdbNameResolver(
            String database,
            String authority,
            GrpcDiscoveryRpc discoveryRpc,
            SynchronizationContext synchronizationContext,
            Duration discoveryPeriod) {
        this.database = database;
        this.authority = authority;
        this.discoveryRpc = discoveryRpc;
        this.synchronizationContext = synchronizationContext;
        if (discoveryPeriod.toMillis() < 5000) {
            discoveryPeriod = Duration.ofSeconds(5);
        }
        this.discoveryPeriod = discoveryPeriod;
    }

    static String makeTarget(String endpoint, String database) {
        StringBuilder sb = new StringBuilder();
        sb.append(SCHEME).append("://").append(endpoint);
        if (!database.startsWith("/")) {
            sb.append('/');
        }
        sb.append(database);
        return sb.toString();
    }

    @Override
    public String getServiceAuthority() {
        return authority;
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;
        resolve();
    }

    @Override
    public void refresh() {
        cancelScheduledRefresh();
        resolve();
    }

    @Override
    public void shutdown() {
        cancelScheduledRefresh();
        discoveryRpc.close();
        shutdown = true;
    }

    private void cancelScheduledRefresh() {
        if (scheduledHandle != null) {
            scheduledHandle.cancel();
            scheduledHandle = null;
        }
    }

    private void resolve() {
        if (shutdown) {
            return;
        }

        discoveryRpc.listEndpoints(database, System.nanoTime() + discoveryPeriod.dividedBy(2).toNanos())
                .thenAccept(result -> {
                    ListEndpointsResult response = ensureSuccessResolved(result);
                    if (response == null) {
                        return;
                    }

                    List<EquivalentAddressGroup> groups = new ArrayList<>(response.getEndpointsCount());
                    for (EndpointInfo e : response.getEndpointsList()) {
                        try {
                            groups.add(createAddressGroup(e));
                        } catch (UnknownHostException x) {
                            String msg = "unable to resolve database " + database +
                                    ", got unknown hostname: " + e.getAddress();
                            listener.onError(Status.UNAVAILABLE.withDescription(msg).withCause(x));
                            return;
                        }
                    }

                    listener.onAddresses(groups, Attributes.EMPTY);
                })
                .exceptionally(e -> {
                    String msg = "unable to resolve database " + database + ", unhandled exception";
                    listener.onError(Status.UNAVAILABLE.withDescription(msg).withCause(e));
                    scheduleNextDiscovery();
                    return null;
                });
    }

    private ListEndpointsResult ensureSuccessResolved(Result<ListEndpointsResult> result) {
        if (!result.isSuccess()) {
            String msg = "unable to resolve database " + database +
                    ", got non SUCCESS response: " + result.getCode() +
                    ", issues: " + Arrays.toString(result.getIssues());
            listener.onError(Status.UNAVAILABLE.withDescription(msg));
            return null;
        }

        ListEndpointsResult response = result.expect("listEndpoints()");
        logger.debug("response METHOD_LIST_ENDPOINTS - {}", response.toString());
        if (response.getEndpointsCount() == 0) {
            String msg = "unable to resolve database " + database + ", got empty list of endpoints";
            listener.onError(Status.UNAVAILABLE.withDescription(msg));
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("ListEndpointsResult - {}",
                    response.getEndpointsList().stream()
                            .map(e -> String.format("{addr - %s, loc - %s}", e.getAddress(), e.getLocation()))
                            .collect(Collectors.joining(",")));
        }
        return response;
    }

    private void scheduleNextDiscovery() {
        synchronizationContext.execute(() -> {
            cancelScheduledRefresh();
            scheduledHandle = Async.runAfter((timeout) -> {
                if (!timeout.isCancelled()) {
                    synchronizationContext.execute(this::refresh);
                }
            }, randomDelay(discoveryPeriod.toMillis()), TimeUnit.MILLISECONDS);
        });
    }

    private long randomDelay(long delayMillis) {
        long half = delayMillis / 2;
        return half + ThreadLocalRandom.current().nextLong(half);
    }

    // TODO: resolve name asynchronously
    private static EquivalentAddressGroup createAddressGroup(EndpointInfo endpoint) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(endpoint.getAddress());
        if (addresses.length == 1) {
            return new EquivalentAddressGroup(new InetSocketAddress(addresses[0], endpoint.getPort()),
                    Attributes.newBuilder().set(LOCATION_ATTR, endpoint.getLocation()).build());
        }

        List<SocketAddress> socketAddresses = new ArrayList<>(addresses.length);
        for (InetAddress address : addresses) {
            socketAddresses.add(new InetSocketAddress(address, endpoint.getPort()));
        }
        return new EquivalentAddressGroup(socketAddresses,
                Attributes.newBuilder().set(LOCATION_ATTR, endpoint.getLocation()).build());
    }

    static Factory newFactory(
            AuthProvider authProvider,
            @Nullable byte[] cert,
            boolean useTLS,
            Duration discoveryPeriod,
            Executor executor,
            Consumer<NettyChannelBuilder> channelCustomizer) {
        return new Factory() {
            @Nullable
            @Override
            public NameResolver newNameResolver(URI targetUri, Helper helper) {
                if (!SCHEME.equals(targetUri.getScheme())) {
                    return null;
                }
                int port = targetUri.getPort();
                if (port == -1) {
                    port = GrpcTransport.DEFAULT_PORT;
                }

                String host = targetUri.getHost();
                String database = targetUri.getPath();
                String authority = GrpcUtil.authorityFromHostAndPort(host, port);
                GrpcTransport.Builder transportBuilder = GrpcTransport.forHost(host, port)
                        .withAuthProvider(authProvider)
                        .withCallExecutor(executor)
                        .withDataBase(database)
                        .withChannelInitializer(channelCustomizer);
                if (useTLS) {
                    if (cert != null) {
                        transportBuilder.withSecureConnection(cert);
                    } else {
                        transportBuilder.withSecureConnection();
                    }
                }
                GrpcTransport transport = transportBuilder.build();
                GrpcDiscoveryRpc rpc = new GrpcDiscoveryRpc(transport);
                return new YdbNameResolver(database, authority, rpc, helper.getSynchronizationContext(), discoveryPeriod);
            }

            @Override
            public String getDefaultScheme() {
                return SCHEME;
            }
        };
    }
}
