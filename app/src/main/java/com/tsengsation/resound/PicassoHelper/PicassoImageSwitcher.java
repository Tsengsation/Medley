package com.tsengsation.resound.PicassoHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageSwitcher;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by jonathantseng on 1/29/15.
 */
public class PicassoImageSwitcher implements Target {

    private ImageSwitcher mImageSwitcher;
    private Context mContext;

    public PicassoImageSwitcher(Context context, ImageSwitcher imageSwitcher){
        mImageSwitcher = imageSwitcher;
        mContext = context;
    }

    public ImageSwitcher getImageSwitcher() {
        return mImageSwitcher;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        mImageSwitcher.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {

    }

    @Override
    public void onPrepareLoad(Drawable drawable) {

    }

}