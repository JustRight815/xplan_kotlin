package com.zh.xplan.ui.menusetting

import android.content.Intent
import android.os.Bundle
import com.zh.xplan.R
import com.zh.xplan.ui.base.BaseActivity
import com.zh.xplan.ui.robot.RobotKotlinActivity
import kotlinx.android.synthetic.main.activity_kotin_demo.*

/**
 *
 */
class KotlinDemoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotin_demo)
        setStatusBarColor(resources.getColor(R.color.colorPrimaryDark), 0)
        tv_text.text = "你好啊"
        btn_text.text = "小机器人"
        btn_text.setOnClickListener {
            startActivity(Intent(this,RobotKotlinActivity::class.java))
        }
    }
}
