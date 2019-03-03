package com.zh.xplan.ui.menuvideo.kaiyanonlinevideo;

import com.module.common.net.rx.NetManager;
import com.zh.xplan.ui.base.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zh on 2017/12/6.
 */
public class OnlineVideoPresenter extends BasePresenter<OnlineVideoView> {

    public OnlineVideoPresenter(){
    }

    @Override
    public void onDestory() {
        detachView();
    }

    public void getOnlineVideos(String url,String date, final Boolean isPullDownRefresh) {
        DisposableObserver disposableObserver = NetManager.get()
                .url(url)
                .params("date",date)
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String response) {
                        if(view != null){
                            view.updateOnlineData(true,response,isPullDownRefresh);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(view != null){
                            view.updateOnlineData(false,e.toString(),isPullDownRefresh);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDisposable(disposableObserver);
    }
}
