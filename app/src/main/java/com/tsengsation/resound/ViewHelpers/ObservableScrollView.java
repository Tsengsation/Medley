package com.tsengsation.resound.ViewHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.tsengsation.resound.Events.OnScrolledListener;

/**
 * Created by jonathantseng on 3/24/15.
 */
public class ObservableScrollView extends ScrollView {

    private OnScrolledListener mScrollViewListener;
    private boolean mInterceptScroll = true;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnScrolled(OnScrolledListener scrollViewListener) {
        this.mScrollViewListener = scrollViewListener;
    }

    public void removeOnScrolled() {
        this.mScrollViewListener = null;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        if (mInterceptScroll) {
            mInterceptScroll = false;
            super.onScrollChanged(x, y, oldx, oldy);
            if (mScrollViewListener != null) {
                mScrollViewListener.onScrolled(y, oldy);
            }
            mInterceptScroll = true;
        }
    }
}
