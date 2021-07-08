package com.yandex.ydb.jdbc;

import com.yandex.ydb.jdbc.impl.YdbVersionCollector;
import com.yandex.ydb.jdbc.impl.YdbVersionCollector.VersionInfo;

public final class YdbDriverInfo {

    // Driver info
    public static final String DRIVER_NAME = "YDB JDBC Driver";
    public static final int DRIVER_MAJOR_VERSION;
    public static final int DRIVER_MINOR_VERSION;
    public static final String DRIVER_VERSION;
    public static final String DRIVER_FULL_NAME;
    public static final int JDBC_MAJOR_VERSION = 4;
    public static final int JDBC_MINOR_VERSION = 2;

    static {
        VersionInfo versionInfo = YdbVersionCollector.lookupVersion();
        DRIVER_MAJOR_VERSION = versionInfo.getMajor();
        DRIVER_MINOR_VERSION = versionInfo.getMinor();
        DRIVER_VERSION = versionInfo.getFull();
        DRIVER_FULL_NAME = DRIVER_NAME + " " + DRIVER_VERSION;
    }

}
