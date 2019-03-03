package com.module.common.net;

import android.support.annotation.NonNull;

import com.module.common.net.callback.IDownLoadCallback;
import com.module.common.net.callback.IRequestCallback;
import com.module.common.net.callback.RetrofitCallback;
import com.module.common.net.download.DownloadProgressHandler;

import java.io.File;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * 添加网络请求参数 Builder模式,链式函数调用
 */
public final class HttpManagerBuilder {
    private String mUrl = null;
    private WeakHashMap<String, Object> mParams = new WeakHashMap();
    private WeakHashMap<String, String> mHeaders = new WeakHashMap();
    private RequestBody mBody = null;
    private File mFile = null;
    private String mDownloadDir = null;
    private String mExtension = null;
    private String mName = null;

    private final int GET = 0;
    private final int POST = 1;
    private final int POST_RAW = 2;
    private final int PUT = 3;
    private final int PUT_RAW = 4;
    private final int DELETE = 5;
    private final int UPLOAD = 6;
    private final int DOWNLOAD = 7;
    private final int DOWNLOAD_PRO = 8;

    private int httpMethod = -1;

    HttpManagerBuilder(int httpMethod) {
        this.httpMethod = httpMethod;
    }

    public final HttpManagerBuilder url(String url) {
        this.mUrl = url;
        return this;
    }

    public final HttpManagerBuilder params(WeakHashMap<String, Object> params) {
        mParams.putAll(params);
        return this;
    }

    public final HttpManagerBuilder params(String key, Object value) {
        mParams.put(key, value);
        return this;
    }

    public final HttpManagerBuilder headers(@NonNull String key, @NonNull String value) {
        mHeaders.put(key, value);
        return this;
    }

    public final HttpManagerBuilder file(File file) {
        this.mFile = file;
        return this;
    }

    public final HttpManagerBuilder file(String file) {
        this.mFile = new File(file);
        return this;
    }

    public final HttpManagerBuilder name(String name) {
        this.mName = name;
        return this;
    }

    public final HttpManagerBuilder dir(String dir) {
        this.mDownloadDir = dir;
        return this;
    }

    public final HttpManagerBuilder extension(String extension) {
        this.mExtension = extension;
        return this;
    }

    public final HttpManagerBuilder raw(String raw) {
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), raw);
        return this;
    }

    public void enqueue(IRequestCallback iRequestCallback) {
        switch (httpMethod) {
            case GET:
                request(GET,iRequestCallback);
                break;
            case POST:
                if (mBody == null) {
                    request(POST,iRequestCallback);
                } else {
                    if (!mParams.isEmpty()) {
                        throw new RuntimeException("params must be null!");
                    }
                    request(POST_RAW,iRequestCallback);
                }
                break;
            case PUT:
                if (mBody == null) {
                    request(PUT,iRequestCallback);
                } else {
                    if (!mParams.isEmpty()) {
                        throw new RuntimeException("params must be null!");
                    }
                    request(PUT_RAW,iRequestCallback);
                }
                break;
            case DELETE:
                request(DELETE,iRequestCallback);
                break;
            case UPLOAD:
                request(UPLOAD,iRequestCallback);
                break;
            default:
                break;
        }
    }

    public void enqueue(IDownLoadCallback iRequestCallback) {
        switch (httpMethod) {
            case DOWNLOAD_PRO:
                new DownloadProgressHandler(mUrl,mParams,mDownloadDir, mExtension, mName,
                        iRequestCallback)
                        .Download();
                break;
            default:
                break;
        }
    }


    private void request(int method,IRequestCallback iRequestCallback) {
        final RetrofitApiService service = RetrofitCreator.getApiService();
        Call<String> call = null;
        if (iRequestCallback != null) {
            iRequestCallback.onStart();
        }
        switch (method) {
            case GET:
                call = service.get(mUrl, mParams);
                break;
            case POST:
                call = service.post(mUrl, mParams);
                break;
            case POST_RAW:
                call = service.postRaw(mUrl, mBody);
                break;
            case PUT:
                call = service.put(mUrl, mParams);
                break;
            case PUT_RAW:
                call = service.putRaw(mUrl, mBody);
                break;
            case DELETE:
                call = service.delete(mUrl, mParams);
                break;
            case UPLOAD:
                final RequestBody requestBody =
                        RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), mFile);
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", mFile.getName(), requestBody);
                call = service.upload(mUrl, body);
                break;
            default:
                break;
        }

        if (call != null) {
            call.enqueue(new RetrofitCallback(iRequestCallback));
        }
    }
}
