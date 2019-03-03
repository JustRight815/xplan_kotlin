package com.zh.xplan.ui.menusetting;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.module.common.image.ImageLoader;
import com.module.common.log.LogUtil;
import com.module.common.net.rx.NetManager;
import com.module.common.utils.CleanCacheUtils;
import com.zh.xplan.AppConstants;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BasePresenter;
import com.zh.xplan.ui.weather.model.WeatherBeseModel;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Created by zh on 2017/12/6.
 */
public class SettingFragmentPresenter extends BasePresenter<SettingFragmentView> {

    public SettingFragmentPresenter(){
    }

    @Override
    public void onDestory() {
        detachView();
    }

    /**
     * 根据城市获得天气信息
     * @param provinceName
     * @param cityName
     */
    public void getCityWeather(String provinceName, String cityName) {
        provinceName = "";
        if(cityName == null || TextUtils.isEmpty(cityName)){
            cityName = "北京";
        }
        DisposableObserver disposableObserver = NetManager.get()
                .url(AppConstants.URL_Mob + "v1/weather/query")
                .params("key", AppConstants.URL_APP_Key)
                .params("city",cityName)
                .params("province",provinceName)
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String response) {
                        WeatherBeseModel weatherBeseModel = new Gson().fromJson(response,WeatherBeseModel.class);
                        if (weatherBeseModel != null && weatherBeseModel.getMsg().equals("success")) {
                            List<WeatherBeseModel.WeatherBean> weathers = weatherBeseModel.getResult();
                            if (weathers.size() > 0) {
                                WeatherBeseModel.WeatherBean resultBean = weathers.get(0);
                                if (resultBean != null) {
                                    //当前温度
                                    String temperature = resultBean.getTemperature();
                                    String pm = resultBean.getPollutionIndex();
                                    //空气
                                    String airCondition = resultBean.getAirCondition();
                                    //天气
                                    String weather = resultBean.getWeather();
                                    //城市
                                    String cityName = resultBean.getCity();
                                    //赋值
                                    LogUtil.e(TAG,"temperature:" + temperature);
                                    if(temperature != null && temperature.endsWith("℃")){
                                        LogUtil.e(TAG,"temperature.endsWith c:");
                                        temperature = temperature.replace("℃","");
                                        char symbol = 176;
                                        temperature += "" + String.valueOf(symbol);
                                    }
                                    if(view != null){
                                        view.updateCityWeather(resultBean,temperature,pm,getPmBg(Integer.parseInt(pm)),
                                                airCondition,cityName,weather,getWeatherImage(weather));
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDisposable(disposableObserver);
    }

    public int getWeatherImage(String weather){
        int restult = 0;
        switch (weather){
            case "未知":
                restult = R.drawable.notification_weather_error_small;
                break;
            case "晴":
                restult = R.drawable.notification_weather_sunny_small;
                break;
            case "阴":
                restult = R.drawable.notification_weather_mostly_cloudy_small;
                break;
            case "多云":
                restult = R.drawable.notification_weather_cloudy_small;
                break;
            case "少云":
                restult = R.drawable.notification_weather_cloudy_small;
                break;
            case "晴间多云":
                restult = R.drawable.notification_weather_cloudy_small;
                break;
            case "局部多云":
                restult = R.drawable.notification_weather_cloudy_small;
                break;
            case "雨":
                restult = R.drawable.notification_weather_drizzle_small;
                break;
            case "小雨":
                restult = R.drawable.notification_weather_drizzle_small;
                break;
            case "中雨":
                restult = R.drawable.notification_weather_heavy_rain_small;
                break;
            case "大雨":
                restult = R.drawable.notification_weather_heavy_rain_small;
                break;
            case "阵雨":
                restult = R.drawable.notification_weather_thunderstorms_small;
                break;
            case "雷阵雨":
                restult = R.drawable.notification_weather_thunderstorms_small;
                break;
            case "霾":
                restult = R.drawable.notification_weather_fog_small;
                break;
            case "雾":
                restult = R.drawable.notification_weather_fog_small;
                break;
            case "雨夹雪":
                restult = R.drawable.notification_weather_sleet_small;
                break;
            default:
                restult = R.drawable.notification_weather_error_small;
                break;
        }
        return restult;
    }

    public int getPmBg(int weather){
        LogUtil.e(TAG,"getPmBg: " + weather +"");
        int restult = R.drawable.weather_shape1;
        if(0 <= weather && weather <= 50 ){
            restult = R.drawable.weather_shape1;
        }else if(51 <= weather && weather <= 100 ){
            restult = R.drawable.weather_shape2;
        }else if(101 <= weather && weather <= 150 ){
            restult = R.drawable.weather_shape3;
        }else if(151 <= weather && weather <= 200 ){
            restult = R.drawable.weather_shape4;
        }else if(201 <= weather && weather <= 300 ){
            restult = R.drawable.weather_shape5;
        }else if(301 <= weather ){
            restult = R.drawable.weather_shape6;
        }
        return restult;
    }


    public void getCacheSize(){
        DisposableObserver disposableObserver = Observable.create(new ObservableOnSubscribe<String>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                long bDatabasesSize = 0;
                long wDatabasesSize = 0;
//                bDatabasesSize = CleanCacheUtils.getInternalFileSize(new File(
//                        "/data/data/" + BaseLib.getContext().getPackageName()
//                                + "/databases"));
//                wDatabasesSize = CleanCacheUtils.getExternalFileSize(new File(
//                        "/mnt/sdcard/android/data/"
//                                + BaseLib.getContext().getPackageName() + "/databases"));
                String cacheSize = CleanCacheUtils.getFormatSize(bDatabasesSize
                        + wDatabasesSize + ImageLoader.getAllCacheSize()) ;
                e.onNext(cacheSize);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        if(view != null){
                            view.updateCacheSize(s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDisposable(disposableObserver);
    }

    /**
     * 清除缓存
     * @return
     */
    public void clearCache() {
        DisposableObserver disposableObserver = Observable.create(new ObservableOnSubscribe<String>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                ImageLoader.clearAllCaches();
//                CleanCacheUtils.cleanDatabases(BaseLib.getContext());
//                CleanCacheUtils.cleanExternalDatabases(BaseLib.getContext());
                Thread.sleep(500);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {

                    @Override
                    public void onNext(String s) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getCacheSize();
                    }

                    @Override
                    public void onComplete() {
                        getCacheSize();
                    }
                });
        addDisposable(disposableObserver);
    }

    /**
     * 根据传入的uniqueName获取硬盘缓存的路径地址。
     */
    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
}
