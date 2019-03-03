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
public class CloudyView extends BaseAnimView {

    public CloudyView(Context context, int backColor) {
        super(context, backColor);
    }

    @Override
    protected void init() {
        super.init();
        paint = new Paint();
        paint.setStrokeWidth(getFitSize(3));
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setAlpha(70);
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
        RectF rect2 = new RectF((windowWidth / 2 - radius), -radius, (windowWidth / 2 + radius), radius);
        RectF rect3 = new RectF(windowWidth - radius, -radius, windowWidth + radius, radius);

        canvas.drawArc(rect3, 0, 360, false, paint);
        canvas.drawArc(rect2, 0, 360, false, paint);
        canvas.drawArc(rect1, 0, 360, false, paint);
    }

    @Override
    public void reset() {
        radius = MIN;
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

    //////////////////////////////////////////////////////
    Paint paint;
    //圆最小半径
    private float MIN = getFitSize(560);
    //圆最大半径
    private float MAX = MIN + getFitSize(60);
    //圆半径
    private float radius = MIN;
    private int deltaRadius = 1;
}
