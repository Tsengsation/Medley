package com.tsengsation.resound.ViewHelpers;

import android.content.Context;

import com.squareup.picasso.Picasso;

/**
 * Image loader that loads multiple images into views. Supports callback on all images loaded.
 */
public class MultiImageLoader {

    private OnImageLoadedListener mOnImageLoadedListener;
    private Context mContext;

    public MultiImageLoader(Context mContext) {
        this.mContext = mContext;
    }

    public void attachImages(ImageUrlViewPair... pairs) {
        for (ImageUrlViewPair pair : pairs) {
            if (pair.transformation == null) {
                Picasso.with(mContext)
                        .load(pair.url)
                        .into(pair.imageView);
            } else {
                Picasso.with(mContext)
                        .load(pair.url)
                        .transform(pair.transformation)
                        .into(pair.imageView);
            }
        }
        if (mOnImageLoadedListener != null) {
            mOnImageLoadedListener.onSuccess();
        }
    }

    public void setOnImageLoaded(OnImageLoadedListener listener) {
        mOnImageLoadedListener = listener;
    }

    /**
     * Listener for parse image loading completions.
     */
    public static interface OnImageLoadedListener {

        public void onSuccess();
    }
}
