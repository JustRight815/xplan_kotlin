package com.zh.xplan.ui.menusetting;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.module.common.BaseLib;
import com.module.common.log.LogUtil;
import com.module.common.net.FileUtil;
import com.module.common.net.callback.IDownLoadCallback;
import com.module.common.net.rx.NetManager;
import com.module.common.pay.alipay.Alipay;
import com.module.common.pay.weixin.WXPay;
import com.module.common.sharesdk.ShareSDKManager;
import com.module.common.view.roundimageview.RoundImageView;
import com.module.common.view.snackbar.SnackbarUtils;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.zh.xplan.ui.aboutapp.AboutAppActivity;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.camera.RecordVideoSet;
import com.zh.xplan.ui.camera.record.CustomCameraActivity;
import com.zh.xplan.ui.iptoolsactivity.IpToolsActivity;
import com.zh.xplan.ui.logisticsdetail.LogisticsDetailActivity;
import com.zh.xplan.ui.robot.RobotKotlinActivity;
import com.zh.xplan.ui.utils.TitleUtil;
import com.zh.xplan.ui.view.addialog.AdDialog;
import com.zh.xplan.ui.weather.WeatherMoreActivity;
import com.zh.xplan.ui.weather.model.WeatherBeseModel;

import org.qcode.qskinloader.SkinManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import io.reactivex.observers.DisposableObserver;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * 第四个菜单 设置菜单
 * Created by zh
 */
@RuntimePermissions
public class SettingFragment extends BaseFragment implements OnClickListener,SettingFragmentView {
	private View mContentView;
	private LinearLayout mShare,mClearHistory, mAbout,mGoMarket,mRobot,mIpTools,mAdDialog,ll_camera,ll_kotlin;
	private RelativeLayout mChackVersion;
	private TextView mCurrentVersion;
	private TextView mCache; //缓存大小
	private ProgressDialog mProgressDialog;//清理缓存时的对话框
	private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果
	private RoundImageView mHeadPicture; //圆形头像
	private Bitmap bitmap;
	/* 头像名称 */
	private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
	private static String HEAD_PATH = XPlanApplication.getInstance().getExternalFilesDir(null).getAbsolutePath() + "/head.jpg";
	private File tempFile;

	private RelativeLayout rl_weather;
	private ImageView header_iv_weather;
	private TextView header_tv_temperature;
	private TextView tv_pm,tv_pm_str,tv_city,tv_weathr;
	private  WeatherBeseModel.WeatherBean resultBean;

