package com.zh.xplan.ui.indexactivity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import com.module.common.log.LogUtil
import com.zh.xplan.XPlanApplication
import com.zh.xplan.ui.base.BaseActivity
import com.zh.xplan.ui.mainactivity.MainActivity
import com.zh.xplan.ui.utils.CustomDensityUtil
import com.zh.xplan.ui.utils.FileUtils
import java.io.File

/**
 * 启动界面
 */
class IndexActivity : BaseActivity() {
    private val SPLASH_FILE_NAME = "splash"

    override fun isSupportSwipeBack(): Boolean {
        return false
    }

    override fun isSupportSkinChange(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //第一次安装应用在系统的安装器安装完成界面有“完成”和“打开”两个按钮。
        // 当用户点击“打开”按钮并进行了一些操作后，若此时用户点击Home键切出应用到桌面，
        //        再从桌面点击应用程序图标试图切回应用接着刚才的操作继续操作时，应用重新到了初始界面，
        //        此时之前从系统的安装完成界面点击打开启动的应用其实还在后面运行。
        //        一是、如果上面的Activity中实现了finish() 和 onDestroy() 方法，一定要保证这两个方法中
        // 不会有对空对象的操作以及注销未注册的广播等类似操作，因为第二次打开应用时，程序会调用finish()方法，
        // 及触发onDestroy()方法，而这两个函数里面的对象变量都还未进行初始化等操作。二是、finish() 和 onDestroy()
        // 方法中不能有System.exit(0);否则第二次打开应用杀掉进程时也会将第一次打开的应用杀掉。
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
        //==========================
        //		setContentView(R.layout.activity_index);
        setSwipeBackEnable(false)
        CustomDensityUtil.setCustomDensity(this, XPlanApplication.getInstance())
    }

    override fun onResume() {
        super.onResume()
        jumpActivity()
    }

    private fun jumpActivity() {
        // 跳转到主界面
        val splashImg = File(FileUtils.getSplashDir(), SPLASH_FILE_NAME)
        if (splashImg.exists()) {
            LogUtil.e("zh", "splashImg.exists() ")
            val bitmap = BitmapFactory.decodeFile(splashImg.path)
            if (bitmap != null) {
                LogUtil.e("zh", "FileUtils.bitmap ")
                startActivity(Intent(this, AdActivity::class.java))
                finish()
                return
            }
        }
        LogUtil.e("zh", "IndexActivity splashImg not exists() ")
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

}
