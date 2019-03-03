package com.module.common.net.callback;

import java.io.File;

/**
 * 网络请求结果统一回调
 */
public abstract class IDownLoadCallback {
    public void onStart(long totalBytes) {}
    public void onCancel() {}
    public abstract void onFinish(File downloadFile);
    public abstract void onProgress(long currentBytes, long totalBytes);
    public abstract void onFailure(String error_msg);
}
