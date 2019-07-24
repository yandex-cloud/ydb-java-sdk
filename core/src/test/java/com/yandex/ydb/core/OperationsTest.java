package com.yandex.ydb.core;

import com.yandex.ydb.OperationProtos.Operation;
import com.yandex.ydb.StatusCodesProtos.StatusIds;
import com.yandex.ydb.YdbIssueMessage.IssueMessage;
import com.yandex.yql.proto.IssueSeverity.TSeverityIds.ESeverityId;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Sergey Polovko
 */
public class OperationsTest {

    @Test
    public void successWithoutIssues() {
        Status s = Operations.status(Operation.newBuilder()
            .setStatus(StatusIds.StatusCode.SUCCESS)
            .setId("some-id")
            .setReady(true)
            .build());

        assertSame(Status.SUCCESS, s);
        assertEquals(0, s.getIssues().length);
    }

    @Test
    public void successWithIssues() {
        Status s = Operations.status(Operation.newBuilder()
            .setStatus(StatusIds.StatusCode.SUCCESS)
            .setId("some-id")
            .setReady(true)
            .addIssues(IssueMessage.newBuilder()
                .setIssueCode(12345)
                .setSeverity(ESeverityId.S_INFO.getNumber())
                .setMessage("some-issue")
                .build())
            .build());

        assertTrue(s.isSuccess());
        assertArrayEquals(new Issue[]{
            Issue.of(12345, "some-issue", ESeverityId.S_INFO)
        }, s.getIssues());
    }
}
