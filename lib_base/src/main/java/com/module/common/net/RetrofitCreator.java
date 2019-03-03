package com.module.common.net;

import com.module.common.BaseLib;
import com.module.common.net.download.FileResponseBody;
import com.module.common.net.download.RetrofitCallback;
import com.module.common.net.rx.RxApiService;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Retrofit 创建
 */
public final class RetrofitCreator {
    private static final String BASE_URL = BaseLib.HTTP_HOST;
    /**
     * 构建OkHttp
     */
    private static final class OKHttpHolder {
        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();
        //设置缓存路径
        private static File httpCacheDirectory = new File(BaseLib.getContext().getCacheDir(), "okhttpCache");

        private static final OkHttpClient OK_HTTP_CLIENT = addInterceptor()
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
//                .cache(new Cache(httpCacheDirectory, 10 * 1024 * 1024)) //设置缓存 10M
                .build();

        private static OkHttpClient.Builder addInterceptor() {
            //**  统一添加header
            // ** 放在loggingInterceptor之前，否则可能添加header失败
            Interceptor headerInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
//                            .removeHeader("User-Agent")
//                            .addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6")
//                            .addHeader("mac", "11:22:33:10:55")
//                            .addHeader("uuid", "11111111111111111")
                            .build();
                    return chain.proceed(request);
                }
            };
            BUILDER.addInterceptor(headerInterceptor);

            //可以判断是否是debug模式，不是则不加拦截
            if(BaseLib.isDebug){
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                BUILDER.addInterceptor(loggingInterceptor);
            }
            return BUILDER;
        }
    }

    /**
     * 获取OkHttpClient 应用内最好是单例模式，不要再创建其他Client
     * @return
     */
    public static OkHttpClient getOkHttpClient() {
        return OKHttpHolder.OK_HTTP_CLIENT;
    }


    //============================================Retrofit ==============================================

    /**
     * Service接口
     */
    private static final class RetrofitServiceHolder {
        private static final Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OKHttpHolder.OK_HTTP_CLIENT)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        private static final RetrofitApiService API_SERVICE =
                RETROFIT_CLIENT.create(RetrofitApiService.class);
    }

    /**
     * 获取Retrofit 网络请求api
     * @return
     */
    public static RetrofitApiService getApiService() {
        return RetrofitServiceHolder.API_SERVICE;
    }

    /**
     * 获取Retrofit 网络请求api  带下载进度
     * @return
     */
    public static RetrofitApiService getDownLoadApiService(final RetrofitCallback callback) {
        OkHttpClient httpClient = getOkHttpClient().newBuilder()
                .addNetworkInterceptor(new Interceptor() {      //设置拦截器
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new FileResponseBody(originalResponse.body(), callback))
                                .build();
                    }
                }).build();
        String BASE_URL = BaseLib.HTTP_HOST;
        Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
         RetrofitApiService API_SERVICE =
                RETROFIT_CLIENT.create(RetrofitApiService.class);
        return API_SERVICE;
    }

    //============================================RxJava ==============================================

    /**
     * 获取RxJava 网络请求api  带下载进度
     * @return
     */
    public static RxApiService getRxApiService(final RetrofitCallback callback) {
        OkHttpClient httpClient = getOkHttpClient().newBuilder()
                .addNetworkInterceptor(new Interceptor() {      //设置拦截器
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new FileResponseBody(originalResponse.body(), callback))
                                .build();
                    }
                }).build();
        String BASE_URL = BaseLib.HTTP_HOST;
        Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        RxApiService API_SERVICE =
                RETROFIT_CLIENT.create(RxApiService.class);
        return API_SERVICE;
    }

    /**
     * 获取RxJava 网络请求api
     * @return
     */
    public static RxApiService getRxApiService() {
        return RxServiceHolder.API_SERVICE;
    }

    /**
     * RxJava Service接口
     */
    private static final class RxServiceHolder {
        private static final Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OKHttpHolder.OK_HTTP_CLIENT)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        private static final RxApiService API_SERVICE =
                RETROFIT_CLIENT.create(RxApiService.class);
    }
}
