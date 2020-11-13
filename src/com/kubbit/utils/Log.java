package com.kubbit.utils;

import com.kubbit.horkonpon.BuildConfig;

public class Log
{
	private static final String LOG_TAG = "com.kubbit.horkonpon";

	public static void info(String message)
	{
		if (message == null)
			message = "NULL";

		android.util.Log.i(LOG_TAG, message);
	}

	public static void error(String message)
	{
		if (message == null)
			message = "NULL";

		android.util.Log.e(LOG_TAG, message);
	}

	public static void debug(String message)
	{
		if (!BuildConfig.DEBUG)
			return;

		if (message == null)
			message = "NULL";

		android.util.Log.d(LOG_TAG, message);
	}

	public static void trace()
	{
		if (!BuildConfig.DEBUG)
			return;

		java.lang.Thread.dumpStack();
	}
}
