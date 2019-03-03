package com.module.common.net.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 继承retrofit2 Callback，对外统一返回IRequestCallback，避免过度依赖框架
 */
public final class RetrofitCallback implements Callback<String> {
    private final IRequestCallback iRequestCallback;

    public RetrofitCallback(IRequestCallback iRequestCallback) {
        this.iRequestCallback = iRequestCallback;
    }

    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        if (response.isSuccessful()) {
            if (call.isExecuted()) {
                if (iRequestCallback != null) {
                    iRequestCallback.onSuccess(response.body());
                }
            }
        } else {
            if (iRequestCallback != null) {
                iRequestCallback.onError(response.code(), response.message());
            }
        }
        onRequestFinish();
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {
        if (iRequestCallback != null) {
            iRequestCallback.onError(-1, t.toString());
        }
        onRequestFinish();
    }

    private void onRequestFinish() {
        if (iRequestCallback != null) {
            iRequestCallback.onComplete();
        }
    }
}
