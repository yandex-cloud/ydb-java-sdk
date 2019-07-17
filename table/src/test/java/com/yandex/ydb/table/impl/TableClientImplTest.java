package com.yandex.ydb.table.impl;

import java.util.concurrent.CompletableFuture;

import com.yandex.ydb.core.Operations;
import com.yandex.ydb.core.Result;
import com.yandex.ydb.table.Session;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.TableRpcStub;
import com.yandex.ydb.table.YdbTable;
import com.yandex.ydb.table.YdbTable.CreateSessionResponse;
import com.yandex.ydb.table.YdbTable.CreateSessionResult;
import org.junit.Assert;
import org.junit.Test;

import static java.util.concurrent.CompletableFuture.completedFuture;


/**
 * @author Sergey Polovko
 */
public class TableClientImplTest {

    @Test
    public void createSessionAndRelease() {
        TableClient client = TableClient.newClient(new TableRpcStub() {

            @Override
            public CompletableFuture<Result<CreateSessionResponse>> createSession(
                YdbTable.CreateSessionRequest request, long deadlineAfter)
            {
                CreateSessionResult result = CreateSessionResult.newBuilder()
                    .setSessionId("session1")
                    .build();
                CreateSessionResponse response = CreateSessionResponse.newBuilder()
                    .setOperation(Operations.packResult(result))
                    .build();
                return completedFuture(Result.success(response));
            }
        })
        .build();

        Session session = client.createSession().join().expect("cannot create session");
        Assert.assertFalse(session.release());

        session.close();
        client.close();
    }
}
