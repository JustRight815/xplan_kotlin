package com.zh.xplan.ui.logisticsdetail;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.logisticsdetail.adapter.LogisticsInfoAdapter;
import com.zh.xplan.ui.logisticsdetail.bean.LogisticsInfoBean;
import java.util.ArrayList;
import java.util.List;

/**
 * 仿物流流程详情效果
 */
public class LogisticsDetailActivity extends BaseActivity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logistics_detail);
		initTitle();
		RecyclerView recyclerView = findViewById(R.id.rv_logistics);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setFocusable(false);
		//解决ScrollView嵌套RecyclerView出现的系列问题
		recyclerView.setNestedScrollingEnabled(false);
		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(new LogisticsInfoAdapter(this, R.layout.item_logistics, getData()));
	}

	private void initTitle() {
		findViewById(R.id.title_bar_back).setOnClickListener(this);
		setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark),0);
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

	private List<LogisticsInfoBean> getData() {
		List<LogisticsInfoBean> data = new ArrayList<>();
		data.add(new LogisticsInfoBean("2018-05-20 13:37:57", "客户 签收人: 已签收 感谢使用圆通速递，期待再次为您服务"));
		data.add(new LogisticsInfoBean("2018-05-20 09:03:42", "【广东省深圳市宝安区公司】 派件人: 快递员 派件中 派件员电话133111111"));
		data.add(new LogisticsInfoBean("2018-05-20 08:27:10", "【广东省深圳市宝安区公司】 已收入"));
		data.add(new LogisticsInfoBean("2018-05-20 04:38:32", "【深圳转运中心】 已收入"));
		data.add(new LogisticsInfoBean("2018-05-19 01:27:49", "【北京转运中心】 已发出 下一站 【深圳转运中心】"));
		data.add(new LogisticsInfoBean("2018-05-19 01:17:19", "【北京转运中心】 已收入"));
		data.add(new LogisticsInfoBean("2018-05-18 18:34:28", "【河北省北戴河公司】 已发出 下一站 【北京转运中心】"));
		data.add(new LogisticsInfoBean("2018-05-18 18:33:23", "【河北省北戴河公司】 已打包"));
		data.add(new LogisticsInfoBean("2018-05-18 18:27:21", "【河北省北戴河公司】 已收件"));
		return data;
	}
}
