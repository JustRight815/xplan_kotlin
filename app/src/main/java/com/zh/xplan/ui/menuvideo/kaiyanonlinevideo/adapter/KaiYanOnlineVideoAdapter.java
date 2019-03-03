package com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.zh.xplan.R;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.bean.DataBean;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.bean.ItemListBean;
import com.zh.xplan.ui.menuvideo.localvideo.StringUtils;
import com.zh.xplan.ui.playeractivity.OrientationUtilsMy;
import com.zh.xplan.ui.playeractivity.SampleCoverVideo;
import com.zh.xplan.ui.playeractivity.listener.SampleListener;
import com.zh.xplan.ui.view.CustomTextView;

import org.qcode.qskinloader.SkinManager;

import java.util.List;

public class KaiYanOnlineVideoAdapter extends BaseMultiItemQuickAdapter<ItemListBean, BaseViewHolder> implements BaseQuickAdapter.SpanSizeLookup{
    private OnItemClickLitener mOnItemClickLitener;
    private Context mContext;
    private OrientationUtilsMy orientationUtils;

    public KaiYanOnlineVideoAdapter(List<ItemListBean> data, Context context){
        super(data);
        setSpanSizeLookup(this);
        mContext = context;
        addItemType(0, R.layout.listview_item);
        addItemType(1, R.layout.list_home_text_item);
    }

    @Override
    protected void convert(BaseViewHolder helper, ItemListBean item) {
        if("video".equals(item.getType()) ) {
            bindListData(helper, item);
        }else if("textHeader".equals(item.getType()) ){
            bindTextData(helper, item);
        }
    }

    private void bindTextData(BaseViewHolder helper, ItemListBean item) {
        SkinManager.with(helper.itemView).applySkin(true);
        DataBean data = item.getData();
        CustomTextView customTextView = helper.getView(R.id.tv_home_text);
        customTextView.setText(data.getText());
    }

    @Override
    public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
        return 0;
    }

    private void bindListData(final BaseViewHolder helper, ItemListBean item) {
        SkinManager.getInstance().applySkin(helper.itemView, true);
        DataBean data = item.getData();
        helper.setText(R.id.video_title, data.getTitle()); // video_title不能和播放器里面的字段重名
        helper.setText(R.id.video_duration, "视频时长：" + StringUtils.gennerTime(data.getDuration()));// video_duration 不能和播放器里面的字段重名
        //增加封面
        final SampleCoverVideo gsyVideoPlayer = helper.getView(R.id.video_item_player);
        gsyVideoPlayer.loadCoverImage(data.getCover().getDetail(),0);

        helper.itemView.setClickable(true);
        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickLitener != null){
                    mOnItemClickLitener.onItemClick(v, helper.getLayoutPosition());

                }
            }
        });

        //增加 视频播放地址
        String url = data.getPlayUrl();
        gsyVideoPlayer.setUp(url, false, null, "" + data.getTitle());
        //是否点击封面可以播放
        gsyVideoPlayer.setThumbPlay(true);
        //增加title
//        gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        gsyVideoPlayer.getTitleTextView().setTextSize(16);
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
//                if (orientationUtils != null) {
//                    orientationUtils.setEnable(false);
//                    orientationUtils.releaseListener();
//                    orientationUtils = null;
//                }
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

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}
