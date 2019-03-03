package com.zh.xplan.ui.menupicture.adapter;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.module.common.image.ImageLoader;
import com.module.common.view.snackbar.SnackbarUtils;
import com.zh.xplan.R;
import com.zh.xplan.ui.aboutapp.AboutAppActivity;
import com.zh.xplan.ui.citypicker.util.ToastUtils;
import com.zh.xplan.ui.menupicture.model.Constant;
import com.zh.xplan.ui.menupicture.model.HomeIndex;
import com.zh.xplan.ui.menupicture.utils.ScreenUtil;
import com.zh.xplan.ui.menupicture.widget.IndicatorView;
import com.zh.xplan.ui.view.autoscrollviewpager.BGABanner;
import com.zh.xplan.ui.view.viewswitcher.UpDownViewSwitcher;

import org.qcode.qskinloader.SkinManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 瀑布流图片适配器
 */
public class GridPictureAdapter extends BaseMultiItemQuickAdapter<HomeIndex.ItemInfoListBean, BaseViewHolder> implements BaseQuickAdapter.SpanSizeLookup, BaseQuickAdapter.OnItemChildClickListener{
    private OnItemClickLitener mOnItemClickLitener;
    private int mWidth;
    private int maxHasLoadPosition;

    public GridPictureAdapter(List<HomeIndex.ItemInfoListBean> data){
        super(data);
        setSpanSizeLookup(this);
        addItemType(Constant.TYPE_TOP_BANNER, R.layout.homerecycle_item_top_banner);
        addItemType(Constant.TYPE_ICON_LIST, R.layout.home_icon_menu);
        addItemType(Constant.TYPE_JD_BULLETIN, R.layout.home_item_jd_bulletin);
        addItemType(Constant.TYPE_PU_BU_LIU, R.layout.item_simple_textview);
    }

    /**
     * 防止复杂布局item复用 https://github.com/CymChad/BaseRecyclerViewAdapterHelper/issues/2324
     */
    public void resetMaxHasLoadPosition() {
        maxHasLoadPosition = 0;
    }

    @Override
    protected void convert(BaseViewHolder helper, HomeIndex.ItemInfoListBean item) {
        int position = helper.getLayoutPosition() - getHeaderLayoutCount();
        if (maxHasLoadPosition < position) {
            maxHasLoadPosition = position;
        }

        if ("topBanner".equals(item.itemType) && maxHasLoadPosition <= position) {
            setFullSpan(helper);
            bindTopBannerData(helper, item);
        }else if ("iconList".equals(item.itemType) && maxHasLoadPosition <= position) {
            setFullSpan(helper);
            bindIconListData(helper, item);
        }else if ("jdBulletin".equals(item.itemType) && maxHasLoadPosition <= position) {
            setFullSpan(helper);
            bindJDBulletinData(helper, item);
        }else if ("pubuliu".equals(item.itemType)) {
            bindPuBuLiuData(helper, item);
        }
    }

