package com.zh.xplan.ui.base;

import android.util.SparseArray;

import com.zh.xplan.ui.menupicture.PictureFragment;
import com.zh.xplan.ui.menusetting.SettingFragment;
import com.zh.xplan.ui.menutoutiao.TouTiaoFragment;
import com.zh.xplan.ui.menuvideo.VideoFragment;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.KaiYanOnlineVideoFragment;
import com.zh.xplan.ui.menuvideo.localvideo.LocalVideoFragment;

/**
 * Fragments工厂   该工厂的fragment只创建一次
 * Created by zh
 */
public class FragmentsFactory  {

    public static final int MENU_PICTURE = 0;
    public static final int MENU_VIDEO = 2;
    public static final int MENU_SETTING = 3;
    public static final int ONLINE_VIDEO = 4;
    public static final int LOCAL_VIDEO = 5;
    public static final int MENU_TOU_TIAO = 7;
    // private static Map<Integer, BaseFragment> mFragments = new HashMap<Integer, BaseFragment>();
    // android  APi SparseArray代替HashMap 更为高效
    private static SparseArray<BaseFragment> mFragments = new SparseArray<BaseFragment>();
    
    public static BaseFragment createFragment(int position) {
        BaseFragment fragment = mFragments.get(position);
        if(fragment == null){
            switch (position) {
                case MENU_PICTURE:
                    fragment = new PictureFragment();
                    break;
                case MENU_VIDEO:
                    fragment = new VideoFragment();
                    break;
                case MENU_SETTING:
                    fragment = new SettingFragment();
//                    fragment = new SettingFragmentKotlin();
                    break;
                case ONLINE_VIDEO:
                    fragment = new KaiYanOnlineVideoFragment();
                    break;
                case LOCAL_VIDEO:
                    fragment = new LocalVideoFragment(); //
                    break;
                case MENU_TOU_TIAO:
                    fragment = new TouTiaoFragment(); //
                    break;
                default:
                    break;
            }
            mFragments.put(position, fragment);
        }
        return fragment;
    }

    public static BaseFragment getFragment(int position) {
        return mFragments.get(position,null);
    }
}
