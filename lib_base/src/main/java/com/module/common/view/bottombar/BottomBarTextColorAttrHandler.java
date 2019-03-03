package com.module.common.view.bottombar;

import android.content.res.Resources;
import android.view.View;
import org.qcode.qskinloader.IResourceManager;
import org.qcode.qskinloader.ISkinAttrHandler;
import org.qcode.qskinloader.entity.SkinAttr;
import org.qcode.qskinloader.entity.SkinConstant;

/**
 * Created by zh on 2017/12/30.
 * 自定义底部菜单文字颜色换肤
 */
public class BottomBarTextColorAttrHandler implements ISkinAttrHandler {
    public static final String BOTTOM_BAR_TEXT_COLOR = "textColorNormal";

    @Override
    public void apply(View view, SkinAttr skinAttr, IResourceManager resourceManager) {
        if(!(view instanceof BottomBarItem)) {
            //防止在错误的View上设置了此属性
            return;
        }
        BottomBarItem tv = (BottomBarItem) view;
        if (SkinConstant.RES_TYPE_NAME_COLOR.equals(skinAttr.mAttrValueTypeName)) {
            if (SkinConstant.RES_TYPE_NAME_COLOR.equals(
                    skinAttr.mAttrValueTypeName)) {
                try {
                    //先尝试按照int型颜色解析
                    int textColor = resourceManager.getColor(
                            skinAttr.mAttrValueRefId,
                            skinAttr.mAttrValueRefName);
                    if(tv.mTextColorNormal == tv.getTextView().getCurrentTextColor()){
                        tv.mTextColorNormal = textColor;
                        tv.getTextView().setTextColor(tv.mTextColorNormal);
                    }else{
                        tv.mTextColorNormal = textColor;
                    }
                } catch (Resources.NotFoundException ex) {
                    //不是int型则按照ColorStateList引用来解析
                }
            }
        }
    }
}