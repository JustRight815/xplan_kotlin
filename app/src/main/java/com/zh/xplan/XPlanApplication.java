package com.zh.xplan;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Process;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.mob.MobApplication;
import com.module.common.BaseLib;
import com.module.common.log.LogUtil;
import com.module.common.utils.CrashUtils;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;
import com.zh.swipeback.SlideFinishManager;
import com.zh.xplan.ui.menupicture.utils.ScreenUtil;
import com.zh.xplan.ui.skin.Settings;
import com.zh.xplan.ui.skin.SkinChangeHelper;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by zh on 2017/5/5.
 */

public class XPlanApplication extends MobApplication {
    private static XPlanApplication instance = null;
    public  static XPlanApplication getInstance(){
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        delayInit();
    }

    public void delayInit() {
        if(isMainProcess()){
//            initLeakCanary();
            CrashUtils.init();
            CrashReport.initCrashReport(getApplicationContext(), "0785b70a94", AppConstants.isDebug);
            BaseLib.init(this, AppConstants.isDebug)
                    .setBaseUrl(AppConstants.HTTP_HOST)
                    .initImageManager(this);
            initSkinLoader();
            SlideFinishManager.getInstance().init(this);
            //初始化litePal
            LitePal.initialize(getApplicationContext());
            initX5QbSdk();
            Glide.get(this);
            ScreenUtil.init(this);
        }
    }

    private void initLeakCanary() {
        //在其他代码之前初始化LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }

    /**
     * 当前进程是否是主线程
     * @return
     */
    private boolean isMainProcess(){
        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        if((packageName != null && packageName.equals(processName))){
            return true;
        }
        return false;
    }

    /**
     * 获取进程号对应的进程名 bugly推荐
     *
     * @param pid 进程号
     * @return 进程名
     */
    private String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }


    private void initX5QbSdk() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),   new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                LogUtil.e("application", "X5QbSdk onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        });
        if(!QbSdk.isTbsCoreInited()) {
            // preinit只需要调用一次，如果已经完成了初始化，那么就直接构造view
            LogUtil.e("application","预加载中...preinitX5WebCore");
            QbSdk.preInit(getApplicationContext(), null);
        }
    }

    public void destroyApp() {
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    /**
     * Must call init first
     */
    private void initSkinLoader() {
        Settings.createInstance(this);
        // 初始化皮肤框架
        SkinChangeHelper.getInstance().init(this);
        //初始化上次缓存的皮肤
        SkinChangeHelper.getInstance().refreshSkin(null);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    //设置app字体不允许随系统调节而发生大小变化
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1) {
            //非默认值
            getResources();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        Configuration newConfig = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (newConfig.fontScale != 1) {
            newConfig.fontScale = 1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Context configurationContext = createConfigurationContext(newConfig);
                resources = configurationContext.getResources();
                displayMetrics.scaledDensity = displayMetrics.density * newConfig.fontScale;
            } else {
                //updateConfiguration 在 API 25(7.0以上系统)之后，被方法 createConfigurationContext 替代
                resources.updateConfiguration(newConfig, displayMetrics);
            }
        }
        return resources;
    }

}
