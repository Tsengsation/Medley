package com.tsengsation.medley.ViewHelpers;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Utility class for overall view information and conversions between px and dp.
 */
public class ViewCalculator {

    public static double pxToDP(Context context, double px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static double dpToPX(Context context, double dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getWindowHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public ViewCalculator() {}
}
