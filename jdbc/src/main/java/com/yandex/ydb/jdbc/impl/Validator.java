package com.yandex.ydb.jdbc.impl;

import java.io.InterruptedIOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.yandex.ydb.core.Issue;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.core.Status;
import com.yandex.ydb.core.StatusCode;
import com.yandex.ydb.jdbc.exception.YdbConditionallyRetryableException;
import com.yandex.ydb.jdbc.exception.YdbExecutionException;
import com.yandex.ydb.jdbc.exception.YdbNonRetryableException;
import com.yandex.ydb.jdbc.exception.YdbRetryableException;
import com.yandex.ydb.jdbc.settings.YdbOperationProperties;
import com.yandex.ydb.table.settings.RequestSettings;
import io.grpc.Context;
import org.slf4j.Logger;

import static com.yandex.ydb.jdbc.YdbConst.DATABASE_QUERY_INTERRUPTED;
import static com.yandex.ydb.jdbc.YdbConst.DATABASE_UNAVAILABLE;
import static com.yandex.ydb.jdbc.YdbConst.DB_QUERY_CANCELLED;
import static com.yandex.ydb.jdbc.YdbConst.DB_QUERY_DEADLINE_EXCEEDED;

public class Validator {
    private final YdbOperationProperties properties;

    public Validator(YdbOperationProperties properties) {
        this.properties = properties;
    }

    //

    public <T extends RequestSettings<T>> T init(T settings) {
        settings.setTimeout(properties.getDeadlineTimeout());
        return settings;
    }

    public Status joinStatus(Logger logger,
                             Supplier<String> operation,
                             Supplier<CompletableFuture<Status>> action) throws SQLException {
        return joinWrapped(logger, operation, action, Function.identity());
    }

    public <T, R extends Result<T>> R joinResult(Logger logger,
                                                 Supplier<String> operation,
                                                 Supplier<CompletableFuture<R>> action) throws SQLException {
        return joinWrapped(logger, operation, action, Result::toStatus);
    }

    private <T> T joinWrapped(Logger logger,
                              Supplier<String> operation,
                              Supplier<CompletableFuture<T>> action,
                              Function<T, Status> toStatus) throws SQLException {
        boolean isDebugEnabled = logger.isDebugEnabled();
        Stopwatch sw;
        if (isDebugEnabled) {
            logger.debug("{}", operation.get());
            sw = Stopwatch.createStarted();
        } else {
            sw = null;
        }
        Throwable throwable = null;
        T result = null;
        try {
            result = join(action.get());
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            if (isDebugEnabled) {
                StatusCode status = result != null ? toStatus.apply(result).getCode() : StatusCode.UNUSED_STATUS;
                Object message = status != StatusCode.SUCCESS ? result : status;
                logger.debug("[{}] {}", sw.stop(), message, throwable);
            }
        }
        Objects.requireNonNull(result, "Internal error. Result cannot be null");
        validate(result, toStatus.apply(result).getCode());
        return result;
    }

