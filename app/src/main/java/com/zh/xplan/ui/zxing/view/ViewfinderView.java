/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zh.xplan.ui.zxing.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.google.zxing.ResultPoint;
import com.zh.xplan.R;
import com.module.common.utils.DPIUtil;
import com.zh.xplan.ui.zxing.camera.CameraManager;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial transparency outside it, as well as the laser scanner animation and result points.
 *
 */
public final class ViewfinderView extends View {
    private static final String TAG = "log";
    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 3L;
    private static final int OPAQUE = 0xFF;

    /**
     * 四个绿色边角对应的长度
     */
    private final int ScreenRate;

    /**
     * 四个绿色边角对应的宽度
     */
    private static final int CORNER_WIDTH = 5;//原来是2

    /**
     * 四个边角与扫描框的距离
     */
    private static final int CORNER_DIST = 8;
    /**
     * 扫描框中的中间线的高度
     */
    private static int MIDDLE_LINE_HEIGHT = 24;

    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 1;

    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;

    /**
     * 手机的屏幕密度
     */
    private static float density;
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 16;
    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 30;

    /**
     * 扫描框的边线宽度
     */
    private static final int FRAME_EDAGE_LINE_WIDTH = 1;
    /**
     * 画笔对象的引用
     */
    private final Paint paint;

    private int bgPadding = DPIUtil.dip2px(10);

    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;

    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;

    /**
     * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
     */
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;

    private final int resultPointColor;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    boolean isFirst;
    boolean isReverse;

    private Bitmap middleBmp;
//	private Bitmap middleLineBmp;
//	private Bitmap arrowBmp;
//	private Bitmap bgBmp;
    private Button zoomPlus;
    private Button zoomMinus;

