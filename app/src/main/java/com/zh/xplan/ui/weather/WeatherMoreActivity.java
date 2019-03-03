package com.zh.xplan.ui.weather;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.zh.xplan.R;
import com.module.common.log.LogUtil;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.view.weather.BaseDrawer;
import com.zh.xplan.ui.view.weather.DynamicWeatherView;
import com.zh.xplan.ui.view.weather.WeekForecastView;
import com.zh.xplan.ui.weather.model.WeatherBeseModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 天气界面
 */
public class WeatherMoreActivity extends AppCompatActivity
        implements View.OnClickListener {
    private final String TAG = getClass().getName();

    //头部布局
    private ImageView header_iv_weather,iv_colse,iv_weather;
    private TextView header_tv_temperature;
    private TextView header_tv_other,tv_city,tv_refresh_time,tv_temperature,tv_pm,tv_pm_str,tv_wind,tv_future_temperature,tv_future_weather;
    private RelativeLayout rootView;
    private  WeatherBeseModel.WeatherBean resultBean;


    private DynamicWeatherView weatherView;
    private WeekForecastView mWeekForecastView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_more);
        StatusBarUtil.setTranslucentForImageView(this,0,null);//状态栏透明
        initViews();
        initDatas();
    }

//    @Override
//    public boolean isSupportSwipeBack() {
//        return false;
//    }

    private void initViews() {
        rootView = (RelativeLayout) findViewById(R.id.rootView);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            findViewById(R.id.rl_base_layout).setPadding(0,0,0,0);
        }
        weatherView = (DynamicWeatherView) findViewById(R.id.main_dynamicweatherview);
        mWeekForecastView = (WeekForecastView) findViewById(R.id.w_dailyForecastView);
        header_iv_weather = (ImageView) findViewById(R.id.header_iv_weather);
        header_tv_temperature = (TextView) findViewById(R.id.header_tv_temperature);
        header_tv_other = (TextView) findViewById(R.id.header_tv_other);
        tv_city  = (TextView) findViewById(R.id.tv_city);
        tv_temperature  = (TextView) findViewById(R.id.tv_temperature);
        tv_pm  = (TextView) findViewById(R.id.tv_pm);
        tv_pm_str  = (TextView) findViewById(R.id.tv_pm_str);
        tv_wind  = (TextView) findViewById(R.id.tv_wind);
        tv_future_temperature  = (TextView) findViewById(R.id.tv_future_temperature);
        tv_future_weather  = (TextView) findViewById(R.id.tv_future_weather);
        iv_weather  = (ImageView) findViewById(R.id.iv_weather);
        tv_refresh_time = (TextView) findViewById(R.id.tv_refresh_time);
        iv_colse = (ImageView) findViewById(R.id.iv_colse);
        iv_colse.setOnClickListener(this);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        colseActivity();
        return true;
    }

    public void initDatas() {

//        skyView.setWeather("晴",true);
        Intent intent = getIntent();
        resultBean = (WeatherBeseModel.WeatherBean) intent.getSerializableExtra("resultBean");
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
            //风
            String wind = resultBean.getWind();
//            20170712201051
            String updateTime = resultBean.getUpdateTime();
            updateTime = getDate(updateTime);
            List<WeatherBeseModel.WeatherBean.FutureBean> future = resultBean.getFuture();

            weatherView.setDrawerType(getWeatherType(weather));

            //赋值
            LogUtil.e(TAG,"temperature:" + temperature);
            if(temperature != null && temperature.endsWith("℃")){
                LogUtil.e(TAG,"temperature.endsWith c:");
                temperature = temperature.replace("℃","");
                char symbol = 176;
                temperature += "" + String.valueOf(symbol);
            }
            header_tv_temperature.setText(temperature);
            tv_city.setText(cityName);
            tv_temperature.setText(temperature);
            header_tv_temperature.setText(temperature);
            header_iv_weather.setImageDrawable(getResources().getDrawable(getWeatherImage(weather)));
            iv_weather.setImageDrawable(getResources().getDrawable(getWeatherImage(weather)));

            tv_pm.setText(pm);
            tv_pm_str.setText(airCondition);
            tv_wind.setText(wind);
            tv_future_weather.setText(weather);
            tv_refresh_time.setText(updateTime + "更新");
            if(future != null && future.size() > 0){
                WeatherBeseModel.WeatherBean.FutureBean futureBean = future.get(0);
                if(TextUtils.isEmpty(wind)){
                    wind = futureBean.getWind().replace("C","").replace(" ","");
                    tv_wind.setText(wind);
                }
                String temperature1 = futureBean.getTemperature().replace("℃","°").replace("C","").replace(" ","");
                tv_future_temperature.setText(temperature1);

                List<WeatherBeseModel.WeatherBean.FutureBean> futureList = new ArrayList();
                if(future != null && future.size() > 1){
                    if(future.size() >= 7){
                        futureList = future.subList(1,7);
                    }else{
                        futureList = future.subList(1,future.size());
                    }
                }
//                mDailyForecastView.setData(futureList);
                mWeekForecastView.setForeCasts(futureList);
            }
        }else{
            weatherView.setDrawerType(getWeatherType(""));
        }
    }

    public static String getDate(String dateStr){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = null;
        String str = "";
        try {
            date = formatter.parse(dateStr);
            SimpleDateFormat formatter1 = new SimpleDateFormat("HH:mm");
            str = formatter1.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }


    @Override
    protected void onResume() {
        super.onResume();
        weatherView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        weatherView.onPause();
    }
    @Override
    protected void onDestroy() {
        if(weatherView != null ){
            weatherView.onDestroy();
            weatherView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_colse:// 添加入网设备
                colseActivity();
                break;
            default:
                break;
        }
    }

    public void colseActivity() {
        final AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                alphaAnimation.cancel();

                finish();
                overridePendingTransition(0,0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        rootView.startAnimation(alphaAnimation);

        int[] temperatureLocation=new int[2];
//        header_tv_temperature.getLocationOnScreen(location);
        header_tv_temperature.getLocationInWindow(temperatureLocation);
        int temperatureX = temperatureLocation[0];//获取当前位置的横坐标
        int temperatureY = temperatureLocation[1];//获取当前位置的纵坐标
        LogUtil.e(TAG,"temperatureX" + temperatureX);
        LogUtil.e(TAG,"temperatureY" + temperatureY);
        int[] temperatureEndLocation = new int[2];
        tv_city.getLocationInWindow(temperatureEndLocation);
        //计算位移
        int temperatureEndX = temperatureEndLocation[0] - temperatureLocation[0];
        int temperatureEndY = temperatureEndLocation[1] - temperatureLocation[1];
        LogUtil.e("temperatureEndX:"+temperatureEndX+" ,temperatureEndY: "+temperatureEndY);
        TranslateAnimation translateAnimation = new TranslateAnimation(0,temperatureEndX,0,temperatureEndY);
        translateAnimation.setDuration(400);
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                header_tv_temperature.setVisibility(View.GONE);
            }
        });

        int[] weatherLocation =new int[2];
        header_iv_weather.getLocationInWindow(weatherLocation);
        int weatherX = weatherLocation[0];//获取当前位置的横坐标
        int weatherY = weatherLocation[1];//获取当前位置的纵坐标
        LogUtil.e(TAG,"weatherX" + weatherX);
        LogUtil.e(TAG,"weatherY" + weatherY);

        int[] weatherEndLocation = new int[2];
        tv_refresh_time.getLocationInWindow(weatherEndLocation);
        //计算位移
        int weatherEndX = weatherEndLocation[0] - weatherLocation[0];
        int weatherEndY = weatherEndLocation[1] - weatherLocation[1];
        LogUtil.e("weatherEndX:"+weatherEndX+" ,weatherEndY: "+weatherEndY);

        Animation translateAnimation2 = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.ABSOLUTE, weatherEndX, TranslateAnimation.RELATIVE_TO_SELF, 0.0f, TranslateAnimation.ABSOLUTE, weatherEndY);// 移动
