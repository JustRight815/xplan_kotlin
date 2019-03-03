package com.zh.xplan.ui.playeractivity;

import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.module.common.log.LogUtil;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.menutoutiao.GetVideoUrl;
import com.zh.xplan.ui.menutoutiao.model.VideoContentBean;
import com.zh.xplan.ui.playeractivity.listener.SampleListener;

import io.reactivex.observers.DisposableObserver;

/**
 * 默认半屏显示，可随重力感应改变视频方向
 */
public class PlayerDetailActivity extends BaseActivity {

    NestedScrollView postDetailNestedScroll;
    //推荐使用StandardGSYVideoPlayer，功能一致
    SampleCoverVideo detailPlayer;
    LinearLayout activityDetailPlayer;
    private TextView mVideoName;// 视频名称
    private TextView mVideoDec;// 视频描述


    private boolean isPlay;
    private boolean isPause;
    private OrientationUtilsMy orientationUtils;
    public String playUrl;
    public String playTitle;
    public String playDescription;
    public String playPic;
    public String playId;

//    @Override
//    public boolean isSupportSwipeBack() {
//        return false;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_player);
        StatusBarUtil.setColor(this,getResources().getColor(R.color.black));
        initViews();
        initDatas();
    }

    private void initViews() {
        detailPlayer  = (SampleCoverVideo) findViewById(R.id.detail_player);
        activityDetailPlayer   = (LinearLayout) findViewById(R.id.activity_detail_player);
        mVideoName = (TextView)findViewById(R.id.video_name);
        mVideoDec = (TextView)findViewById(R.id.video_dec);
    }

    private void initDatas() {
        playUrl = getIntent().getStringExtra("playUrl");
        playTitle = getIntent().getStringExtra("playTitle");
        playDescription = getIntent().getStringExtra("playDescription");
        playPic = getIntent().getStringExtra("playPic");
        playId = getIntent().getStringExtra("playId");
        //增加封面
        detailPlayer.getTitleTextView().setTextSize(16);
        detailPlayer.loadCoverImage(playPic + "",0);
        if(! TextUtils.isEmpty(playId)){
            GetVideoUrl.doLoadVideoData(playId, new DisposableObserver<String>() {
                @Override
                public void onNext(String response) {
                    LogUtil.e("zh","doLoadVideoData response " + response);
                    VideoContentBean videoContentBean = new Gson().fromJson(response,VideoContentBean.class);
                    String url = null;
                    if(videoContentBean != null && videoContentBean.getData() != null ){
                        VideoContentBean.DataBean.VideoListBean videoList = videoContentBean.getData().getVideo_list();
                        if(videoList != null ){
                            if (videoList.getVideo_3() != null) {
                                String base64 = videoList.getVideo_3().getMain_url();
                                url = (new String(Base64.decode(base64.getBytes(), Base64.DEFAULT)));
                                Log.e("zh", "getVideoUrls getVideo_3: " + url);
                            }else if (videoList.getVideo_2() != null) {
                                String base64 = videoList.getVideo_2().getMain_url();
                                url = (new String(Base64.decode(base64.getBytes(), Base64.DEFAULT)));
                                Log.e("zh", "getVideoUrls getVideo_2: " + url);
                            }else if (videoList.getVideo_1() != null) {
                                String base64 = videoList.getVideo_1().getMain_url();
                                url = (new String(Base64.decode(base64.getBytes(), Base64.DEFAULT)));
                                Log.e("zh", "getVideoUrls getVideo_2: " + url);
                            }
                        }
                    }
                    if (url != null){
                        playUrl = url;
                        detailPlayer.setUp(url, false, null, "" + playTitle);
                    }else{
                        detailPlayer.setUp("", false, null, "" + playTitle);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    LogUtil.e("zh","+++++++++++++++++++++onError msg " + e.toString());
                    detailPlayer.setUp("", false, null, "" + playTitle);
                }

                @Override
                public void onComplete() {

                }
            });
        }else{
            detailPlayer.setUp(playUrl, false, null, "" + playTitle);
        }
        //detailPlayer.setLooping(true);
        //detailPlayer.setShowPauseCover(false);
        /*VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);*/
        //GSYVideoManager.instance().setTimeOut(4000, true);

        mVideoName.setText(playTitle);
        mVideoDec.setText(playDescription);


        resolveNormalVideoUI();
        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtilsMy(this, detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);
        detailPlayer.setIsTouchWiget(true);
        //detailPlayer.setIsTouchWigetFull(false);
        //关闭自动旋转
//        detailPlayer.setRotateViewAuto(true);
        //重力旋转
        orientationUtils.setRotateWithSystem(false);
        //是否点击封面可以播放
        detailPlayer.setThumbPlay(true);
//        detailPlayer.setLockLand(true);
        detailPlayer.setShowFullAnimation(false);
        detailPlayer.setNeedLockFull(true);
        //detailPlayer.setOpenPreView(false);
        detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                detailPlayer.startWindowFullscreen(PlayerDetailActivity.this, true, true);
            }
        });

        detailPlayer.setStandardVideoAllCallBack(new SampleListener() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                //开始播放了才能旋转和全屏
                orientationUtils.setEnable(true);
                isPlay = true;
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
            }

            @Override
            public void onClickStartError(String url, Object... objects) {
                super.onClickStartError(url, objects);
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
            }
        });

        detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });

    }



    @Override
    public void onBackPressed() {

        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }

        if (StandardGSYVideoPlayer.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoPlayer.releaseAllVideos();
        //GSYPreViewManager.instance().releaseMediaPlayer();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        LogUtil.e("zh","onConfigurationChanged "  + newConfig.orientation);
//        super.onConfigurationChanged(newConfig);
//        //如果旋转了就全屏
//        if (isPlay && !isPause  ) {
//            LogUtil.e("zh","isPlay " );
//            if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
//                if (!detailPlayer.isIfCurrentIsFullscreen()) {
//                    detailPlayer.startWindowFullscreen(PlayerDetailActivity.this, true, true);
//                }
//            } else {
//                //新版本isIfCurrentIsFullscreen的标志位内部提前设置了，所以不会和手动点击冲突
//                boolean autoRotateOn = (Settings.System.getInt(PlayerDetailActivity.this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
//                if(autoRotateOn){
//                    if (detailPlayer.isIfCurrentIsFullscreen()) {
//                        StandardGSYVideoPlayer.backFromWindowFull(this);
//                    }
//                    if (orientationUtils != null) {
//                        orientationUtils.setEnable(true);
//                    }
//                }
//            }
//        }
//    }


    private void resolveNormalVideoUI() {
        //增加title
//        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getTitleTextView().setText("" + playTitle);
        detailPlayer.getBackButton().setVisibility(View.GONE);
    }

}
