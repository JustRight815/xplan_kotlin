package com.zh.xplan.ui.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import static android.animation.ObjectAnimator.ofFloat;

/**
 * 仿今日头条图片浏览上下滑动退出界面
 */
public class UpDownHideLayout extends FrameLayout {
    private ScrollListener mScrollListener;

    public interface ScrollListener {

        void onScrolling(float percent,float pixDistance);
    }

    public void setScrollListener(ScrollListener scrollListener) {
        mScrollListener = scrollListener;
    }

    public interface OnLayoutCloseListener {
        void OnLayoutClosed();
    }

    enum Direction {
        UP_DOWN,
        LEFT_RIGHT,
        NONE
    }

    private Direction direction = Direction.NONE;
    private int previousFingerPositionY;
    private int previousFingerPositionX;
    private int baseLayoutPosition;
    private boolean isScrollingUp;  //上滑 true  下滑false
    private boolean isLocked = false;//是否拦截事件
    private OnLayoutCloseListener listener;
    private int RootViewHeight = 0;


    public UpDownHideLayout(Context context) {
        super(context);
    }

    public UpDownHideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UpDownHideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UpDownHideLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isLocked) {
            return false;
        } else {
            final int y = (int) ev.getRawY();
            final int x = (int) ev.getRawX();
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                previousFingerPositionX = x;
                previousFingerPositionY = y;
            } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                int diffY = y - previousFingerPositionY;
                int diffX = x - previousFingerPositionX;
                if (Math.abs(diffX) + 50 < Math.abs(diffY))
                    return true;{
                }
            }
            return false;
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//      初始化界面的高度
        RootViewHeight = getMeasuredHeight() + 24;//有状态栏和虚拟键
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if (!isLocked) {
            final int y = (int) ev.getRawY();
            final int x = (int) ev.getRawX();
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                previousFingerPositionX = x;
                previousFingerPositionY = y;
                baseLayoutPosition = (int) this.getY();
            } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                int diffY = y - previousFingerPositionY;
                int diffX = x - previousFingerPositionX;
                if (direction == Direction.NONE) {
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        direction = Direction.LEFT_RIGHT;
                    } else if (Math.abs(diffX) < Math.abs(diffY)) {
                        direction = Direction.UP_DOWN;
                    } else {
                        direction = Direction.NONE;
                    }
                }
                if (direction == Direction.UP_DOWN) {
                    isScrollingUp = diffY <= 0;
                    this.setY(baseLayoutPosition + diffY);
                    requestLayout();
                    if (mScrollListener != null) {
                        diffY = Math.abs(diffY);
                        float percent = ((float) diffY / (float) RootViewHeight) > 0.99 ? 1 : ((float) diffY / (float) RootViewHeight);
                        mScrollListener.onScrolling(percent,diffY);
                    }
                    return true;
                }
            } else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
                if (direction == Direction.UP_DOWN) {
                    if (isScrollingUp) {
                        int height = this.getHeight();
                        if (Math.abs(this.getY()) > (height / 5)) {
//                            上滑退出
                            scrollToHide(true);
                        } else {
                            scrollToBack();
                        }
                    } else {
                        int height = this.getHeight();
                        if (Math.abs(this.getY()) > (height / 5)) {
                            scrollToHide(false);
                        } else {
                            scrollToBack();
                        }
                    }
                    direction = Direction.NONE;
                    return true;
                }
                direction = Direction.NONE;
            }
            return true;
        }
        return false;
    }


    public void scrollToHide(boolean isScrollingUp){
        ObjectAnimator positionAnimator = null;
        if(isScrollingUp){
            positionAnimator = ObjectAnimator.ofFloat(this, "y", this.getY(), -RootViewHeight);
        }else{
            positionAnimator = ofFloat(this, "y", this.getY(), RootViewHeight);
        }
        positionAnimator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                notityListener(animation);
            }
        });
        positionAnimator.setDuration(260);
        positionAnimator.start();
    }

    public void scrollToBack(){
        ObjectAnimator positionAnimator = ofFloat(this, "y", this.getY(), 0);
        positionAnimator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                notityListener(animation);
            }
        });
        positionAnimator.setDuration(300);
        positionAnimator.start();
    }

    public void notityListener(ValueAnimator animation){
        if (mScrollListener != null) {
            float diffY = Math.abs((float) animation.getAnimatedValue());
            float percent = ( diffY / (float) RootViewHeight) > 0.99 ? 1 : (diffY / (float) RootViewHeight);
            mScrollListener.onScrolling(percent,diffY);
        }
    }


    public void setOnLayoutCloseListener(OnLayoutCloseListener closeListener) {
        this.listener = closeListener;
    }

    public void lock() {
        isLocked = true;
    }

    public void unLock() {
        isLocked = false;
    }

    public void downToFinish(final ScrollListener scrollListener){
        ObjectAnimator positionAnimator = null;
        positionAnimator = ofFloat(this, "y", this.getY(), RootViewHeight);
        positionAnimator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (scrollListener != null) {
                    float diffY = Math.abs((float) animation.getAnimatedValue());
                    float percent = ( diffY / (float) RootViewHeight) > 0.99 ? 1 : (diffY / (float) RootViewHeight);
                    scrollListener.onScrolling(percent,diffY);
                }
            }
        });
        positionAnimator.setDuration(260);
        positionAnimator.start();
    }
}
