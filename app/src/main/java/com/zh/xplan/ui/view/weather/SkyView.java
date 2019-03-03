package com.zh.xplan.ui.view.weather;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zh.xplan.R;
import com.zh.xplan.ui.weather.BaseAnimView;


/**
 * Created by ghbha on 2016/5/15.
 */
public class SkyView extends FrameLayout {

    public SkyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public SkyView(Context context) {
        super(context);
        this.context = context;

    }

    public SkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }


    public void setWeather(String weather, String sunrise, String sunset,boolean isNight) {
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.weather = weather;
        refreshView(isNight);
    }

    public void setWeather(String weather,boolean isNight) {
        this.weather = weather;
        refreshView(isNight);
    }

    private void refreshView(boolean isNight) {

        if (oldWeather.equals(weather)) {
            baseView.reset();
            return;
        }
        oldWeather = weather;

        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View childView = getChildAt(i);
                if (childView instanceof BaseAnimView) {
                    ((BaseAnimView) childView).callStop();
                }
            }
        }

        this.removeAllViews();
        if (baseView != null) {
            baseView = null;
        }

        if (weather.equals("晴")) {
            if (isNight) {
                baseView = new SunnyNightView(context, backGroundColor);
            } else {
                baseView = new SunnyDayView(context, backGroundColor);
            }
            addView(baseView, layoutParams);
            return;
        }
        if (weather.equals("多云")) {
            baseView = new CloudyView(context, backGroundColor);
            addView(baseView, layoutParams);
            return;
        }
        if (weather.contains("雨") || weather.contains("雪")) {
            if (weather.contains("雨") && !weather.contains("雪")) {
                baseView = new RainSnowHazeView(context, RainSnowHazeView.Type.RAIN, backGroundColor);
            } else if (!weather.contains("雨") && weather.contains("雪")) {
                baseView = new RainSnowHazeView(context, RainSnowHazeView.Type.SNOW, backGroundColor);
            } else {
                baseView = new RainSnowHazeView(context, RainSnowHazeView.Type.RAIN_SNOW, backGroundColor);
            }
            addView(baseView, layoutParams);
            return;
        }
        if (weather.equals("霾") || weather.equals("浮尘") || weather.equals("扬沙")) {

            baseView = new RainSnowHazeView(context, RainSnowHazeView.Type.HAZE, backGroundColor);
            addView(baseView, layoutParams);
            return;
        }
        if (weather.contains("阴")) {

            baseView = new CloudyView(context, backGroundColor);
            addView(baseView, layoutParams);
            return;
        }
        if (weather.contains("雾")) {
            baseView = new FogView(context, backGroundColor);
            addView(baseView, layoutParams);
            return;
        }
    }

    public int getBackGroundColor() {
        return backGroundColor;
    }

    /////////////////////////////////////////////////////////////
    LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private String weather, oldWeather = "";
    private String sunrise = "06:00", sunset = "18:00";
    private Context context;
    private BaseAnimView baseView;
    private int backGroundColor = R.color.white;

}
