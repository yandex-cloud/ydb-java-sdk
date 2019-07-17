package ru.yandex.ydb.examples.indexes.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Series {
    private final long seriesId;
    private final String title;
    private final String seriesInfo;
    private final LocalDate releaseDate;
    private final long views;

    @JsonCreator
    public Series(
            @JsonProperty("seriesId") long seriesId,
            @JsonProperty("title") String title,
            @JsonProperty("seriesInfo") String seriesInfo,
            @JsonProperty("releaseDate") LocalDate releaseDate,
            @JsonProperty("views") long views)
    {
        this.seriesId = seriesId;
        this.title = title;
        this.seriesInfo = seriesInfo;
        this.releaseDate = releaseDate;
        this.views = views;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public String getTitle() {
        return title;
    }

    public String getSeriesInfo() {
        return seriesInfo;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public long getViews() {
        return views;
    }

    @Override
    public String toString() {
        return "Series{" +
                "seriesId=" + seriesId +
                ", title='" + title + '\'' +
                ", seriesInfo='" + seriesInfo + '\'' +
                ", releaseDate=" + releaseDate +
                ", views=" + views +
                '}';
    }
}
