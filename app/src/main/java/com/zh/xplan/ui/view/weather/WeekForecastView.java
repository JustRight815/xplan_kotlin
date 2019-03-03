package com.zh.xplan.ui.view.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.zh.xplan.XPlanApplication;
import com.module.common.log.LogUtil;
import com.module.common.utils.PixelUtil;
import com.zh.xplan.ui.weather.model.WeatherBeseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghbha on 2016/5/13.
 */
public class WeekForecastView extends View {

    public WeekForecastView(Context context) {
        super(context);
        this.context = context;
    }

    public WeekForecastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public WeekForecastView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = ScreenUtil.getScreenWidth(context);
        height = width - getFitSize(20);
        leftRight = getFitSize(30);
        radius = getFitSize(8);
        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (foreCasts.size() == 0)
            return;

        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(0);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(ScreenUtil.getSp(context, 13));

        drawWeatherDetail(canvas);

    }

    private void drawWeatherDetail(Canvas canvas) {

        float weekPaddingBottom = getFitSize(200);
        float weekInfoPaddingBottom = getFitSize(120);
        float linePaddingBottom = getFitSize(330);
        float tempPaddingTop = getFitSize(20);
        float tempPaddingBottom = getFitSize(45);

        //获取每个天气所占空间
        float lineHigh = getFitSize(320);
        float widthAvg = (width - leftRight) / foreCasts.size();
        float heightAvg = lineHigh / maxMinDelta;

        Matrix matrix = new Matrix();
        matrix.postScale(0.45f, 0.45f); //长和宽放大缩小的比例

        Path pathTempHigh = new Path();
        Path pathTempLow = new Path();

        float paddingLeft = 0;
        int i = 1;
        List<PointF> mPointHs = new ArrayList<>();
        List<PointF> mPointLs = new ArrayList<>();
        for (Data foreCast : foreCasts) {
            paddingLeft = leftRight / 2 + (i - 1 + 0.5f) * widthAvg;
            if (type == TYPE_LINE) {
                if (i == 1) {
                    pathTempHigh.moveTo(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_max - tempL) * heightAvg));
                    pathTempLow.moveTo(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_min - tempL) * heightAvg));
                } else {
                    pathTempHigh.lineTo(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_max - tempL) * heightAvg));
                    pathTempLow.lineTo(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_min - tempL) * heightAvg));
                }
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(getFitSize(2));
                canvas.drawCircle(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_max - tempL) * heightAvg), radius, paint);
                canvas.drawCircle(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_min - tempL) * heightAvg), radius, paint);
            } else {
                PointF pointFH = new PointF(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_max - tempL) * heightAvg) + PixelUtil.dp2px(15,XPlanApplication.getInstance()));
                mPointHs.add(pointFH);
                PointF pointFL = new PointF(paddingLeft, height - (linePaddingBottom + (foreCast.tmp_min - tempL) * heightAvg));
                mPointLs.add(pointFL);
            }

            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawText(foreCast.tmp_max + "°", paddingLeft, height - (linePaddingBottom + tempPaddingTop + (foreCast.tmp_max - tempL) * heightAvg), paint);
