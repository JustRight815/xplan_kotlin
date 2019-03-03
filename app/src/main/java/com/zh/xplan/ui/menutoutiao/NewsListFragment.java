package com.zh.xplan.ui.menutoutiao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.module.common.log.LogUtil;
import com.module.common.net.rx.NetManager;
import com.module.common.utils.NetworkUtils;
import com.module.common.utils.SpUtil;
import com.module.common.view.snackbar.SnackbarUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.menutoutiao.model.News;
import com.zh.xplan.ui.menutoutiao.model.NewsData;
import com.zh.xplan.ui.menutoutiao.model.NewsRecord;
import com.zh.xplan.ui.menutoutiao.model.response.NewsResponse;
import com.zh.xplan.ui.menutoutiao.view.PowerfulRecyclerView;
import com.zh.xplan.ui.menutoutiao.view.TipView;
import com.zh.xplan.ui.playeractivity.PlayerDetailActivity;
import com.zh.xplan.ui.view.pulltorefresh.PtrFrameLayout;
import com.zh.xplan.ui.view.pulltorefresh.PtrHandler;
import com.zh.xplan.ui.view.pulltorefresh.customheader.PullToRefreshLayout;
import com.zh.xplan.ui.webviewActivity.WeatherDetailsActivity;

import org.qcode.qskinloader.SkinManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;

/**
 * @author 参考了 ChayChan大哥的项目源码
 * @description: 仿今日头条每个频道新闻列表的fragment
 */
public class NewsListFragment extends BaseFragment implements BaseQuickAdapter.RequestLoadMoreListener {
    TipView mTipView;
    PowerfulRecyclerView mRvNews;
    private String mChannelCode;
    private boolean isVideoList;//是否是视频列表页面,根据判断频道号是否是视频
    private boolean isRecommendChannel;//是否是推荐频道
    private List<News> mNewsList = new ArrayList<>();
    protected BaseQuickAdapter mNewsAdapter;
    private Gson mGson = new Gson();
    //新闻记录
    private NewsRecord mNewsRecord;
    /** 标志位，标志界面是否已经初始化完成 */
    private boolean isPrepared;
    /** 是否已被加载过一次，第二次就不再去请求数据了 */
    private boolean mHasLoadedOnce;
    //当前fragment是否可见
    protected boolean isVisible;
    private View mView;
    private PullToRefreshLayout mPtrFrame;//下拉刷新
    private static Handler mHandler = new Handler();//主线程Handler
    private Button mToTopBtn;// 返回顶部的按钮

    private LinearLayout mContentLayout;//

    public static NewsListFragment newInstance(String channelCode,boolean isVideoList) {
        Bundle args = new Bundle();
        args.putString(Constant.CHANNEL_CODE, channelCode);
        args.putBoolean(Constant.IS_VIDEO_LIST, isVideoList);
        NewsListFragment fragment = new NewsListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()){
            isVisible=true;
            lazyLoad();
        }else {
            isVisible=false;
            GSYVideoPlayer.releaseAllVideos();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtil.e("zh","onCreateView");
        if (mView == null) {
            mView =  View.inflate(getActivity(),
                    R.layout.fragment_news_list, null);
            isPrepared = true;
            LogUtil.e("zh","onCreateView  inflate");
            lazyLoad();
        }
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SkinManager.getInstance().applySkin(view, true);
    }

    /**
     * 避免Fragment中的view重复加载   避免Fragment中的数据重复加载
     */
    private void lazyLoad() {
        if (!isPrepared||!isVisible || mHasLoadedOnce) {
            return;
        }
        LogUtil.e("zh","onCreateView  lazyLoad");
        initView(mView);
        initData();
        initListener();
        loadData();
    }

