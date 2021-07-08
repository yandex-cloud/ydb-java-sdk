package com.yandex.ydb.jdbc.impl;

import java.util.Optional;

import com.yandex.ydb.core.utils.Version;

public class YdbVersionCollector {

    public static final String LATEST_VERSION = "1.0.latest";

    private YdbVersionCollector() {
        //
    }

    public static VersionInfo lookupVersion() {
        // TODO: support yandexBuild ?
        return universalBuild()
                .orElseGet(() -> new VersionInfo(1, 0, LATEST_VERSION));
    }
    private static Optional<VersionInfo> universalBuild() {
            return Version.getVersion()
                    .map(version -> {
                        String[] parts = version.split("\\.", 3);
                        if (parts.length == 3) {
                            return new VersionInfo(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), version);
                        } else {
                            return null;
                        }
                    });
    }


    public static class VersionInfo {
        private final int major;
        private final int minor;
        private final String full;

        private VersionInfo(int major, int minor, String full) {
            this.major = major;
            this.minor = minor;
            this.full = full;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public String getFull() {
            return full;
        }
    }
}
