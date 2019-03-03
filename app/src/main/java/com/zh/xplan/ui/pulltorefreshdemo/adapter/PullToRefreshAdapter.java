package com.zh.xplan.ui.pulltorefreshdemo.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zh.xplan.R;
import com.zh.xplan.ui.pulltorefreshdemo.model.PullToRefreshModel;
import com.zh.xplan.ui.view.CustomTextView;

import java.util.List;

/**
 * 下拉刷新，加载更多 demo适配器
 * Created by zh on 2017/5/28.
 */
public class PullToRefreshAdapter extends BaseMultiItemQuickAdapter<PullToRefreshModel, BaseViewHolder> implements BaseQuickAdapter.SpanSizeLookup{
    private OnItemClickLitener mOnItemClickLitener;
    private Context mContext;

    public PullToRefreshAdapter(List<PullToRefreshModel> data, Context context){
        super(data);
        setSpanSizeLookup(this);
        mContext = context;
        addItemType(0, R.layout.list_home_text_item);
    }

    @Override
    protected void convert(BaseViewHolder helper, PullToRefreshModel item) {
            bindTextData(helper, item);
    }

    private void bindTextData(BaseViewHolder helper, PullToRefreshModel item) {
        CustomTextView customTextView = helper.getView(R.id.tv_home_text);
        customTextView.setText(item.getType());
    }

    @Override
    public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
        return 0;
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}
