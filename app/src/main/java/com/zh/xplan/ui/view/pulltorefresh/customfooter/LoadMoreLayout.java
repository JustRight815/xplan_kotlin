package com.zh.xplan.ui.view.pulltorefresh.customfooter;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.module.common.log.LogUtil;
import com.zh.xplan.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
public class LoadMoreLayout {
    private static boolean isEnableLoadMore = false;
    private static final int STATE_DISABLED = 0;
    private static final int STATE_LOADING = 1;
    private static final int STATE_FINISHED = 2;
    private static final int STATE_END = 3;
    private static final int STATE_FAILED = 4;

    @IntDef({STATE_DISABLED, STATE_LOADING, STATE_FINISHED, STATE_END, STATE_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    public interface OnLoadMoreListener {

        void onLoadMore();

    }
    RelativeLayout footViewLayout;
    ProgressBar progressWheel;

    TextView tvText;

    @State
    private int state = STATE_DISABLED;

    private final OnLoadMoreListener loadMoreListener;

    public LoadMoreLayout(@NonNull Context context, @NonNull RecyclerView recyclerView, @NonNull BaseQuickAdapter baseQuickAdapter, @NonNull OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
        View footerView = LayoutInflater.from(context).inflate(R.layout.footer_load_more, recyclerView, false);
        baseQuickAdapter.addFooterView(footerView);
        footViewLayout = (RelativeLayout) footerView.findViewById(R.id.footView_layout);
        progressWheel = (ProgressBar) footerView.findViewById(R.id.footView_pb);
        tvText = (TextView) footerView.findViewById(R.id.footView_tv);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean flag = true;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(!isEnableLoadMore){
                    return;
                }
                if(newState == 1){
                    flag = true;
                }else if(newState == 0 && flag){
                    if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                        checkLoadMore();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LogUtil.e("zh","onScrolled");
                if(!isEnableLoadMore){
                    return;
                }
                LogUtil.e("zh","onScrolled isEnableLoadMore ");
                if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                    checkLoadMore();
                }
            }

        });
    }

//    public LoadMoreLayout(@NonNull Context context, @NonNull ListView listView, @NonNull OnLoadMoreListener loadMoreListener) {
//        this.loadMoreListener = loadMoreListener;
//        View footerView = LayoutInflater.from(context).inflate(R.layout.footer_load_more, listView, false);
//        listView.addFooterView(footerView, null, false);
//        progressWheel = footerView.findViewById(R.id.footView_pb);
//        tvText = footerView.findViewById(R.id.footView_tv);
//        listView.addOnScrollListener(new AbsListView.OnScrollListener() {
//
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (view.getLastVisiblePosition() == view.getCount() - 1) {
//                    checkLoadMore();
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (view.getLastVisiblePosition() == view.getCount() - 1) {
//                    checkLoadMore();
//                }
//            }
//
//        });
//    }

    //设置状态为正在加载
    public void loadMoreEnable(boolean isEnable) {
        isEnableLoadMore = isEnable;
        if(isEnable){
            setState(STATE_FINISHED);
        }else{
            setState(STATE_DISABLED);
        }
    }

    //设置状态为正在加载
    public void loading() {
        if(!isEnableLoadMore){
            return;
        }
        setState(STATE_LOADING);
    }

    //设置状态为加载完成
    public void loadMoreComplete() {
        if(!isEnableLoadMore){
            return;
        }
        setState(STATE_FINISHED);
    }

    //设置状态为加载失败
    public void loadMoreFail() {
        if(!isEnableLoadMore){
            return;
        }
        setState(STATE_FAILED);
    }

    //设置状态为加载结束，到底了
    public void loadMoreEnd() {
        if(!isEnableLoadMore){
            return;
        }
        setState(STATE_END);
    }

    @State
    public int getState() {
        return state;
    }

    private void setState(@State int state) {
        if (this.state != state) {
            this.state = state;
            switch (state) {
                case STATE_DISABLED:
//                    footViewLayout.setVisibility(View.GONE);
//                    progressWheel.setVisibility(View.INVISIBLE);
//                    tvText.setVisibility(View.INVISIBLE);
//                    tvText.setText(null);
//                    tvText.setClickable(false);

                    footViewLayout.setVisibility(View.VISIBLE);
                    progressWheel.setVisibility(View.INVISIBLE);
                    tvText.setVisibility(View.INVISIBLE);
                    tvText.setText("null");
                    tvText.setClickable(false);
                    break;
                case STATE_LOADING:
                    footViewLayout.setVisibility(View.VISIBLE);
                    progressWheel.setVisibility(View.VISIBLE);
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText("正在加载中...");
                    tvText.setClickable(false);
                    break;
                case STATE_FAILED:
                    footViewLayout.setVisibility(View.VISIBLE);
                    progressWheel.setVisibility(View.INVISIBLE);
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText("加载失败，点击重试");
                    tvText.setClickable(true);
                    break;
                case STATE_END://加载结束，不能再加载了
                    footViewLayout.setVisibility(View.VISIBLE);
                    progressWheel.setVisibility(View.INVISIBLE);
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText("已经到底啦");
                    tvText.setClickable(false);
                    break;
                case STATE_FINISHED://加载完成，还可以继续加载
//                    footViewLayout.setVisibility(View.GONE);
//                    progressWheel.setVisibility(View.INVISIBLE);
//                    tvText.setVisibility(View.VISIBLE);
//                    tvText.setText(null);
//                    tvText.setClickable(true);

                    footViewLayout.setVisibility(View.VISIBLE);
                    progressWheel.setVisibility(View.VISIBLE);
                    tvText.setVisibility(View.VISIBLE);
                    tvText.setText("正在加载中...");
                    tvText.setClickable(false);
                    break;
                default:
                    throw new AssertionError("Unknow load more state.");
            }
        }
    }

    private void checkLoadMore() {
        if (getState() == STATE_END || getState() == STATE_FINISHED || getState() == STATE_FAILED) {
            setState(STATE_LOADING);
            loadMoreListener.onLoadMore();
        }
    }

    void onBtnTextClick() {
        checkLoadMore();
    }
}
