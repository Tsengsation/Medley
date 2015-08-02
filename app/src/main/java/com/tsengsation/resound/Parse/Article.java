package com.tsengsation.resound.Parse;

import java.util.Date;

/**
 * Struct for article info.
 */
public class Article {
    private final String NEWS = "news";
    private final String ALBUMS = "albums";
    private final String CONCERTS = "concerts";
    private final String PLAYLISTS = "playlists";

    public final int TYPE_NEWS = 1;
    public final int TYPE_ALBUMS = 2;
    public final int TYPE_CONCERTS = 3;
    public final int TYPE_PLAYLISTS = 4;

    public final int type;
    public final String imageUrl;
    public final Date date;
    public final String text;
    public final String title;
    public final String sourceName;
    public final String sourceUrl;
    public final long numLikes;
    public final String url;
    public final Author author;

    public Article(String type, String imageUrl, Date date, String text, String title,
                   String sourceName, String sourceUrl, long numLikes, String url, Author author) {
        this.type = typeToInt(type);
        this.imageUrl = imageUrl;
        this.date = date;
        this.text = text;
        this.title = title;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.numLikes = numLikes;
        this.url = url;
        this.author = author;
    }

    private int typeToInt(String type) {
        switch (type.toLowerCase()) {
            case NEWS:
                return TYPE_NEWS;
            case ALBUMS:
                return TYPE_ALBUMS;
            case CONCERTS:
                return TYPE_CONCERTS;
            case PLAYLISTS:
            default:
                return TYPE_PLAYLISTS;
        }
    }
}
