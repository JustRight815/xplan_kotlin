package com.zh.xplan.ui.view.addialog;

import android.app.Activity;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zh.xplan.R;


/**
 * 广告弹窗  https://github.com/RmondJone/SpringDiaLog
 */
public class AdDialog {
    private Activity mContext;
    private View mContentView; //弹框内容视图布局ID
    private ViewType mViewType; //视图类型

    public enum ViewType {
        TEXT  //纯文本
        , IMG  //单图片
        , BLEND //图文混排
    }

    private int mBackGroudImg = -1; //弹框背景图片
    private int mCloseButtonImg = -1;//关闭按钮资源
    private View.OnClickListener mCloseButtonListener;//关闭按钮点击事件
    private boolean isOverScreen = true;    // 是否覆盖全屏幕
    private int mContentView_Margin_Left = 8;//内容视图左边距
    private int mContentView_Margin_Top = 8;//内容视图上边距
    private int mContentView_Margin_Right = 8;//内容视图右边距
    private int mContentView_Margin_Bottom = 8;//内容视图底边距
    private int mContentView_Margin = -1;//内容视图边距
    private boolean isShowCloseButton = true;//是否显示关闭按钮
    private boolean isCanceledOnTouchOutside = true; //是否点击外围触发关闭事件
    private int mStartAnimAngle = 270;//开始动画角度,0代表从右往左,逆时针算
    private int mContentViewWidth = 280;//内容视图宽度
    private int mContentViewHeight = 350;//内容视图高度


    private ImageView mCloseButton;//关闭按钮
    private ViewGroup androidContentView;//屏幕根视图
    private View mRootView;//根视图
    private RelativeLayout mContainerView; //内容视图背景视图
    private RelativeLayout mAnimationView;//动画View;
    private FrameLayout mContentView_FrameLayout;
    private boolean isShowing;//弹框是否显示
    private double heightY;
    private double widthX;


    public AdDialog(Activity mContext, View mContentView) {
        this.mContext = mContext;
        this.mContentView = mContentView;
        this.mViewType = ViewType.BLEND;
        initView();
    }

    public AdDialog(Activity mContext, View mContentView, ViewType mViewType) {
        this.mContext = mContext;
        this.mContentView = mContentView;
        this.mViewType = mViewType;
        initView();
    }

