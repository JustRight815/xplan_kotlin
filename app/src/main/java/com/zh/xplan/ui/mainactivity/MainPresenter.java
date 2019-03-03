package com.zh.xplan.ui.mainactivity;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.module.common.BaseLib;
import com.module.common.log.LogUtil;
import com.module.common.net.callback.IDownLoadCallback;
import com.module.common.net.rx.NetManager;
import com.module.common.utils.SpUtil;
import com.zh.xplan.AppConstants;
import com.zh.xplan.ui.base.BasePresenter;
import com.zh.xplan.ui.indexactivity.model.AdModel;
import com.zh.xplan.ui.utils.FileUtils;
import com.zh.xplan.ui.weather.model.WeatherBeseModel;

import java.io.File;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zh on 2017/12/6.
 */
public class MainPresenter extends BasePresenter<MainView> {

    public MainPresenter(){
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
                        if (weatherBeseModel != null) {
                            if (weatherBeseModel.getMsg().equals("success")) {
                                List<WeatherBeseModel.WeatherBean> weathers = weatherBeseModel.getResult();
                                if (weathers.size() > 0) {
                                    WeatherBeseModel.WeatherBean resultBean = weathers.get(0);
                                    if (resultBean != null) {
                                        //当前温度
                                        String temperature = resultBean.getTemperature();
                                        //空气
                                        String airCondition = resultBean.getAirCondition();
                                        //天气
                                        String weather = resultBean.getWeather();
                                        //城市
                                        String cityName = resultBean.getCity();
                                        //赋值
                                        if(temperature != null){
                                            temperature = temperature.replace("℃","").replace("C","").replace(" ","");
                                        }
                                        if(view != null){
                                            view.showCityWeather(cityName,temperature);
                                        }
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

    /**
     * 更新启动页广告图片
     */
    public void updateAdPicture() {
        DisposableObserver disposableObserver =
                NetManager.get()
                .url(AppConstants.SPLASH_URL)
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {

                    @Override
                    public void onNext(String response) {
                        if(TextUtils.isEmpty(response)){
                            return;
                        }
                        try{
                            AdModel adModel = new Gson().fromJson(response,AdModel.class);
                            if (adModel == null || TextUtils.isEmpty(adModel.getUrl())) {
                                LogUtil.e("zh","response == null" );
                                return;
                            }
                            final String url = adModel.getUrl();
                            LogUtil.e("zh","response != null url " + url);
                            String lastImgUrl =  SpUtil.getFromLocal(BaseLib.getContext(),"Splash","splash_url","");
                            if (url == null || TextUtils.isEmpty(url) || TextUtils.equals(lastImgUrl, url)) {
                                return;
                            }
                            //下载新的图片
                            NetManager.download(url,
                                    null,
                                    FileUtils.getSplashDir(),
                                    null,
                                    "splash", new IDownLoadCallback() {
                                        @Override
                                        public void onFinish(File downloadFile) {
                                            LogUtil.e("zh","download onFinish , url) " );
                                            SpUtil.saveToLocal(BaseLib.getContext(),"Splash","splash_url",url);
                                        }

                                        @Override
                                        public void onProgress(long currentBytes, long totalBytes) {

                                        }

                                        @Override
                                        public void onFailure(String error_msg) {
                                            LogUtil.e("zh","download onFailure , url) " );
                                        }
                                    });
                        }catch (Exception e){
                            e.printStackTrace();
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

}
