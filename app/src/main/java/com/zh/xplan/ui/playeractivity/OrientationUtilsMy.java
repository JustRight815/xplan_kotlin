package com.zh.xplan.ui.playeractivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.OrientationEventListener;

import com.module.common.log.LogUtil;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

/**
 * Created by zh on 2018/1/21.
 */

public class OrientationUtilsMy extends OrientationUtils {

    private Activity activity;
    private GSYBaseVideoPlayer gsyVideoPlayer;
    private OrientationEventListener orientationEventListener;

    private int screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private int mIsLand;

    private boolean mClick, mClickLand, mClickPort;
    private boolean mEnable = true;
    private boolean mRotateWithSystem = true; //是否跟随系统

    private int mOrientation;// 当前重力旋转方向

    /**
     * @param activity
     * @param gsyVideoPlayer
     */
    public OrientationUtilsMy(Activity activity, GSYBaseVideoPlayer gsyVideoPlayer) {
        super(activity, gsyVideoPlayer);
        super.releaseListener();
        this.activity = activity;
        this.gsyVideoPlayer = gsyVideoPlayer;
        init();
    }

    private void init() {
        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int rotation) {
                mOrientation = rotation;
                boolean autoRotateOn = (Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if(mRotateWithSystem ){
                   return;
                }
                if (autoRotateOn) {
                    //if (mIsLand == 0) {
                    return;
                    //}
                }
                // 设置竖屏
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
//                    LogUtil.e("zh","onOrientationChanged ********* *******8 设置竖屏" );
                }
                // 设置横屏
                else if (((rotation >= 230) && (rotation <= 310))) {
                    if(gsyVideoPlayer.isIfCurrentIsFullscreen()){
                        if (!(mIsLand == 1)) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                            mIsLand = 1;
                            mClick = false;
                        }
                    }
                }
                // 设置反向横屏
                else if (rotation > 30 && rotation < 95) {
                    if(gsyVideoPlayer.isIfCurrentIsFullscreen()){
                        if (!(mIsLand == 2)) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                            gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                            mIsLand = 2;
                            mClick = false;
                        }
                    }
                }
            }
        };
        orientationEventListener.enable();
    }

    /**
     * 点击切换的逻辑，比如竖屏的时候点击了就是切换到横屏不会受屏幕的影响
     */
    public void resolveByClick() {
        mClick = true;
        if (mIsLand == 0) {
           if ((mOrientation > 45) && (mOrientation <= 135)) {
                LogUtil.e("zh","onOrientationChanged ********* ******* 90" );
                screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                mIsLand = 2;
                mClickLand = false;
            }else {
               screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
               activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
               gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
               mIsLand = 1;
               mClickLand = false;
            }
        } else {
            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (gsyVideoPlayer.isIfCurrentIsFullscreen()) {
                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
            } else {
                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
            }
            mIsLand = 0;
            mClickPort = false;
        }

    }

    /**
     * 列表返回的样式判断。因为立即旋转会导致界面跳动的问题
     */
    public int backToProtVideo() {
        if (mIsLand > 0) {
            mClick = true;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (gsyVideoPlayer != null)
                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
            mIsLand = 0;
            mClickPort = false;
            return 500;
        }
        return 0;
    }


    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        if (mEnable) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
        }
    }

    public void releaseListener() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }

    public boolean isClick() {
        return mClick;
    }

    public void setClick(boolean Click) {
        this.mClick = mClick;
    }

    public boolean isClickLand() {
        return mClickLand;
    }

    public void setClickLand(boolean ClickLand) {
        this.mClickLand = ClickLand;
    }

    public int getIsLand() {
        return mIsLand;
    }

    public void setIsLand(int IsLand) {
        this.mIsLand = IsLand;
    }


    public boolean isClickPort() {
        return mClickPort;
    }

    public void setClickPort(boolean ClickPort) {
        this.mClickPort = ClickPort;
    }

    public int getScreenType() {
        return screenType;
    }

    public void setScreenType(int screenType) {
        this.screenType = screenType;
    }


    public boolean isRotateWithSystem() {
        return mRotateWithSystem;
    }

    /**
     * 是否更新系统旋转，false的话，系统禁止旋转也会跟着旋转
     * @param rotateWithSystem 默认true
     */
    public void setRotateWithSystem(boolean rotateWithSystem) {
        this.mRotateWithSystem = rotateWithSystem;
    }
}
