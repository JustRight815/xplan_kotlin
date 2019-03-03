package com.module.common.net.rx;

import com.module.common.net.callback.IDownLoadCallback;

import java.util.WeakHashMap;

import io.reactivex.observers.DisposableObserver;

/**
 * 网络请求 统一管理 Retrofit + RxJava
 */
public final class NetManager {

    private final static int GET = 0;
    private final static int POST = 1;
    private final static int POST_RAW = 2;
    private final static int PUT = 3;
    private final static int PUT_RAW = 4;
    private final static int DELETE = 5;
    private final static int UPLOAD = 6;
    private final static int DOWNLOAD = 7;

    public static final NetManagerBuilder get() {
        return new NetManagerBuilder(GET);
    }

    public static final NetManagerBuilder post() {
        return new NetManagerBuilder(POST);
    }

    public static final NetManagerBuilder put() {
        return new NetManagerBuilder(PUT);
    }

    public static final NetManagerBuilder delete() {
        return new NetManagerBuilder(DELETE);
    }

    public static final NetManagerBuilder upload() {
        return new NetManagerBuilder(UPLOAD);
    }

    public static final DisposableObserver download(String url, WeakHashMap<String, Object> params, String downloadDir, String extension, String name, IDownLoadCallback iRequestCallback) {
        return NetManagerBuilder.downLoad(url,params,downloadDir,extension,name,iRequestCallback);
    }
}
