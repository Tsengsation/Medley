package com.tsengsation.resound.Parse;

import java.util.Date;

/**
 * Created by jonathantseng on 1/21/15.
 */
public class Article {
    private ArticleType mType;
    private String mImageUrl;
    private Date mDate;
    private String mText;
    private String mTitle;
    private String mSourceName;
    private String mSourceUrl;
    private long mLikes;
    private String mUrl;
    private Author mAuthor;

    public Article(ArticleType mType, String mImageUrl, Date mDate, String mText, String mTitle,
                   String mSourceName, String mSourceUrl, long mLikes, String mUrl, Author mAuthor) {
        this.mType = mType;
        this.mImageUrl = mImageUrl;
        this.mDate = mDate;
        this.mText = mText;
        this.mTitle = mTitle;
        this.mSourceName = mSourceName;
        this.mSourceUrl = mSourceUrl;
        this.mLikes = mLikes;
        this.mUrl = mUrl;
        this.mAuthor = mAuthor;
    }

    public ArticleType getType() {
        return mType;
    }

    public Date getDate() {
        return mDate;
    }

    public String getText() {
        return mText;
    }

    public String getTitle() {
        return mTitle;
    }

    public Author getAuthor() {
        return mAuthor;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getImageSourceName() {
        return mSourceName;
    }

    public String getImageSourceUrl() {
        return mSourceUrl;
    }

    public String getResourUrl() {
        return mUrl;
    }

    public long getNumLikes() {
        return mLikes;
    }
}
