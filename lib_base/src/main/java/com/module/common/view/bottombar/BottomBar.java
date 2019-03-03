package com.module.common.view.bottombar;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 底部菜单布局
 */
public class BottomBar extends LinearLayout implements ViewPager.OnPageChangeListener {

    private static final String STATE_INSTANCE = "instance_state";
    private static final String STATE_ITEM = "state_item";


    private ViewPager mViewPager;
    private int mChildCount;//子条目个数
    private List<BottomBarItem> mItemViews = new ArrayList<>();
    private int mCurrentItem;//当前条目的索引
    private boolean mSmoothScroll;

    public BottomBar(Context context) {
        this(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(HORIZONTAL);
    }

    @Override
    public void setOrientation(int orientation) {
        if (LinearLayout.VERTICAL == orientation) {
            throw new IllegalArgumentException("BottomBarLayout only supports Horizontal Orientation.");
        }
        super.setOrientation(orientation);
    }

    public void init(ViewPager mViewPager) {
        this.mViewPager = mViewPager;
        init();
    }

    private void init() {
        if (mViewPager == null) {
//            throw new IllegalArgumentException("参数不能为空");
        }

        mChildCount = getChildCount();
        if(mViewPager != null){
            if (mViewPager.getAdapter().getCount() != mChildCount) {
                throw new IllegalArgumentException("LinearLayout的子View数量必须和ViewPager条目数量一致");
            }
        }
        for (int i = 0; i < mChildCount; i++) {
            if (getChildAt(i) instanceof BottomBarItem) {
                BottomBarItem bottomBarItem = (BottomBarItem) getChildAt(i);
                mItemViews.add(bottomBarItem);
                //设置点击监听
                bottomBarItem.setOnClickListener(new MyOnClickListener(i));
            } else {
                throw new IllegalArgumentException("AlphaIndicator的子View必须是AlphaView");
            }
        }

        mItemViews.get(mCurrentItem).setStatus(true);//设置选中项
        if(mViewPager != null){
            mViewPager.setOnPageChangeListener(this);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentItem = position;//记录当前位置
        resetState();
        mItemViews.get(position).setStatus(true);
        if(mViewPager != null){
            mViewPager.setCurrentItem(position, mSmoothScroll);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyOnClickListener implements OnClickListener {

        private int currentIndex;

        public MyOnClickListener(int i) {
            this.currentIndex = i;
        }

        @Override
        public void onClick(View v) {
            //点击前先重置所有按钮的状态
            resetState();
            mItemViews.get(currentIndex).setStatus(true);//设置为选中状态
            //点击是保存当前按钮索引
            mCurrentItem = currentIndex;
            //回调点击的位置
            if (onItemSelectedListener != null) {
                onItemSelectedListener.onItemSelected(getBottomItem(currentIndex),currentIndex);
            }

            //不能使用平滑滚动，否者颜色改变会乱
            if(mViewPager != null){
                mViewPager.setCurrentItem(currentIndex, mSmoothScroll);
            }

        }
    }

    /**
     * 重置所有按钮的状态
     */
    private void resetState() {
        for (int i = 0; i < mChildCount; i++) {
            mItemViews.get(i).setStatus(false);
        }
    }

    public void setCurrentItem(int currentItem) {
        mCurrentItem = currentItem;
        if(mViewPager != null){
            mViewPager.setCurrentItem(mCurrentItem,mSmoothScroll);
        }else{
            if (onItemSelectedListener != null) {
                onItemSelectedListener.onItemSelected(getBottomItem(currentItem),currentItem);
            }
        }
    }

    /**
     * 设置未读数
     * @param position 底部标签的下标
     * @param unreadNum 未读数
     */
    public void setUnread(int position,int unreadNum){
        mItemViews.get(position).setUnreadNum(unreadNum);
    }

    /**
     * 设置提示消息
     * @param position 底部标签的下标
     * @param msg 未读数
     */
    public void setMsg(int position,String msg){
        mItemViews.get(position).setMsg(msg);
    }

    /**
     * 隐藏提示消息
     * @param position 底部标签的下标
     */
    public void hideMsg(int position){
        mItemViews.get(position).hideMsg();
    }

    /**
     * 显示提示的小红点
     * @param position 底部标签的下标
     */
    public void showNotify(int position){
        mItemViews.get(position).showNotify();
    }

    /**
     * 隐藏提示的小红点
     * @param position 底部标签的下标
     */
    public void hideNotify(int position){
        mItemViews.get(position).hideNotify();
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public void setSmoothScroll(boolean mSmoothScroll) {
        this.mSmoothScroll = mSmoothScroll;
    }

    public BottomBarItem getBottomItem(int position){
        return mItemViews.get(position);
    }

    /**
     * @return 当View被销毁的时候，保存数据
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putInt(STATE_ITEM, mCurrentItem);
        return bundle;
    }

    /**
     * @param state 用于恢复数据使用
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mCurrentItem = bundle.getInt(STATE_ITEM);
            //重置所有按钮状态
            resetState();
            //恢复点击的条目颜色
            mItemViews.get(mCurrentItem).setStatus(true);
            super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private OnItemSelectedListener onItemSelectedListener;

    public interface OnItemSelectedListener {
        void onItemSelected(BottomBarItem bottomBarItem, int position);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }
}
