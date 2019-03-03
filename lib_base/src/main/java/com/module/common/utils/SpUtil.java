package com.module.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

/**
 * SharedPreferences存储封装
 */
public class SpUtil {

	public static <T> void saveToLocal(Context context, String name, String key, T t) {
		if (context == null) {
			return;
		}

		SharedPreferences sp;
		if (name == null) {
			sp = getDefaultSharedPreferences(context);
		} else {
			sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		}

		if (sp == null) {
			return;
		}

		if (t instanceof Boolean) {
			sp.edit().putBoolean(key, (Boolean) t).commit();
		} else if (t instanceof String) {
			sp.edit().putString(key, (String) t).commit();
		} else if (t instanceof Integer) {
			sp.edit().putInt(key, (Integer) t).commit();
		} else if (t instanceof Float) {
			sp.edit().putFloat(key, (Float) t).commit();
		} else if (t instanceof Long) {
			sp.edit().putLong(key, (Long) t).commit();
		}
	}

	/**
	 * 从本地取回数据
	 * 
	 * @param context
	 * @param name
	 *            SharedPreferences名字 null为getDefaultSharedPreferences;
	 * @param key
	 * @param defaultValue
	 *            默认值
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public static <T> T getFromLocal(Context context, String name, String key, T defaultValue) {
		if (context == null) {
			return defaultValue;
		}

		SharedPreferences sp;
		if (name == null) {
			sp = getDefaultSharedPreferences(context);
		} else {
			sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		}

		if (sp == null) {
			return defaultValue;
		}

		Map<String, ?> map = sp.getAll();
		if (map == null) {
			return defaultValue;
		}

		if (map.get(key) == null) {
			return defaultValue;
		}

		return (T) map.get(key);
	}

	public static boolean clearSp(String name, Context context) {
		if (context == null) {
			return false;
		}

		SharedPreferences sp;
		if (name == null) {
			sp = getDefaultSharedPreferences(context);
		} else {
			sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		}

		if (sp == null) {
			return false;
		}

		return sp.edit().clear().commit();
	}

	public static boolean clearSpOthers(String name, String password, String cookie, Context context) {
		if (context == null) {
			return false;
		}

		SharedPreferences sp;
		if (name == null) {
			sp = getDefaultSharedPreferences(context);
		} else {
			sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		}

		if (sp == null) {
			return false;
		}

		SharedPreferences.Editor editor = sp.edit();
		editor.putString(password, "");
		editor.putString(cookie, "");

		return editor.commit();
	}

	private static SharedPreferences getDefaultSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}
