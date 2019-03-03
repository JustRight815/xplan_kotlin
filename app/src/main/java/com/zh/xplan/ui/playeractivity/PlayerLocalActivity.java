package com.zh.xplan.ui.playeractivity;

import android.os.Bundle;
import android.transition.Transition;
import android.view.View;

import com.jaeger.library.StatusBarUtil;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.menuvideo.localvideo.model.LocalVideoBean;
/**
 * 单独的视频播放页面
 */
public class PlayerLocalActivity extends BaseActivity {

    public final static String IMG_TRANSITION = "IMG_TRANSITION";
    public final static String TRANSITION = "TRANSITION";
    public final static String DATA = "DATA";

    private SampleCoverVideo videoPlayer;
    private OrientationUtils orientationUtils;
    private boolean isTransition;
    private Transition transition;
    private LocalVideoBean mLocalVideoBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        StatusBarUtil.setColor(this,getResources().getColor(R.color.black));
        isTransition = getIntent().getBooleanExtra(TRANSITION, false);
        mLocalVideoBean = getIntent().getParcelableExtra("video");
        init();
    }

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    private void init() {
        videoPlayer  = (SampleCoverVideo) findViewById(R.id.video_player);
        videoPlayer.setUp(mLocalVideoBean.getPath(), false, mLocalVideoBean.getTitle());

        //增加封面
        videoPlayer.loadCoverImage("file:///" + mLocalVideoBean.getPath(),0);

        //增加title
        videoPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        videoPlayer.getTitleTextView().setTextSize(16);
        //设置返回键
        videoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置旋转
        orientationUtils = new OrientationUtils(this, videoPlayer);
        //重力旋转
        orientationUtils.setRotateWithSystem(false);
        videoPlayer.setShowFullAnimation(false);
        videoPlayer.setNeedLockFull(true);

        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveFullBtn(videoPlayer);
            }
        });
        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(true);
        //设置返回按键功能
        videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        videoPlayer.startPlayLogic();
        resolveFullBtn(videoPlayer);
    }

    /**
     * 全屏幕按键处理
     */
    private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {
        if(orientationUtils != null){
            orientationUtils.resolveByClick();
        }
        //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
        standardGSYVideoPlayer.startWindowFullscreen(PlayerLocalActivity.this, true, true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (StandardGSYVideoPlayer.backFromWindowFull(this)) {
            return;
        }
        GSYVideoPlayer.releaseAllVideos();
        super.onBackPressed();
    }
}
