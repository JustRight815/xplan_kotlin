package com.module.common.utils;

import android.util.Log;

import com.module.common.BaseLib;

/**
 * log日志工具类，控制log的显示 ;
 * 1.上线时isDebug必须设为false
 * 2.可以使用默认的TAG标签，也可以传入自己的标签
 * Created by rick on 2016/9/9.
 */
public class LogUtilBackUP {
	public static boolean isDebug = true;// 控制是否需要打印bug，true表示打印，false表示不打印。上线时必须设为false。
	private static final String TAG = "LogUtil"; // 默认log标签，也可以传入自己的标签
	static {
        // 在AppConfig中控制是否打印log
        isDebug = BaseLib.isDebug;
	}

	public static void v(String tag, String msg) {
		if (isDebug) {
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (isDebug) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (isDebug) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (isDebug) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (isDebug) {
			Log.e(tag, msg);
		}
	}

	public static void v(String msg) {
		if (isDebug) {
			Log.v(TAG, msg);
		}
	}

	public static void d(String msg) {
		if (isDebug) {
			Log.d(TAG, msg);
		}
	}

	public static void i(String msg) {
		if (isDebug) {
			Log.i(TAG, msg);
		}
	}

	public static void w(String msg) {
		if (isDebug) {
			Log.w(TAG, msg);
		}
	}

	public static void e(String msg) {
		if (isDebug) {
			Log.e(TAG, msg);
		}
	}
	
	/** 以级别为 e 的形式输出Throwable */
	public static void e(Throwable tr) {
		if (isDebug) {
			Log.e(TAG,"", tr);
		}
	}
}
