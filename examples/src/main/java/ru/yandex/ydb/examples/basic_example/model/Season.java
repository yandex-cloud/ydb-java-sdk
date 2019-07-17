package ru.yandex.ydb.examples.basic_example.model;

import java.time.Instant;


/**
 * @author Sergey Polovko
 */
public class Season {

    private final long seriesId;
    private final long seasonId;
    private final String title;
    private final Instant firstAired;
    private final Instant lastAired;

    public Season(long seriesId, long seasonId, String title, Instant firstAired, Instant lastAired) {
        this.seriesId = seriesId;
        this.seasonId = seasonId;
        this.title = title;
        this.firstAired = firstAired;
        this.lastAired = lastAired;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public long getSeasonId() {
        return seasonId;
    }

    public String getTitle() {
        return title;
    }

    public Instant getFirstAired() {
        return firstAired;
    }

    public Instant getLastAired() {
        return lastAired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Season season = (Season) o;

        if (seriesId != season.seriesId) return false;
        if (seasonId != season.seasonId) return false;
        if (!title.equals(season.title)) return false;
        if (!firstAired.equals(season.firstAired)) return false;
        return lastAired.equals(season.lastAired);
    }

    @Override
    public int hashCode() {
        int result = (int) (seriesId ^ (seriesId >>> 32));
        result = 31 * result + (int) (seasonId ^ (seasonId >>> 32));
        result = 31 * result + title.hashCode();
        result = 31 * result + firstAired.hashCode();
        result = 31 * result + lastAired.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Season{" +
            "seriesId=" + seriesId +
            ", seasonId=" + seasonId +
            ", title='" + title + '\'' +
            ", firstAired=" + firstAired +
            ", lastAired=" + lastAired +
            '}';
    }
}
