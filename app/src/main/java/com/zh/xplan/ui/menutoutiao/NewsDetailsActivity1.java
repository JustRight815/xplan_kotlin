package com.zh.xplan.ui.menutoutiao;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.module.common.log.LogUtil;
import com.module.common.net.rx.NetManager;
import com.module.common.view.snackbar.SnackbarUtils;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.menutoutiao.model.NewsDetail;
import com.zh.xplan.ui.menutoutiao.model.response.ResultResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * X5 webview 演示实例
 * X5 webview 控件默认有个细长的滚动条，但是当快速滑动的时候，滚动条就变成一个小方块。
 * ScrollView 嵌套X5 webview  小方块不显示，滚动条显示为ScrollView样式
 * @author zh
 */
public class NewsDetailsActivity1 extends BaseActivity implements View.OnClickListener {
	public static final String CHANNEL_CODE = "channelCode";
	public static final String POSITION = "position";
	public static final String DETAIL_URL = "detailUrl";
	public static final String GROUP_ID = "groupId";
	public static final String ITEM_ID = "itemId";

	private View mContentView;

	private WebView mWebView;
	private String mURL = null;
	private String mTitle = ""; //页面标题
	private TextView title_name;
	private LinearLayout title_bar_back_layout;

	private String mDetalUrl;
	protected String mChannelCode;
	protected int mPosition;
    TextView mTvTitle;//新闻标题
	private View mLoadingView;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		mSwipeBackHelper.setIsOnlyTrackingLeftEdge(false);
		mContentView = View.inflate(this,R.layout.activity_news_details, null);
		setContentView(mContentView);
		setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark),0);
		initView();
		initData();
		loadURL();
	}

	private void initView() {
		title_name = (TextView) findViewById(R.id.title_name);
		findViewById(R.id.title_bar_back).setOnClickListener(this);
		findViewById(R.id.title_bar_share).setOnClickListener(this);
		findViewById(R.id.title_bar_colse).setOnClickListener(this);
		title_bar_back_layout = (LinearLayout) findViewById(R.id.title_bar_back_layout);
		mTvTitle = (TextView) findViewById(R.id.tvTitle);
		mLoadingView = findViewById(R.id.loading_view);

		//防止内存泄露
		LinearLayout webViewLayout = (LinearLayout) findViewById(R.id.ll_webview_layout);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mWebView = new WebView(getApplicationContext());
		mWebView.setLayoutParams(params);
		webViewLayout.addView(mWebView);
		initWebView(mWebView);
	}

	private void initWebView(WebView webView) {
		WebSettings webSettings = webView.getSettings();
		//下面方法去掉滚动条
//        webView.setHorizontalScrollBarEnabled(false)
//        webView.setVerticalScrollBarEnabled(false)
//        val ix5 = webView.getX5WebViewExtension()
//        if (null != ix5) {
//            ix5!!.setScrollBarFadingEnabled(false)
//        }
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setUseWideViewPort(true);
		String appCachePath = XPlanApplication.getInstance().getCacheDir().getAbsolutePath();
		webSettings.setAppCachePath(appCachePath);
		webSettings.setAppCacheEnabled(true);
		webSettings.setLoadWithOverviewMode(true);
		//密码明文存储漏洞 关闭密码保存提醒
		webSettings.setSavePassword(false);
		//域控制不严格漏洞 禁用 file 协议；
		webSettings.setAllowFileAccess(false);
		webSettings.setAllowFileAccessFromFileURLs(false);
		webSettings.setAllowUniversalAccessFromFileURLs(false);
	}

	private void pageNavigator(int tag) {
		title_bar_back_layout.setVisibility(tag);
	}

	private void initData() {
		Intent intent = getIntent();
		mURL = intent.getStringExtra("URL");
		mChannelCode = intent.getStringExtra(CHANNEL_CODE);
		mPosition = intent.getIntExtra(POSITION, 0);
		mDetalUrl = intent.getStringExtra(DETAIL_URL);
		getNewsDetail(mDetalUrl);
	}

	public void getNewsDetail(String url) {
		LogUtil.e("zh","getNewsDetail :url: " + url);
		NetManager.get()
				.url(url)
				.build()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(new DisposableObserver<String>() {

					@Override
					public void onNext(String response) {
						LogUtil.e("zh","getNewsDetail :onSuccess response: " + response);
						if (response == null ) {
							return;
						}
						ResultResponse<NewsDetail> response1 = new Gson().fromJson(response, new TypeToken<ResultResponse<NewsDetail>>(){}.getType());
						NewsDetail newsDetail = response1.data;
						LogUtil.e("zh","getNewsDetail :onSuccess newsDetail: " + newsDetail);
						mTvTitle.setText(newsDetail.title);
						LogUtil.e("zh","getNewsDetail :onSuccess newsDetail.title: " + newsDetail.title);
						if (newsDetail.media_user == null) {
							//如果没有用户信息
							LogUtil.e("zh","getNewsDetail :如果没有用户信息 ");
						} else {
							if (!TextUtils.isEmpty(newsDetail.media_user.avatar_url)){
//								GlideUtils.loadRound(mContext, detail.media_user.avatar_url, mIvAvatar);
							}
						}

						if (TextUtils.isEmpty(newsDetail.content))
							mWebView.setVisibility(View.GONE);
						LogUtil.e("zh","getNewsDetail :newsDetail.content " + newsDetail.content);

						mWebView.getSettings().setJavaScriptEnabled(true);//设置JS可用
						mWebView.addJavascriptInterface(new ShowPicRelation(NewsDetailsActivity1.this),"chaychan");//绑定JS和Java的联系类，以及使用到的昵称

						String htmlPart1 = "<!DOCTYPE HTML html>\n" +
								"<head><meta charset=\"utf-8\"/>\n" +
								"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=no\"/>\n" +
								"</head>\n" +
								"<body>\n" +
								"<style> \n" +
								"img{width:100%!important;height:auto!important}\n" +
								" </style>";
						String htmlPart2 = "</body></html>";
						String html = htmlPart1 + newsDetail.content + htmlPart2;
						mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
						mWebView.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient(){
							@Override
							public void onPageFinished(WebView view, String url) {
								addJs(view);//添加多JS代码，为图片绑定点击函数
								mLoadingView.setVisibility(View.GONE);
							}
						});
					}

					@Override
					public void onError(Throwable e) {
						LogUtil.e("zh",":::onFailure response: " + e.toString());
						SnackbarUtils.ShortToast(mContentView,"数据请求失败");
					}

					@Override
					public void onComplete() {

					}
				});
	}

	/**添加JS代码，获取所有图片的链接以及为图片设置点击事件*/
	private void addJs(WebView wv) {
		wv.loadUrl("javascript:(function  pic(){"+
				"var imgList = \"\";"+
				"var imgs = document.getElementsByTagName(\"img\");"+
				"for(var i=0;i<imgs.length;i++){"+
				"var img = imgs[i];"+
				"imgList = imgList + img.src +\";\";"+
				"img.onclick = function(){"+
				"window.chaychan.openImg(this.src);"+
				"}"+
				"}"+
				"window.chaychan.getImgArray(imgList);"+
				"})()");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_bar_back:
					finish();
				break;
			case R.id.title_bar_colse:
				finish();
				break;
			case R.id.title_bar_share:
				String curUrl = mDetalUrl;
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

	}

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
