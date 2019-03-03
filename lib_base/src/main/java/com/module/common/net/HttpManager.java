package com.module.common.net;

/**
 * 网络请求 统一管理  Retrofit
 */
public final class HttpManager {

    private final static int GET = 0;
    private final static int POST = 1;
    private final static int POST_RAW = 2;
    private final static int PUT = 3;
    private final static int PUT_RAW = 4;
    private final static int DELETE = 5;
    private final static int UPLOAD = 6;
    private final static int DOWNLOAD = 7;
    private HttpManager() {
    }

    public final static HttpManagerBuilder get() {
        return new HttpManagerBuilder(GET);
    }

    public final static HttpManagerBuilder post() {
        return new HttpManagerBuilder(POST);
    }

    public final static HttpManagerBuilder put() {
        return new HttpManagerBuilder(PUT);
    }

    public final static HttpManagerBuilder delete() {
        return new HttpManagerBuilder(DELETE);
    }

    public final static HttpManagerBuilder upload() {
        return new HttpManagerBuilder(UPLOAD);
    }

    public final static HttpManagerBuilder download() {
        return new HttpManagerBuilder(DOWNLOAD);
    }
}