    private void initView() {
        initDisplayOpinion();
        double radius = Math.sqrt(DisplayUtil.screenhightPx * DisplayUtil.screenhightPx + DisplayUtil.screenWidthPx * DisplayUtil.screenWidthPx);
        heightY = -Math.sin(Math.toRadians(mStartAnimAngle)) * radius;
        widthX = Math.cos(Math.toRadians(mStartAnimAngle)) * radius;
        if (isOverScreen) {
            androidContentView = (ViewGroup) mContext.getWindow().getDecorView();
        } else {
            androidContentView = (ViewGroup) mContext.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        }
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.ad_dialog_layout, null);
        if (mRootView != null) {
            mCloseButton = (ImageView) mRootView.findViewById(R.id.iv_close);
            mCloseButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ad_dialog_closebutton));

            mContainerView = (RelativeLayout) mRootView.findViewById(R.id.contentView);

            if (mViewType != ViewType.TEXT) {
                mContainerView.setBackgroundResource(R.drawable.ad_dialog_bg);
            } else {
                mContainerView.setBackgroundResource(R.drawable.ad_dialog_backimg);
            }

            mAnimationView = (RelativeLayout) mRootView.findViewById(R.id.anim_container);

            mContentView_FrameLayout = (FrameLayout) mRootView.findViewById(R.id.fl_content_container);

        } else {
            Log.e("控件初始化失败", "LayoutInflater获取根视图失败！");
        }
    }

    /**
     * 初始化屏幕宽高
     */
    private void initDisplayOpinion() {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(mContext, dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(mContext, dm.heightPixels);
    }

    /**
     * 显示弹框
     */
    public void show() {
        if (mRootView != null) {
            isShowing = true;

            if (isShowCloseButton) {
                mCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mCloseButtonListener != null) {
                            mCloseButtonListener.onClick(view);
                        }
                        AdAnim.getInstance(mAnimationView).startTranslationAnim(0, 0, -widthX, -heightY);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                androidContentView.removeView(mRootView);
                            }
                        }, 400);
                    }
                });
            } else {
                mCloseButton.setVisibility(View.GONE);
                if (isCanceledOnTouchOutside) {
                    mRootView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AdAnim.getInstance(mAnimationView).startTranslationAnim(0, 0, -widthX, -heightY);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    androidContentView.removeView(mRootView);
                                }
                            }, 400);
                        }
                    });
                }
            }

            if (mCloseButtonImg != -1) {
                mCloseButton.setImageDrawable(mContext.getResources().getDrawable(mCloseButtonImg));
            }
            if (mBackGroudImg != -1) {
                mContainerView.setBackgroundResource(mBackGroudImg);
            }

            //设置内容视图布局大小
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.width = DisplayUtil.dip2px(mContext, mContentViewWidth);
            layoutParams.height = DisplayUtil.dip2px(mContext, mContentViewHeight);
            mContainerView.setLayoutParams(layoutParams);

            //加入视图操作
            FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (mContentView_Margin != -1) {
                contentParams.setMargins(mContentView_Margin, mContentView_Margin, mContentView_Margin, mContentView_Margin);
            } else {
                contentParams.setMargins(mContentView_Margin_Left, mContentView_Margin_Top, mContentView_Margin_Right, mContentView_Margin_Bottom);
            }


            mContentView_FrameLayout.addView(mContentView, contentParams);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            androidContentView.addView(mRootView, params);

            //加入视图动画
            double radius = Math.sqrt(DisplayUtil.screenhightPx * DisplayUtil.screenhightPx + DisplayUtil.screenWidthPx * DisplayUtil.screenWidthPx);
            heightY = -Math.sin(Math.toRadians(mStartAnimAngle)) * radius;
            widthX = Math.cos(Math.toRadians(mStartAnimAngle)) * radius;
            AdAnim.getInstance(mAnimationView).startTranslationAnim(widthX, heightY, 0, 0);
        } else {
            Log.e("控件初始化失败", "LayoutInflater获取根视图失败！");
        }
    }

    /**
     * 关闭弹框
     */
    public void close() {
        if (isShowing) {
            AdAnim.getInstance(mAnimationView).startTranslationAnim(0, 0, -widthX, -heightY);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    androidContentView.removeView(mRootView);
                }
            }, 400);
            isShowing = false;
        } else {
            Log.e("关闭失败", "弹框未显示！");
        }
    }


    //属性初始化

    public AdDialog setBackGroudImg(int mBackGroudImg) {
        this.mBackGroudImg = mBackGroudImg;
        return this;
    }

    public AdDialog setCloseButtonImg(int mCloseButtonImg) {
        this.mCloseButtonImg = mCloseButtonImg;
        return this;
    }

    public AdDialog setCloseButtonListener(View.OnClickListener mCloseButtonListener) {
        this.mCloseButtonListener = mCloseButtonListener;
        return this;
    }

    public boolean isOverScreen() {
        return isOverScreen;
    }

    public AdDialog setOverScreen(boolean overScreen) {
        isOverScreen = overScreen;
        return this;
    }

    public AdDialog setContentView_Margin_Left(int mContentView_Margin_Left) {
        this.mContentView_Margin_Left = mContentView_Margin_Left;
        return this;
    }

    public AdDialog setContentView_Margin_Top(int mContentView_Margin_Top) {
        this.mContentView_Margin_Top = mContentView_Margin_Top;
        return this;
    }

    public AdDialog setContentView_Margin_Right(int mContentView_Margin_Right) {
        this.mContentView_Margin_Right = mContentView_Margin_Right;
        return this;
    }

    public AdDialog setContentView_Margin_Bottom(int mContentView_Margin_Bottom) {
        this.mContentView_Margin_Bottom = mContentView_Margin_Bottom;
        return this;
    }

    public AdDialog setContentView_Margin(int mContentView_Margin) {
        this.mContentView_Margin = mContentView_Margin;
        return this;
    }

    public AdDialog setShowCloseButton(boolean showCloseButton) {
        isShowCloseButton = showCloseButton;
        return this;
    }

    public AdDialog setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        isCanceledOnTouchOutside = canceledOnTouchOutside;
        return this;
    }

    public AdDialog setStartAnimAngle(int mStartAnimAngle) {
        this.mStartAnimAngle = mStartAnimAngle;
        return this;
    }

    public AdDialog setContentViewWidth(int mContentViewWidth) {
        this.mContentViewWidth = mContentViewWidth;
        return this;
    }

    public AdDialog setContentViewHeight(int mContentViewHeight) {
        this.mContentViewHeight = mContentViewHeight;
        return this;
    }
}
