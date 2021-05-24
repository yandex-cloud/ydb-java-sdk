package com.yandex.ydb.jdbc;

import ru.yandex.library.svnversion.VcsVersion;

public final class YdbDriverInfo {

    // Driver info
    public static final String DRIVER_NAME = "YDB JDBC Driver";
    public static final int DRIVER_MAJOR_VERSION = 1;
    public static final int DRIVER_MINOR_VERSION;
    public static final String DRIVER_VERSION;
    public static final String DRIVER_FULL_NAME;
    public static final int JDBC_MAJOR_VERSION = 4;
    public static final int JDBC_MINOR_VERSION = 2;

    static {
        VcsVersion version = new VcsVersion(VcsVersion.class);
        DRIVER_MINOR_VERSION = Math.max(0, version.getProgramSvnRevision()); // -1 -> 0
        DRIVER_VERSION = DRIVER_MINOR_VERSION == 0 ? "latest" : ("r" + DRIVER_MINOR_VERSION);
        DRIVER_FULL_NAME = DRIVER_NAME + " " + DRIVER_VERSION;
    }


}
