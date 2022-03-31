package com.yandex.ydb.examples.basic_example.model;

import java.time.Instant;
import java.util.Objects;


/**
 * @author Sergey Polovko
 */
public class Series {

    private final long seriesId;
    private final String title;
    private final Instant releaseDate;
    private final String seriesInfo;

    public Series(long seriesId, String title, Instant releaseDate, String seriesInfo) {
        this.seriesId = seriesId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.seriesInfo = seriesInfo;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public String getTitle() {
        return title;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public String getSeriesInfo() {
        return seriesInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Series series = (Series) o;

        return Objects.equals(seriesId, series.seriesId)
                && Objects.equals(title, series.title)
                && Objects.equals(releaseDate, series.releaseDate)
                && Objects.equals(seriesInfo, series.seriesInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seriesId, title, releaseDate, seriesInfo);
    }

    @Override
    public String toString() {
        return "Series{" +
            "seriesId=" + seriesId +
            ", title='" + title + '\'' +
            ", releaseDate=" + releaseDate +
            ", seriesInfo='" + seriesInfo + '\'' +
            '}';
    }
}
