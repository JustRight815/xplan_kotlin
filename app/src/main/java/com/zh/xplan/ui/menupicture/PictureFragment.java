package com.zh.xplan.ui.menupicture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.module.common.log.LogUtil;
import com.module.common.utils.PixelUtil;
import com.module.common.view.snackbar.SnackbarUtils;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.imagedetailactivity.ImageDetailActivity;
import com.zh.xplan.ui.mainactivity.MainActivity;
import com.zh.xplan.ui.menupicture.adapter.GridPictureAdapter;
import com.zh.xplan.ui.menupicture.model.GridPictureModel;
import com.zh.xplan.ui.menupicture.model.HomeIndex;
import com.zh.xplan.ui.utils.TitleUtil;
import com.zh.xplan.ui.view.pulltorefresh.PtrFrameLayout;
import com.zh.xplan.ui.view.pulltorefresh.PtrHandler;
import com.zh.xplan.ui.view.pulltorefresh.customheader.PullToRefreshLayout;
import com.zh.xplan.ui.view.stateiew.StateView;
import com.zh.xplan.ui.zxing.activity.CaptureActivity;

import org.qcode.qskinloader.SkinManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 美图美句菜单项
 * Created by zh
 */
public class PictureFragment extends BaseFragment implements OnClickListener,PictureFragmentView{
	private View mContentView;
	List<HomeIndex.ItemInfoListBean> mPictureModelList;
	List<GridPictureModel> mPictureList  = new ArrayList();

	private RecyclerView mRecyclerView;
	private GridPictureAdapter mGridPictureAdapter;
	private PullToRefreshLayout mPtrFrame;//下拉刷新
	private int mCurrentPage = 0;// 当前页数
	private Button mToTopBtn;// 返回顶部的按钮
	private StaggeredGridLayoutManager mLayoutManager;

	private StateView mStateView;//加载状态控件
	private View mStateViewRetry;//错误状态布局的根布局
	private PictureFragmentPresenter presenter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = View.inflate(getActivity(),
				R.layout.fragment_picture, null);
		initTitle(getActivity(), mContentView);
		presenter = new PictureFragmentPresenter();
		presenter.attachView(this);
		initView(mContentView);
		initData();
		return mContentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SkinManager.getInstance().applySkin(view, true);
	}

	/**
	 * 初始化标题栏
	 * @param activity
	 * @param view
	 */
	private void initTitle(Activity activity, View view) {
		// 1.设置左边的图片按钮显示，以及事件 2.设置中间TextView显示的文字 3.设置右边的图片按钮显示，并设置事件
		new TitleUtil(activity, view).setLeftImageRes(R.drawable.title_bar_menu, this)
				.setMiddleTitleText("图片")
				.setMiddleTitleBgColor(getActivity().getResources().getColor(R.color.white))
				.setRightImageRes(R.drawable.scan_barcode, this);
	}

	/**
	 * 初始化界面
	 * @param rootView
	 */
	private void initView(View rootView) {
		mPtrFrame = (PullToRefreshLayout) rootView.findViewById(R.id.rotate_header_list_view_frame);
		mPtrFrame.setPtrHandler(new PtrHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				presenter.updateData(0,true);
			}
		});

		mStateView = (StateView) rootView.findViewById(R.id.mStateView);
		mStateViewRetry  =  rootView.findViewById(R.id.ll_stateview_error);
		mStateViewRetry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStateView.setCurrentState(StateView.STATE_LOADING);
				mCurrentPage = 0;
				presenter.updateData(mCurrentPage,true);
			}
		});

		mToTopBtn = (Button) rootView.findViewById(R.id.btn_top);
		mToTopBtn.setOnClickListener(this);
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
		mLayoutManager = new StaggeredGridLayoutManager(
				2, StaggeredGridLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.addItemDecoration(new SpacesItemDecoration(PixelUtil.dp2px(3,getContext())));
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int[] fistVisibleItem = mLayoutManager.findFirstVisibleItemPositions(new int[2]);
				// 判断是否滚动超过一屏
				if (0 == fistVisibleItem[0]) {
					mToTopBtn.setVisibility(View.GONE);
				} else {
					mToTopBtn.setVisibility(View.VISIBLE);
				}
			}

