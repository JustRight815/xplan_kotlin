package com.zh.xplan.ui.indexactivity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.jaeger.library.StatusBarUtil
import com.module.common.log.LogUtil
import com.zh.xplan.R
import com.zh.xplan.ui.base.BaseActivity
import com.zh.xplan.ui.mainactivity.MainActivity
import com.zh.xplan.ui.utils.FileUtils
import kotlinx.android.synthetic.main.activity_splash.*
import java.io.File
import java.util.*

/**
 * 广告界面
 */
class AdActivity : BaseActivity(), View.OnClickListener {
    private var isShowTimer = false
    private var isJumpFinish = false
    private val SPLASH_FILE_NAME = "splash"

    private val countDownTimer = object : CountDownTimer(1200, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            btn_jump?.text = "跳过(" + millisUntilFinished / 1000 + "s)"
        }

        override fun onFinish() {
            btn_jump?.text = "跳过(" + 0 + "s)"
            gotoMainActivity()
        }
    }

    override fun isSupportSwipeBack(): Boolean {
        return false
    }

    override fun isSupportSkinChange(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        setContentView(R.layout.activity_splash)
        StatusBarUtil.setTranslucentForImageView(this, 0, null)//状态栏透明
        btn_jump?.setOnClickListener(this)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        tvCopyright?.text = "Copyright © 2016--$year XPlan"
    }

    override fun onClick(view: View) {
        when (view.id) {
        //            case R.id.sp_bg:
        //                gotoWebActivity();
        //                break;
            R.id.btn_jump -> gotoMainActivity()
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isShowTimer) {
            isShowTimer = true
            showSplash()
        }
    }

    private fun showSplash() {
        val splashImg = File(FileUtils.getSplashDir(), SPLASH_FILE_NAME)
        if (splashImg.exists()) {
            LogUtil.e("zh", "splashImg.exists() ")
            val bitmap = BitmapFactory.decodeFile(splashImg.path)
            if (bitmap != null) {
                LogUtil.e("zh", "FileUtils.bitmap ")
                ivSplash?.setImageBitmap(bitmap)
                countDownTimer.start()
                btn_jump?.visibility = View.VISIBLE
                return
            }
        } else {
            LogUtil.e("zh", "splashImg not exists() ")
        }
        gotoMainActivity()
    }


    private fun gotoMainActivity() {
        synchronized(this) {
            // 跳转到主界面,防止计时结束重复打开
            if(!isJumpFinish){
                isJumpFinish = true
                countDownTimer.cancel()
                startActivity(Intent(this@AdActivity,
                        MainActivity::class.java))
                overridePendingTransition(0,0)
                finish()
            }
        }
    }

    override fun onBackPressed() {}

    override fun onDestroy() {
        countDownTimer.cancel()
        super.onDestroy()
    }
}
