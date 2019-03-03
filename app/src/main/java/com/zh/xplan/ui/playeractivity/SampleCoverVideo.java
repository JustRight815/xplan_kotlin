package com.zh.xplan.ui.playeractivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.module.common.image.ImageLoader;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.zh.xplan.R;
import moe.codeest.enviews.ENDownloadView;

/**
 * 重写播放器界面。 添加封面，替换开始播放和暂停按钮
 */
public class SampleCoverVideo extends StandardGSYVideoPlayer {
    ImageView mCoverImage;
    String mPicUrl;
    int mDefaultRes;
    LoadVideoUrlListener mLoadVideoUrlListener;

    public SampleCoverVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SampleCoverVideo(Context context) {
        super(context);
    }

    public SampleCoverVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCoverImage = (ImageView) findViewById(R.id.thumbImage);
        findViewById(R.id.thumb).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickThumb();
            }
        });
    }

    /**
     * 重写父类方法，使用自己的布局
     * @return
     */
    @Override
    public int getLayoutId() {
        return R.layout.video_layout_cover;
    }

    /**
     * 加载视频封面
     * @param url 封面图片url
     * @param res 默认封面图片资源
     */
    public void loadCoverImage(String url, int res) {
        mPicUrl = url;
        mDefaultRes = res;
        ImageLoader.displayImage(mContext,mCoverImage,Uri.parse(url));
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        SampleCoverVideo sampleCoverVideo = (SampleCoverVideo) gsyBaseVideoPlayer;
        sampleCoverVideo.loadCoverImage(mPicUrl, mDefaultRes);
        return gsyBaseVideoPlayer;
    }

    /**
     * 重写方法，使用自定义的开始播放和暂停图片
     */
    @Override
    protected void updateStartImage() {
        if(mStartButton instanceof ImageView) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(R.drawable.video_pause_selector);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(R.drawable.video_play_selector);
            } else {
                imageView.setImageResource(R.drawable.video_play_selector);
            }
        }
    }

    /**
     * 自定义点击开始按钮后的效果
     */
    @Override
    public void clickStartIcon(){
        if (mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR) {
            if (!NetworkUtils.isAvailable(mContext)) {
                Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!mUrl.startsWith("file") && !CommonUtil.isWifiConnected(getContext())
                    && mNeedShowWifiTip) {
                if(TextUtils.isEmpty(mUrl)){
                    Debuger.printfError("********" + getResources().getString(R.string.no_url));
                    if(mLoadVideoUrlListener != null){
                        mLoadVideoUrlListener.onLoadUrl();
                    }
                    return;
                }else{
                    showWifiDialog();
                    return;
                }
            }
            if (TextUtils.isEmpty(mUrl)) {
                Debuger.printfError("********" + getResources().getString(R.string.no_url));
                if(mLoadVideoUrlListener != null){
                    mLoadVideoUrlListener.onLoadUrl();
                }
                return;
            }
            startButtonLogic();
            return;
        }

        if (TextUtils.isEmpty(mUrl)) {
            Debuger.printfError("********" + getResources().getString(R.string.no_url));
            if(mLoadVideoUrlListener != null){
                mLoadVideoUrlListener.onLoadUrl();
            }
            return;
        }
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            try {
                GSYVideoManager.instance().getMediaPlayer().pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setStateAndUi(CURRENT_STATE_PAUSE);
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                if (mIfCurrentIsFullscreen) {
                    Debuger.printfLog("onClickStopFullscreen");
                    mVideoAllCallBack.onClickStopFullscreen(mOriginUrl, mTitle, this);
                } else {
                    Debuger.printfLog("onClickStop");
                    mVideoAllCallBack.onClickStop(mOriginUrl, mTitle, this);
                }
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                if (mIfCurrentIsFullscreen) {
                    Debuger.printfLog("onClickResumeFullscreen");
                    mVideoAllCallBack.onClickResumeFullscreen(mOriginUrl, mTitle, this);
                } else {
                    Debuger.printfLog("onClickResume");
                    mVideoAllCallBack.onClickResume(mOriginUrl, mTitle, this);
                }
            }
            try {
                GSYVideoManager.instance().getMediaPlayer().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setStateAndUi(CURRENT_STATE_PLAYING);
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            startButtonLogic();
        }
    }

    /**
     * 自定义点击封面的效果
     */
    public void clickThumb(){
        if (!mThumbPlay) {
            return;
        }
        if (mCurrentState == CURRENT_STATE_NORMAL) {
            if (!NetworkUtils.isAvailable(mContext)) {
                Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!mUrl.startsWith("file") && !CommonUtil.isWifiConnected(getContext())
                    && mNeedShowWifiTip) {
                if(TextUtils.isEmpty(mUrl)){
                    Debuger.printfError("********" + getResources().getString(R.string.no_url));
                    if(mLoadVideoUrlListener != null){
                        mLoadVideoUrlListener.onLoadUrl();
                    }
                    return;
                }else{
                    showWifiDialog();
                    return;
                }
            }
            if (TextUtils.isEmpty(mUrl)) {
                Debuger.printfError("********" + getResources().getString(R.string.no_url));
                if(mLoadVideoUrlListener != null){
                    mLoadVideoUrlListener.onLoadUrl();
                }
                return;
            }
            startPlayLogic();
            return;
        }
        if (TextUtils.isEmpty(mUrl)) {
            Debuger.printfError("********" + getResources().getString(R.string.no_url));
            if(mLoadVideoUrlListener != null){
                mLoadVideoUrlListener.onLoadUrl();
            }
            return;
        }else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            onClickUiToggle();
        }
    }


    /**
     * 重写显示wifi确定框，
     */
    @Override
    protected void showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startPlayLogic();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //预加载时期显示 mThumbImageViewLayout
    @Override
    protected void changeUiToPreparingShow() {
        Debuger.printfLog("changeUiToPreparingShow");

        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomContainer, INVISIBLE);
        setViewShowState(mStartButton, INVISIBLE);
        setViewShowState(mLoadingProgressBar, VISIBLE);
        setViewShowState(mThumbImageViewLayout, VISIBLE);
        setViewShowState(mBottomProgressBar, INVISIBLE);
        setViewShowState(mLockScreen, GONE);

        if (mLoadingProgressBar instanceof ENDownloadView) {
            ENDownloadView enDownloadView = (ENDownloadView) mLoadingProgressBar;
            if (enDownloadView.getCurrentState() == ENDownloadView.STATE_PRE) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
    }

    public interface   LoadVideoUrlListener{
        void onLoadUrl();
    }

    public void setLoadVideoUrlListener(LoadVideoUrlListener loadVideoUrlListener){
        mLoadVideoUrlListener = loadVideoUrlListener;
    }
}