//        TranslateAnimation translateAnimation2 = new TranslateAnimation(0,endX,0,endY);
        translateAnimation2.setDuration(400);
        translateAnimation2.setFillAfter(true);
        translateAnimation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                header_iv_weather.setVisibility(View.GONE);
            }
        });

        TranslateAnimation translateAnimation3 = new TranslateAnimation(0,0,0,-200);
        translateAnimation3.setDuration(800);
        translateAnimation3.setFillAfter(true);
        translateAnimation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                header_iv_weather.setVisibility(View.GONE);
            }
        });

        AlphaAnimation alphaAnimation1 = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation1.setDuration(300);
        alphaAnimation1.setFillAfter(true);
        alphaAnimation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet animationSet = new AnimationSet(true);
        //设置插值器
        animationSet.setInterpolator(new LinearInterpolator());
        animationSet.addAnimation(translateAnimation3);
        animationSet.addAnimation(alphaAnimation1);

//        rl_weather.bringToFront();
//        weatherView.setBackgroundResource(R.color._black);
        weatherView.startAnimation(alphaAnimation);
        header_tv_temperature.startAnimation(translateAnimation);
        header_iv_weather.startAnimation(translateAnimation2);
        tv_city.startAnimation(animationSet);
        tv_refresh_time.startAnimation(animationSet);
        tv_future_temperature.startAnimation(animationSet);
        tv_future_weather.startAnimation(animationSet);
        tv_pm.startAnimation(animationSet);
        tv_pm_str.startAnimation(animationSet);
        tv_wind.startAnimation(animationSet);
    }

    public int getWeatherImage(String weather){
        int restult = R.drawable.notification_weather_error_small;
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

    public BaseDrawer.Type getWeatherType(String weather){
        BaseDrawer.Type type = BaseDrawer.Type.CLEAR_N;
        switch (weather){
            case "未知":
                type = BaseDrawer.Type.CLEAR_N;
                break;
            case "晴":
                type = BaseDrawer.Type.CLEAR_N;
                break;
            case "阴":
                type = BaseDrawer.Type.CLOUDY_D;
                break;
            case "多云":
                type = BaseDrawer.Type.CLOUDY_D;
                break;
            case "少云":
                type = BaseDrawer.Type.CLOUDY_D;
                break;
            case "晴间多云":
                type = BaseDrawer.Type.CLEAR_N;
                break;
            case "局部多云":
                type = BaseDrawer.Type.CLEAR_N;
                break;
            case "雨":
                type = BaseDrawer.Type.RAIN_D;
                break;
            case "小雨":
                type = BaseDrawer.Type.RAIN_D;
                break;
            case "中雨":
                type = BaseDrawer.Type.RAIN_D;
                break;
            case "大雨":
                type = BaseDrawer.Type.RAIN_D;
                break;
            case "阵雨":
                type = BaseDrawer.Type.RAIN_D;
                break;
            case "雷阵雨":
                type = BaseDrawer.Type.RAIN_D;
                break;
            case "霾":
                type = BaseDrawer.Type.CLEAR_N;
                break;
            case "雾":
                type = BaseDrawer.Type.FOG_D;
                break;
            case "雨夹雪":
                type = BaseDrawer.Type.SNOW_D;
                break;
            case "小雪":
                type = BaseDrawer.Type.SNOW_D;
                break;
            case "中雪":
                type = BaseDrawer.Type.SNOW_D;
                break;
            case "大雪":
                type = BaseDrawer.Type.SNOW_D;
                break;
            default:
                type = BaseDrawer.Type.CLEAR_N;
                break;
        }
        return type;
    }

}
