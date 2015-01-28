package com.tsengsation.resound.Parse;

/**
 * Created by jonathantseng on 1/21/15.
 */
public enum ArticleType {
    NEWS("News"), ALBUMS("Albums"), CONCERTS("Concerts"), PLAYLISTS("Playlists");

    public static ArticleType createType(String type) {
        switch (type.toLowerCase()) {
            case "news": return NEWS;
            case "albums": return ALBUMS;
            case "concerts": return CONCERTS;
            default: return PLAYLISTS;
        }
    }

    private String mName;

    private ArticleType(String name) {
        mName = name;
    }

    public String toString() {
        return mName;
    }
}
