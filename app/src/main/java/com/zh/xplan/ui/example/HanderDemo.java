package com.zh.xplan.ui.example;

/**
 *  Handler标准写法
 * Created by zh on 2018/6/30.
 */

public class HanderDemo {
    //	private final IndexActivity.MyHandler mHandler = new IndexActivity.MyHandler(this);
//	private static class MyHandler extends Handler {
//		private final WeakReference<IndexActivity> mActivity;
//
//		public MyHandler(IndexActivity activity) {
//			mActivity = new WeakReference<IndexActivity>(activity);
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			final IndexActivity activity = mActivity.get();
//			if (activity != null) {
//				switch (msg.what) {
//					case 0:
//						// 跳转到主界面
//						File splashImg = new File(FileUtils.getSplashDir(), SPLASH_FILE_NAME);
//						if (splashImg.exists()) {
//							LogUtil.e("zh","splashImg.exists() ");
//							Bitmap bitmap = BitmapFactory.decodeFile(splashImg.getPath());
//							if(bitmap != null){
//								LogUtil.e("zh","FileUtils.bitmap ");
//								activity.startActivity(new Intent(activity,
//										AdActivity.class));
//								activity.finish();
//								return;
//							}
//						}else{
//							LogUtil.e("zh","IndexActivity splashImg not exists() ");
//						}
//						activity.startActivity(new Intent(activity,
//								MainActivity.class));
//						activity.finish();
//						break;
//					default:
//						break;
//				}
//			}
//		}
//	}
}
