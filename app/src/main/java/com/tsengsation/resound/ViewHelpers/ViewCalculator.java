package com.tsengsation.resound.ViewHelpers;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by jonathantseng on 1/29/15.
 */
public class ViewCalculator {

    private static ViewCalculator mInstance;
    private static Context mContext;

    private ViewCalculator(Context context) {
        mInstance = this;
        mContext = context;
    }

    public synchronized static ViewCalculator getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ViewCalculator(context);
        }
        return mInstance;
    }

    public static double pxToDP(double px) {
        return mContext.getResources().getDisplayMetrics().density * px;
    }

    public static double dpToPX(double dp) {
        return dp / (mContext.getResources().getDisplayMetrics().density);
    }

    public static int getWindowHeight() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

}
