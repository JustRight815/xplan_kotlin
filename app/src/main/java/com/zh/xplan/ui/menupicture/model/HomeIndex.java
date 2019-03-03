package com.zh.xplan.ui.menupicture.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import java.util.List;

/**
 * 首页数据模型
 */
public class HomeIndex {
    public String code;
    public List<ItemInfoListBean> itemInfoList;

    public static class ItemInfoListBean implements MultiItemEntity {

        public String itemType;
        public String module;
        public List<ItemContentListBean> itemContentList;

        @Override
        public int getItemType() {
            if("topBanner".equals(itemType)){
                return Constant.TYPE_TOP_BANNER;
            }
            else if("iconList".equals(itemType)){
                return Constant.TYPE_ICON_LIST;
            }
            else if("jdBulletin".equals(itemType)){
                return Constant.TYPE_JD_BULLETIN;
            }
            else if("pubuliu".equals(itemType)){
                return Constant.TYPE_PU_BU_LIU;
            }
            return Constant.TYPE_DEFAULT;
        }

        public int getSpanSize() {
            return 4;
        }

        public static class ItemContentListBean {
            /**
             * imageUrl : https://m.360buyimg.com/mobilecms/s720x322_jfs/t4903/41/12296166/85214/15205dd6/58d92373N127109d8.jpg!q70.jpg
             * clickUrl : 男装超级品牌类日
             */

            public String imageUrl;
            public String clickUrl;
            public String itemTitle;
            public String itemSubTitle;
            public String itemSubscript;
            public String itemRecommendedLanguage;
            public String itemBackgroundImageUrl;

        }
    }
}
