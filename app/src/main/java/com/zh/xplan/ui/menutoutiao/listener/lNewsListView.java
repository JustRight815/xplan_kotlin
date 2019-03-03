package com.zh.xplan.ui.menutoutiao.listener;

import com.zh.xplan.ui.menutoutiao.model.News;
import java.util.List;

/**
 * @author ChayChan
 * @description: 获取各种频道广告的View回调接口
 * @date 2017/6/18  9:33
 */

public interface lNewsListView {

    void onGetNewsListSuccess(List<News> newList, String tipInfo);

    void  onError();
}
