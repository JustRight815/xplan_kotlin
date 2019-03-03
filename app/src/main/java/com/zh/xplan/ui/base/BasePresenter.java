package com.zh.xplan.ui.base;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by zh on 2017/12/7.
 */

public abstract class BasePresenter<T extends BaseView> {
    public abstract void onDestory();

    protected T view;

    public void attachView(T view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        unDisposable();
    }


    /**
     * 防止RxJava 内存泄露
     */
    protected CompositeDisposable mCompositeDisposable;

    protected void addDisposable(Disposable disposable) {
        //如果已经解绑了的话再次添加disposable需要创建新的实例，否则绑定是无效的
        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    /**
     * 在界面销毁后等需要解绑观察者的情况下调用此方法统一解绑，防止Rx造成的内存泄漏
     * CompositeDisposable.clear() 即可切断所有的水管
     */
    protected void unDisposable() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }
}
