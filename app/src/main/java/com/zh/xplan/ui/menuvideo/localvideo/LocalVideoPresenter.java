package com.zh.xplan.ui.menuvideo.localvideo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.module.common.BaseLib;
import com.zh.xplan.ui.base.BasePresenter;
import com.zh.xplan.ui.menuvideo.localvideo.model.LocalVideoBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zh on 2017/12/6.
 */
public class LocalVideoPresenter extends BasePresenter<LocalVideoView> {

    public LocalVideoPresenter(){
    }

    @Override
    public void onDestory() {
        detachView();
    }

    public void getLocalVideos(){
        DisposableObserver disposableObserver = Observable.create(new ObservableOnSubscribe<List<LocalVideoBean>>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<List<LocalVideoBean>> e) throws Exception {
                ContentResolver contentResolver = BaseLib.getContext().getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projections = new String[]{
                        MediaStore.Video.Media.TITLE,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATA
                };
                Cursor cursor = contentResolver.query(uri,projections,null,null,null);
                List<LocalVideoBean> videos = new ArrayList<LocalVideoBean>();
                while (cursor.moveToNext()){
                    LocalVideoBean v = new LocalVideoBean(cursor.getString(0),cursor.getString(1),cursor.getLong(2),cursor.getString(3));
                    videos.add(v);
                }
                cursor.close();
                e.onNext(videos);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<LocalVideoBean>>() {
                    @Override
                    public void onNext(List<LocalVideoBean> list) {
                        if(view != null){
                            view.updateLocalVideoData(list);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                    }
                });
        addDisposable(disposableObserver);
    }
}