	private SettingFragmentPresenter presenter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContentView = View.inflate(getActivity(),
				R.layout.fragment_setting, null);
		initTitle(getActivity(), mContentView);
		initView(mContentView);
		initDatas();
		return mContentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SkinManager.getInstance().applySkin(view, true);
	}

	/**
	 * 每次进入设置页面都更新下缓存的大小
	 */
	@Override
	public void onResume() {
		super.onResume();
		if(presenter != null){
			presenter.getCityWeather("","");
			presenter.getCacheSize();
		}
	}

	/**
	 * 每次进入设置页面都更新下缓存的大小
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		if(hidden == false){
			if(presenter != null){
				presenter.getCacheSize();
			}
		}
		super.onHiddenChanged(hidden);
	}

	/**
	 * 初始化标题栏
	 * @param activity
	 * @param view
	 */
	private void initTitle(Activity activity, View view) {
		// 1.设置左边的图片按钮显示，以及事件 2.设置中间TextView显示的文字 3.设置右边的图片按钮显示，并设置事件
		new TitleUtil(activity, view).setMiddleTitleText("设置");
	}
	
	/**
	 * 初始化界面
	 * @param mView
	 */
	private void initView(View mView) {
		rl_weather = (RelativeLayout) mView.findViewById(R.id.rl_weather);
		header_iv_weather = (ImageView) mView.findViewById(R.id.header_iv_weather);
		header_tv_temperature = (TextView) mView.findViewById(R.id.header_tv_temperature);
		tv_pm = (TextView) mView.findViewById(R.id.tv_pm);
		tv_pm_str = (TextView) mView.findViewById(R.id.tv_pm_str);
		tv_city = (TextView) mView.findViewById(R.id.tv_city);
		tv_weathr = (TextView) mView.findViewById(R.id.tv_weathr);
		rl_weather.setOnClickListener(this);

		mHeadPicture = (RoundImageView) mView.findViewById(R.id.iv_head_picture);
		TextView versionName = (TextView) mView
				.findViewById(R.id.tv_version_name);
		mShare = (LinearLayout) mView.findViewById(R.id.ll_share);
		mChackVersion = (RelativeLayout) mView
				.findViewById(R.id.ll_chack_version);
		mCurrentVersion = (TextView) mView.findViewById(R.id.tv_current_version);
		mClearHistory = (LinearLayout) mView.findViewById(R.id.ll_clear);
		mAbout = (LinearLayout) mView.findViewById(R.id.ll_about);
		mCache = (TextView) mView.findViewById(R.id.tv_cache);
		mGoMarket = (LinearLayout) mView.findViewById(R.id.ll_go_market);
		mRobot = (LinearLayout) mView.findViewById(R.id.ll_robot);
		mIpTools = (LinearLayout) mView.findViewById(R.id.ll_ip_tools);
		mAdDialog  = (LinearLayout) mView.findViewById(R.id.ll_ad_dialog);
		mView.findViewById(R.id.ly_wechat_login).setOnClickListener(this);
		mView.findViewById(R.id.ly_sina_weibo_login).setOnClickListener(this);
		mView.findViewById(R.id.ly_qq_login).setOnClickListener(this);
		mView.findViewById(R.id.ll_pay).setOnClickListener(this);
		mView.findViewById(R.id.ll_camera).setOnClickListener(this);
		mView.findViewById(R.id.ll_kotlin).setOnClickListener(this);
		mView.findViewById(R.id.ll_logistics).setOnClickListener(this);
		mHeadPicture.setOnClickListener(this);
		mShare.setOnClickListener(this);
		mChackVersion.setOnClickListener(this);
		mClearHistory.setOnClickListener(this);
		mAbout.setOnClickListener(this);
		mGoMarket.setOnClickListener(this);
		mRobot.setOnClickListener(this);
		mIpTools.setOnClickListener(this);
		mAdDialog.setOnClickListener(this);
		Bitmap bt = getHead(HEAD_PATH);
		if (bt != null) {
			Drawable drawable = new BitmapDrawable(bt);
			mHeadPicture.setImageDrawable(drawable);
		} else {
			mHeadPicture.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.head_default));
		}
		try {
			PackageManager pm = getActivity().getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(getActivity().getPackageName(), 0);
			mCurrentVersion.setText("当前版本:" + pi.versionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initDatas() {
		presenter = new SettingFragmentPresenter();
		presenter.attachView(this);
		presenter.getCityWeather("","北京");
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.rl_weather:// 打开或关闭侧滑菜单
				Intent weatherIntent = new Intent(getActivity(),WeatherMoreActivity.class);
				weatherIntent.putExtra("resultBean",resultBean);
				startActivity(weatherIntent);
				getActivity().overridePendingTransition(0,0);
				break;
			case R.id.iv_head_picture:
				//编辑头像
				changeHeadPicture();
				break;
			case R.id.ly_wechat_login:
				//微信登录
				ShareSDKManager.Login(Wechat.NAME);
				break;
			case R.id.ly_sina_weibo_login:
				//新浪微博登录
				ShareSDKManager.Login(SinaWeibo.NAME);
				break;
			case R.id.ly_qq_login:
				//QQ登录
				ShareSDKManager.Login(QQ.NAME);
				break;
			case R.id.ll_share:
				//分享软件
				share();
				break;
			case R.id.ll_chack_version:
				//检查新版本
				chackVersion();
				break;
			case R.id.ll_clear:
				//清除缓存
				clearHistory();
				break;
			case R.id.ll_about:
				//关于软件
				startActivity(new Intent(getActivity(), AboutAppActivity.class));
				break;
			case R.id.ll_go_market:
				//市场评分
				goToMarket();
				break;
			case R.id.ll_pay:
				//支付工具
				goToPay();
				break;
			case R.id.ll_robot:
				//小机器人
				startActivity(new Intent(getActivity(), RobotKotlinActivity.class));
				break;
			case R.id.ll_ip_tools:
				//IP工具
				startActivity(new Intent(getActivity(), IpToolsActivity.class));
				break;
			case R.id.ll_ad_dialog:
				//弹窗广告
				showAdDialog();
				break;
			case R.id.ll_camera:
				//拍照、录视频
				customRecord(true);
				break;
			case R.id.ll_kotlin:
				//kotlin测试
				startActivity(new Intent(getActivity(), KotlinDemoActivity.class));
				break;
			case R.id.ll_logistics:
				//kotlin测试
				startActivity(new Intent(getActivity(), LogisticsDetailActivity.class));
				break;
			default:
				break;
		}
	}

	/**
	 * 启用自定义相机录制视频
	 * @param isSmallVideo
	 */
	public void customRecord(boolean isSmallVideo) {
		RecordVideoSet recordVideoSet = new RecordVideoSet();
		recordVideoSet.setLimitRecordTime(30);
		recordVideoSet.setSmallVideo(isSmallVideo);
		Intent intent = new Intent(getActivity(), CustomCameraActivity.class);
		intent.putExtra("RECORD_VIDEO_CONFIG", recordVideoSet);
		startActivity(intent);
	}

	/**
	 * 去应用市场评分
	 */
	private void goToMarket() {
		if (!isMarketInstalled(getActivity())) {
			SnackbarUtils.ShortToast(mContentView,"您的手机没有安装应用市场");
		    return;
		}
		try {
			//Uri uri = Uri.parse("market://details?id="+getPackageName());
			Uri uri = Uri.parse("market://details?id=" + "com.tencent.mobileqq");  
			Intent intent = new Intent(Intent.ACTION_VIEW,uri);  
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
			    startActivity(intent);
			}
		} catch (Exception e) {
			// 该功能部分手机可能有问题，待验证。详情参见http://blog.csdn.net/wangfayinn/article/details/10351655
			// 也可以调到某个网页应用市场
			SnackbarUtils.ShortToast(mContentView,"您的手机没有安装应用市场");
		}
	}

	/**
	 * 本手机是否安装了应用市场
	 * @param context
	 * @return
	 */
	public boolean isMarketInstalled(Context context) {
		Intent intent = new Intent();
		intent.setData(Uri.parse("market://details?id=android.browser"));
		List list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return 0 != list.size();
	}

	/**
	 * 替换头像对话框
	 */
	public void changeHeadPicture() {
		View view = View.inflate(getActivity(),R.layout.dialog_change_head_picture, null);
		//要用 android.support.v7.app.AlertDialog 并且设置主题
		final AlertDialog dialog = new  AlertDialog.Builder(getActivity())
			.setTitle("更换头像")
			.setView(view)
			.create();
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = getActivity().getWindowManager().getDefaultDisplay().getWidth() * 5 / 6 ;
		//	params.height = 200 ;
		dialog.getWindow().setAttributes(params);
		view.findViewById(R.id.ll_from_camera).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 从相机截取头像
				SettingFragmentPermissionsDispatcher.cameraWithCheck(SettingFragment.this);
				dialog.dismiss();
			}
		});
		view.findViewById(R.id.ll_from_gallery).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 从图库截取头像
				gallery();
				dialog.dismiss();
			}
		});
	}

	/**
	 * 手动检测更新版本
	 */
	public void chackVersion() {
//		Beta.checkUpgrade();//检查版本号
		//要用 android.support.v7.app.AlertDialog 并且设置主题
		AlertDialog dialog = new  AlertDialog.Builder(getActivity())
			.setTitle("发现新版本")
			.setMessage("1.测试下载 \n2.测试下载带进度 \n3.测试下载带进度")
			.setNegativeButton("取消", null)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
//					getApk();
					SettingFragmentPermissionsDispatcher.getApkWithCheck(SettingFragment.this);
				}
			})
			.create();
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = getActivity().getWindowManager().getDefaultDisplay().getWidth() * 5 / 6 ;
		//	params.height = 200 ;
		dialog.getWindow().setAttributes(params);
	}

	@NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
	void getApk(){
//		View view = LayoutInflater.from(getActivity()).inflate(R.layout.update_app_progress,null);
		View view = View.inflate(getActivity(),
				R.layout.update_app_progress, null);
		final TextView tv_Progress = (TextView) view.findViewById(R.id.tv_progress);
		final TextView title = (TextView) view.findViewById(R.id.title);
		final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
		progressBar.setMax(100);
		progressBar.setProgress(0);
		tv_Progress.setText("0%");
		title.setText("正在下载");
		final AlertDialog dialog = new  AlertDialog.Builder(getActivity())
				.setView(view)
				.setNegativeButton("取消",  null)
				.setPositiveButton("重试", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				})
				.create();
		dialog.show();
		dialog.setCanceledOnTouchOutside(false);
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = getActivity().getWindowManager().getDefaultDisplay().getWidth() * 5 / 6 ;
		//	params.height = 200 ;
		dialog.getWindow().setAttributes(params);
		final Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		negativeButton.setVisibility(View.GONE);
		positiveButton.setVisibility(View.GONE);
		LogUtil.e("zh","getApk , ");
