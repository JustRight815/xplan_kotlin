package com.zh.xplan.ui.menutoutiao;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.module.common.log.LogUtil;
import com.zh.xplan.ui.imagedetailactivity.ImageDetailActivity;
import com.zh.xplan.ui.menupicture.model.GridPictureModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShowPicRelation {

    private static final String TAG = ShowPicRelation.class.getSimpleName();

    private Context mContext;
    private List<String> mUrls = new ArrayList();

    public ShowPicRelation(Context context) {
        this.mContext = context;
    }

    /**JS中点击图片执行的Java代码*/
    @JavascriptInterface
    public void openImg(String url){
        LogUtil.e(TAG,"openImg url" + url);
        //传到展示图片的viewPager
        List<GridPictureModel> mPictureModelList = new ArrayList();
        for (int i = 0; i < mUrls.size(); i++) {
            GridPictureModel gr = new GridPictureModel();
            gr.setPictureUrl(mUrls.get(i));
            mPictureModelList.add(gr);
        }
        Intent intent = new Intent(mContext, ImageDetailActivity.class);
        intent.putExtra("mPictureModelList", (Serializable) mPictureModelList);
        intent.putExtra("position", mUrls.indexOf(url));
        mContext.startActivity(intent);
    }

    /**页面加载时JS调用的Java代码*/
    @JavascriptInterface
    public void getImgArray(String urlArray){
        LogUtil.e(TAG,urlArray);
        String[] urls = urlArray.split(";");//url拼接成的字符串，有分号隔开
        for (String url : urls) {
            mUrls.add(url);
        }
    }
}
