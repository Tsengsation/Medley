package com.tsengsation.medley.ViewHelpers;

import android.widget.ImageView;

import com.squareup.picasso.Transformation;

/**
 * Struct to wrap info on image url and view to apply image url to.
 */
public class ImageUrlViewPair {

    public final String url;
    public final ImageView imageView;
    public final Transformation transformation;

    public ImageUrlViewPair(String url, ImageView imageView, Transformation transformation) {
        this.url = url;
        this.imageView = imageView;
        this.transformation = transformation;
    }
}
