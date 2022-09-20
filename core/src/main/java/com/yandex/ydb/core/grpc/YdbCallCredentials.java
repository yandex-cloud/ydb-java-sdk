package com.yandex.ydb.core.grpc;

import java.util.concurrent.Executor;

import com.yandex.ydb.core.auth.AuthProvider;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sergey Polovko
 */
public class YdbCallCredentials extends CallCredentials {
    private static final Logger logger = LoggerFactory.getLogger(YdbCallCredentials.class);

    private final AuthProvider authProvider;

    public YdbCallCredentials(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public void applyRequestMetadata(
        RequestInfo requestInfo,
        Executor appExecutor,
        MetadataApplier applier)
    {
        try {
            Metadata headers = new Metadata();
            String token = authProvider.getToken();
            if (token != null) {
                headers.put(YdbHeaders.AUTH_TICKET, token);
            }
            applier.apply(headers);
        } catch (RuntimeException ex) {
            logger.error("invalid token", ex);
            applier.fail(Status.UNAUTHENTICATED.withCause(ex));
        }
    }

    @Override
    public void thisUsesUnstableApi() {
    }
}
