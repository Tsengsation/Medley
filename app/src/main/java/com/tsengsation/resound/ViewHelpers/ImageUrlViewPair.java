package com.tsengsation.resound.ViewHelpers;

import android.widget.ImageView;

import com.squareup.picasso.Transformation;

/**
 * Created by jonathantseng on 1/27/15.
 */
public class ImageUrlViewPair {

    private String mUrl;
    private ImageView mImageView;
    private Transformation mTransformation;

    public ImageUrlViewPair(String mUrl, ImageView mImageView) {
        this.mUrl = mUrl;
        this.mImageView = mImageView;
        this.mTransformation = null;
    }

    public ImageUrlViewPair(String mUrl, ImageView mImageView, Transformation mTransformation) {
        this.mUrl = mUrl;
        this.mImageView = mImageView;
        this.mTransformation = mTransformation;
    }

    public Transformation getTransformation() {
        return mTransformation;
    }

    public String getUrl() {
        return mUrl;
    }

    public ImageView getImageView() {
        return mImageView;
    }

}
