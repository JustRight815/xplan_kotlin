package com.module.common.log;

import com.module.common.BaseLib;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

/**
 * log日志工具类，控制log的显示 ;
 * 1.上线时isDebug必须设为false
 * 2.可以使用默认的TAG标签，也可以传入自己的标签
 * Created by rick on 2016/9/9.
 */
public class LogUtil {
	private static boolean isDebug = BaseLib.isDebug;// 在AppConfig中控制是否打印log true表示打印，false表示不打印。上线时必须设为false。
	private static final String TAG = "LogUtil"; // 默认log标签，也可以传入自己的标签

	public static void init() {
		FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
				.showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
				.methodCount(2)         // (Optional) How many method line to show. Default 2
//				.methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
//				.logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
				.tag("Xplan")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
				.build();
		Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy){
			@Override
			public boolean isLoggable(int priority, String tag) {
				return BaseLib.isDebug;
			}
		});
	}

	public static void v(String tag, String msg) {
		if (isDebug) {
			Logger.t(tag).v(msg);
		}
	}

	public static void d(String tag, String msg) {
		if (isDebug) {
			Logger.t(tag).d(msg);
		}
	}

	public static void i(String tag, String msg) {
		if (isDebug) {
			Logger.t(tag).i(msg);
		}
	}

	public static void w(String tag, String msg) {
		if (isDebug) {
			Logger.t(tag).w(msg);
		}
	}

	public static void e(String tag, String msg) {
		if (isDebug) {
			Logger.t(tag).e(msg);
//			Log.e(tag,msg);
		}
	}

	public static void v(String msg) {
		if (isDebug) {
			Logger.v(msg);
		}
	}

	public static void d(String msg) {
		if (isDebug) {
			Logger.d(msg);
		}
	}

	public static void i(String msg) {
		if (isDebug) {
			Logger.i(msg);
		}
	}

	public static void w(String msg) {
		if (isDebug) {
			Logger.w(msg);
		}
	}

	public static void e(String msg) {
		if (isDebug) {
			Logger.e(msg);
		}
	}

	/** 以级别为 e 的形式输出Throwable */
	public static void e(Throwable tr) {
		if (isDebug) {
			Logger.e(tr.toString());
		}
	}
}
