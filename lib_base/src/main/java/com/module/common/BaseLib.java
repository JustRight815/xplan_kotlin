package com.module.common;

import android.content.Context;
import android.support.annotation.NonNull;

import com.module.common.image.ImageLoader;
import com.module.common.log.LogUtil;

/**
 * Created by zh on 2017/7/31.
 *  BaseLib 工具类初始化相关 主要是为了避免其他util直接使用Application.this造成耦合太大
 */
public final class BaseLib {
    private static Context context;
    public static boolean isDebug = true;
    public static String HTTP_HOST = "";

    public static BaseLib instance = new BaseLib();

    private BaseLib() {}

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static BaseLib init(@NonNull final Context context, boolean isDebug) {
        BaseLib.context = context.getApplicationContext();
        BaseLib.isDebug = isDebug;
        LogUtil.init();
        return instance;
    }

    /**
     * 获取ApplicationContext
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("u should init first");
    }

    /**
     * 设置OkHtttp网络请求baseUrl
     * @param baseUrl
     * @return
     */
    public static BaseLib setBaseUrl(@NonNull String baseUrl) {
        BaseLib.HTTP_HOST = baseUrl;
        return instance;
    }

    /**
     * 初始化imageloader
     * @param context
     * @return
     */
    public static BaseLib initImageManager(@NonNull Context context) {
        ImageLoader.init(context);
        return instance;
    }

}