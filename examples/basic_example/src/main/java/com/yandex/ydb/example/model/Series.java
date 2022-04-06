package com.yandex.ydb.example.model;

import java.time.Instant;


public class Series {
    private final long seriesID;
    private final String title;
    private final Instant releaseDate;
    private final String seriesInfo;

    public Series(long seriesID, String title, Instant releaseDate, String seriesInfo) {
        this.seriesID = seriesID;
        this.title = title;
        this.releaseDate = releaseDate;
        this.seriesInfo = seriesInfo;
    }

    public long seriesID() {
        return seriesID;
    }

    public String title() {
        return title;
    }

    public Instant releaseDate() {
        return releaseDate;
    }

    public String seriesInfo() {
        return seriesInfo;
    }
}