//			@Override
//			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//					ImageManager.resume();
//				}else {
//					ImageManager.pause();
//				}
//			}
		});

	}

	private void initData() {
		//设置加载状态为加载中
		mStateView.setCurrentState(StateView.STATE_LOADING);
		mPictureModelList = new ArrayList();
		mGridPictureAdapter = new GridPictureAdapter(mPictureModelList);
		mGridPictureAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
			@Override
			public void onLoadMoreRequested() {
				presenter.updateData(mCurrentPage,false);
			}
		}, mRecyclerView);
		mGridPictureAdapter.setEnableLoadMore(true);
//		mGridPictureAdapter.setHeaderView(headView,1);
		mGridPictureAdapter.setOnItemClickLitener(new GridPictureAdapter.OnItemClickLitener() {
			@Override
			public void onItemClick(View view, String url) {
				if(mPictureList != null){
					int position = -1;
					for (int i = 0; i < mPictureList.size(); i++) {
						if(mPictureList.get(i).getPictureUrl().equals(url)){
							position = i;
							break;
						}
					}
					if(position != -1){
						Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
						intent.putExtra("mPictureModelList", (Serializable) mPictureList);
						intent.putExtra("position", position);
						getActivity().startActivity(intent);
						getActivity().overridePendingTransition(0,0);
					}
				}
			}
		});
		mRecyclerView.setAdapter(mGridPictureAdapter);
		presenter.updateData(mCurrentPage,true);
	}


	@Override
	public void isShowLoading(boolean isShow, String message) {

	}

	@Override
	public void updateBanner(List<String> imageUrlList) {
	}

	@Override
	public void updatePictureData(boolean isSuccess,boolean isPullDownRefresh,
								  List<HomeIndex.ItemInfoListBean> pictureModelList,
								  List<GridPictureModel> pictureList) {
		if(isSuccess && pictureModelList != null && pictureModelList.size() > 0){
			mStateView.setCurrentState(StateView.STATE_CONTENT);
			mCurrentPage++;
			if (isPullDownRefresh) {
				// 下拉刷新，重新刷新列表
				mPictureModelList.clear();
				mPictureModelList.addAll(pictureModelList);
				mPictureList.clear();
				mPictureList.addAll(pictureList);
				mCurrentPage = 1;
				mPtrFrame.refreshComplete();
				mGridPictureAdapter.resetMaxHasLoadPosition();
				mGridPictureAdapter.notifyDataSetChanged();
			} else {
				mPictureList.addAll(pictureList);
				mPictureModelList.addAll(pictureModelList);
				mGridPictureAdapter.loadMoreComplete();
			}
		}else{
			LogUtil.e("zh", "onFailure: ");
			if (mPictureModelList.isEmpty()) {
				mStateView.setCurrentState(StateView.STATE_ERROR);
			} else {
//				Toast.makeText(getActivity(), "数据请求失败", Toast.LENGTH_SHORT).show();
				SnackbarUtils.ShortToast(mContentView,"数据请求失败");
			}
			if (isPullDownRefresh) {
				mPtrFrame.refreshComplete();
			} else {
				mGridPictureAdapter.loadMoreFail();
			}
		}
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_left_imageview:// 打开或关闭侧滑菜单
				((MainActivity)getActivity()).openDrawer();
				break;
			case R.id.title_right_imageview:// 扫一扫
				startActivity(new Intent(getActivity(), CaptureActivity.class));
				break;
			case R.id.btn_top:
				mRecyclerView.scrollToPosition(0);//快速返回顶部
//				mRecyclerView.smoothScrollToPosition(0);//滚动回到顶部
				break;
			default:
				break;
		}
	}

	@Override
	public void onDestroy() {
		if(presenter != null){
			presenter.onDestory();
		}
		super.onDestroy();
	}

	public static  class SpacesItemDecoration extends RecyclerView.ItemDecoration {
		private int space;
		public SpacesItemDecoration(int space) {
			this.space = space;
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();
			int spanCount = layoutManager.getSpanCount();
			if( spanCount == 2 &&  parent.getChildAdapterPosition(view) != 0){
				int index = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
				if( index % 2  == 0){
					outRect.right = space/2;
				}else{
					outRect.left = space/2;
				}
			}
		}

		@Override
		public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
			super.onDraw(c, parent, state);
		}
	}
}