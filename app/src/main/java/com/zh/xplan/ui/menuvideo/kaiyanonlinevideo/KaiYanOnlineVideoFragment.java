package com.zh.xplan.ui.menuvideo.kaiyanonlinevideo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.module.common.log.LogUtil;
import com.module.common.view.snackbar.SnackbarUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.zh.xplan.AppConstants;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.adapter.KaiYanOnlineVideoAdapter;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.bean.DataBean;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.bean.ItemListBean;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.bean.VideoListBean;
import com.zh.xplan.ui.playeractivity.PlayerDetailActivity;
import com.zh.xplan.ui.view.pulltorefresh.PtrFrameLayout;
import com.zh.xplan.ui.view.pulltorefresh.PtrHandler;
import com.zh.xplan.ui.view.pulltorefresh.customheader.PullToRefreshLayout;
import com.zh.xplan.ui.view.stateiew.StateView;

import org.qcode.qskinloader.SkinManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 开眼在线视频 数据来自每日精选接口 Created by zh
 */
public class KaiYanOnlineVideoFragment extends BaseFragment implements OnClickListener,OnlineVideoView{
	private View mContentView;
	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLayoutManager;
	private KaiYanOnlineVideoAdapter mOnlineVideoAdapter;
	private List<ItemListBean> mVideoList;
	private Button mToTopBtn;// 返回顶部的按钮
	private PullToRefreshLayout mPtrFrame;//下拉刷新
	private String mDate = "";
	private StateView mStateView;//加载状态控件
	private View mStateViewRetry;//错误状态布局的根布局
	private OnlineVideoPresenter presenter;

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (getUserVisibleHint()){
		}else {
			GSYVideoPlayer.releaseAllVideos();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = View.inflate(getActivity(),
				R.layout.fragment_online_video, null);
		initView(mContentView);
		presenter  = new OnlineVideoPresenter();
		presenter.attachView(this);
		initDatas();
		return mContentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SkinManager.getInstance().applySkin(view, true);
	}

