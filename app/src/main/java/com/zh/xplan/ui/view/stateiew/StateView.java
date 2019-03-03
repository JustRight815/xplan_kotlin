package com.zh.xplan.ui.view.stateiew;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zh.xplan.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 描述: 加载状态控件 加载中，加载失败，空数据等等
 * 名称: StateView
 * User: csx
 * Date: 11-16  https://github.com/hqucsx/StateView
 */
public class StateView extends FrameLayout {
    //content
    public static final int STATE_CONTENT = 0;
    //loading
    public static final int STATE_LOADING = 1;
    //error
    public static final int STATE_ERROR = 2;
    //empty
    public static final int STATE_EMPTY = 3;
    //loading with content
    public static final int STATE_CONTENT_LOADING = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_CONTENT, STATE_LOADING, STATE_ERROR, STATE_EMPTY, STATE_CONTENT_LOADING})
    public @interface ViewState {
    }

    //default show state
    @ViewState
    public int mCurrentState = STATE_CONTENT;

    //各种状态下的View
    private View mContentView, mLoadingView, mErrorView, mEmptyView;

    private LayoutInflater inflater;

    public StateView(Context context) {
        super(context);
    }

    public StateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStateView(attrs);
    }

    public StateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStateView(attrs);
    }

    /**
     * @param attrs
     */
    private void initStateView(AttributeSet attrs) {
        inflater = LayoutInflater.from(getContext());
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.StateView);

        //初始化各种状态下的布局并添加到stateView中

        int emptyViewResId = a.getResourceId(R.styleable.StateView_state_empty, -1);
        if (emptyViewResId > -1) {
            mEmptyView = inflater.inflate(emptyViewResId, this, false);
            addView(mEmptyView, mEmptyView.getLayoutParams());
        }
        int errorViewResId = a.getResourceId(R.styleable.StateView_state_error, -1);
        if (errorViewResId > -1) {
            mErrorView = inflater.inflate(errorViewResId, this, false);
            addView(mErrorView, mErrorView.getLayoutParams());
        }
        int contentViewResId = a.getResourceId(R.styleable.StateView_state_content,-1);
        if(contentViewResId>-1){
            mContentView = inflater.inflate(contentViewResId,this,false);
            addView(mContentView,mContentView.getLayoutParams());
        }
        int loadingViewResId = a.getResourceId(R.styleable.StateView_state_loading, -1);
        if (loadingViewResId > -1) {
            mLoadingView = inflater.inflate(loadingViewResId, this, false);
            addView(mLoadingView, mLoadingView.getLayoutParams());
        }
        //获取指定的状态，如未指定则默认content
        int givenState = a.getInt(R.styleable.StateView_state_current, STATE_CONTENT);
        switch (givenState) {
            case STATE_CONTENT:
                mCurrentState = STATE_CONTENT;
                break;
            case STATE_LOADING:
                mCurrentState = STATE_LOADING;
                break;
            case STATE_EMPTY:
                mCurrentState = STATE_EMPTY;
                break;
            case STATE_ERROR:
                mCurrentState = STATE_ERROR;
                break;
            case STATE_CONTENT_LOADING:
                mCurrentState = STATE_CONTENT_LOADING;
                break;
        }
        a.recycle();
    }

    /**
     * 设置各个状态下View的显示与隐藏
     */
    private void setStateView() {
        switch (mCurrentState) {
            case STATE_CONTENT:
                showStateView(mContentView, "Content View");
                break;
            case STATE_EMPTY:
                showStateView(mEmptyView, "Empty View");
                break;
            case STATE_ERROR:
                showStateView(mErrorView, "Error View");
                break;
            case STATE_LOADING:
                showStateView(mLoadingView, "Loading View");
                break;
            case STATE_CONTENT_LOADING:
                if (mContentView == null) {
                    throw new NullPointerException("Content View with Loading View");
                }
                mContentView.setVisibility(VISIBLE);
                if (mLoadingView == null) {
                    throw new NullPointerException("Loading View with Content View");
                }
                mLoadingView.setVisibility(VISIBLE);
                if (mEmptyView != null) mEmptyView.setVisibility(GONE);
                if (mErrorView != null) mErrorView.setVisibility(GONE);
                break;
            default:
                showStateView(mContentView, "Content View");
                break;
        }
    }

    /**
     * 获取指定状态下的View
     *
     * @param state
     * @return
     */
    public View getStateView(@ViewState int state) {
        switch (state) {
            case STATE_CONTENT:
                return mContentView;
            case STATE_EMPTY:
                return mEmptyView;
            case STATE_ERROR:
                return mErrorView;
            case STATE_LOADING:
                return mLoadingView;
            default:
                return null;
        }
    }

    /**
     * @param view
     * @param viewName
     */
    private void showStateView(View view, String viewName) {
        if (mContentView != null) mContentView.setVisibility(GONE);
        if (mErrorView != null) mErrorView.setVisibility(GONE);
        if (mEmptyView != null) mEmptyView.setVisibility(GONE);
        if (mLoadingView != null) mLoadingView.setVisibility(GONE);

        if (view == null) {
            throw new NullPointerException(viewName);
        }
        view.setVisibility(VISIBLE);
    }


    @ViewState
    public int getCurrentState() {
        return mCurrentState;
    }

    /**
     * 设置当前状态
     *
     * @param state
     */
    public void setCurrentState(@ViewState int state) {
        if (state != mCurrentState) {
            mCurrentState = state;
            setStateView();
        }
    }

    /**
     * 通过代码设置指定状态的View
     *
     * @param stateView     view in state
     * @param state         viewState
     * @param switchToState 是否切换到指定的state
     */
    public void setViewForState(View stateView, @ViewState int state, boolean switchToState) {
        switch (state) {
            case STATE_CONTENT:
                if (mContentView != null)
                    removeView(mContentView);
                mContentView = stateView;
                addView(mContentView);
                break;
            case STATE_LOADING:
                if (mLoadingView != null)
                    removeView(mLoadingView);
                mLoadingView = stateView;
                addView(mLoadingView);
                break;
            case STATE_EMPTY:
                if (mEmptyView != null)
                    removeView(mEmptyView);
                mEmptyView = stateView;
                addView(mEmptyView);
                break;
            case STATE_ERROR:
                if (mErrorView != null)
                    removeView(mErrorView);
                mErrorView = stateView;
                addView(mErrorView);
                break;
        }
        //切换到指定状态
        if (switchToState)
            setCurrentState(state);
    }

    /**
     * 方法重载，默认不切换至指定状态
     *
     * @param stateView
     * @param state
     */
    public void setViewForState(View stateView, @ViewState int state) {
        setViewForState(stateView, state, false);
    }

    /**
     * 动态设置指定状态的布局
     *
     * @param layoutRes
     * @param state
     * @param switchToState
     */
    public void setViewForState(int layoutRes, @ViewState int state, boolean switchToState) {
        if (inflater == null)
            inflater = LayoutInflater.from(getContext());
        View stateView = inflater.inflate(layoutRes, this, false);
        setViewForState(stateView, state, switchToState);
    }

    /**
     * 方法重载，默认不切换至指定状态
     *
     * @param layoutRes
     * @param state
     */
    public void setViewForState(int layoutRes, @ViewState int state) {
        setViewForState(layoutRes, state, false);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mContentView == null) throw new IllegalArgumentException("Content view is not defined");
        setStateView();
    }

    /**
     * 复写所有的addView()方法
     */
    @Override
    public void addView(View child) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, width, height);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }

    /**
     * 判断所添加的View是否是有效的contentView
     *
     * @param view
     * @return
     */
    private boolean isValidContentView(View view) {
        if (mContentView != null && mContentView != view) {
            return false;
        }
        return view != mLoadingView && view != mErrorView && view != mEmptyView;
    }

}