//		String url = "https://raw.githubusercontent.com/JustRight815/XPlan/master/apk/app-debug.apk";
//		String url = "http://cn.bing.com/az/hprichbg/rb/TartumaaEstonia_ZH-CN13968964399_720x1280.jpg";
		String url = "http://app.2345.cn/appsoft/80202.apk";
	    IDownLoadCallback  callback = new IDownLoadCallback() {

			@Override
			public void onStart(long totalBytes) {
				super.onStart(totalBytes);
				LogUtil.e("zh","onStart , " + totalBytes);
			}

			@Override
			public void onCancel() {
				super.onCancel();
				LogUtil.e("zh","onCancel , " );
				title.setText("已取消下载");
			}

			@Override
			public void onFinish(File downloadFile) {
				LogUtil.e("zh","onFinish , " + downloadFile.getPath());
				title.setText("下载成功");
				progressBar.setProgress(100);
				tv_Progress.setText(100 + "%");
				autoInstallApk(downloadFile);
				dialog.dismiss();
			}

			@Override
			public void onProgress(long currentBytes, long totalBytes) {
				LogUtil.e("zh", "doDownload onProgress:" + currentBytes + "/" + totalBytes);
				int currentProgress = (int) (((float)currentBytes)/totalBytes * 100);
				LogUtil.e("zh", "doDownload currentProgress:" + currentProgress);
				if(currentProgress > 100){
					currentProgress = 100;
				}
				title.setText("正在下载");
				progressBar.setProgress(currentProgress);
				tv_Progress.setText(currentProgress + "%");
			}

			@Override
			public void onFailure(String error_msg) {
				LogUtil.e("zh","onFailure , " + error_msg);
				title.setText("下载失败！");
				negativeButton.setVisibility(View.VISIBLE);
//						positiveButton.setVisibility(View.VISIBLE);
			}
		};
		final DisposableObserver disposableObserver = NetManager.download(url,
				null,
				Environment.getExternalStorageDirectory().getPath() + "/xplan/",
				null,
				getDefaultDownLoadFileName(url),
				callback
				);
		negativeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtil.e("zh","setOnClickListener setOnClickListener , ");
				if(dialog != null &&  dialog.isShowing()){
					dialog.dismiss();
				}
				if(disposableObserver != null){
					LogUtil.e("zh","disposableObserver dispose , ");
					disposableObserver.dispose();
				}
			}
		});

