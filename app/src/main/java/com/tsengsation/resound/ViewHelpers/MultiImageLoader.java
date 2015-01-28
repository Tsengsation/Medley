package com.tsengsation.resound.ViewHelpers;

import android.content.Context;

import com.squareup.picasso.Picasso;
import com.tsengsation.resound.Events.OnImageLoadedListener;

/**
 * Created by jonathantseng on 1/26/15.
 */
public class MultiImageLoader {

    private OnImageLoadedListener mOnImageLoadedListener;
    private Context mContext;

    public MultiImageLoader(Context mContext) {
        this.mContext = mContext;
    }

    public void attachImages(ImageUrlViewPair... pairs) {
        for (ImageUrlViewPair pair : pairs) {
            if (pair.getTransformation() == null) {
                Picasso.with(mContext).load(pair.getUrl()).into(pair.getImageView());
            } else {
                Picasso.with(mContext).load(pair.getUrl()).transform(pair.getTransformation()).into(pair.getImageView());
            }
        }
        if (mOnImageLoadedListener != null) {
            mOnImageLoadedListener.onSuccess();
        }
    }

    public void setOnImageLoaded(OnImageLoadedListener listener) {
        mOnImageLoadedListener = listener;
    }

}
