package com.yandex.ydb.jdbc.impl;

import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialClob;

public class NClobImpl extends SerialClob implements NClob {
    public NClobImpl(char[] ch) throws SQLException {
        super(ch);
    }

    public NClobImpl(Clob clob) throws SQLException {
        super(clob);
    }
}