	/**
	 * 初始化views
	 * 
	 * @param mView
	 */
	private void initView(View mView) {
		mStateView = (StateView) mView.findViewById(R.id.mStateView);
		mStateViewRetry  =  mView.findViewById(R.id.ll_stateview_error);
		mStateViewRetry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStateView.setCurrentState(StateView.STATE_LOADING);
				updateData(AppConstants.DAILY,"",true);
			}
		});

		initPtrFrame(mView);
		mToTopBtn = (Button) mView.findViewById(R.id.btn_top);
		mToTopBtn.setOnClickListener(this);
		mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

			}
		});

		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			int firstVisibleItem, lastVisibleItem;

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int fistVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
				// 判断是否滚动超过一屏
				if (0 == fistVisibleItem) {
					mToTopBtn.setVisibility(View.GONE);
				} else {
					mToTopBtn.setVisibility(View.VISIBLE);
				}
				firstVisibleItem   = mLayoutManager.findFirstVisibleItemPosition();
				lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
				//大于0说明有播放
				if (GSYVideoManager.instance().getPlayPosition() >= 0) {
					//当前播放的位置
					int position = GSYVideoManager.instance().getPlayPosition();
					if((position < firstVisibleItem || position > lastVisibleItem)){
						GSYVideoPlayer.releaseAllVideos();
						mOnlineVideoAdapter.notifyDataSetChanged();
					}
				}
			}
		});
	}

	public boolean onBackPressed() {
        return StandardGSYVideoPlayer.backFromWindowFull(getActivity());
    }


	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden){
			GSYVideoPlayer.releaseAllVideos();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		GSYVideoPlayer.releaseAllVideos();
	}

	@Override
	public void onDestroy() {
		if(presenter != null){
			presenter.onDestory();
		}
		super.onDestroy();
		GSYVideoPlayer.releaseAllVideos();
	}

	/**
	 * 初始化下拉刷新
	 */
	private void initPtrFrame(View rootView) {
		mPtrFrame = (PullToRefreshLayout) rootView.findViewById(R.id.rotate_header_list_view_frame);
		mPtrFrame.setPtrHandler(new PtrHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				updateData(AppConstants.DAILY,"",true);
			}
		});
	}

	private void initDatas() {
		//设置加载状态为加载中
		mStateView.setCurrentState(StateView.STATE_LOADING);

		mVideoList = new ArrayList();
		mOnlineVideoAdapter = new KaiYanOnlineVideoAdapter(mVideoList,getActivity());
		mOnlineVideoAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
			@Override
			public void onLoadMoreRequested() {
				updateData(AppConstants.DAILY,mDate,false);
			}
		}, mRecyclerView);
		mOnlineVideoAdapter.setEnableLoadMore(true);
		mOnlineVideoAdapter.setOnItemClickLitener(new KaiYanOnlineVideoAdapter.OnItemClickLitener() {
			@Override
			public void onItemClick(View view, int position) {
				DataBean dataBean = mVideoList.get(position).getData();
				Intent intent = new Intent(getActivity(), PlayerDetailActivity.class);
				intent.putExtra("playUrl", dataBean.getPlayUrl());
				intent.putExtra("playTitle", dataBean.getTitle());
				intent.putExtra("playDescription", dataBean.getDescription());
				intent.putExtra("playPic", dataBean.getCover().getDetail());
				getActivity().startActivity(intent);
			}
		});
		mRecyclerView.setAdapter(mOnlineVideoAdapter);
		updateData(AppConstants.DAILY,"",true);
	}

	private void updateData(String url,String date, final Boolean isPullDownRefresh) {
		LogUtil.e("zh", ":::url: " + url);
		if (url == null && isPullDownRefresh == false) {
			mOnlineVideoAdapter.loadMoreEnd(false);
			return;
		}
		if (presenter != null) {
			presenter.getOnlineVideos(url, date, isPullDownRefresh);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_top://快速返回顶部
				mRecyclerView.scrollToPosition(0);
				break;
			default:
				break;
		}
	}

	@Override
	public void updateOnlineData(boolean isSuccess, String response, final Boolean isPullDownRefresh) {
		if(isSuccess){
			if (response == null ) {
				if (isPullDownRefresh) {
					mPtrFrame.refreshComplete();
				}else{
					mOnlineVideoAdapter.loadMoreComplete();
				}
				return;
			}
			VideoListBean videoListBean = new Gson().fromJson(response,VideoListBean.class);
			LogUtil.e("zh",":::onSuccess response: " + videoListBean.toString());
			if(videoListBean != null && videoListBean.getItemList() != null ){
				List<ItemListBean> videoList = new ArrayList();
				for (ItemListBean itemListBean:videoListBean.getItemList()) {
					if(itemListBean != null && (itemListBean.getItemType() == 0 || itemListBean.getItemType() == 1)){
						videoList.add(itemListBean);
					}
				}
				int end = videoListBean.getNextPageUrl().lastIndexOf("&num");
				int start = videoListBean.getNextPageUrl().lastIndexOf("date=");
				mDate = videoListBean.getNextPageUrl().substring(start + 5, end);
				if (isPullDownRefresh) {
					// 下拉刷新，重新刷新列表
					mVideoList.clear();
					mVideoList.addAll(videoList);
					mPtrFrame.refreshComplete();
					mOnlineVideoAdapter.setNewData(mVideoList);
					mOnlineVideoAdapter.notifyDataSetChanged();
				} else {
					mVideoList.addAll(videoList);
					mOnlineVideoAdapter.loadMoreComplete();
				}
				mStateView.setCurrentState(StateView.STATE_CONTENT);
			}else{
				if(mVideoList.isEmpty()){
					mStateView.setCurrentState(StateView.STATE_ERROR);
				}
				if (isPullDownRefresh) {
					mPtrFrame.refreshComplete();
				}else{
					mOnlineVideoAdapter.loadMoreComplete();
				}
			}
		}else{
			LogUtil.e("zh",":::onFailure response: " + response);
			if (isPullDownRefresh) {
				mPtrFrame.refreshComplete();
			}else{
				mOnlineVideoAdapter.loadMoreFail();
			}
			if(mVideoList.isEmpty()){
				mStateView.setCurrentState(StateView.STATE_ERROR);
			}else{
				SnackbarUtils.ShortToast(mContentView,"数据请求失败");
			}
		}
	}

	@Override
	public void isShowLoading(boolean isShow, String message) {

	}
}