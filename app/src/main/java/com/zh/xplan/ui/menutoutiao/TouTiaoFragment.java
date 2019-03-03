package com.zh.xplan.ui.menutoutiao;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.module.common.utils.PixelUtil;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.menutoutiao.model.Channel;

import org.qcode.qskinloader.SkinManager;

import java.util.ArrayList;
import java.util.List;

import me.weyye.library.colortrackview.ColorTrackTabLayout;

/**
 * 今日头条菜单项的基础fragment
 */
public class TouTiaoFragment extends BaseFragment {

    ColorTrackTabLayout mTabLayout;
    ViewPager mViewPager;
    ImageView mAddChannel;
    private List<Channel> mSelectedChannels = new ArrayList<>();
    private List<NewsListFragment> mNewsFragmentList = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = View.inflate(getActivity(),
                R.layout.fragment_toutiao, null);
        initView(mView);
        initData();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SkinManager.getInstance().applySkin(view, true);
    }

    /**
     * 初始化界面
     * @param mView
     */
    private void initView(View mView) {
        mTabLayout = (ColorTrackTabLayout) mView.findViewById(R.id.tab_channel);
        mViewPager = (ViewPager) mView.findViewById(R.id.vp_content);
        mAddChannel = (ImageView) mView.findViewById(R.id.iv_operation);
    }

    public void initData() {
        initChannelData();
        initChannelFragments();
        initListener();
    }


    /**
     * 初始化已选频道和未选频道的数据
     */
    private void initChannelData() {
        String[] channels = getResources().getStringArray(R.array.channel);
        String[] channelCodes = getResources().getStringArray(R.array.channel_code);
        //添加全部频道
        for (int i = 0; i < channelCodes.length; i++) {
            String title = channels[i];
            String code = channelCodes[i];
            mSelectedChannels.add(new Channel(title, code));
        }
    }

    /**
     * 初始化已选频道的fragment的集合
     */
    private void initChannelFragments() {
//        String[] channelCodes = getResources().getStringArray(R.array.channel_code);
//        for (Channel channel : mSelectedChannels) {
//            NewsListFragment newsFragment = NewsListFragment.newInstance(channel.channelCode,channel.channelCode.equals(channelCodes[1]));
//            mNewsFragmentList.add(newsFragment);//添加到集合中
//        }
    }

    public void initListener() {
        String[] channelCodes = getResources().getStringArray(R.array.channel_code);
        ChannelAdapter channelAdapter = new ChannelAdapter(mNewsFragmentList, mSelectedChannels,channelCodes,getChildFragmentManager());
        mViewPager.setAdapter(channelAdapter);
//        mViewPager.setOffscreenPageLimit(mSelectedChannels.size());

        mTabLayout.setTabPaddingLeftAndRight(PixelUtil.dp2px(10,getActivity()), PixelUtil.dp2px(10,getActivity()));
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                //设置最小宽度，使其可以在滑动一部分距离
                ViewGroup slidingTabStrip = (ViewGroup) mTabLayout.getChildAt(0);
                slidingTabStrip.setMinimumWidth(slidingTabStrip.getMeasuredWidth() + mAddChannel.getMeasuredWidth());
            }
        });
        //隐藏指示器
        mTabLayout.setSelectedTabIndicatorHeight(0);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                 //当页签切换的时候，如果有播放视频，则释放资源
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public String getCurrentChannelCode(){
        int currentItem = mViewPager.getCurrentItem();
        return mSelectedChannels.get(currentItem).channelCode;
    }

    public static  class ChannelAdapter extends FragmentStatePagerAdapter {

        private List<NewsListFragment> mFragments;
        private List<Channel> mChannels;
        String[] mChannelCodes;

        public ChannelAdapter(List<NewsListFragment> fragmentList, List<Channel> channelList, String[] channelCodes,FragmentManager fm) {
            super(fm);
            mFragments = fragmentList != null ? fragmentList : new ArrayList();
            mChannels = channelList != null ? channelList : new ArrayList();
            mChannelCodes = channelCodes != null ? channelCodes : new String[0];
        }

        public Fragment getFragment(int position){
//            if(mFragments != null && mFragments.size() > position){
//                NewsListFragment fragment = mFragments.get(position);
//                return fragment;
//            }
            Channel channel = mChannels.get(position);
            NewsListFragment newsFragment = NewsListFragment.newInstance(channel.channelCode,channel.channelCode.equals(mChannelCodes[1]));
            mFragments.add(newsFragment);
            return  newsFragment;
        }

        @Override
        public Fragment getItem(int position) {
            return getFragment(position);
        }

        @Override
        public int getCount() {
            return mChannels.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mChannels.get(position).title;
        }
    }
}