//		//下载新的图片
//		HttpManager.download()
//				.url(url)
//				.dir(Environment.getExternalStorageDirectory().getPath() + "/xplan/")
//				.name(getDefaultDownLoadFileName(url))
//				.enqueue(callback);
	}


	private void autoInstallApk(File file) {
		if(file == null){
			return;
		}
		if (FileUtil.getExtension(file.getPath()).equals("apk")) {
			final Intent install = new Intent();
			install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			install.setAction(Intent.ACTION_VIEW);
			install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			BaseLib.getContext().startActivity(install);
		}
	}

	/**
	 * 从url中，获得默认文件名
	 */
	public static String getDefaultDownLoadFileName(String url) {
		if (url == null || url.length() == 0) return "";
		int nameStart = url.lastIndexOf('/')+1;
		return url.substring(nameStart);
	}

	@OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
	void onNeverAskAgain() {
		new AlertDialog.Builder(getActivity())
				.setPositiveButton("好的", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//打开系统设置权限
						Intent intent = getAppDetailSettingIntent(getActivity());
						startActivity(intent);
						dialog.cancel();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setCancelable(false)
				.setMessage("您已经禁止了存储权限,是否去开启权限")
				.show();
	}

	@OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
	void onPermissionDenied(){
		SnackbarUtils.ShortToast(mContentView,"拒绝存储权限将无法升级");
	}


	/**
	 * share SDk 分享
	 */
	public void share() {
		OnekeyShare oks = new OnekeyShare();
		//关闭sso授权
//		oks.disableSSOWhenAuthorize();

		// 分享时Notification的图标和文字  2.5.9以后的版本不     调用此方法
		//oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
//		oks.setTitle("测试分享");
//		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
//		oks.setTitleUrl("http://sharesdk.cn");
//		// text是分享文本，所有平台都需要这个字段
//		oks.setText("我是分享文本");
//		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//		oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
//		// url仅在微信（包括好友和朋友圈）中使用
//		oks.setUrl("http://sharesdk.cn");
//		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
//		oks.setComment("我是测试评论文本");
//		// site是分享此内容的网站名称，仅在QQ空间使用
//		oks.setSite(getString(R.string.app_name));
//		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
//		oks.setSiteUrl("http://sharesdk.cn");

		// 启动分享GUI
//		oks.show(getActivity());
		ShareSDKManager.show(getActivity(),oks);
	}

	/**
	 * 清除历史记录
	 */
	public void clearHistory() {
		if(presenter != null){
			mProgressDialog = ProgressDialog.show(getActivity(), null, "清除记录中...",
					true, false);
			presenter.clearCache();
		}
	}

	/*
	 * 从相册获取
	 */
	public void gallery() {
		// 激活系统图库，选择一张图片
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
	}

	/*
	 * 从相机获取
	 */
	@NeedsPermission(Manifest.permission.CAMERA)
	public void camera() {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		// 判断存储卡是否可以用，可用进行存储
		if (hasSdcard()) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), PHOTO_FILE_NAME)));
		}
		startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
	}

	@OnPermissionDenied(Manifest.permission.CAMERA)
	void showRecordDenied(){
		SnackbarUtils.ShortToast(mContentView,"拒绝相机权限将无法从相机获取头像");
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		SettingFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	@OnNeverAskAgain(Manifest.permission.CAMERA)
	void onRecordNeverAskAgain() {
		new AlertDialog.Builder(getActivity())
				.setPositiveButton("好的", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//打开系统设置权限
						Intent intent = getAppDetailSettingIntent(getActivity());
						startActivity(intent);
						dialog.cancel();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setCancelable(false)
				.setMessage("您已经禁止了相机权限,是否去开启权限")
				.show();
	}

	/**
	 * 获取应用详情页面intent
	 *
	 * @return
	 */
	public Intent getAppDetailSettingIntent(Context context) {
		Intent localIntent = new Intent();
		localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (Build.VERSION.SDK_INT >= 9) {
			localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
		} else if (Build.VERSION.SDK_INT <= 8) {
			localIntent.setAction(Intent.ACTION_VIEW);
			localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
		}
		return localIntent;
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_REQUEST_GALLERY) {
			if (data != null) {
				// 得到图片的全路径
				Uri uri = data.getData();
				crop(uri);
			}
		} else if (requestCode == PHOTO_REQUEST_CAMERA) {
			if(resultCode == Activity.RESULT_OK){
				if (hasSdcard()) {
					tempFile = new File(Environment.getExternalStorageDirectory(),
							PHOTO_FILE_NAME);
					crop(Uri.fromFile(tempFile));
				} else {
					SnackbarUtils.ShortToast(mContentView,"未找到存储卡，无法存储照片！");
				}
			}
		} else if (requestCode == PHOTO_REQUEST_CUT) {
			try {
				bitmap = data.getParcelableExtra("data");
				if (bitmap != null) {
					/**
					 * 上传服务器代码
					 */
					setPicToView(bitmap);// 保存在SD卡中
					this.mHeadPicture.setImageBitmap(bitmap);// 用ImageView显示出来
//					if (bitmap != null && !bitmap.isRecycled()) {
//						bitmap.recycle();
//					}
				}
				boolean delete = tempFile.delete();
				System.out.println("delete = " + delete);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setPicToView(Bitmap mBitmap) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			return;
		}
		FileOutputStream b = null;
		String fileName = XPlanApplication.getInstance().getExternalFilesDir(null).getAbsolutePath() + "/head.jpg";// 图片名字
		try {
			b = new FileOutputStream(fileName);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭流
				b.flush();
				b.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 从本地的文件中以保存的图片中 获取图片的方法
	private Bitmap getHead(String pathString) {
		Bitmap bitmap = null;
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}


	/**
	 * 剪切图片
	 * 
	 * @function:
	 * @author:Jerry
	 * @date:2013-12-30
	 * @param uri
	 */
	private void crop(Uri uri) {
		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
		// 图片格式
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);// true:不返回uri，false：返回uri
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	private boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
	}

	/**
	 * 支付工具
	 */
	private void goToPay() {
		View view = View.inflate(getActivity(),R.layout.dialog_change_head_picture, null);
		((TextView) view.findViewById(R.id.text1)).setText("微信支付");
		((TextView) view.findViewById(R.id.text2)).setText("支付宝支付");
		//要用 android.support.v7.app.AlertDialog 并且设置主题
		final AlertDialog dialog = new  AlertDialog.Builder(getActivity())
				.setTitle("支付方式")
				.setView(view)
				.create();
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		params.width = getActivity().getWindowManager().getDefaultDisplay().getWidth() * 5 / 6 ;
		//	params.height = 200 ;
		dialog.getWindow().setAttributes(params);
		view.findViewById(R.id.ll_from_camera).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SnackbarUtils.ShortToast(mChackVersion,"应用需要审核才能使用");
//				doWXPay("");
				dialog.dismiss();
			}
		});
		view.findViewById(R.id.ll_from_gallery).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SnackbarUtils.ShortToast(mChackVersion,"应用需要审核才能使用");
//				doAlipay("");
				dialog.dismiss();
			}
		});
	}

	/**
	 * 微信支付
	 * 支付流程参见https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_3
	 * 商户系统和微信支付系统主要交互说明：
	 步骤1：用户在商户APP中选择商品，提交订单，选择微信支付。
	 步骤2：商户后台收到用户支付单，调用微信支付统一下单接口。参见【统一下单API】。
	 步骤3：统一下单接口返回正常的prepay_id，再按签名规范重新生成签名后，将数据传输给APP。
	 参与签名的字段名为appid，partnerid，prepayid，noncestr，timestamp，package。注意：package的值格式为Sign=WXPay
	 步骤4：商户APP调起微信支付。api参见本章节【app端开发步骤说明】
	 步骤5：商户后台接收支付通知。api参见【支付结果通知API】
	 步骤6：商户后台查询支付结果。，api参见【查询订单API】


	 微信客户端支付完成后会返回给你客户端一个支付结果。
	 同时微信的服务端会主动调用你服务端的接口发送支付结果通知。
	 逻辑处理应该是你服务端接收到支付结果后处理，比如修改订单状态，发货等等。
	 不能依赖客户端的返回结果认为支付成功，是不可靠的，微信的文档也是这么建议的。
	 你前后端的时序可以这样。客户端支付完成收到支付结果后，在一定时间内不断轮询查询服务端订单的状态有没有修改。
	 （比如5s内每s查询一次）这样以服务端的交易状态为准（参见微信流程图）

	 * @param pay_param 支付服务生成的支付参数
	 */
	private void doWXPay(String pay_param) {
		String wx_appid = "wxXXXXXXX";     //替换为自己的appid
		WXPay.init(XPlanApplication.getInstance(), wx_appid);      //要在支付前调用
		WXPay.getInstance().doPay(pay_param, new WXPay.WXPayResultCallBack() {
			@Override
			public void onSuccess() {
//				Toast.makeText(XPlanApplication.getInstance(), "支付成功", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
//				Toast.makeText(XPlanApplication.getInstance(), "支付取消", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(int error_code) {
				switch (error_code) {
					case WXPay.NO_OR_LOW_WX:
//						Toast.makeText(XPlanApplication.getInstance(), "未安装微信或微信版本过低", Toast.LENGTH_SHORT).show();
						break;
					case WXPay.ERROR_PAY_PARAM:
//						Toast.makeText(XPlanApplication.getInstance(), "参数错误", Toast.LENGTH_SHORT).show();
						break;
					case WXPay.ERROR_PAY:
//						Toast.makeText(XPlanApplication.getInstance(), "支付失败", Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
				}
			}
		});
	}

	/**
	 * 支付宝支付
	 * @param pay_param 支付服务生成的支付参数
	 */
	private void doAlipay(String pay_param) {
		new Alipay(getActivity(), pay_param, new Alipay.AlipayResultCallBack() {
			@Override
			public void onSuccess() {
//				Toast.makeText(XPlanApplication.getInstance(), "支付成功", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onDealing() {
//				Toast.makeText(XPlanApplication.getInstance(), "支付处理中...", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
//				Toast.makeText(XPlanApplication.getInstance(), "支付取消", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(int error_code) {
				switch (error_code) {
					case Alipay.ERROR_RESULT:
//						Toast.makeText(XPlanApplication.getInstance(), "支付失败:支付结果解析错误", Toast.LENGTH_SHORT).show();
						break;
					case Alipay.ERROR_NETWORK:
//						Toast.makeText(XPlanApplication.getInstance(), "支付失败:网络连接错误", Toast.LENGTH_SHORT).show();
						break;
					case Alipay.ERROR_PAY:
//						Toast.makeText(XPlanApplication.getInstance(), "支付错误:支付码支付失败", Toast.LENGTH_SHORT).show();
						break;
					default:
//						Toast.makeText(XPlanApplication.getInstance(), "支付错误", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}).doPay();
	}

	@Override
	public void isShowLoading(boolean isShow, String message) {
	}

	@Override
	public void updateCityWeather(WeatherBeseModel.WeatherBean weatherBean, String temperature, String pm, int resid, String airCondition, String cityName, String weather, int weatherRes) {
		resultBean =weatherBean;
		if (resultBean != null) {
			header_tv_temperature.setText(temperature);
			tv_pm.setText(pm);
			tv_pm.setBackgroundResource(resid);
			tv_pm_str.setText(airCondition);
			tv_city.setText(cityName);
			tv_weathr.setText(weather);
			header_iv_weather.setImageDrawable(getResources().getDrawable(weatherRes));
		}
	}

	@Override
	public void updateCacheSize(String cacheSize) {
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
		}
		mCache.setText(cacheSize);
	}

	/**
	 * 显示弹框公告
	 */
	private void showAdDialog() {
//		String url = "http://cn.bing.com/az/hprichbg/rb/TartumaaEstonia_ZH-CN13968964399_720x1280.jpg";
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.ad_dialog_contentview, null);
		AdDialog diaLog = new AdDialog(getActivity(),view, AdDialog.ViewType.IMG);
		diaLog //设置外边距
				.setContentView_Margin_Top(0)
				.setContentView_Margin_Bottom(0)
				.setContentView_Margin_Left(0)
				.setContentView_Margin_Right(0)
				.setOverScreen(true) //设置是否全屏,覆盖状态栏
				.setCloseButtonImg(R.drawable.ad_dialog_closebutton) //设置关闭按钮图片
				.setCloseButtonListener(new View.OnClickListener() { //设置关闭按钮监听事件
					@Override
					public void onClick(View view) {
					}
				})
				.show();
	}


	@Override
	public void onDestroy() {
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		if(presenter != null){
			presenter.onDestory();
		}
		super.onDestroy();
	}
}