    @Override
    public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
        return mData.get(position).getSpanSize();
    }

    /**
     * 绑定banner数据
     *
     * @param helper
     * @param item
     */
    private void bindTopBannerData(BaseViewHolder helper, final HomeIndex.ItemInfoListBean item) {
        if (helper.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager
                    .LayoutParams) helper
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
        BGABanner banner = helper.getView(R.id.banner);
        banner.setDelegate(new BGABanner.Delegate<View, HomeIndex.ItemInfoListBean.ItemContentListBean>() {
            @Override
            public void onBannerItemClick(BGABanner banner, View itemView, HomeIndex.ItemInfoListBean.ItemContentListBean model, int position) {
                Toast.makeText(itemView.getContext(), "" + item.itemContentList.get(position).clickUrl, Toast.LENGTH_SHORT).show();
            }
        });
        banner.setAdapter(new BGABanner.Adapter<View, HomeIndex.ItemInfoListBean.ItemContentListBean>() {
            @Override
            public void fillBannerItem(BGABanner banner, View itemView, HomeIndex.ItemInfoListBean.ItemContentListBean model, int position) {
                ImageView imageView = (ImageView) itemView.findViewById(R.id.sdv_item_fresco_content);
                ImageLoader.displayImage(mContext,imageView,model.imageUrl,R.drawable.holder_img);
            }
        });
        banner.setData(R.layout.homerecycle_top_banner_content, item.itemContentList, null);
    }


    private void setFullSpan(BaseViewHolder helper){
        if (helper.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager
                    .LayoutParams) helper
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }


    private void bindIconListData(final BaseViewHolder helper, final HomeIndex.ItemInfoListBean item) {
        ViewPager entranceViewPager = helper.getView(R.id.main_home_entrance_vp);
        final IndicatorView entranceIndicatorView = helper.getView(R.id.main_home_entrance_indicator);
        //首页菜单分页
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ((float) ScreenUtil.getScreenWidth() / 2.0f));
        entranceViewPager.setLayoutParams(layoutParams);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        //将RecyclerView放至ViewPager中：
        int pageSize = 10;
        //一共的页数等于 总数/每页数量，并取整。
        int pageCount = (int) Math.ceil(item.itemContentList.size() * 1.0 / pageSize);
        List<View> viewList = new ArrayList<View>();
        for (int index = 0; index < pageCount; index++) {
            //每个页面都是inflate出一个新实例
            RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.item_home_entrance_vp, entranceViewPager, false);
            recyclerView.setLayoutParams(layoutParams);
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, 5));
            EntranceAdapter entranceAdapter = new EntranceAdapter(mContext, item.itemContentList, index, pageSize);
            entranceAdapter.setOnItemClickLitener(new EntranceAdapter.OnItemClickLitener() {
                @Override
                public void onItemClick(View view,int position) {
                    HomeIndex.ItemInfoListBean.ItemContentListBean entrance = item.itemContentList.get(position);
                    ToastUtils.showToast(mContext,entrance.itemTitle + position);
                    mContext.startActivity(new Intent(mContext,AboutAppActivity.class));
                }
            });
            recyclerView.setAdapter(entranceAdapter);
            viewList.add(recyclerView);
        }
        CagegoryViewPagerAdapter adapter = new CagegoryViewPagerAdapter(viewList);
        entranceViewPager.setAdapter(adapter);
        entranceIndicatorView.setIndicatorCount(entranceViewPager.getAdapter().getCount());
        entranceIndicatorView.setCurrentIndicator(entranceViewPager.getCurrentItem());
        entranceViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                entranceIndicatorView.setCurrentIndicator(position);
            }
        });
    }


    private void bindJDBulletinData(BaseViewHolder helper, final HomeIndex.ItemInfoListBean item) {
        UpDownViewSwitcher viewSwitcher = helper.getView(R.id.home_view_switcher);
        viewSwitcher.setSwitcheNextViewListener(new UpDownViewSwitcher.SwitchNextViewListener() {
            @Override
            public void switchTONextView(View nextView, int index) {
                if (nextView == null){
                    return;
                }
                final String tag = item.itemContentList.get(index % item.itemContentList.size()).itemTitle;
                final String tag1 = item.itemContentList.get(index % item.itemContentList.size()).itemSubTitle;
                ((TextView) nextView.findViewById(R.id.textview)).setText(tag1);
                ((TextView) nextView.findViewById(R.id.switch_title_text)).setText(tag);
                nextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SnackbarUtils.ShortToast(v,tag1);
                    }
                });
            }
        });
        viewSwitcher.setContentLayout(R.layout.switch_view);
    }

    private void bindPuBuLiuData(final BaseViewHolder helper, HomeIndex.ItemInfoListBean item) {
        SkinManager.with(helper.itemView).applySkin(true);
        final List<HomeIndex.ItemInfoListBean.ItemContentListBean> itemContentList = item.itemContentList;
        if(itemContentList != null && itemContentList.size() > 0 ){
            helper.itemView.setClickable(true);
            helper.setText(R.id.id_tv, itemContentList.get(0).itemTitle);

            ImageView imageView = helper.getView(R.id.iv_image);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getWidth()/2);
            imageView.setLayoutParams(layoutParams);
            ImageLoader.displayImage(mContext,imageView,Uri.parse(itemContentList.get(0).imageUrl),0);
            helper.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickLitener != null){
                        mOnItemClickLitener.onItemClick(v, itemContentList.get(0).imageUrl );
                    }
                }
            });
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
    }

    private int getWidth(){
        //获取屏幕宽高
        if(mWidth != 0){
            return mWidth;
        }
        mWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        return mWidth;
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, String url);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}
