package com.yandex.ydb.demo.ydb;

import java.util.Objects;

/**
 *
 * @author Alexandr Gorshenin
 */
public class UrlRecord {
    private final String hash;
    private final String url;

    public UrlRecord(String hash, String url) {
        this.hash = hash;
        this.url = url;
    }

    public UrlRecord(String url) {
        this.hash = HashTool.hash(url);
        this.url = url;
    }

    public String url() {
        return this.url;
    }

    public String hash() {
        return this.hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UrlRecord other = (UrlRecord) o;
        return Objects.equals(hash, other.hash)
                && Objects.equals(url, other.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, url);
    }

    @Override
    public String toString() {
        return "UrlRecord{" +
            "url=" + url +
            ", hash=" + hash +
            '}';
    }
}
