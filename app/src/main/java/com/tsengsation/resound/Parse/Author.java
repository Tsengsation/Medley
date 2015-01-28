package com.tsengsation.resound.Parse;

/**
 * Created by jonathantseng on 1/21/15.
 */
public class Author {

    private String mImageUrl;
    private String mName;

    public Author(String mImageUrl, String mName) {
        this.mImageUrl = mImageUrl;
        this.mName = mName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getName() {
        return mName;
    }

}