    boolean isLine = false;
    private boolean isDecode;
    private Bitmap arrow = null,arrow2 = null,arrow3 = null,arrow4 = null;
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        density = context.getResources().getDisplayMetrics().density;
        // 将像素转换成dp
        ScreenRate = (int) (20 * density);

        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);

        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<ResultPoint>(5);

        middleBmp = ((BitmapDrawable)resources.getDrawable(R.drawable.qrcode_scan_line)).getBitmap();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
    public void onDraw(Canvas canvas) {
        // 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        CameraManager cm = CameraManager.get();
        if (cm == null) {
            return;
        }
        Rect frame = cm.getFramingRect();
        if (frame == null) {
            return;
        }

        // 初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top - MIDDLE_LINE_HEIGHT;
            slideBottom = frame.bottom - MIDDLE_LINE_HEIGHT;
        }

        // 获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        int padding = DPIUtil.dip2px(25);
        paint.setColor(resultColor);
        canvas.drawRect(new RectF(0, 0, width + FRAME_EDAGE_LINE_WIDTH, frame.top - FRAME_EDAGE_LINE_WIDTH), paint);
        canvas.drawRect(new RectF(0, frame.top - FRAME_EDAGE_LINE_WIDTH, frame.left - FRAME_EDAGE_LINE_WIDTH, frame.bottom + FRAME_EDAGE_LINE_WIDTH), paint);
        canvas.drawRect(new RectF(frame.right + FRAME_EDAGE_LINE_WIDTH, frame.top - FRAME_EDAGE_LINE_WIDTH, width + FRAME_EDAGE_LINE_WIDTH, frame.bottom + FRAME_EDAGE_LINE_WIDTH), paint);
        canvas.drawRect(new RectF(0, frame.bottom + FRAME_EDAGE_LINE_WIDTH, width, height), paint);
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {

            //原来的样式，使用图片
//            paint.setColor(Color.parseColor("#32B767"));
//                if (!isDecode){
//                     arrow = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_qrarrow1);
//                     arrow2 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_qrarrow2);
//                     arrow3 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_qrarrow3);
//                     arrow4 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_qrarrow4);
//                    isDecode = true;
//                }
//                int i = arrow.getWidth()/2;
//                canvas.drawBitmap(arrow,frame.left-i,frame.top-i,paint);
//                canvas.drawBitmap(arrow2,frame.right-i,frame.top-i,paint);
//                canvas.drawBitmap(arrow3,frame.left-i,frame.bottom-i,paint);
//                canvas.drawBitmap(arrow4,frame.right-i,frame.bottom-i,paint);

            paint.setColor(Color.parseColor("#FFFFFF"));

            // 画扫描框边上的角，总共8个部分
            canvas.drawRect(frame.left-CORNER_DIST, frame.top-CORNER_DIST, frame.left-CORNER_DIST + ScreenRate, frame.top-CORNER_DIST + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left-CORNER_DIST, frame.top-CORNER_DIST, frame.left-CORNER_DIST + CORNER_WIDTH, frame.top-CORNER_DIST + ScreenRate, paint);
            canvas.drawRect(frame.right+CORNER_DIST - ScreenRate, frame.top-CORNER_DIST, frame.right+CORNER_DIST, frame.top-CORNER_DIST + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right+CORNER_DIST - CORNER_WIDTH, frame.top-CORNER_DIST, frame.right+CORNER_DIST, frame.top-CORNER_DIST + ScreenRate, paint);
            canvas.drawRect(frame.left-CORNER_DIST, frame.bottom+CORNER_DIST - CORNER_WIDTH, frame.left-CORNER_DIST + ScreenRate, frame.bottom+CORNER_DIST, paint);
            canvas.drawRect(frame.left-CORNER_DIST, frame.bottom+CORNER_DIST - ScreenRate, frame.left-CORNER_DIST + CORNER_WIDTH, frame.bottom+CORNER_DIST, paint);
            canvas.drawRect(frame.right+CORNER_DIST - ScreenRate, frame.bottom+CORNER_DIST - CORNER_WIDTH, frame.right+CORNER_DIST, frame.bottom+CORNER_DIST, paint);
            canvas.drawRect(frame.right+CORNER_DIST - CORNER_WIDTH, frame.bottom+CORNER_DIST - ScreenRate, frame.right+CORNER_DIST, frame.bottom+CORNER_DIST, paint);

            // 画扫描边框线, 共四条
            canvas.drawLine(frame.left-FRAME_EDAGE_LINE_WIDTH, frame.top-FRAME_EDAGE_LINE_WIDTH, frame.right+FRAME_EDAGE_LINE_WIDTH, frame.top-FRAME_EDAGE_LINE_WIDTH, paint);
            canvas.drawLine(frame.left-FRAME_EDAGE_LINE_WIDTH, frame.top-FRAME_EDAGE_LINE_WIDTH, frame.left-FRAME_EDAGE_LINE_WIDTH, frame.bottom+FRAME_EDAGE_LINE_WIDTH, paint);
            canvas.drawLine(frame.left-FRAME_EDAGE_LINE_WIDTH, frame.bottom+FRAME_EDAGE_LINE_WIDTH, frame.right+FRAME_EDAGE_LINE_WIDTH, frame.bottom+FRAME_EDAGE_LINE_WIDTH, paint);
            canvas.drawLine(frame.right+FRAME_EDAGE_LINE_WIDTH, frame.top-FRAME_EDAGE_LINE_WIDTH, frame.right+FRAME_EDAGE_LINE_WIDTH, frame.bottom+FRAME_EDAGE_LINE_WIDTH+1, paint);


            Bitmap bitmap = middleBmp;
            // 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= slideBottom) {//位移到底部
                slideTop = frame.top - MIDDLE_LINE_HEIGHT;
            }

            final Rect dst = new Rect(frame.left,(slideTop < frame.top) ? frame.top : slideTop, frame.right, slideTop+MIDDLE_LINE_HEIGHT);
            if(bitmap!=null&&!bitmap.isRecycled()){
                final Rect src = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
                canvas.drawBitmap(bitmap, src, dst, paint);
            }

            // 画扫描框下面的字
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * density);
            String hintString = getResources().getString(R.string.scan_text);
            float txtWidth = paint.measureText(hintString);// 计算文字宽度
            int rectCoreLeft = (frame.left + frame.right) / 2;// 计算居中位置
            rectCoreLeft = (int) (rectCoreLeft - txtWidth / 2);// 计算左顶点
            canvas.drawText(hintString, rectCoreLeft, frame.top-2*padding + TEXT_PADDING_TOP * density, paint);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;

            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                synchronized (possibleResultPoints) {
                    lastPossibleResultPoints = currentPossible;
                    paint.setAlpha(OPAQUE);
                    paint.setColor(resultPointColor);
                    try {
                        for (ResultPoint point : currentPossible) {
                            canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
                        }
                    }
                    catch(ConcurrentModificationException e){
                        e.printStackTrace();
                    }

                }
            }

            if (currentLast != null) {
                synchronized (possibleResultPoints) {
                    paint.setAlpha(OPAQUE / 2);
                    paint.setColor(resultPointColor);
                    for (ResultPoint point : currentLast) {
                        canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
                    }
                }
            }

            // 只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);

        }

    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }


    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode
     *            An image of the decoded scan_barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        synchronized (possibleResultPoints) {
            possibleResultPoints.add(point);
        }
    }

    int measureedWidth;
    int measureedHeight;
    private Rect frame = new Rect();


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureedWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureedHeight = MeasureSpec.getSize(heightMeasureSpec);
        int borderWidth = (int) (measureedWidth -30*density);
        int borderHeight=measureedHeight/3;
        int left=(measureedWidth - borderWidth) / 2;
        int top=(measureedHeight - borderHeight) / 2;
        frame.set(left, top, left+borderWidth, top + borderHeight);
    }

    public Rect getScanImageRect(int w, int h)
    {
        Rect rect = new Rect();
        rect.left = frame.left;
        rect.right = frame.right;
        float temp = h / (float) measureedHeight;
        rect.top = (int) (frame.top * temp);
        rect.bottom = (int) (frame.bottom * temp);
        return rect;
    }

}
