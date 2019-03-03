package com.zh.swipeback;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.zh.swipeback.tools.Util;

/**
 * Created by zh on 2018/10/19.
 * 滑动返回基类
 */
public class BaseSwipeBackActivity extends AppCompatActivity{
    private final String TAG = getClass().getSimpleName();
    private Activity mActivity;
    private SwipeBackActivityHelper mHelper;
    private boolean mTranslucent = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        Log.e("zh","onCreate mActivity ========" + mActivity.getClass().getSimpleName());
        mActivity = this;
        mHelper = new SwipeBackActivityHelper(this);
        //是否支持缩放动画
        mHelper.onActivityCreate(isSupportFinishAnim());
        //是否支持滑动返回
        setSwipeBackEnable(isSupportSwipeBack());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        Log.e("zh","onPostCreate  mActivity ========" + mActivity.getClass().getSimpleName());
        mHelper.onPostCreate();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if( getSwipeBackLayout().finishAnim && !getSwipeBackLayout().mIsActivitySwipeing){
            if(! mTranslucent){
                Util.convertActivityFromTranslucent(mActivity);
            }
            getSwipeBackLayout().mIsActivityTranslucent = false;
//            Log.e("zh","onEnterAnimationComplete  mActivity ========" + mActivity.getClass().getSimpleName());
        }
    }

    /**
     * 是否支持滑动返回。这里在父类中默认返回 true 来支持滑动返回，如果某个界面不想支持滑动返回则重写该方法返回 false 即可
     * @return
     */
    public boolean isSupportSwipeBack() {
        return true;
    }

    public boolean isSupportFinishAnim() {
        return true;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        if(mHelper != null){
            return mHelper.getSwipeBackLayout();
        }
        return null;
    }

    public void setSwipeBackEnable(boolean enable) {
        if(getSwipeBackLayout() != null){
            getSwipeBackLayout().setEnableGesture(enable);
        }
    }

    public boolean getSwipeBackEnable() {
        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        if(swipeBackLayout != null){
            return swipeBackLayout.getSwipeBackEnable();
        }
        return false;
    }

    public void setActivityFromTranslucent(boolean translucent) {
        mTranslucent = translucent;
    }

    @Override
    protected void onPause() {
//        Log.e("zh","onPause ==========================" + mActivity.getClass().getSimpleName());
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.e("zh","onStop ==========================" + mActivity.getClass().getSimpleName());
    }

    @Override
    public void finish() {
        super.finish();
        mHelper.finish();
    }

    @Override
    protected void onDestroy() {
//        Log.e("zh","onDestroy ==========================" + mActivity.getClass().getSimpleName());
        super.onDestroy();
    }
}
