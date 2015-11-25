package com.tsengsation.medley.Parse;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

/**
 * Struct for article info.
 */
public class Article implements Parcelable, Serializable {
    private static final String FEATURED = "featured";
    private static final String NEWS = "news";
    private static final String ALBUMS = "albums";
    private static final String CONCERTS = "concerts";
    private static final String PLAYLISTS = "playlists";

    public static final int TYPE_FEATURED = 1;
    public static final int TYPE_NEWS = 2;
    public static final int TYPE_ALBUMS = 3;
    public static final int TYPE_CONCERTS = 4;
    public static final int TYPE_PLAYLISTS = 5;

    public final String id;
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
    public final boolean prevLiked;

    public Article(String id, String type, String imageUrl, Date date, String text, String title,
                   String sourceName, String sourceUrl, long numLikes, String url, Author author,
                   boolean prevLiked) {
        this.id = id;
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
        this.prevLiked = prevLiked;
    }

    public Article(String id, int type, String imageUrl, Date date, String text, String title,
                   String sourceName, String sourceUrl, long numLikes, String url, Author author,
                   boolean prevLiked) {
        this.id = id;
        this.type = type;
        this.imageUrl = imageUrl;
        this.date = date;
        this.text = text;
        this.title = title;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.numLikes = numLikes;
        this.url = url;
        this.author = author;
        this.prevLiked = prevLiked;
    }

    protected Article(Parcel in) {
        id = in.readString();
        type = in.readInt();
        imageUrl = in.readString();
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
        text = in.readString();
        title = in.readString();
        sourceName = in.readString();
        sourceUrl = in.readString();
        numLikes = in.readLong();
        url = in.readString();
        author = (Author) in.readValue(Author.class.getClassLoader());
        prevLiked = in.readByte() != 0x00;
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
                return TYPE_PLAYLISTS;
            case FEATURED:
            default:
                return TYPE_FEATURED;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(type);
        dest.writeString(imageUrl);
        dest.writeLong(date != null ? date.getTime() : -1L);
        dest.writeString(text);
        dest.writeString(title);
        dest.writeString(sourceName);
        dest.writeString(sourceUrl);
        dest.writeLong(numLikes);
        dest.writeString(url);
        dest.writeValue(author);
        dest.writeByte((byte) (prevLiked ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}