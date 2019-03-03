package com.zh.xplan.ui.logisticsdetail.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zh.xplan.R;
import com.zh.xplan.ui.logisticsdetail.bean.LogisticsInfoBean;

import java.util.ArrayList;
import java.util.List;

public class LogisticsInfoAdapter extends BaseQuickAdapter<LogisticsInfoBean, BaseViewHolder> {
    private Context context;
    private List<LogisticsInfoBean> data = new ArrayList<>();

    public LogisticsInfoAdapter(Context context, int layoutResId, @Nullable List<LogisticsInfoBean> data) {
        super(layoutResId, data);
        this.context = context;
        this.data.clear();
        if(data != null){
            this.data.addAll(data);
        }
    }

    @Override
    protected void convert(BaseViewHolder helper, LogisticsInfoBean item) {
        //获取物流信息和物流时间的字体颜色, 最新的一条物流数据字体为绿色
        int newInfoColor = context.getResources().getColor(helper.getLayoutPosition() == 0 ? R.color.green : R.color.gray);
        //当前item的索引==0 && 物流数据的数量大于1条   ->  显示绿色大圆圈
        helper.setGone(R.id.iv_new, helper.getLayoutPosition() == 0 && data.size() > 1)
                //当前item的索引!=0 && 物流数据的数量大于1条   ->  显示灰色小圆圈
                .setGone(R.id.iv_old, helper.getLayoutPosition() != 0 && data.size() > 1)
                //当前item的索引 != 0    ->  显示圆点上面短一点的灰线
                .setVisible(R.id.v_short_line, helper.getLayoutPosition() != 0)
                //当前item的索引 != 物流数据的最后一条    ->  显示圆点下面长一点的灰线
                .setGone(R.id.v_long_line, helper.getLayoutPosition() != data.size() - 1)
                //当前item的索引 != 物流数据的最后一条    ->  显示物流时间下面的横向的灰线
                .setGone(R.id.v_bottom_line, helper.getLayoutPosition() != data.size() - 1)
                .setTextColor(R.id.tv_info, newInfoColor)
                .setTextColor(R.id.tv_date, newInfoColor)
                //物流信息
                .setText(R.id.tv_info, item.getAcceptStation())
                //物流时间
                .setText(R.id.tv_date, item.getAcceptTime());
    }
}
