package com.zh.xplan.ui.webviewActivity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.module.common.utils.PixelUtil
import com.tencent.smtt.sdk.WebView
import com.zh.xplan.R
import com.zh.xplan.XPlanApplication
import com.zh.xplan.ui.base.BaseActivity

/**
 * 腾讯 X5 演示实例
 * X5 webview 控件默认有个细长的滚动条，但是当快速滑动的时候，滚动条就变成一个小方块。
 * ScrollView 嵌套X5 webview  小方块不显示，滚动条显示为ScrollView样式
 * @author zh
 */
class WeatherDetailsActivity : BaseActivity(), View.OnClickListener {
    private var mURL: String? = null
    private var mTitle: String? = "" //页面标题
    private var title_name: TextView? = null
    private var title_bar_back_layout: LinearLayout? = null
    protected var mWebView: com.tencent.smtt.sdk.WebView? = null
    private var mProgressbar: ProgressBar? = null// 网页加载进度条
    private var currentProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mSwipeBackHelper.setIsOnlyTrackingLeftEdge(false)
        setContentView(R.layout.activity_weather_details)
        setStatusBarColor(resources.getColor(R.color.colorPrimaryDark), 0)
        initView()
        initData()
        loadURL()
    }

    private fun initView() {
        title_name = findViewById<View>(R.id.title_name) as TextView
        findViewById<View>(R.id.title_bar_back).setOnClickListener(this)
        findViewById<View>(R.id.title_bar_share).setOnClickListener(this)
        findViewById<View>(R.id.title_bar_colse).setOnClickListener(this)
        title_bar_back_layout = findViewById<View>(R.id.title_bar_back_layout) as LinearLayout
        mProgressbar = findViewById<View>(R.id.ProgressBar) as ProgressBar
        mProgressbar!!.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(2f, this))

        //防止内存泄露
        val webViewLayout = findViewById<View>(R.id.ll_webview_layout) as LinearLayout
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mWebView = WebView(applicationContext)
        mWebView!!.layoutParams = params
        webViewLayout.addView(mWebView)
        initWebView(mWebView!!)
    }

    private fun initWebView(webView: com.tencent.smtt.sdk.WebView) {
        val webSettings = webView.settings
        //下面方法去掉滚动条
//        webView.setHorizontalScrollBarEnabled(false)
//        webView.setVerticalScrollBarEnabled(false)
//        val ix5 = webView.getX5WebViewExtension()
//        if (null != ix5) {
//            ix5!!.setScrollBarFadingEnabled(false)
//        }
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        val appCachePath = XPlanApplication.getInstance().cacheDir
                .absolutePath
        webSettings.setAppCachePath(appCachePath)
        webSettings.allowFileAccess = true
        webSettings.setAppCacheEnabled(true)
        webSettings.cacheMode = com.tencent.smtt.sdk.WebSettings.LOAD_DEFAULT

        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        //密码明文存储漏洞 关闭密码保存提醒
        webSettings.savePassword = false
        //域控制不严格漏洞 禁用 file 协议；
        webSettings.allowFileAccess = false
        webSettings.setAllowFileAccessFromFileURLs(false)
        webSettings.setAllowUniversalAccessFromFileURLs(false)

        webView.webChromeClient = object : com.tencent.smtt.sdk.WebChromeClient() {
            // 监听网页进度
            override fun onProgressChanged(view: com.tencent.smtt.sdk.WebView?, newProgress: Int) {
                if (newProgress > currentProgress) {
                    currentProgress = newProgress
                    mProgressbar!!.progress = newProgress
                }
                if (newProgress == 100) {
                    val h = Handler()
                    h.postDelayed({
                        mProgressbar!!.visibility = View.GONE
                        currentProgress = 0
                    }, 500)
                } else {
                    if (mProgressbar!!.visibility == View.GONE) {
                        mProgressbar!!.visibility = View.VISIBLE
                    }
                }
                super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: com.tencent.smtt.sdk.WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                //获取到网页标题
                mTitle = title
                title_name!!.text = mTitle
            }
        }

        // WebView默认使用系统默认浏览器打开网页的行为，覆盖这个方法使网页用自己的WebView打开
        webView.webViewClient = object : com.tencent.smtt.sdk.WebViewClient() {
            override fun shouldOverrideUrlLoading(view: com.tencent.smtt.sdk.WebView?, url: String?): Boolean {
                // 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view!!.loadUrl(url)
                return true
            }

            override fun onPageStarted(webView: WebView, s: String, bitmap: Bitmap) {
                if (webView.canGoBack()) {
                    pageNavigator(View.VISIBLE)
                } else {
                    pageNavigator(View.GONE)
                }
                super.onPageStarted(webView, s, bitmap)
            }
        }
    }

    private fun pageNavigator(tag: Int) {
        title_bar_back_layout!!.visibility = tag
    }

    private fun initData() {
        val intent = intent
        mURL = intent.getStringExtra("URL")
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.title_bar_back -> if (!mWebView!!.canGoBack()) {
                finish()
            } else {
                mWebView!!.goBack()
            }
            R.id.title_bar_colse -> finish()
            R.id.title_bar_share -> {
                var curUrl = ""
                if (mWebView != null) {
                    curUrl = mWebView!!.url
                }
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, "$mTitle:$curUrl")
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }
            else -> {
            }
        }
    }


    private fun loadURL() {
        if (mURL != null) {
            mWebView!!.loadUrl(mURL)
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView != null && mWebView!!.canGoBack()) {
            mWebView!!.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (mWebView != null) {
            mWebView!!.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            mWebView!!.clearHistory()
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再destory()
            val parent = mWebView!!.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(mWebView)
            }
            mWebView!!.stopLoading()
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            mWebView!!.settings.javaScriptEnabled = false
            mWebView!!.removeAllViews()
            mWebView!!.destroy()
            mWebView = null
        }
        super.onDestroy()
    }
}
