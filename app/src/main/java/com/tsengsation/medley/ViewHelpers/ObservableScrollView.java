package com.tsengsation.medley.ViewHelpers;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * ScrollView that supports callbacks when scroll events and fling events happen.
 */
public class ObservableScrollView extends ScrollView {

    private OnScrolledListener mScrollViewListener;
    private OnFlingListener mFlingListener;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Prevents scrolling due to focus changes.
    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return true;
    }

    public void setOnScrolled(OnScrolledListener scrollViewListener) {
        this.mScrollViewListener = scrollViewListener;
    }

    public void setOnFlung(OnFlingListener flingListener) {
        this.mFlingListener = flingListener;
    }

    public void removeOnScrolled() {
        this.mScrollViewListener = null;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
        if (mFlingListener != null) {
            mFlingListener.onFlung(this, velocityY);
        }
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (mScrollViewListener != null) {
            mScrollViewListener.onScrolled(this, y, oldy);
        }
    }

    /**
     * Listener for scroll event occurrences.
     */
    public static interface OnScrolledListener {

        void onScrolled(ObservableScrollView view, int oldY, int newY);
    }

    /**
     * Listener for fling event occurrences.
     */
    public static interface OnFlingListener {

        public void onFlung(ObservableScrollView view, int velocityY);
    }
}
