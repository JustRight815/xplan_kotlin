package com.zh.xplan.ui.menutoutiao;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.google.gson.Gson;
import com.module.common.image.ImageLoader;
import com.module.common.log.LogUtil;
import com.module.common.utils.PixelUtil;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.zh.xplan.R;
import com.zh.xplan.ui.menutoutiao.model.News;
import com.zh.xplan.ui.menutoutiao.model.VideoContentBean;
import com.zh.xplan.ui.playeractivity.OrientationUtilsMy;
import com.zh.xplan.ui.playeractivity.SampleCoverVideo;
import com.zh.xplan.ui.playeractivity.listener.SampleListener;

import java.util.List;

import io.reactivex.observers.DisposableObserver;

/**
 * @author ChayChan
 * @description: TODO
 * @date 2017/7/6  17:10
 */
public class NewsAdapter extends BaseQuickAdapter<News, BaseViewHolder> {

    /**
     * 纯文字布局(文章、广告)
     */
    private static final int TEXT_NEWS = 100;
    /**
     * 居中大图布局(1.单图文章；2.单图广告；3.视频，中间显示播放图标，右侧显示时长)
     */
    private static final int CENTER_SINGLE_PIC_NEWS = 200;
    /**
     * 右侧小图布局(1.小图新闻；2.视频类型，右下角显示视频时长)
     */
    private static final int RIGHT_PIC_VIDEO_NEWS = 300;
    /**
     * 三张图片布局(文章、广告)
     */
    private static final int THREE_PICS_NEWS = 400;

    /**
     * 视频列表类型
     */
    private static final int VIDEO_LIST_NEWS = 500;


    private Context mContext;
    private String mChannelCode;
    private boolean isVideoList;

    private OrientationUtilsMy orientationUtils;


    /**
     * @param context     上下文
     * @param channelCode 频道
     * @param isVideoList 是否是视频列表
     * @param data        新闻集合
     */
    public NewsAdapter(Context context, String channelCode, final boolean isVideoList, List<News> data) {
        super(data);
        mContext = context;
        mChannelCode = channelCode;
        this.isVideoList = isVideoList;

        //Step.1
        setMultiTypeDelegate(new MultiTypeDelegate<News>() {
            @Override
            protected int getItemType(News news) {
                if (isVideoList) {
                    return VIDEO_LIST_NEWS;
                }

                if (news.has_video) {
                    //如果有视频
                    if (news.video_style ==0) {
                        //右侧视频
                        if (news.middle_image == null || TextUtils.isEmpty(news.middle_image.url)){
                            return TEXT_NEWS;
                        }
                        return RIGHT_PIC_VIDEO_NEWS;
                    } else if (news.video_style == 2) {
                        //居中视频
                        return CENTER_SINGLE_PIC_NEWS;
                    }
                } else {
                    //非视频新闻
                    if (!news.has_image) {
                        //纯文字新闻
                        return TEXT_NEWS;
                    } else {
                        if (ListUtils.isEmpty(news.image_list)) {
                            //图片列表为空，则是右侧图片
                            return RIGHT_PIC_VIDEO_NEWS;
                        }

                        if (news.gallary_image_count == 3) {
                            //图片数为3，则为三图
                            return THREE_PICS_NEWS;
                        }

                        //中间大图，右下角显示图数
                        return CENTER_SINGLE_PIC_NEWS;
                    }
                }

                return TEXT_NEWS;
            }
        });
        //Step .2
        getMultiTypeDelegate()
                .registerItemType(TEXT_NEWS, R.layout.toutiao_item_text_news)//纯文字布局
                .registerItemType(CENTER_SINGLE_PIC_NEWS, R.layout.toutiao_item_center_pic_news)//居中大图布局
                .registerItemType(RIGHT_PIC_VIDEO_NEWS, R.layout.toutiao_item_pic_video_news)//右侧小图布局
                .registerItemType(THREE_PICS_NEWS, R.layout.toutiao_item_three_pics_news)//三张图片布局
                .registerItemType(VIDEO_LIST_NEWS, R.layout.toutiao_item_video_list);//居中视频
    }

