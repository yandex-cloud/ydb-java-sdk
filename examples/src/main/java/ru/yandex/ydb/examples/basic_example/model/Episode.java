package ru.yandex.ydb.examples.basic_example.model;

import java.time.Instant;


/**
 * @author Sergey Polovko
 */
public class Episode {

    private final long seriesId;
    private final long seasonId;
    private final long episodeId;
    private final String title;
    private final Instant airDate;

    public Episode(long seriesId, long seasonId, long episodeId, String title, Instant airDate) {
        this.seriesId = seriesId;
        this.seasonId = seasonId;
        this.episodeId = episodeId;
        this.title = title;
        this.airDate = airDate;
    }

    public long getSeriesId() {
        return seriesId;
    }

    public long getSeasonId() {
        return seasonId;
    }

    public long getEpisodeId() {
        return episodeId;
    }

    public String getTitle() {
        return title;
    }

    public Instant getAirDate() {
        return airDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode = (Episode) o;

        if (seriesId != episode.seriesId) return false;
        if (seasonId != episode.seasonId) return false;
        if (episodeId != episode.episodeId) return false;
        if (!title.equals(episode.title)) return false;
        return airDate.equals(episode.airDate);
    }

    @Override
    public int hashCode() {
        int result = (int) (seriesId ^ (seriesId >>> 32));
        result = 31 * result + (int) (seasonId ^ (seasonId >>> 32));
        result = 31 * result + (int) (episodeId ^ (episodeId >>> 32));
        result = 31 * result + title.hashCode();
        result = 31 * result + airDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Episode{" +
            "seriesId=" + seriesId +
            ", seasonId=" + seasonId +
            ", episodeId=" + episodeId +
            ", title='" + title + '\'' +
            ", airDate=" + airDate +
            '}';
    }
}
