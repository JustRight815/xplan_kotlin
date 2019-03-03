package com.zh.xplan.ui.view.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.zh.xplan.R;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangc on 2017/4/24.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated:仿微信录小视频按钮
 */
public class RecordedButton extends View {
    private static final String TAG = "ZoomProgressButton";
    //圆的半径
    private float mRadius;
    //中心圆半径
    private float mCenterRadius;
    //色带的宽度
    private float mStripeWidth;
    //总体大小
    private int mHeight;
    private int mWidth;
    //圆心坐标
    private float x;
    private float y;
    //要画的弧度
    private int mEndAngle;
    //进度的颜色
    private int mProgressColor;
    //按钮背景颜色
    private int mBackgroundColor;
    //按钮中心颜色
    private int mCenterColor;
    private Paint mPaint;
    private ProgressListener progressListener;
    //录制的最大时常，由于使用MediaRecorder录制会有提示音卡顿一下，所以进度多给1S
    public int mMaxMillSecond = 31000;
    /*** 定时任务类 ***/
    private ScheduledExecutorService timer;
    ValueAnimator valueAnimator;//全局动画对象，用于取消时候复位
    private boolean progressMax = false;//进度完成
    public int videoTime = -1;//视频时长
    private boolean recording = false;

    public RecordedButton(Context context) {
        this(context, null);
    }

    public RecordedButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordedButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecordedButton, defStyleAttr, 0);
        mStripeWidth = a.getDimension(R.styleable.RecordedButton_stripeWidth, dpToPx(30, context));
        mProgressColor = a.getColor(R.styleable.RecordedButton_progressColor, 0xff2baefd);
        mBackgroundColor = a.getColor(R.styleable.RecordedButton_backgroundColor, 0xffffffff);
        mCenterColor = a.getColor(R.styleable.RecordedButton_centerColor, 0xffFF5557);
        mRadius = a.getDimensionPixelSize(R.styleable.RecordedButton_zpb_radius, dpToPx(100, context));
        mPaint = new Paint();
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取测量模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //获取测量大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            mRadius = widthSize / 2;
            x = widthSize / 2;
            y = heightSize / 2;
            mWidth = widthSize;
            mHeight = heightSize;
        }

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            mWidth = (int) (mRadius * 2);
            mHeight = (int) (mRadius * 2);
            x = mRadius;
            y = mRadius;
        }
        mCenterRadius = mRadius - mStripeWidth - 10;
        //ZoomProgressButton的父布局固定了高度，widthSize等于0，不知道为什么，大神看到了给Issuer解答下
        setMeasuredDimension(mHeight, mHeight);
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制圆形背景
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBackgroundColor);
        canvas.drawCircle(x, y, mRadius, mPaint);

        //进度
        mPaint.setColor(mProgressColor);
        mPaint.setAntiAlias(true);
        RectF rect = new RectF(0f, 0f, mWidth, mHeight);
        canvas.drawArc(rect, 270, mEndAngle, true, mPaint);

        //绘制小圆,间隔
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBackgroundColor);
        canvas.drawCircle(x, y, mRadius - mStripeWidth, mPaint);

        //绘制按钮中心
        mPaint.setAntiAlias(true);
        mPaint.setColor(mCenterColor);
        canvas.drawCircle(x, y, mCenterRadius, mPaint);
    }

    /**
     * 绘制进度条
     */
    public void onStartDrawProgress() {
        valueAnimator = ValueAnimator.ofInt(0, 360);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mEndAngle = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                progressListener.progressEnd();
                progressMax = true;
                onResetProgress();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                progressListener.progressStart();
                recording = true;
                videoTime = -1;
                timerTask();
            }
        });
        valueAnimator.setDuration(mMaxMillSecond);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }

    /**
     * 中心圆缩放（放大）
     */
    public void onStartCenterDraw() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCenterRadius, mCenterRadius - 30);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCenterRadius = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 中心圆缩放（缩小）
     */
    public void onEndCenterDraw() {
        float tempCenterRadius;
        if (progressMax) {
            tempCenterRadius = mCenterRadius + 30;
        } else {
            tempCenterRadius = mCenterRadius;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mCenterRadius, tempCenterRadius);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCenterRadius = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 按钮放大
     */
    public void enlargeBtn() {
        ObjectAnimator oaDownScaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.5f);
        ObjectAnimator oaDownScaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.5f);
        AnimatorSet enlargeSet = new AnimatorSet();
        enlargeSet.play(oaDownScaleX).with(oaDownScaleY);
        enlargeSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onStartDrawProgress();
                progressMax = true;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                onStartCenterDraw();
                progressListener.onBtnStartEnlarge();
            }
        });
        enlargeSet.setDuration(300).start();
    }

    /**
     * 按钮缩小
     */
    public void narrowBtn() {
        ObjectAnimator oaUpScaleX = ObjectAnimator.ofFloat(this, "scaleX", 1.5f, 1f);
        ObjectAnimator oaUpScaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.5f, 1f);
        AnimatorSet narrowSet = new AnimatorSet();
        narrowSet.play(oaUpScaleX).with(oaUpScaleY);
        narrowSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onResetProgress();
                progressListener.onnEndNarrowBtn();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                onEndCenterDraw();
                recording = false;
            }
        });
        narrowSet.setDuration(300).start();
    }

    /**
     * 按钮复位
     */
    public void onResetProgress() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        mEndAngle = 0;
        mCenterRadius = mRadius - mStripeWidth - 10;
        postInvalidate();
        progressMax = false;
        recording = false;
    }

    public interface ProgressListener {
        void progressStart();

        void progressEnd();

        void onBtnStartEnlarge();

        void onnEndNarrowBtn();
    }

    public void onDestroy() {
        progressMax = false;
        recording = false;
        videoTime = -1;
        mPaint = null;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (task != null) {
            task.cancel();
        }
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
        }
        progressListener=null;
    }

    public void timerTask() {
        if (null == timer) {
            timer = Executors.newScheduledThreadPool(1);
            /*1s后启动任务，每1s执行一次**/
            timer.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
        }

    }

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (recording) {
                videoTime += 1;
            }
        }
    };

    public static int dpToPx(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
