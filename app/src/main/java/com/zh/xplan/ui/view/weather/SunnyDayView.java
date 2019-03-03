package com.zh.xplan.ui.view.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;

import com.zh.xplan.ui.weather.BaseAnimView;


/**
 * Created by ghbha on 2016/5/16.
 */
public class SunnyDayView extends BaseAnimView {

    public SunnyDayView(Context context,int backColor) {
        super(context,backColor);
    }

    @Override
    protected void init() {
        super.init();
        paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
    }

    @Override
    protected void drawSub(Canvas canvas) {
        if (radius > MAX) {
            deltaRadius = -deltaRadius;
        }
        if (radius < MIN) {
            deltaRadius = -deltaRadius;
        }

        RectF rect1 = new RectF(-radius, -radius, radius, radius);
        RectF rect2 = new RectF(-(radius + addRadius), -(radius + addRadius), (radius + addRadius), (radius + addRadius));
        RectF rect3 = new RectF(-(radius + 2 * addRadius), -(radius + 2 * addRadius), (radius + 2 * addRadius), (radius + 2 * addRadius));
        paint.setAlpha(50);
        canvas.drawArc(rect3, 0, 360, false, paint);
        paint.setAlpha(30);
        canvas.drawArc(rect2, 0, 360, false, paint);
        paint.setAlpha(15);
        canvas.drawArc(rect1, 0, 360, false, paint);
    }

    @Override
    protected void animLogic() {
        radius += deltaRadius;
    }

    @Override
    protected int sleepTime() {
        return 50;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startAnim();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        doLogic();
    }

    @Override
    public void reset() {
        radius = MIN;
    }

    ////////////////////////////////////////////////////////////
    Paint paint;

    //最内圆最小半径
    private float MIN = getFitSize(200);
    //最内圆最大半径
    private float MAX = getFitSize(260);
    private float addRadius = getFitSize(200);

    //最内圆半径
    private float radius = MIN;
    private int deltaRadius = 1;
}
