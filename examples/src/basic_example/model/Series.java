package ru.yandex.ydb.examples.basic_example.model;

import java.time.Instant;


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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Series series = (Series) o;

        if (seriesId != series.seriesId) return false;
        if (!title.equals(series.title)) return false;
        if (!releaseDate.equals(series.releaseDate)) return false;
        return seriesInfo.equals(series.seriesInfo);
    }

    @Override
    public int hashCode() {
        int result = (int) (seriesId ^ (seriesId >>> 32));
        result = 31 * result + title.hashCode();
        result = 31 * result + releaseDate.hashCode();
        result = 31 * result + seriesInfo.hashCode();
        return result;
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
