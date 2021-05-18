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
                // grpc сообщил, что запрос не аутентифицирован.
                // Это интернал ошибка, но возможно была проблема с выпиской токена и можно
                // попробовать поретраить - если не поможет, то отдать наружу
            case UNAUTHORIZED:
                // БД сообщила, что запрос не авторизован. Это интернал ошибка, но сейчас это
                // стабильно происходит по непонятным причинам; простой ретрай помогает  -
                // https://st.yandex-team.ru/KIKIMR-8694
            case SCHEME_ERROR:
            case GENERIC_ERROR:
            case CLIENT_CALL_UNIMPLEMENTED:
            case UNSUPPORTED:
            case UNUSED_STATUS:
            case ALREADY_EXISTS:
                throw new YdbNonRetryableException(response, statusCode);

            case ABORTED:
            case UNAVAILABLE:
                // БД ответила, что она или часть ее подсистем не доступны
            case OVERLOADED:
                // БД перегружена - нужно ретраить с экспоненциальной задержкой
            case TRANSPORT_UNAVAILABLE:
                // проблемы с сетевой связностью
            case CLIENT_RESOURCE_EXHAUSTED:
                // недостаточно свободных ресурсов для обслуживания запроса
            case NOT_FOUND:
                // Вероятнее всего это проблемы с prepared запросом.
                // Стоит поретраить с новой сессией.
                // Еще может быть Transaction not found
            case BAD_SESSION:
                // На самом деле можно повторить всю транзакцию
            case SESSION_EXPIRED:
                // На самом деле можно повторить всю транзакцию
                throw new YdbRetryableException(response, statusCode);

            case CANCELLED:
                // Запрос был отменен, тк закончился установленный в запросе таймаут (CancelAfter).
                // Запрос на сервере гарантированно отменен.
            case CLIENT_CANCELLED:
            case CLIENT_INTERNAL_ERROR:
                // неизвестная ошибка на клиентской стороне (чаще всего транспортного уровня)
                checkGrpcContextStatus(response, statusCode);
                throw new YdbConditionallyRetryableException(response, statusCode);

            case UNDETERMINED:
            case TIMEOUT:
                // БД отвечала слишком долго - нужно ретраить с экспоненциальной задержкой
            case PRECONDITION_FAILED:
            case CLIENT_DEADLINE_EXCEEDED:
                // запрос был отменен на транспортном уровне, тк закончился установленный
            case SESSION_BUSY:
                // в этот сессии скорей всего исполняется другой запрос, стоит поретраить с новой сессией
            case CLIENT_DISCOVERY_FAILED:
                // ошибка в ходе получения списка эндпоинтов
            case CLIENT_LIMITS_REACHED:
                // достигнут лимит на количество сессий на клиентской стороне
                throw new YdbConditionallyRetryableException(response, statusCode);
            default:
                throw new YdbNonRetryableException(response, statusCode);
        }
    }

    public static void checkGrpcContextStatus(Object response, StatusCode statusCode) throws SQLException {
        if (Context.current().getDeadline() != null && Context.current().getDeadline().isExpired()) {
            // время на обработку запроса закончилось, нужно выбросить отдельное исключение чтобы не было ретраев
            throw new YdbNonRetryableException(DB_QUERY_DEADLINE_EXCEEDED + response, statusCode);
        } else if (Context.current().isCancelled()) {
            // запрос отменил сам клиент, эту ошибку не нужно ретраить
            throw new YdbNonRetryableException(DB_QUERY_CANCELLED + response, statusCode);
        }
    }

    public static void checkGrpcContextStatus(Exception exception) throws SQLException {
        if (Context.current().getDeadline() != null && Context.current().getDeadline().isExpired()) {
            // время на обработку запроса закончилось, нужно выбросить отдельное исключение чтобы не было ретраев
            throw new YdbExecutionException(DB_QUERY_DEADLINE_EXCEEDED + exception.getMessage(), exception);
        } else if (Context.current().isCancelled()) {
            // запрос отменил сам клиент, эту ошибку не нужно ретраить
            throw new YdbExecutionException(DB_QUERY_CANCELLED + exception.getMessage(), exception);
        }
    }

}
