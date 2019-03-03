package com.zh.xplan.ui.menuvideo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.base.FragmentsFactory;
import com.zh.xplan.ui.camera.RecordVideoSet;
import com.zh.xplan.ui.camera.record.CustomCameraActivity;

import org.qcode.qskinloader.SkinManager;


/**
 * 视频菜单  Created by zh
 */
public class VideoFragment extends BaseFragment {
	private Boolean isFirst = true;// 是否是第一次进入应用
	private FragmentManager mFragmentManager;
	private BaseFragment mCurrentFragment;// 当前FrameLayout中显示的Fragment
	private RadioButton mOnlineVideoBtn,mLocalVideoBtn;
	private ImageView mCameraView;
	private ViewPager mViewPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_video, null);
		initView(mView);
		initDatas();
		return mView;
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
		mViewPager = (ViewPager) mView.findViewById(R.id.viewPager);
		mOnlineVideoBtn = (RadioButton) mView.findViewById(R.id.rb_online_video);
		mLocalVideoBtn = (RadioButton) mView.findViewById(R.id.rb_local_video);
		mCameraView = (ImageView) mView.findViewById(R.id.iv_camera);
		mOnlineVideoBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked){
					mViewPager.setCurrentItem(0);
				}
			}
		});
		mLocalVideoBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked){
					mViewPager.setCurrentItem(1);
				}
			}
		});
		mCameraView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				customRecord(true);
			}
		});

	}

	private void initDatas() {
		mFragmentManager = getActivity().getSupportFragmentManager();
		mOnlineVideoBtn.setChecked(true);
		mViewPager.setAdapter(new FragmentPagerAdapter(mFragmentManager));
		mViewPager.addOnPageChangeListener(OnPageChangeListener);
	}

	private static class FragmentPagerAdapter extends
			android.support.v4.app.FragmentPagerAdapter {

		public FragmentPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			if(position == 0){
				return FragmentsFactory.createFragment(4);
			}else{
				return FragmentsFactory.createFragment(5);
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	}

	//控制当前界面是否可以滑动返回到上一界面
	ViewPager.OnPageChangeListener OnPageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if(position == 0){
				if(! mOnlineVideoBtn.isChecked()){
					mOnlineVideoBtn.setChecked(true);
				}
			}else{
				if(! mLocalVideoBtn.isChecked()){
					mLocalVideoBtn.setChecked(true);
				}
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	};


	/**
	 * 启用自定义相机录制视频
	 * @param isSmallVideo
	 */
	public void customRecord(boolean isSmallVideo) {
		RecordVideoSet recordVideoSet = new RecordVideoSet();
		recordVideoSet.setLimitRecordTime(30);
		recordVideoSet.setSmallVideo(isSmallVideo);
		Intent intent = new Intent(getActivity(), CustomCameraActivity.class);
		intent.putExtra("RECORD_VIDEO_CONFIG", recordVideoSet);
		startActivity(intent);
	}

}