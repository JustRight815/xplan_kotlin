package com.zh.xplan.ui.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zh.xplan.R;

/**
 * 公共的标题栏配置类
 */
public class TitleUtil {

    private RelativeLayout titleBar;
    private ImageView left_imageview;
    private TextView middle_textview;
    private ImageView right_imageview;
    private Activity mActivity;

    /**
     * 第一种  初始化方式
     * 布局中直接引用进文件的初始化方式
     * @param context
     */
    public TitleUtil(Activity context) {
    	mActivity = context;
    	titleBar = (RelativeLayout) context.findViewById(R.id.title_bar_layout);
        left_imageview = (ImageView) context.findViewById(R.id.title_left_imageview);
        middle_textview = (TextView) context.findViewById(R.id.title_middle_textview);
        right_imageview = (ImageView) context.findViewById(R.id.title_right_imageview);
    }

    /**
     * 第二种初始化方式
     * 用代码创建布局或者在fragment中的时候使用
     * @param context
     */
    public TitleUtil(Activity context, View view) {
    	mActivity = context;
    	titleBar = (RelativeLayout) view.findViewById(R.id.title_bar_layout);
        left_imageview = (ImageView) view.findViewById(R.id.title_left_imageview);
        middle_textview = (TextView) view.findViewById(R.id.title_middle_textview);
        right_imageview = (ImageView) view.findViewById(R.id.title_right_imageview);
    }

    /**
     * 设置标题栏的背景图片
     */
    public TitleUtil setTitleBgRes(int resid) {
        titleBar.setBackgroundResource(resid);
        return this;
    }

    /**
     * 设置标题栏的背景颜色
     */
    public TitleUtil setTitleBgColor(int resid) {
        titleBar.setBackgroundColor(resid);
        return this;
    }

    /**
     * 设置中间文字的背景图片
     */
    public TitleUtil setMiddleTitleBgRes(int resid) {
        middle_textview.setBackgroundResource(resid);
        return this;
    }

    /**
     * 设置中间文字的背景颜色
     */
    public TitleUtil setMiddleTitleBgColor(int resid) {
        middle_textview.setTextColor(resid);
        return this;
    }

    /**
     * 设置中间文字的文本
     */
    public TitleUtil setMiddleTitleText(int resid) {
        middle_textview.setVisibility(resid > 0 ? View.VISIBLE : View.GONE);
        middle_textview.setText(resid);
        return this;
    }


    /**
     * 设置中间文字的文本
     *
     * @param text
     * @return
     */
    public TitleUtil setMiddleTitleText(String text) {
        middle_textview.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        middle_textview.setText(text);
        return this;
    }

   
    /**
     * 设置左边图片的资源以及监听事件
     * @param listener 左侧图片监听事件
     * @param resId 图片资源
     * @return
     */
    public TitleUtil setLeftImageRes(int resId, OnClickListener listener) {
        left_imageview.setVisibility(resId > 0 ? View.VISIBLE : View.GONE);
        left_imageview.setImageResource(resId);
        if(listener == null){
            left_imageview.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mActivity.finish();
                }
            });
        }else{
            left_imageview.setOnClickListener(listener);
        }
        return this;
    }

    /**
     * 设置左边图片的资源
     * @param listener 左侧图片监听事件
     * @param resId 图片资源
     * @return
     */
    public TitleUtil setRightImageRes(int resId, OnClickListener listener) {
        right_imageview.setVisibility(resId > 0 ? View.VISIBLE : View.GONE);
        right_imageview.setImageResource(resId);
        if(listener == null){
        }else{
            right_imageview.setOnClickListener(listener);
        }
        return this;
    }


    /**
     * 设置右边图片的资源
     *
     * @param resId
     * @return
     */
    public TitleUtil setRightImageRes(int resId) {
        right_imageview.setVisibility(resId > 0 ? View.VISIBLE : View.GONE);
        right_imageview.setImageResource(resId);
        return this;
    }

    public View build(){
        return titleBar;
    }

}
