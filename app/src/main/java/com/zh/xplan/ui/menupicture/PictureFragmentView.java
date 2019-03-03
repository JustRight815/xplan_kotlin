package com.zh.xplan.ui.menupicture;

import com.zh.xplan.ui.base.BaseView;
import com.zh.xplan.ui.menupicture.model.HomeIndex;
import com.zh.xplan.ui.menupicture.model.GridPictureModel;

import java.util.List;

/**
 * Created by zh on 2017/12/6.
 */

public interface PictureFragmentView extends BaseView {

    void updateBanner(List<String> imageUrlList);

    void updatePictureData(boolean isSuccess,boolean isPullDownRefresh,List<HomeIndex.ItemInfoListBean> pictureModelList,List<GridPictureModel> pictureList);
}
