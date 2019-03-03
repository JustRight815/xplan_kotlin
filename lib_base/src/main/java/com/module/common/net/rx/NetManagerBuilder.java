package com.module.common.net.rx;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.module.common.log.LogUtil;
import com.module.common.net.RetrofitCreator;
import com.module.common.net.callback.IDownLoadCallback;
import com.module.common.net.download.RetrofitCallback;
import com.module.common.net.download.SaveFileTask;

import java.io.File;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 添加网络请求参数 Builder模式,链式函数调用
 */
public final class NetManagerBuilder {
    private String mUrl = null;
    private WeakHashMap<String, Object> mParams = new WeakHashMap();
    private RequestBody mBody = null;
    private File mFile = null;

    private final int GET = 0;
    private final int POST = 1;
    private final int POST_RAW = 2;
    private final int PUT = 3;
    private final int PUT_RAW = 4;
    private final int DELETE = 5;
    private final int UPLOAD = 6;
    private int httpMethod = -1;


    NetManagerBuilder(int httpMethod) {
        this.httpMethod = httpMethod;
    }

    public final NetManagerBuilder url(String url) {
        this.mUrl = url;
        return this;
    }

    public final NetManagerBuilder params(WeakHashMap<String, Object> params) {
        mParams.putAll(params);
        return this;
    }

    public final NetManagerBuilder params(String key, Object value) {
        mParams.put(key, value);
        return this;
    }

    public final NetManagerBuilder file(File file) {
        this.mFile = file;
        return this;
    }

    public final NetManagerBuilder file(String file) {
        this.mFile = new File(file);
        return this;
    }

    public final NetManagerBuilder raw(String raw) {
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), raw);
        return this;
    }

    public final Observable<String> build() {
        final RxApiService service = RetrofitCreator.getRxApiService();
        Observable<String> observable = null;
        switch (httpMethod) {
            case GET:
                observable =  service.get(mUrl, mParams);
                break;
            case POST:
                if (mBody == null) {
                    observable = service.post(mUrl, mParams);
                } else {
                    if (!mParams.isEmpty()) {
                        throw new RuntimeException("params must be null!");
                    }
                    observable = service.postRaw(mUrl, mBody);
                }
                break;
            case PUT:
                if (mBody == null) {
                    observable = service.put(mUrl, mParams);
                } else {
                    if (!mParams.isEmpty()) {
                        throw new RuntimeException("params must be null!");
                    }
                    observable = service.putRaw(mUrl, mBody);
                }
                break;
            case DELETE:
                observable = service.delete(mUrl, mParams);
                break;
            case UPLOAD:
                final RequestBody requestBody =
                        RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()), mFile);
                final MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", mFile.getName(), requestBody);
                observable = service.upload(mUrl, body);
                break;
            default:
                break;
        }
        return observable;
    }


    private static Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 下载文件 带进度条可以取消
     * @param url  下载地址
     * @param params 请求参数
     * @param downloadDir 下载目录
     * @param extension   后缀名
     * @param name  保存名称
     * @param iRequestCallback 回调
     * @return
     */
    public static DisposableObserver downLoad(String url, WeakHashMap<String, Object> params,final String downloadDir, final String extension, final String name, final IDownLoadCallback iRequestCallback) {
        LogUtil.e("zh","Download , " );
        RetrofitCallback retrofitCallback = new RetrofitCallback<ResponseBody>() {

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }

            @Override
            public void onSuccess(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onLoading(final long total, final long progress) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(iRequestCallback != null) {
                            iRequestCallback.onProgress(progress, total);
                        }
                    }
                });
            }
        };

        if(params == null){
            params = new WeakHashMap();
        }
        DisposableObserver disposableObserver = RetrofitCreator.getRxApiService(retrofitCallback)
                .download(url, params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        LogUtil.e("zh","onNext ");
                        try {
                            SaveFileTask task = new SaveFileTask(iRequestCallback);
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                    downloadDir, extension, responseBody, name);

                            //这里一定要注意判断，否则文件下载不全
                            if (task.isCancelled()) {
                                if (iRequestCallback != null) {
                                    iRequestCallback.onCancel();
                                }
                            }
                        } catch (final Exception e) {
                            if(isDisposed()) {     //判断是主动取消还是别动出错
                                if (iRequestCallback != null) {
                                    iRequestCallback.onCancel();
                                }
                            } else {
                                if (iRequestCallback != null) {
                                    iRequestCallback.onFailure("onResponse saveFile fail." + e.toString());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (iRequestCallback != null) {
                            iRequestCallback.onFailure("onResponse fail." + e.toString());
                        }
                    }

                    @Override
                    public void onComplete() {
                        LogUtil.e("zh","onComplete " );
                    }
                });
        return disposableObserver;
    }

}