//            canvas.drawText(foreCast.tmp_min + "°", paddingLeft, height - (linePaddingBottom - tempPaddingBottom + (foreCast.tmp_min - tempL) * heightAvg), paint);

            canvas.drawText(foreCast.tmp_min + "°", paddingLeft, height - (linePaddingBottom + tempPaddingTop + (foreCast.tmp_max - tempL) * heightAvg) + PixelUtil.dp2px(50,XPlanApplication.getInstance()), paint);
            //星期
            canvas.drawText(foreCast.date, paddingLeft, height - (weekPaddingBottom), paint);

            //天气描述
            canvas.drawText(foreCast.cond_txt_d, paddingLeft, height - (weekInfoPaddingBottom), paint);
            i++;
        }
        paint.setStrokeWidth(getFitSize(3));
        paint.setStyle(Paint.Style.STROKE);

        if (type == TYPE_LINE) {
            canvas.drawPath(pathTempHigh, paint);
            canvas.drawPath(pathTempLow, paint);
        } else {

            drawBezier(canvas, mPointHs, pathTempHigh);
//            drawBezier(canvas, mPointLs, pathTempLow);
        }
    }

    private void drawBezier(Canvas canvas, List<PointF> pointFs, Path path) {
        path.reset();
        for (int i = 0; i < pointFs.size(); i++) {
            PointF pointCur = pointFs.get(i);
            PointF pointPre = null;
            if (i > 0)
                pointPre = pointFs.get(i - 1);
            if (i == 0) {
                path.moveTo(pointCur.x, pointCur.y );
            } else {
                path.quadTo(pointPre.x, pointPre.y, (pointPre.x + pointCur.x) / 2, (pointPre.y + pointCur.y) / 2);
            }
        }
        path.lineTo(pointFs.get(pointFs.size() - 1).x, pointFs.get(pointFs.size() - 1).y );

        canvas.drawPath(path, paint);
    }


    private int getMaxMinDelta() {
        if (foreCasts.size() > 0) {
            tempH = foreCasts.get(0).tmp_max;
            tempL = foreCasts.get(0).tmp_min;
            for (Data weekForeCast : foreCasts) {
                if (weekForeCast.tmp_max > tempH) {
                    tempH = weekForeCast.tmp_max;
                }
                if (weekForeCast.tmp_min < tempL) {
                    tempL = weekForeCast.tmp_min;
                }
            }
            return tempH - tempL;
        }
        return 0;
    }


    public void setForeCasts(List<WeatherBeseModel.WeatherBean.FutureBean> futureList) {
        if(futureList==null){
            return ;
        }

        if(this.futureList == futureList){
            maxMinDelta = getMaxMinDelta();
            invalidate();
            return ;
        }
        this.futureList = futureList;
        if (futureList == null && futureList.size() == 0) {
            return;
        }
        // this.points = new PointF[forecastList.size()];
        LogUtil.e(TAG,"futureList.size:" + futureList.size() + "");
        foreCasts.clear();
        try {
            int all_max = Integer.MIN_VALUE;
            int all_min = Integer.MAX_VALUE;
            for (int i = 0; i < futureList.size(); i++) {
                WeatherBeseModel.WeatherBean.FutureBean futureBean = futureList.get(i);
                String temp = futureBean.getTemperature();
                LogUtil.e(TAG,"temp :" + temp + "");
                temp = temp.replace("℃","").replace("°C","").replace(" ","");
                if(TextUtils.isEmpty(temp)){
//					LogUtil.e(TAG,"continue :" +  "");
                    continue;
                }
                String[] tempSplit = temp.split("/");
                String tempMax = "0";
                String tempMin = "0";
//				LogUtil.e(TAG,"tempSplit.length :" + tempSplit.length + "");
                if(tempSplit.length == 2){
                    tempMax = tempSplit[0];
                    tempMin = tempSplit[1];
//					LogUtil.e(TAG,"tempMax :" + tempMax + "");
//					LogUtil.e(TAG,"tempMin :" + tempMin + "");
                }
                if(tempSplit.length == 1){
                    tempMin = tempSplit[0];
//					LogUtil.e(TAG,"tempMin :" + tempMin + "");
                }
                if(tempSplit.length == 0){
                    continue;
                }
                int max = Integer.valueOf(tempMax);
                int min = Integer.valueOf(tempMin);
                if (all_max < max) {
                    all_max = max;
                }
                if (all_min > min) {
                    all_min = min;
                }
                final Data data = new Data();
                data.tmp_max = max;
                data.tmp_min = min;
                if(i == 0){
                    data.date = "明天";
                }else{
                    data.date = futureBean.getWeek();
                }
                data.wind_sc = futureBean.getWind();
                data.cond_txt_d = futureBean.getDayTime();

                float all_distance = Math.abs(all_max - all_min);
                float average_distance = (all_max + all_min) / 2f;
//		toast("all->" + all_distance + " aver->" + average_distance);
                data.maxOffsetPercent = (data.tmp_max - average_distance) / all_distance;
                data.minOffsetPercent = (data.tmp_min - average_distance) / all_distance;

                foreCasts.add(data);
            }
        } catch (Exception e) {
            LogUtil.e(TAG,"Exception :" + e.toString() + "");
            e.printStackTrace();
        }
        maxMinDelta = getMaxMinDelta();
        this.invalidate();
    }

    private float getFitSize(float orgSize) {
        return orgSize * width * 1.0f / 1080;
    }


    private final static String TAG = "ForeCastView";
    private float height, width;
    private Paint paint = new Paint();
    private Context context;
    private List<Data> foreCasts = new ArrayList<>();
    private float maxMinDelta;
    private int tempH, tempL;
    private float radius = 0;
    private float leftRight;

    private static final int TYPE_LINE = 0;
    private static final int TYPE_BESSEL = 1;
    private int type = TYPE_BESSEL;
    List<WeatherBeseModel.WeatherBean.FutureBean> futureList;

    public class Data {
        public float minOffsetPercent, maxOffsetPercent;// 差值%
        public int tmp_max, tmp_min;
        public String date;
        public String wind_sc;
        public String cond_txt_d;
    }
}
