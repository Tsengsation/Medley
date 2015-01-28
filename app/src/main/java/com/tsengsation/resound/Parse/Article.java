package com.tsengsation.resound.Parse;

/**
 * Created by jonathantseng on 1/21/15.
 */
public class Article {
    private ArticleType mType;
    private String mImageUrl;
    private String mDate;
    private String mText;
    private String mTitle;
    private Author mAuthor;

    public Article(ArticleType mType, String mImageUrl, String mDate,
                   String mText, String mTitle, Author mAuthor) {
        this.mType = mType;
        this.mImageUrl = mImageUrl;
        this.mDate = mDate;
        this.mText = mText;
        this.mTitle = mTitle;
        this.mAuthor = mAuthor;
    }

    public ArticleType getType() {
        return mType;
    }

    public String getDate() {
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
}
