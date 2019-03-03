package com.zh.xplan.ui.menusetting;

import android.support.annotation.DrawableRes;

import com.zh.xplan.ui.base.BaseView;
import com.zh.xplan.ui.weather.model.WeatherBeseModel;

/**
 * Created by zh on 2017/12/6.
 */

public interface SettingFragmentView extends BaseView {

    void updateCityWeather(WeatherBeseModel.WeatherBean weatherBean,String temperature, String pm, @DrawableRes int resid, String airCondition, String cityName, String weather,@DrawableRes int weatherRes);
    void updateCacheSize(String cacheSize);
}
