package com.zh.xplan.ui.webviewActivity;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.module.common.log.LogUtil;
import com.module.common.utils.PixelUtil;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.zh.xplan.ui.base.BaseActivity;
/**
 * 简单的加载web的页面示例  没有用到
 * @author zh
 */
public class WebviewActivity extends BaseActivity implements View.OnClickListener {
	private WebView mWebView;
	private String mURL = null;
	private ProgressBar mProgressbar;// 网页加载进度条
	private int currentProgress = 0;
	private String mTitle = ""; //页面标题
	private TextView title_name;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark),0);
		initTitle();
		initView();
		initData();
		loadURL();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** attention to this below ,must add this **/
	}

	private void initTitle() {
		/**
		 * 1.设置左边的图片按钮显示，以及事件 2.设置中间TextView显示的文字 3.设置右边的图片按钮显示，并设置事件
		 */
		title_name = (TextView) findViewById(R.id.title_name);
		findViewById(R.id.title_bar_back).setOnClickListener(this);
		findViewById(R.id.title_bar_share).setOnClickListener(this);
	}

	private void initView() {
		mProgressbar = (ProgressBar) findViewById(R.id.ProgressBar);
		mProgressbar.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(2,this)));
		//防止内存泄露
		LinearLayout webViewLayout = (LinearLayout) findViewById(R.id.ll_webview_layout);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mWebView = new WebView(getApplicationContext());
		mWebView.setLayoutParams(params);
		webViewLayout.addView(mWebView);
		initWebView(mWebView);
	}

	private void initData() {
		Intent intent = getIntent();
		mURL = intent.getStringExtra("URL");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_bar_back:
				finish();
				break;
			case R.id.title_bar_share:
				String curUrl = "";
				if(mWebView != null ){
					curUrl = mWebView.getUrl();
				}
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, mTitle + ":" + curUrl);
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
				break;
			default:
				break;
		}
	}


	private void loadURL() {
//		mURL = "https://m.baidu.com/";
		LogUtil.e("zh","mURL" + mURL);
		if (mURL != null) {
			mWebView.loadUrl(mURL);
		}
	}

	private void initWebView(WebView webView) {
		//		mProgressbar = (ProgressBar) LayoutInflater.from(this).inflate(
//				R.layout.custom_webview_progressbar, null);
//		mProgressbar.setLayoutParams(new LayoutParams(
//				LayoutParams.MATCH_PARENT, PixelUtil.dp2px(3,this), 0, 0));
//		webView.addView(mProgressbar);
		//远程代码执行漏洞
		if(VERSION.SDK_INT > 11){
			webView.removeJavascriptInterface("accessibility");
			webView.removeJavascriptInterface("searchBoxJavaBridge_");
			webView.removeJavascriptInterface("accessibilityTraversal");
		}
		WebSettings webSettings = webView.getSettings();
		//密码明文存储漏洞 关闭密码保存提醒
		webSettings.setSavePassword(false);
		//域控制不严格漏洞 禁用 file 协议；
		webSettings.setAllowFileAccess(false);
		webSettings.setAllowFileAccessFromFileURLs(false);
		webSettings.setAllowUniversalAccessFromFileURLs(false);

		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setUseWideViewPort(true);
		String appCachePath = XPlanApplication.getInstance().getCacheDir()
				.getAbsolutePath();
		webSettings.setAppCachePath(appCachePath);
		webSettings.setAllowFileAccess(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);

		webView.setWebChromeClient(new WebChromeClient() {
			// 监听网页进度
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if(newProgress > currentProgress){
					currentProgress = newProgress;
					mProgressbar.setProgress(newProgress);
				}
				if (newProgress == 100) {
					Handler h = new Handler();
					h.postDelayed(new Runnable() {
						public void run() {
							mProgressbar.setVisibility(View.GONE);
							currentProgress = 0;
						}
					}, 500);
				} else {
					if (mProgressbar.getVisibility() == View.GONE) {
						mProgressbar.setVisibility(View.VISIBLE);
					}
				}
				super.onProgressChanged(view, newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				//获取到网页标题
				mTitle = title;
				title_name.setText(mTitle);
			}
		});

		// WebView默认使用系统默认浏览器打开网页的行为，覆盖这个方法使网页用自己的WebView打开
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				view.loadUrl(url);
				return true;
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) { 
        	mWebView.goBack();
            return true;
        }    
		return super.onKeyDown(keyCode, event);
	}

	//在 Activity销毁（ WebView ）的时候，先让 WebView 加载null内容，然后移除 WebView，再销毁 WebView，最后置空。
	@Override
	protected void onDestroy() {
		if (mWebView != null) {
			mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
			mWebView.clearHistory();
			// 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再destory()
			ViewParent parent = mWebView.getParent();
			if (parent != null) {
				((ViewGroup) parent).removeView(mWebView);
			}
			mWebView.stopLoading();
			// 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
			mWebView.getSettings().setJavaScriptEnabled(false);
			mWebView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
		super.onDestroy();
	}
}
