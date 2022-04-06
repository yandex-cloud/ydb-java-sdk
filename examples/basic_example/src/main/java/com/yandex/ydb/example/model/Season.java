package com.yandex.ydb.example.model;

import java.time.Instant;


public class Season {
    private final long seriesID;
    private final long seasonID;
    private final String title;
    private final Instant firstAired;
    private final Instant lastAired;

    public Season(long seriesID, long seasonID, String title, Instant firstAired, Instant lastAired) {
        this.seriesID = seriesID;
        this.seasonID = seasonID;
        this.title = title;
        this.firstAired = firstAired;
        this.lastAired = lastAired;
    }

    public long seriesID() {
        return this.seriesID;
    }

    public long seasonID() {
        return this.seasonID;
    }

    public String title() {
        return this.title;
    }

    public Instant firstAired() {
        return this.firstAired;
    }

    public Instant lastAired() {
        return this.lastAired;
    }
}
