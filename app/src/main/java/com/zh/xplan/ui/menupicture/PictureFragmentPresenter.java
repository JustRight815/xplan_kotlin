package com.zh.xplan.ui.menupicture;

import com.google.gson.Gson;
import com.module.common.BaseLib;
import com.module.common.log.LogUtil;
import com.module.common.net.rx.NetManager;
import com.zh.xplan.AppConstants;
import com.zh.xplan.ui.base.BasePresenter;
import com.zh.xplan.ui.menupicture.model.Constant;
import com.zh.xplan.ui.menupicture.model.GridPictureModel;
import com.zh.xplan.ui.menupicture.model.HomeIndex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zh on 2017/12/6.
 */
public class PictureFragmentPresenter extends BasePresenter<PictureFragmentView> {

    public PictureFragmentPresenter(){
    }

    @Override
    public void onDestory() {
        detachView();
    }


    public void getBannerData(){
        List<String> imageUrlList = new ArrayList<String>();
        String imageUrl1 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4903/41/12296166/85214/15205dd6/58d92373N127109d8.jpg!q70.jpg";
        String imageUrl2 = "https://img1.360buyimg.com/da/jfs/t4309/113/2596274814/85129/a59c5f5e/58d4762cN72d7dd75.jpg";
        String imageUrl3 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4675/88/704144946/137165/bbbe8a4e/58d3a160N621fc59c.jpg!q70.jpg";
        String imageUrl4 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4627/177/812580410/198036/24a79c26/58d4f1e9N5b9fc5ee.jpg!q70.jpg";
        String imageUrl5 = "https://m.360buyimg.com/mobilecms/s720x322_jfs/t4282/364/2687292678/87315/e4311cd0/58d4d923N24a2f5eb.jpg!q70.jpg";
        String imageUrl6 = "https://img1.360buyimg.com/da/jfs/t4162/171/1874609280/92523/a1206b3f/58c7a832Nc8582e81.jpg";
        imageUrlList.add(imageUrl1);
        imageUrlList.add(imageUrl2);
        imageUrlList.add(imageUrl3);
        imageUrlList.add(imageUrl4);
        imageUrlList.add(imageUrl5);
        imageUrlList.add(imageUrl6);
        if(view != null){
            view.updateBanner(imageUrlList);
        }
    }


    public void updateData(final int currentPage, final Boolean isPullDownRefresh){
        DisposableObserver disposableObserver =  NetManager.get()
                .url(AppConstants.MEI_TU_MEI_JU_URL)
                .params("page",currentPage)
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String response) {
                        Document mDocument = Jsoup.parse(response);
                        List<String> titleList = new ArrayList<>();
                        Elements es = mDocument.getElementsByClass("xlistju");
                        for (Element e : es) {
                            titleList.add(e.text());
                        }
                        List<String> hrefList = new ArrayList<>();
                        List<Integer> heightList = new ArrayList<>();
                        List<Integer> widthList = new ArrayList<>();
                        Elements es1 = mDocument.getElementsByClass("chromeimg");
                        for (Element e : es1) {
                            hrefList.add(e.attr("src"));
                            if (null == e.attr("width") || "".equals(e.attr("width"))) {
                                widthList.add(300); //没有返回宽度时，设置默认高度300
                            } else {
                                widthList.add(Integer.parseInt((e.attr("width").substring(0, e.attr("width").length() - 2))));
                            }
                            if (null == e.attr("height") || "".equals(e.attr("height"))) {
                                heightList.add(200); //没有返回高度时 设置默认高度200
                            } else {
                                heightList.add(Integer.parseInt((e.attr("height").substring(0, e.attr("height").length() - 2))));
                            }
                        }

                        List<HomeIndex.ItemInfoListBean> mDataList = new ArrayList();
                        List<HomeIndex.ItemInfoListBean> itemInfoList = new ArrayList();
                        if(currentPage == 0){
                            try {
                                InputStream is = BaseLib.getContext().getAssets().open("homeindex.txt");
                                String text = readTextFromFile(is);
                                Gson gson = new Gson();
                                HomeIndex homeIndex = gson.fromJson(text, HomeIndex.class);
                                if(homeIndex != null && homeIndex.itemInfoList != null  ){
                                    itemInfoList.addAll(homeIndex.itemInfoList) ;
                                    mDataList.clear();
                                    homeIndex.itemInfoList.remove(homeIndex.itemInfoList.size()-1);
                                    mDataList.addAll(homeIndex.itemInfoList);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            LogUtil.e("zh","mHomeIndex " + mDataList.size());
                        }
                        List<GridPictureModel> pictureModeList = new ArrayList<>();

                        if(currentPage == 0 && itemInfoList != null && itemInfoList.size() >0 && hrefList.size() <= 0){
                            for (int i = 0; i < itemInfoList.size(); i++) {
                                HomeIndex.ItemInfoListBean itemInfoListBean = itemInfoList.get(i);
                                if( Constant.TYPE_PU_BU_LIU == itemInfoListBean.getItemType()){
                                    for (int j = 0; j <itemInfoListBean.itemContentList.size() ; j++) {
                                        HomeIndex.ItemInfoListBean bean = new HomeIndex.ItemInfoListBean();
                                        bean.itemType = "pubuliu";
                                        bean.module = "pubuliu";
                                        bean.itemContentList =  new ArrayList();
                                        bean.itemContentList.add(itemInfoListBean.itemContentList.get(j));
                                        mDataList.add(bean);
                                        GridPictureModel pictureModel = new GridPictureModel();
                                        pictureModel.setPictureTitle(itemInfoListBean.itemContentList.get(j).itemTitle);
                                        pictureModel.setPictureUrl(itemInfoListBean.itemContentList.get(j).imageUrl);
                                        pictureModel.setPictureHeight(123);
                                        pictureModel.setPictureWidth(123);
                                        pictureModeList.add(pictureModel);
                                    }
                                    break;
                                }
                            }
                        }else{
                            for (int i = 0; i < hrefList.size(); i++) {
                                List<HomeIndex.ItemInfoListBean.ItemContentListBean> itemContentList = new ArrayList();
                                HomeIndex.ItemInfoListBean.ItemContentListBean itemContentListBean = new HomeIndex.ItemInfoListBean.ItemContentListBean();
                                itemContentListBean.itemTitle = titleList.get(i);
                                itemContentListBean.imageUrl = ("http:" + hrefList.get(i));
                                itemContentList.add(itemContentListBean);
                                HomeIndex.ItemInfoListBean itemInfoListBean = new HomeIndex.ItemInfoListBean();
                                itemInfoListBean.itemType = "pubuliu";
                                itemInfoListBean.module = "pubuliu";
                                itemInfoListBean.itemContentList = itemContentList;
                                mDataList.add(itemInfoListBean);

                                GridPictureModel pictureModel = new GridPictureModel();
                                pictureModel.setPictureTitle(titleList.get(i));
                                pictureModel.setPictureUrl("http:" + hrefList.get(i));
                                pictureModel.setPictureHeight(heightList.get(i));
                                pictureModel.setPictureWidth(widthList.get(i));
                                pictureModeList.add(pictureModel);
                            }
                        }

                        if(view != null){
                            view.updatePictureData(true,isPullDownRefresh,mDataList,pictureModeList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(view != null){
                            view.updatePictureData(false,isPullDownRefresh,null,null);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        addDisposable(disposableObserver);
    }

    /**
     * 按行读取文本文件
     *
     * @param is
     * @return
     * @throws Exception
     */
    public String readTextFromFile(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuffer buffer = new StringBuffer("");
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
            buffer.append("\n");
        }
        return buffer.toString();
    }

}
