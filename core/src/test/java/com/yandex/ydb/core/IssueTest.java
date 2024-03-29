package com.yandex.ydb.core;

import com.yandex.ydb.core.Issue.Position;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Sergey Polovko
 */
public class IssueTest {

    @Test
    public void testToString() {
        Issue i1 = Issue.of("message", Issue.Severity.ERROR);
        Assert.assertEquals("message (S_ERROR)", i1.toString());

        Issue i2 = Issue.of("message", Issue.Severity.FATAL);
        Assert.assertEquals("message (S_FATAL)", i2.toString());

        Issue i3 = Issue.of(3, "message", Issue.Severity.WARNING);
        Assert.assertEquals("#3 message (S_WARNING)", i3.toString());

        Issue i4 = Issue.of(Position.of(11, 22), 4, "message", Issue.Severity.WARNING);
        Assert.assertEquals("11:22: #4 message (S_WARNING)", i4.toString());

        Issue i5 = Issue.of(Position.of(11, 22, "file.cpp"), 5, "message", Issue.Severity.WARNING);
        Assert.assertEquals("11:22 at file.cpp: #5 message (S_WARNING)", i5.toString());

        Issue i6 = Issue.of(Position.of(10, 20), Position.of(15, 30), 6, "message", Issue.Severity.INFO);
        Assert.assertEquals("10:20 - 15:30: #6 message (S_INFO)", i6.toString());

        Issue x = Issue.of(Position.EMPTY, Position.EMPTY, 7, "root cause", Issue.Severity.FATAL, i3, i4, i5);
        Assert.assertEquals(
            "#7 root cause (S_FATAL)\n" +
            "  #3 message (S_WARNING)\n" +
            "  11:22: #4 message (S_WARNING)\n" +
            "  11:22 at file.cpp: #5 message (S_WARNING)", x.toString());
    }
}