    public void initView(View mView) {
        mTipView = (TipView) mView.findViewById(R.id.tip_view);
        mRvNews = (PowerfulRecyclerView) mView.findViewById(R.id.rv_news);
        mRvNews.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        mToTopBtn = (Button) mView.findViewById(R.id.btn_top);
        mToTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRvNews.scrollToPosition(0);
            }
        });

        mPtrFrame = (PullToRefreshLayout) mView.findViewById(R.id.rotate_header_list_view_frame);
        mPtrFrame.setNeedDelayComplete(false);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                if (!NetworkUtils.isNetworkAvailable(getActivity())) {
                    //网络不可用弹出提示
                    mPtrFrame.refreshComplete();
                    mTipView.show();
                    return;
                }
                updateData(true);
            }
        });
        mContentLayout = (LinearLayout) mView.findViewById(R.id.ll_content);
        mTipView.setAnimation(new TipView.OnAnimation() {
            @Override
            public void onAnimationStart() {
                hide();
            }
        });
    }

    /**隐藏，收起*/
    private void hide() {
        final TranslateAnimation hideAnim = new TranslateAnimation(0,0,0,-mTipView.getHeight());
        hideAnim.setDuration(200);
        hideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTipView.reset();
                //防止动画结束后，界面抖动
                TranslateAnimation anim = new TranslateAnimation(0,0,0,0);
                mContentLayout.setAnimation(anim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mContentLayout.startAnimation(hideAnim);
    }

    public void initData() {
        mChannelCode = getArguments().getString(Constant.CHANNEL_CODE);
        isVideoList = getArguments().getBoolean(Constant.IS_VIDEO_LIST, false);

        String[] channelCodes = getResources().getStringArray(R.array.channel_code);
        isRecommendChannel = mChannelCode.equals(channelCodes[0]);//是否是推荐频道
    }

    public void initListener() {
        mNewsAdapter = new NewsAdapter(getActivity(), mChannelCode, isVideoList, mNewsList);
        mRvNews.setAdapter(mNewsAdapter);

        mNewsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                News news = mNewsList.get(position);
                String itemId = news.item_id;
                StringBuffer urlSb = new StringBuffer("https://m.toutiao.com/i");
                urlSb.append(itemId).append("/info/");
                String url = urlSb.toString();//http://m.toutiao.com/i6412427713050575361/info/
                Intent intent = null;
                if (news.has_video) {
                    //视频
                    intent = new Intent(getActivity(), PlayerDetailActivity.class);
                    intent.putExtra("playUrl", "");
                    intent.putExtra("playTitle", news.title);
                    intent.putExtra("playDescription", "");
                    String picUrl = "";
                    if(news.video_detail_info != null ){
                        if(news.video_detail_info.detail_video_large_image != null ){
                            picUrl =  news.video_detail_info.detail_video_large_image.url;
                        }
                        intent.putExtra("playPic",picUrl);
                        intent.putExtra("playId", news.video_detail_info.video_id);
                    }
                } else {
                    //非视频新闻
                    if (news.article_type == 1) {
                        //如果article_type为1，则是使用WebViewActivity打开   纯网页文章，直接加载webview
                        intent = new Intent(getActivity(), WeatherDetailsActivity.class);
                        intent.putExtra("URL", news.article_url);
                        startActivity(intent);
                        return;
                    }
                    //其他新闻  非纯网页文章，需要加载标题 图片等信息
                    intent = new Intent(getActivity(), NewsDetailsActivity.class);
                }
                intent.putExtra(NewsDetailsActivity.CHANNEL_CODE, mChannelCode);
                intent.putExtra(NewsDetailsActivity.POSITION, position);
                intent.putExtra(NewsDetailsActivity.DETAIL_URL, url);
                intent.putExtra(NewsDetailsActivity.GROUP_ID, news.group_id);
                intent.putExtra(NewsDetailsActivity.ITEM_ID, itemId);
                startActivity(intent);
            }
        });

        mNewsAdapter.setEnableLoadMore(true);
        mNewsAdapter.setOnLoadMoreListener(this, mRvNews);


            mRvNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
                int firstVisibleItem, lastVisibleItem;

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager mLayoutManager = (LinearLayoutManager) mRvNews.getLayoutManager();
                    int fistVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    // 判断是否滚动超过一屏
                    if (0 == fistVisibleItem) {
                        mToTopBtn.setVisibility(GONE);
                    } else {
                        mToTopBtn.setVisibility(View.VISIBLE);
                    }
                    if (isVideoList) {
                        firstVisibleItem   = mLayoutManager.findFirstVisibleItemPosition();
                        lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                        //大于0说明有播放
                        if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                            //当前播放的位置
                            int position = GSYVideoManager.instance().getPlayPosition();
                            if((position < firstVisibleItem || position > lastVisibleItem)){
                                GSYVideoPlayer.releaseAllVideos();
                                mNewsAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
    }

    protected void loadData() {
//        mStateView.showLoading();
        mHasLoadedOnce = true;
        //查找该频道的最后一组记录
        mNewsRecord = NewsRecordHelper.getLastNewsRecord(mChannelCode);
        if (mNewsRecord == null) {
//            找不到记录，拉取网络数据
            mNewsRecord = new NewsRecord();//创建一个没有数据的对象
            updateData(true);
            return;
        }

        //找到最后一组记录，转换成新闻集合并展示
        List<News> newsList = NewsRecordHelper.convertToNewsList(mNewsRecord.getJson());
        mNewsList.addAll(newsList);//添加到集合中
        mNewsAdapter.notifyDataSetChanged();//刷新adapter
//        mStateView.showContent();//显示内容
        //判断时间是否超过10分钟，如果是则自动刷新
        if (Math.abs(mNewsRecord.getTime() - System.currentTimeMillis()) >= 10 * 60 * 1000) {
            mPtrFrame.autoRefresh();
        }
    }

    long lastTime;
    private void updateData(final Boolean isPullDownRefresh) {
        lastTime = SpUtil.getFromLocal(getActivity(),"news",mChannelCode,0);//读取对应频道下最后一次刷新的时间戳
        if (lastTime == 0){
            //如果为空，则是从来没有刷新过，使用当前时间戳
            lastTime = System.currentTimeMillis() / 1000;
        }
        String BASE_SERVER_URL = "http://is.snssdk.com/";
        String GET_ARTICLE_LIST = "api/news/feed/v62/?refer=1&count=20&loc_mode=4&device_id=34960436458&iid=13136511752";
        String url = BASE_SERVER_URL + GET_ARTICLE_LIST;
        LogUtil.e("zh",":::url: " + url);
        if(url == null && isPullDownRefresh == false){
            mNewsAdapter.loadMoreEnd(false);
            return;
        }

        NetManager.get()
                .url(url)
                .params("category",mChannelCode)
                .params("min_behot_time",lastTime)
                .params("last_refresh_sub_entrance_interval",System.currentTimeMillis()/1000)
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {

                    @Override
                    public void onNext(String response) {
                        LogUtil.e("zh",":::onSuccess response: " + response);
                        if (response == null ) {
                            if (isPullDownRefresh) {
                                mPtrFrame.refreshComplete();
                            }
                            return;
                        }
                        lastTime = System.currentTimeMillis() / 1000;
                        NewsResponse newsResponse = new Gson().fromJson(response,NewsResponse.class);
                        List<NewsData> data = newsResponse.data;
                        List<News> newsList = new ArrayList<>();
                        if (!ListUtils.isEmpty(data)){
                            for (NewsData newsData : data) {
                                News news = new Gson().fromJson(newsData.content, News.class);
                                if(news == null || (news.has_video && (news.video_detail_info == null || news.video_detail_info.video_id == null) )){
                                    continue;
                                }
                                if (isVideoList && ! news.has_video) {
                                    LogUtil.e("zh","***************** news.has_video: ");
                                    continue;
                                }
//                                LogUtil.e("zh","News +++++= " + news.toString());
                                if(TextUtils.isEmpty(news.title)){
                                    //由于汽车、体育等频道第一条属于导航的内容，所以如果第一条没有标题，则移除
                                    continue;
                                }
                                newsList.add(news);
                            }
                        }
                        onGetNewsListSuccess(newsList,newsResponse.tips.display_info);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.e("zh",":::onFailure response: " + e.toString());
                        SnackbarUtils.ShortToast(mView,"数据请求失败");

                        if (isPullDownRefresh) {
                            mPtrFrame.refreshComplete();
                        }
                        mTipView.show();//弹出提示
                        if (ListUtils.isEmpty(mNewsList)) {
                            //如果一开始进入没有数据
                            //            mStateView.showRetry();//显示重试的布局
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void onGetNewsListSuccess(List<News> newList, String tipInfo) {
//        mRefreshLayout.endRefreshing();// 加载完毕后在 UI 线程结束下拉刷新
        //如果是第一次获取数据
        if (ListUtils.isEmpty(mNewsList)) {
            if (ListUtils.isEmpty(newList)) {
                //获取不到数据,显示空布局
//                mStateView.showEmpty();
                SnackbarUtils.ShortToast(mPtrFrame,"应用需要审核才能使用");
                mPtrFrame.refreshComplete();
                return;
            }
//            mStateView.showContent();//显示内容
        }

        if (ListUtils.isEmpty(newList)) {
            //已经获取不到新闻了，处理出现获取不到新闻的情况
            SnackbarUtils.ShortToast(mPtrFrame,"应用需要审核才能使用");
            mPtrFrame.refreshComplete();
            return;
        }
        dealRepeat(newList);//处理新闻重复问题
        mNewsList.addAll(0, newList);
        mNewsAdapter.notifyDataSetChanged();
        mPtrFrame.refreshComplete();
        mTipView.show(tipInfo);
        //保存到数据库
        NewsRecordHelper.save(mChannelCode, mGson.toJson(newList));
    }

    /**
     * 处理置顶新闻和广告重复
     */
    private void dealRepeat(List<News> newList) {
        if (isRecommendChannel && !ListUtils.isEmpty(mNewsList)) {
//            //如果是推荐频道并且数据列表已经有数据,处理置顶新闻或广告重复的问题
            mNewsList.remove(0);//由于第一条新闻是重复的，移除原有的第一条
//            //新闻列表通常第4个是广告,除了第一次有广告，再次获取的都移除广告
//            if (newList.size() >= 4) {
//                News fourthNews = newList.get(3);
//                //如果列表第4个和原有列表第4个新闻都是广告，并且id一致，移除
//                if (Constant.ARTICLE_GENRE_AD.equals(fourthNews.tag)) {
//                    newList.remove(fourthNews);
//                }
//            }
        }
    }

//    /**
//     * 详情页关闭后传递过来的事件,更新评论数播放进度等
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
//    public void onDetailCloseEvent(DetailCloseEvent event) {
//        if (!event.getChannelCode().equals(mChannelCode)) {
//            //如果频道不一致，不用处理
//            return;
//        }
//
//        int position = event.getPosition();
//        int commentCount = event.getCommentCount();
//
//        News news = mNewsList.get(position);
//        news.comment_count = commentCount;
//
//        if (news.video_detail_info != null){
//            //如果有视频
//            int progress = event.getProgress();
//            news.video_detail_info.progress = progress;
//        }
//
//        //刷新adapter
//        mNewsList.set(position, news);
//        mNewsAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onLoadMoreRequested() {
// BaseRecyclerViewAdapterHelper的加载更多
        if (mNewsRecord.getPage() == 0 || mNewsRecord.getPage() == 1) {
            //如果记录的页数为0(即是创建的空记录)，或者页数为1(即已经是第一条记录了)
            //mRefreshLayout.endLoadingMore();//结束加载更多
            mNewsAdapter.loadMoreEnd();
            return;
        }

        NewsRecord preNewsRecord = NewsRecordHelper.getPreNewsRecord(mChannelCode, mNewsRecord.getPage());
        if (preNewsRecord == null) {
            // mRefreshLayout.endLoadingMore();//结束加载更多
            mNewsAdapter.loadMoreEnd();
            return;
        }

        mNewsRecord = preNewsRecord;

        long startTime = System.currentTimeMillis();
        final List<News> newsList = NewsRecordHelper.convertToNewsList(mNewsRecord.getJson());
        if (isRecommendChannel) {
            //如果是推荐频道
            newsList.remove(0);//移除第一个，因为第一个是置顶新闻，重复
        }
        long endTime = System.currentTimeMillis();

        //由于是读取数据库，如果耗时不足1秒，则1秒后才收起加载更多
        if (endTime - startTime <= 1000) {

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNewsAdapter.loadMoreComplete();
                    mNewsList.addAll(newsList);//添加到集合下面
                    mNewsAdapter.notifyDataSetChanged();//刷新adapter
                }
            }, (int) (1000 - (endTime - startTime)));
        }else{
            mNewsAdapter.loadMoreComplete();
            mNewsList.addAll(newsList);//添加到集合下面
            mNewsAdapter.notifyDataSetChanged();//刷新adapter
        }
    }

    public boolean onBackPressed() {
        return StandardGSYVideoPlayer.backFromWindowFull(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        GSYVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GSYVideoPlayer.releaseAllVideos();
    }

}
