package com.zh.xplan.ui.view.pulltorefresh.customheader;

import android.content.Context;
import android.util.AttributeSet;

import com.zh.xplan.ui.view.pulltorefresh.PtrFrameLayout;


public class PullToRefreshLayout extends PtrFrameLayout {

    private CustomClassicHeader mPtrClassicHeader;

    public PullToRefreshLayout(Context context) {
        super(context);
        initViews();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews() {
        mPtrClassicHeader = new CustomClassicHeader(getContext());
        setHeaderView(mPtrClassicHeader);
        addPtrUIHandler(mPtrClassicHeader);

        setResistance(2.7f);//阻尼系数 默认: 1.7f，越大，感觉下拉时越吃力。
        setRatioOfHeaderHeightToRefresh(1.0f);//触发刷新时移动的位置比例 默认，1.2f，移动达到头部高度1.2倍时可触发刷新操作。
        setDurationToClose(200);//回弹延时 默认 200ms，回弹到刷新高度所用时间
        setDurationToCloseHeader(200);//头部回弹时间 默认1000ms
        setPullToRefresh(false);//下拉刷新或者释放刷新 默认为false 释放刷新
        setKeepHeaderWhenRefresh(true);//刷新是保持头部 默认值 true.
    }

    public CustomClassicHeader getHeader() {
        return mPtrClassicHeader;
    }

    /**
     * Specify the last update time by this key string
     *
     * @param key
     */
    public void setLastUpdateTimeKey(String key) {
        if (mPtrClassicHeader != null) {
            mPtrClassicHeader.setLastUpdateTimeKey(key);
        }
    }

    /**
     * Using an object to specify the last update time.
     *
     * @param object
     */
    public void setLastUpdateTimeRelateObject(Object object) {
        if (mPtrClassicHeader != null) {
            mPtrClassicHeader.setLastUpdateTimeRelateObject(object);
        }
    }
}
