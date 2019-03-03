package com.module.common.net.callback;

/**
 * 网络请求结果统一回调
 */
public abstract class IRequestCallback{
    //网络请求开始
    public void onStart(){}

    //网络请求完成
    public void onComplete(){}

    //网络请求成功
    public abstract void onSuccess(String response);
    //网络请求失败
    public abstract void onError(int code, String msg);
}