    private <T> T join(CompletableFuture<T> future) throws SQLException {
        try {
            return future.get(properties.getJoinDuration().toMillis(), TimeUnit.MILLISECONDS);
        } catch (CancellationException | CompletionException | InterruptedException | TimeoutException e) {
            if (Thread.interrupted() || isInterrupted(e)) {
                Thread.currentThread().interrupt();
                throw new YdbExecutionException(DATABASE_QUERY_INTERRUPTED, e);
            }
            checkGrpcContextStatus(e);
            throw new YdbExecutionException(DATABASE_UNAVAILABLE + e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new YdbExecutionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean isInterrupted(Exception exception) {
        return Throwables.getCausalChain(exception).stream()
                .anyMatch(e -> e instanceof InterruptedException || e instanceof InterruptedIOException);
    }

    public SQLWarning toSQLWarnings(Issue[] issues) {
        SQLWarning firstWarning = null;
        SQLWarning warning = null;
        for (Issue issue : issues) {
            SQLWarning nextWarning = new SQLWarning(issue.toString(), null, issue.getCode());
            if (firstWarning == null) {
                firstWarning = nextWarning;
            }
            if (warning != null) {
                warning.setNextWarning(nextWarning);
            }
            warning = nextWarning;
        }
        return firstWarning;
    }

    public static void validate(Object response, StatusCode statusCode) throws SQLException {
        switch (statusCode) {
            case SUCCESS:
                return;

            case BAD_REQUEST:
            case INTERNAL_ERROR:
            case CLIENT_UNAUTHENTICATED:
                // gRPC reports, request is not authenticated
                // Maybe internal error, maybe some issue with token
            case UNAUTHORIZED:
                // Unauthorized by database
            case SCHEME_ERROR:
            case GENERIC_ERROR:
            case CLIENT_CALL_UNIMPLEMENTED:
            case UNSUPPORTED:
            case UNUSED_STATUS:
            case ALREADY_EXISTS:
                throw new YdbNonRetryableException(response, statusCode);

            case ABORTED:
            case UNAVAILABLE:
                // Some of database parts are not available
            case OVERLOADED:
                // Database is overloaded, need to retry with exponential backoff
            case TRANSPORT_UNAVAILABLE:
                // Some issues with networking
            case CLIENT_RESOURCE_EXHAUSTED:
                // No resources to handle client request
            case NOT_FOUND:
                // Could be 'prepared query' issue, could be 'transaction not found'
                // Should be retries with new session
            case BAD_SESSION:
                // Retry with new session
            case SESSION_EXPIRED:
                // Retry with new session
                throw new YdbRetryableException(response, statusCode);

            case CANCELLED:
                // Query was canceled due to query timeout (CancelAfter)
                // Query was definitely canceled by database
            case CLIENT_CANCELLED:
            case CLIENT_INTERNAL_ERROR:
                // Some unknown client side error, probably on transport layer
                checkGrpcContextStatus(response, statusCode);
                throw new YdbConditionallyRetryableException(response, statusCode);

            case UNDETERMINED:
            case TIMEOUT:
                // Database cannot respond in time, need to retry with exponential backoff
            case PRECONDITION_FAILED:
            case CLIENT_DEADLINE_EXCEEDED:
                // Query was canceled on transport layer
            case SESSION_BUSY:
                // Another query is executing already, retry with new session
            case CLIENT_DISCOVERY_FAILED:
                // Some issue with database endpoints discovery
            case CLIENT_LIMITS_REACHED:
                // Client side session limit was reached
                throw new YdbConditionallyRetryableException(response, statusCode);
            default:
                throw new YdbNonRetryableException(response, statusCode);
        }
    }

    public static void checkGrpcContextStatus(Object response, StatusCode statusCode) throws SQLException {
        if (Context.current().getDeadline() != null && Context.current().getDeadline().isExpired()) {
            // Query deadline reached, separate exception thrown to prevent retries
            throw new YdbNonRetryableException(DB_QUERY_DEADLINE_EXCEEDED + response, statusCode);
        } else if (Context.current().isCancelled()) {
            // Canceled on client side, no need to retry
            throw new YdbNonRetryableException(DB_QUERY_CANCELLED + response, statusCode);
        }
    }

    public static void checkGrpcContextStatus(Exception exception) throws SQLException {
        if (Context.current().getDeadline() != null && Context.current().getDeadline().isExpired()) {
            // Query deadline reached, separate exception thrown to prevent retries
            throw new YdbExecutionException(DB_QUERY_DEADLINE_EXCEEDED + exception.getMessage(), exception);
        } else if (Context.current().isCancelled()) {
            // Canceled on client side, no need to retry
            throw new YdbExecutionException(DB_QUERY_CANCELLED + exception.getMessage(), exception);
        }
    }

}