    @Override
    protected void convert(BaseViewHolder helper, News news) {
        if (TextUtils.isEmpty(news.title)){
            //如果没有标题，则直接跳过
            return;
        }

        //设置标题、底部作者、评论数、发表时间
        if (!isVideoList) {
            //如果不是视频列表
            helper.setText(R.id.tv_title, news.title)
                    .setText(R.id.tv_author, news.source)
                    .setText(R.id.tv_comment_num, news.comment_count + "评论")
                    .setText(R.id.tv_time, TimeUtils.getShortTime(news.behot_time * 1000));
        }

        int width = PixelUtil.dp2px(100,mContext);
        //根据类型为不同布局的条目设置数据
        switch (helper.getItemViewType()) {
            case CENTER_SINGLE_PIC_NEWS:
                //中间大图布局，判断是否有视频
                TextView tvBottomRight = helper.getView(R.id.tv_bottom_right);
                if (news.has_video) {
                    helper.setVisible(R.id.iv_play, true);//显示播放按钮
                    tvBottomRight.setCompoundDrawables(null, null, null, null);//去除TextView左侧图标
                    helper.setText(R.id.tv_bottom_right, TimeUtils.secToTime(news.video_duration));//设置时长
                    Uri uri = Uri.parse(news.video_detail_info.detail_video_large_image.url);
                    ImageView iv = helper.getView(R.id.iv_img);
                    ImageLoader.displayImage(mContext,iv,uri,R.drawable.holder_img);
//                    ImageManager.displayImage(iv,uri,PixelUtil.dp2px(100,mContext),PixelUtil.dp2px(80,mContext));
                } else {
                    helper.setVisible(R.id.iv_play, false);//隐藏播放按钮
                    if (news.gallary_image_count == 1){
                        tvBottomRight.setCompoundDrawables(null, null, null, null);//去除TextView左侧图标
                    }else{
                        tvBottomRight.setCompoundDrawables(mContext.getResources().getDrawable(R.drawable.toutiao_icon_picture_group), null, null, null);//TextView增加左侧图标
                        helper.setText(R.id.tv_bottom_right, news.gallary_image_count + "图");//设置图片数
                    }
                    Uri uri = Uri.parse(news.image_list.get(0).url.replace("list/300x196", "large"));
                    ImageView iv = helper.getView(R.id.iv_img);
                    ImageLoader.displayImage(mContext,iv,uri);
                }
                break;
            case RIGHT_PIC_VIDEO_NEWS:
                //右侧小图布局，判断是否有视频
                if (news.has_video) {
                    helper.setVisible(R.id.ll_duration, true);//显示时长
                    helper.setText(R.id.tv_duration, TimeUtils.secToTime(news.video_duration));//设置时长
                } else {
                    helper.setVisible(R.id.ll_duration, false);//隐藏时长
                }
                Uri uri = Uri.parse(news.middle_image.url);
                ImageView iv = helper.getView(R.id.iv_img);
                ImageLoader.displayImage(mContext,iv,uri);
                break;
            case THREE_PICS_NEWS:
                //三张图片的新闻
                Uri uri1 = Uri.parse(news.image_list.get(0).url);
                Uri uri2 = Uri.parse(news.image_list.get(1).url);
                Uri uri3 = Uri.parse(news.image_list.get(2).url);

                ImageView iv1 = helper.getView(R.id.iv_img1);
                ImageView iv2 = helper.getView(R.id.iv_img2);
                ImageView iv3 = helper.getView(R.id.iv_img3);
                ImageLoader.displayImage(mContext,iv1,uri1);
                ImageLoader.displayImage(mContext,iv2,uri2);
                ImageLoader.displayImage(mContext,iv3,uri3);
                break;
            case VIDEO_LIST_NEWS:
                dealVideo(helper, news);
                return;//视频列表布局没有下面的设置标签的操作，直接return
        }

        //根据情况显示置顶、广告和热点的标签
        int position = helper.getAdapterPosition();
        String[] channelCodes = mContext.getResources().getStringArray(R.array.channel_code);
        boolean isTop = position == 0 && mChannelCode.equals(channelCodes[0]); //属于置顶
        boolean isHot = news.hot == 1;//属于热点新闻
        boolean isAD = !TextUtils.isEmpty(news.tag) && news.tag.equals(Constant.ARTICLE_GENRE_AD);//属于广告新闻
        boolean isMovie = !TextUtils.isEmpty(news.tag) && news.tag.equals(Constant.TAG_MOVIE);//如果是影视
        helper.setVisible(R.id.tv_tag, isTop || isHot || isAD);//如果是上面任意一个，显示标签
        helper.setVisible(R.id.tv_comment_num, !isAD);//如果是广告，则隐藏评论数

        String tag = "";
        if (isTop) {
            tag = "置顶";
            helper.setTextColor(R.id.tv_tag,  mContext.getResources().getColor(R.color.news_tag_border_red));
        } else if (isHot) {
            tag = "热";
            helper.setTextColor(R.id.tv_tag,  mContext.getResources().getColor(R.color.news_tag_border_red));
        } else if (isAD) {
            tag = "广告";
            helper.setTextColor(R.id.tv_tag,  mContext.getResources().getColor(R.color.news_tag_border_blue));
        } else if (isMovie) {
            //如果是影视
            tag = "影视";
            helper.setTextColor(R.id.tv_tag,  mContext.getResources().getColor(R.color.news_tag_border_red));
        }
        helper.setText(R.id.tv_tag, tag);
    }

