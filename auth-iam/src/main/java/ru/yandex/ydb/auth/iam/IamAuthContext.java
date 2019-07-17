package ru.yandex.ydb.auth.iam;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.yandex.ydb.core.auth.AuthProvider;


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
 * AuthProvider authProvider = ctx.authProvider("account_id", "key_id", Keys.privateKey("/path/to/pem/file"))
 *      .join(); // await first token refresh synchronously or subscribe on future
 *
 * GrpcTransport transport = GrpcTransportBuilder.forEndpoint("ydb://some-ydb-endpoint", "my_db")
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

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final long TOKEN_TTL_MILLIS = TimeUnit.HOURS.toMillis(1);

    private final URI iamUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<TokenKey, TokenState> tokens = new ConcurrentHashMap<>();

    public IamAuthContext() {
        this("https://iam.api.cloud.yandex.net");
    }

    public IamAuthContext(String endpoint) {
        this.iamUri = URI.create(endpoint + "/iam/v1/tokens");
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(iamUri)
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"jwt\":\"" + jwt + "\"}"))
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
            .thenApply(response -> {
                byte[] body = response.body();

                if (response.statusCode() != 200) {
                    String msg = "non OK response: " + response.statusCode() + new String(body, StandardCharsets.UTF_8);
                    throw new IllegalStateException(msg);
                }

                try {
                    var tokenResponse = objectMapper.readValue(body, TokenResponse.class);
                    return tokenResponse.iamToken;
                } catch (IOException e) {
                    String msg = "invalid json: " + new String(body, StandardCharsets.UTF_8);
                    throw new UncheckedIOException(msg, e);
                }
            });
    }

    @Override
    public void close() {
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
