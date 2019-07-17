package com.yandex.ydb.auth.iam;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yandex.ydb.core.auth.AuthProvider;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;


/**
 * This context will automatically refresh tokens of registered keys in background.
 *
 * Instance of this class will start thread to track tokens expiration and perform
 * token refresh in advance. Because of this it is better to use one instance of
 * this class per application.
 *
 * Usage example:
 *
 * <pre>{@code
 * IamAuthContext ctx = new IamAuthContext();
 * AuthProvider authProvider = ctx.authProvider("account_id", "key_id", Keys.privateKey(new File("/path/to/pem/file")))
 *      .join(); // await first token refresh synchronously or subscribe on future
 *
 * GrpcTransport transport = GrpcTransport.forEndpoint("some-ydb-endpoint", "my_db")
 *      .withAuthProvider(authProvider)
 *      .build();
 *
 * // ...
 *
 * // do not forget to close context on application exit
 * ctx.close();
 *
 * } </pre>
 *
 * @author Sergey Polovko
 */
public class IamAuthContext implements AutoCloseable {

    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;
    private static final long TOKEN_TTL_MILLIS = TimeUnit.HOURS.toMillis(1);

    private final String iamUrl;
    private final AsyncHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<TokenKey, TokenState> tokens = new ConcurrentHashMap<>();

    public IamAuthContext() {
        this("https://iam.api.cloud.yandex.net");
    }

    public IamAuthContext(String endpoint) {
        this.iamUrl = endpoint + "/iam/v1/tokens";
        this.httpClient = Dsl.asyncHttpClient(Dsl.config()
            .setConnectTimeout(CONNECT_TIMEOUT_MILLIS)
            .setReadTimeout(READ_TIMEOUT_MILLIS)
            .setKeepAlive(true)
            .setMaxConnections(1)
            .setFollowRedirect(false)
            .build());
    }

    public CompletableFuture<AuthProvider> authProvider(String accountId, String keyId, PrivateKey privateKey) {
        TokenKey tokenKey = new TokenKey(accountId, keyId);
        TokenState existState = tokens.get(tokenKey);
        if (existState != null) {
            return CompletableFuture.completedFuture(existState);
        }

        TokenState newState = new TokenState(tokenKey, privateKey);
        return newState.refresh()
            .thenApply(aVoid -> {
                TokenState prevState = tokens.putIfAbsent(tokenKey, newState);
                if (prevState != null) {
                    return prevState;
                }
                newState.refreshPeriodically(TOKEN_TTL_MILLIS / 2);
                return newState;
            });
    }

    private CompletableFuture<String> fetchToken(String jwt) {
        Request request = Dsl.post(iamUrl)
            .setHeader("Content-Type", "application/json")
            .setHeader("Accept", "application/json")
            .setBody("{\"jwt\":\"" + jwt + "\"}")
            .build();

        return httpClient.executeRequest(request)
            .toCompletableFuture()
            .thenApply(response -> {
                byte[] body = response.getResponseBodyAsBytes();

                if (response.getStatusCode() != 200) {
                    throw new IllegalStateException(String.format(
                        "non OK response: %d (%s) %s",
                        response.getStatusCode(),
                        response.getStatusText(),
                        new String(body, StandardCharsets.UTF_8)));
                }

                try {
                    return objectMapper.readValue(body, TokenResponse.class).iamToken;
                } catch (IOException e) {
                    String msg = "invalid json: " + new String(body, StandardCharsets.UTF_8);
                    throw new UncheckedIOException(msg, e);
                }
            });
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException ignore) {
        }
        tokens.clear();
        scheduler.shutdown();
    }

    /**
     * TOKEN RESPONSE
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class TokenResponse {
        @JsonProperty
        String iamToken;
    }

    /**
     * TOKEN STATE
     */
    private final class TokenState implements AuthProvider {
        private final TokenKey tokenKey;
        private final PrivateKey privateKey;
        private ScheduledFuture<?> scheduledFuture;
        private volatile String token;

        TokenState(TokenKey tokenKey, PrivateKey privateKey) {
            this.tokenKey = tokenKey;
            this.privateKey = privateKey;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public void close() {
            if (!scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
                tokens.remove(tokenKey);
            }
        }

        CompletableFuture<Void> refresh() {
            return CompletableFuture.supplyAsync(() -> {
                    long nowMillis = System.currentTimeMillis();
                    return tokenKey.toJwt(privateKey, nowMillis, TOKEN_TTL_MILLIS);
                })
                .thenCompose(IamAuthContext.this::fetchToken)
                .thenAccept(token -> this.token = token);
        }

        void refreshPeriodically(long periodMillis) {
            this.scheduledFuture = scheduler.scheduleAtFixedRate(
                this::refresh,
                periodMillis,
                periodMillis,
                TimeUnit.MILLISECONDS);
        }
    }
}