    private void dealVideo(final BaseViewHolder helper, final News news) {
        //增加封面
        if(news.video_detail_info == null || news.video_detail_info.video_id == null ){
            return;
        }
        final SampleCoverVideo gsyVideoPlayer = helper.getView(R.id.video_item_player);
        String picUrl = "";
        if(news.video_detail_info != null && news.video_detail_info.detail_video_large_image != null ){
            picUrl =  news.video_detail_info.detail_video_large_image.url;
        }

        gsyVideoPlayer.loadCoverImage(picUrl,0);

        //增加title
//        gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        gsyVideoPlayer.getTitleTextView().setTextSize(16);
        //增加 视频播放地址   先设置标题，防止出现黑色背景
        if(TextUtils.isEmpty(news.videoUrl)){
            gsyVideoPlayer.setUp("", false, null, "" + news.title);
        }else {
            gsyVideoPlayer.setUp(news.videoUrl, false, null, "" + news.title);
        }

//        helper.setVisible(R.id.ll_title,true);//显示标题栏
//        helper.setText(R.id.tv_title, news.title);//设置标题
//        String format = mContext.getResources().getString(R.string.video_play_count);
//        int watchCount = news.video_detail_info.video_watch_count;
//        String countUnit = "";
//        if (watchCount> 10000){
//            watchCount = watchCount / 10000;
//            countUnit = "万";
//        }
//        helper.setText(R.id.tv_watch_count, String.format(format, watchCount + countUnit));//播放次数

        ImageView headPic = helper.getView(R.id.iv_avatar);
        ImageLoader.displayImageCircleCrop(mContext,headPic,Uri.parse(news.user_info.avatar_url),0);

        helper.setVisible(R.id.ll_duration, true)//显示时长
                .setText(R.id.tv_duration, TimeUtils.secToTime(news.video_duration));//设置时长

        helper.setText(R.id.tv_author, news.user_info.name)//昵称
                .setText(R.id.tv_comment_count, String.valueOf(news.comment_count));//评论数



        helper.itemView.setClickable(true);
        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e("zh","dealVideo #####news.url  " + news.url);
            }
        });

        gsyVideoPlayer.setLoadVideoUrlListener(new SampleCoverVideo.LoadVideoUrlListener() {
            @Override
            public void onLoadUrl() {
                GetVideoUrl.doLoadVideoData(news.video_detail_info.video_id, new DisposableObserver<String>() {
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
                                    Log.e(TAG, "getVideoUrls getVideo_3: " + url);
                                }else if (videoList.getVideo_2() != null) {
                                    String base64 = videoList.getVideo_2().getMain_url();
                                    url = (new String(Base64.decode(base64.getBytes(), Base64.DEFAULT)));
                                    Log.e(TAG, "getVideoUrls getVideo_2: " + url);
                                }else if (videoList.getVideo_1() != null) {
                                    String base64 = videoList.getVideo_1().getMain_url();
                                    url = (new String(Base64.decode(base64.getBytes(), Base64.DEFAULT)));
                                    Log.e(TAG, "getVideoUrls getVideo_2: " + url);
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(url)){
                            news.videoUrl = url;
                            gsyVideoPlayer.setUp(url, false, null, "" + news.title);
                            gsyVideoPlayer.clickStartIcon();
                        }else{
                            Toast.makeText(mContext, "播放地址获取失败", Toast.LENGTH_SHORT).show();
                            gsyVideoPlayer.setUp("", false, null, "" + news.title);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.e("zh","+++++++++++++++++++++onError msg " + e.toString());
                        Toast.makeText(mContext, "播放地址获取失败", Toast.LENGTH_SHORT).show();
                        gsyVideoPlayer.setUp("", false, null, "" + news.title);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });

        //是否点击封面可以播放
        gsyVideoPlayer.setThumbPlay(true);
        //设置返回键
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
        //设置全屏按键功能
        gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveFullBtn(gsyVideoPlayer);
            }
        });
//        gsyVideoPlayer.setRotateViewAuto(true);
//        gsyVideoPlayer.setLockLand(true);
        gsyVideoPlayer.setPlayTag(TAG);
        gsyVideoPlayer.setShowFullAnimation(false);
        //循环
        //gsyVideoPlayer.setLooping(true);
        gsyVideoPlayer.setNeedLockFull(true);

        //gsyVideoPlayer.setSpeed(2);

        gsyVideoPlayer.setPlayPosition(helper.getLayoutPosition());
        gsyVideoPlayer.setStandardVideoAllCallBack(new SampleListener(){
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                if (orientationUtils != null) {
                    orientationUtils.setEnable(false);
                    orientationUtils.releaseListener();
                    orientationUtils = null;
                }
                orientationUtils = new OrientationUtilsMy((Activity) mContext, gsyVideoPlayer);
                orientationUtils.setEnable(true);
                orientationUtils.setRotateWithSystem(false);
            }

            @Override
            public void onClickStartIcon(String url, Object... objects) {
                super.onClickStartIcon(url, objects);
                //点击播放
                helper.setVisible(R.id.ll_duration, false);//隐藏时长
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                super.onEnterFullscreen(url, objects);
                GSYVideoManager.instance().setNeedMute(false);
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
            }

        });
    }

    /**
     * 全屏幕按键处理
     */
    private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {
        if(orientationUtils != null){
            orientationUtils.resolveByClick();
        }
        standardGSYVideoPlayer.startWindowFullscreen(mContext, true, true);
    }

}
