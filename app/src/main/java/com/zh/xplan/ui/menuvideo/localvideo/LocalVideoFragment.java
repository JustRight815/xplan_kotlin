package com.zh.xplan.ui.menuvideo.localvideo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.module.common.log.LogUtil;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.menuvideo.localvideo.adapter.LocalVideoListAdapter;
import com.zh.xplan.ui.menuvideo.localvideo.model.LocalVideoBean;
import com.zh.xplan.ui.playeractivity.PlayerLocalActivity;
import com.zh.xplan.ui.view.pulltorefresh.PtrFrameLayout;
import com.zh.xplan.ui.view.pulltorefresh.PtrHandler;
import com.zh.xplan.ui.view.pulltorefresh.customheader.PullToRefreshLayout;

import org.qcode.qskinloader.SkinManager;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import static com.zh.xplan.ui.zxing.activity.CaptureActivity.getAppDetailSettingIntent;

/**
 *  本地视频 Created by zh
 */
@RuntimePermissions
public class LocalVideoFragment extends BaseFragment implements
		LocalVideoView {
	private ListView mListView;
	private PullToRefreshLayout mPtrFrame;//下拉刷新
	private List<LocalVideoBean> mVideoList;
	private LocalVideoListAdapter mListAdapter;
	private Button mToTopBtn;// 返回顶部的按钮
	private View mView;
	/** 标志位，标志界面是否已经初始化完成 */
	private boolean isPrepared;
	/** 是否已被加载过一次，第二次就不再去请求数据了 */
	private boolean mHasLoadedOnce;
	//当前fragment是否可见
	protected boolean isVisible;

	private  LocalVideoPresenter presenter;
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		LogUtil.e("zh","setUserVisibleHint()" + isVisibleToUser);
		super.setUserVisibleHint(isVisibleToUser);
		if (getUserVisibleHint()){
			isVisible=true;
			lazyLoad();
			LogUtil.e("zh","setUserVisibleHint getDatasWithCheck()");
			LocalVideoFragmentPermissionsDispatcher.getDatasWithCheck(LocalVideoFragment.this);
		}else {
			isVisible=false;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mView == null) {
			mView =  View.inflate(getActivity(),
					R.layout.fragment_local_video, null);
			isPrepared = true;
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
		mHasLoadedOnce = true;
		presenter = new LocalVideoPresenter();
		presenter.attachView(this);
		initView(mView);
		initListener();
		initDatas();
	}

	/**
	 * 由于viewpager的预加载机制,在viewpager里面的fragment 其生命周期会发生混乱
	 * onHiddenChanged不起作用、应该使用 setUserVisibleHint
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}


	@Override
	public void onResume() {
		LogUtil.e("zh","onResume()++===============" );
		super.onResume();
		if (!isPrepared||!isVisible) {
			return;
		}
		LogUtil.e("zh","getDatasWithCheck()");
		LocalVideoFragmentPermissionsDispatcher.getDatasWithCheck(LocalVideoFragment.this);
	}

	/**
	 * 初始化views
	 * 
	 * @param mView
	 */
	private void initView(View mView) {
		mPtrFrame = (PullToRefreshLayout) mView.findViewById(R.id.rotate_header_list_view_frame);
		mPtrFrame.setPtrHandler(new PtrHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				LocalVideoFragmentPermissionsDispatcher.getDatasWithCheck(LocalVideoFragment.this);
			}
		});
		mToTopBtn = (Button) mView.findViewById(R.id.btn_top);
		mListView = (ListView) mView.findViewById(R.id.listView);
	}

	private void initListener() {
		mToTopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setListViewPos(0);
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Intent intent = new Intent(getActivity(), PlayerLocalActivity.class);
				intent.putExtra("video", mVideoList.get(position));
				intent.putExtra("videoType", 1);
				startActivity(intent);
			}
		});
	}

	private void initDatas() {
		LogUtil.e("zh","initDatas()");
		mVideoList = new ArrayList<LocalVideoBean>();
		mListAdapter = new LocalVideoListAdapter(mVideoList, getActivity());
		mListView.setAdapter(mListAdapter);

	}

	@NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
	public void getDatas() {
		if(presenter != null){
			presenter.getLocalVideos();
		}
	}

	@OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
	void showRecordDenied(){
		mPtrFrame.refreshComplete();
		Toast.makeText(getActivity(),"拒绝存储权限将无法获取本地视频",Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		LocalVideoFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	@OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
	void onRecordNeverAskAgain() {
		new AlertDialog.Builder(getActivity())
				.setPositiveButton("好的", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//打开系统设置权限
						Intent intent = getAppDetailSettingIntent(getActivity());
						startActivity(intent);
						dialog.cancel();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setCancelable(false)
				.setMessage("您已经禁止了存储权限,是否去开启权限")
				.show();
	}

	/**
	 * 滚动ListView到指定位置
	 * 
	 * @param pos
	 */
	private void setListViewPos(int pos) {
		if (android.os.Build.VERSION.SDK_INT >= 8) {
			mListView.smoothScrollToPosition(pos);
		} else {
			mListView.setSelection(pos);
		}
	}

	@Override
	public void isShowLoading(boolean isShow, String message) {
	}

	@Override
	public void updateLocalVideoData(List<LocalVideoBean> videos) {
		if (videos.size() > 0) {
			mVideoList.clear();
			mVideoList.addAll(videos);
			mListAdapter.notifyDataSetChanged();
		} else {
		}
		mPtrFrame.refreshComplete();
	}

	@Override
	public void onDestroy() {
		if(presenter != null){
			presenter.onDestory();
		}
		super.onDestroy();
	}
}