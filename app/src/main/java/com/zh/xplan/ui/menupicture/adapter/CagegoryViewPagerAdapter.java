package com.zh.xplan.ui.menupicture.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * github: https://github.com/xiaohaibin
 * 首页分类ViewPager适配器
 */
public class CagegoryViewPagerAdapter extends PagerAdapter {
    private List<View> mViewList = new ArrayList<>();

    public CagegoryViewPagerAdapter(List<View> mViewList) {
        this.mViewList.clear();
        this.mViewList.addAll(mViewList);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return (mViewList.get(position));
    }

    @Override
    public int getCount() {
        if (mViewList == null) {
            return 0;
        }
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
