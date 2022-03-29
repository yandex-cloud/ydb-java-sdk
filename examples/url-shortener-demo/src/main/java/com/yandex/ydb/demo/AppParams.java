package com.yandex.ydb.demo;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 *
 * @author Alexandr Gorshenin
 */
public class AppParams {
    private static final int LISTEN_PORT = 9000;
    private static final String ENDPOINT = "localhost:2136";
    private static final String DATABASE = "/local";

    @Parameter(names = { "-p", "--listen-port" }, description = "Listen port", help = true)
    private int listenPort = LISTEN_PORT;

    @Parameter(names = { "-e", "--endpoint" }, description = "YDB endpoint", help = true)
    private String endpoint = ENDPOINT;

    @Parameter(names = { "-d", "--database" }, description = "YDB database name", help = true)
    private String database = DATABASE;

    @Parameter(names = { "-c", "--cert" }, description = "Path to PEM certificate", help = true)
    private String certPath;

    private AppParams() { }

    public static AppParams parseArgs(String[] args) {
        AppParams prms = new AppParams();

        JCommander.newBuilder()
            .addObject(prms)
            .build()
            .parse(args);

        return prms;
    }

    public int listenPort() {
        return this.listenPort;
    }

    public String endpoint() {
        return this.endpoint;
    }

    public String database() {
        return this.database;
    }

    public String certPath() {
        return this.certPath;
    }
}
