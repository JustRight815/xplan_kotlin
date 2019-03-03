package com.zh.xplan.ui.pulltorefreshdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.module.common.log.LogUtil;
import com.zh.xplan.R;
import com.zh.xplan.ui.pulltorefreshdemo.adapter.PullToRefreshAdapter;
import com.zh.xplan.ui.pulltorefreshdemo.model.PullToRefreshModel;
import com.zh.xplan.ui.view.pulltorefresh.customfooter.LoadMoreLayout;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.view.pulltorefresh.customheader.PullToRefreshLayout;
import com.zh.xplan.ui.view.pulltorefresh.PtrFrameLayout;
import com.zh.xplan.ui.view.pulltorefresh.PtrHandler;

import java.util.ArrayList;
import java.util.List;
/**
 * 下拉刷新，加载更多 demo
 */
public class PullToRefreshDemoActivity extends BaseActivity
        implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PullToRefreshAdapter mOnlineVideoAdapter;
    private List<PullToRefreshModel> mVideoList;
    private PullToRefreshLayout mPtrFrame;//下拉刷新

    private LoadMoreLayout loadMoreLayout;

    private static final int PAGE_SIZE = 20;
    private static final int TOTAL_COUNT = 60;
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_rv);
        initViews();
        initDatas();
    }

    @Override
    public boolean isSupportSwipeBack() {
        return true;
    }

    private void initViews() {
        setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark),0);
        mPtrFrame = (PullToRefreshLayout) findViewById(R.id.rotate_header_list_view_frame);
//        mPtrFrame.setLastUpdateTimeRelateObject(this); //显示更新日期
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrFrame.refreshComplete();
                    }
                },1500);
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
        mVideoList = new ArrayList();
        for (int i = 1; i <= PAGE_SIZE ; i++) {
            PullToRefreshModel pullToRefreshModel = new PullToRefreshModel();
            pullToRefreshModel.setType(i +"");
            mVideoList.add(pullToRefreshModel);
        }
        mOnlineVideoAdapter = new PullToRefreshAdapter(mVideoList,this);
        mRecyclerView.setAdapter(mOnlineVideoAdapter);


        loadMoreLayout = new LoadMoreLayout(this, mRecyclerView, mOnlineVideoAdapter, new LoadMoreLayout.OnLoadMoreListener() {
            boolean flag = false;
            @Override
            public void onLoadMore() {
                LogUtil.e("zh","加载更多");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mVideoList.size() < TOTAL_COUNT){
                            if(mVideoList.size() == 40 && flag == false){
                                flag = true;
                                loadMoreLayout.loadMoreFail();
                            }else{
                                for (int i = 1; i <= PAGE_SIZE ; i++) {
                                    PullToRefreshModel pullToRefreshModel = new PullToRefreshModel();
                                    pullToRefreshModel.setType(i +"");
                                    mVideoList.add(pullToRefreshModel);
                                }
                                mOnlineVideoAdapter.notifyDataSetChanged();
                                loadMoreLayout.loadMoreComplete();
                            }
                        }else{
                            loadMoreLayout.loadMoreEnd();
                        }
                    }
                },1000);
            }
        });
        loadMoreLayout.loadMoreEnable(true);
    }

    public void initDatas() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_bar_back:
                finish();
                break;
            default:
                break;
        }
    }

}
