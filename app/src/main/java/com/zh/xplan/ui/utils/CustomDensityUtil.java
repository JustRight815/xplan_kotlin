package com.zh.xplan.ui.utils;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * 今日头条适配方案
 * Created by zh on 2018/6/30.
 */
public class CustomDensityUtil {
    private static final float targetPixels = 360F;
    private static float targetDensity;
    private static float targetScaleDensity;
    private static int targetDensityDpi;

    /**
     * 今日头条的屏幕适配方案
     * 通过修改density值，强行把所有不同尺寸分辨率的手机的宽度dp值改成一个统一的值，这样就解决了所有的适配问题
     * @param activity
     * @param application
     */
    public static void setCustomDensity(@NonNull Activity activity,@NonNull Application application) {
        if(targetDensity == 0){
            setApplicationDensity(application);
        }
        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetScaleDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }

    public static void setApplicationDensity(@NonNull Application application) {
        DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        float sNoncompatDensity = appDisplayMetrics.density;
        float sNoncompatScaledDensity = appDisplayMetrics.scaledDensity;
        targetDensity = appDisplayMetrics.widthPixels / targetPixels;
        targetScaleDensity = targetDensity * (sNoncompatScaledDensity / sNoncompatDensity);
        targetDensityDpi = (int) (160 * targetDensity);

        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaleDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;
    }
}
