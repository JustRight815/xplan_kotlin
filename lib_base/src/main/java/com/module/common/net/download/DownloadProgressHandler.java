package com.module.common.net.download;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import com.module.common.log.LogUtil;
import com.module.common.net.RetrofitCreator;
import com.module.common.net.callback.IDownLoadCallback;
import java.util.Map;
import java.util.WeakHashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 下载文件
 */
public final class DownloadProgressHandler {
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private final String URL;
    private static final WeakHashMap<String, Object> PARAMS = new WeakHashMap();
    private final String DOWNLOAD_DIR;
    private final String EXTENSION;
    private final String NAME;
    private final IDownLoadCallback iRequestCallback;

    public DownloadProgressHandler(String url,
                                   Map<String, Object> params,
                                   String downDir,
                                   String extension,
                                   String name,
                                   IDownLoadCallback iRequestCallback) {
        this.URL = url;
        PARAMS.putAll(params);
        this.DOWNLOAD_DIR = downDir;
        this.EXTENSION = extension;
        this.NAME = name;
        this.iRequestCallback = iRequestCallback;
    }

    public final void Download() {
        LogUtil.e("zh", "Download , ");
        RetrofitCallback retrofitCallback = new RetrofitCallback<ResponseBody>() {

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (iRequestCallback != null) {
                    iRequestCallback.onFailure("onResponse onFailure " + t.toString());
                }
            }

            @Override
            public void onSuccess(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    final ResponseBody responseBody = response.body();
                    final SaveFileTask task = new SaveFileTask(iRequestCallback);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            DOWNLOAD_DIR, EXTENSION, responseBody, NAME);

                    //这里一定要注意判断，否则文件下载不全
                    if (task.isCancelled()) {
                        if (iRequestCallback != null) {
                            iRequestCallback.onCancel();
                        }
                    }
                } catch (final Exception e) {
                    if (call.isCanceled()) {     //判断是主动取消还是别动出错
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
            public void onLoading(final long total, final long progress) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (iRequestCallback != null) {
                            iRequestCallback.onProgress(progress, total);
                        }
                    }
                });
            }
        };
        RetrofitCreator
                .getDownLoadApiService(retrofitCallback)
                .download(URL, PARAMS)
                .enqueue(retrofitCallback);
    }
}
