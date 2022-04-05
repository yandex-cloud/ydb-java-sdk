package com.yandex.ydb.example.model;

import java.time.Instant;


public class Episode {
    private final long seriesID;
    private final long seasonID;
    private final long episodeID;
    private final String title;
    private final Instant airDate;

    public Episode(long seriesID, long seasonID, long episodeID, String title, Instant airDate) {
        this.seriesID = seriesID;
        this.seasonID = seasonID;
        this.episodeID = episodeID;
        this.title = title;
        this.airDate = airDate;
    }

    public long seriesID() {
        return seriesID;
    }

    public long seasonID() {
        return seasonID;
    }

    public long episodeID() {
        return episodeID;
    }

    public String title() {
        return title;
    }

    public Instant airDate() {
        return airDate;
    }
}
