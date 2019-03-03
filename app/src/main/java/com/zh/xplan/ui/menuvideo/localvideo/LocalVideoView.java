package com.zh.xplan.ui.menuvideo.localvideo;

import com.zh.xplan.ui.base.BaseView;
import com.zh.xplan.ui.menuvideo.localvideo.model.LocalVideoBean;

import java.util.List;

/**
 * Created by zh on 2017/12/6.
 */

public interface LocalVideoView extends BaseView {
    void updateLocalVideoData(List<LocalVideoBean> videos);
}
