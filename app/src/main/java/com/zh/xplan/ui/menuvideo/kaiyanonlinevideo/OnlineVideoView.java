package com.zh.xplan.ui.menuvideo.kaiyanonlinevideo;

import com.zh.xplan.ui.base.BaseView;

/**
 * Created by zh on 2017/12/6.
 */

public interface OnlineVideoView extends BaseView {

    void updateOnlineData(boolean isSuccess,String response, final Boolean isPullDownRefresh);
}
