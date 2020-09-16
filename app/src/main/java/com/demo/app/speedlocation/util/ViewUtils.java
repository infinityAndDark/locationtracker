package com.demo.app.speedlocation.util;

import android.content.res.Resources;

public final class ViewUtils {
    private ViewUtils() {
    }

    public static int dpToPx(float dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
