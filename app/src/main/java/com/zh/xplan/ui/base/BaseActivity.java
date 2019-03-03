package com.zh.xplan.ui.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.jaeger.library.StatusBarUtil;
import com.module.common.log.LogUtil;
import com.zh.swipeback.BaseSwipeBackActivity;
import com.zh.xplan.ui.skin.SkinConfigHelper;
import org.qcode.qskinloader.IActivitySkinEventHandler;
import org.qcode.qskinloader.ISkinActivity;
import org.qcode.qskinloader.SkinManager;

/**
 * Created by zh on 2017/3/11.
 * Activity 基类 便于统一管理
 */
@SuppressLint("Registered")
public class BaseActivity extends BaseSwipeBackActivity implements ISkinActivity {
    public final String TAG = getClass().getSimpleName();
    protected Activity mActivity;
    public IActivitySkinEventHandler mSkinEventHandler;//皮肤切换
    private boolean mFirstTimeApplySkin = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        if(isSupportSkinChange()){
            initSkinEventHandler();
        }
        super.onCreate(savedInstanceState);
    }

    /**
     * 初始化换肤
     */
    private void initSkinEventHandler() {
        mSkinEventHandler = SkinManager.newActivitySkinEventHandler()
                .setSwitchSkinImmediately(isSwitchSkinImmediately())
                .setSupportSkinChange(isSupportSkinChange())
                .setWindowBackgroundResource(getWindowBackgroundResource())
                .setNeedDelegateViewCreate(true);
        mSkinEventHandler.onCreate(this);
    }

    public boolean isEnableHideSoftInputFromWindow = true;

    /**
     * 触摸空白区域自动隐藏键盘
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && isEnableHideSoftInputFromWindow) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null){
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isSupportSkinChange()){
            mSkinEventHandler.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //皮肤相关，此通知放在此处，尽量让子类的view都添加到view树内
        if(isSupportSkinChange()){
            if (mFirstTimeApplySkin) {
                mSkinEventHandler.onViewCreated();
                mFirstTimeApplySkin = false;
            }
            mSkinEventHandler.onResume();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isSupportSkinChange()){
            mSkinEventHandler.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isSupportSkinChange()){
            mSkinEventHandler.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isSupportSkinChange()){
            mSkinEventHandler.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isSupportSkinChange()){
            mSkinEventHandler.onDestroy();
        }
    }


    @Override
    public boolean isSupportSkinChange() {
        //告知当前界面是否支持换肤：true支持换肤，false不支持
        return false;
    }
//    /**
//     * true：默认支持所有View换肤，不用添加skin:enable="true"，不想支持则设置skin:enable="false"
//     * false：默认不支持所有View换肤，对需要换肤的View添加skin:enable="true"
//     *
//     * @return
//     */
//    @Override
//    public boolean isSupportAllViewSkin() {
//        return false;
//    }

    @Override
    public boolean isSwitchSkinImmediately() {
        //告知当切换皮肤时，是否立刻刷新当前界面；true立刻刷新，false表示在界面onResume时刷新；
        //减轻换肤时性能压力
        return false;
    }

    @Override
    public void handleSkinChange() {
        //当前界面在换肤时收到的回调，可以在此回调内做一些其他事情；
        //比如：通知WebView内的页面切换到夜间模式等
        LogUtil.e("zh","换肤成功11" + SkinConfigHelper.isDefaultSkin());
    }

    /**
     * 告知当前界面Window的background资源，换肤时会寻找对应的资源替换
     */
    protected int getWindowBackgroundResource()
    {
        return 0;
    }


    /**
     * 设置状态栏颜色
     * @param color
     */
    protected void setStatusBarColor(@ColorInt int color) {
        setStatusBarColor(color, StatusBarUtil.DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 设置状态栏颜色
     * @param color
     * @param statusBarAlpha 透明度
     */
    public void setStatusBarColor(@ColorInt int color, @IntRange(from = 0, to = 255) int statusBarAlpha) {
        StatusBarUtil.setColorForSwipeBack(this, color, statusBarAlpha);
    }

}
