package com.kubbit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import com.kubbit.horkonpon.R;

public class Utils
{
	public static void setLanguage(Context context, String locale)
	{
		setLanguage(context, locale, false);
	}
	public static void setLanguage(Context context, String locale, Boolean force)
	{
		if (locale == null || locale.equals(""))
		{
			if (!force)
				return;

			locale = Locale.getDefault().getLanguage();
		}

		Resources resources = context.getResources();

		DisplayMetrics metrics = resources.getDisplayMetrics();
		Configuration config = resources.getConfiguration();
		config.locale = new Locale(locale);

		resources.updateConfiguration(config, metrics);
	}
	public static String getLanguage(Context context)
	{
		return context.getResources().getConfiguration().locale.getLanguage();
	}

	public static String getRevision(Context context)
	{
		Resources resources = context.getResources();

		try
		{
			InputStream rawResource = resources.openRawResource(R.raw.version);
			Properties properties = new Properties();
			properties.load(rawResource);

			return properties.getProperty("revision");
		}
		catch (IOException e)
		{
			Log.debug(e.getMessage());
		}

		return null;
	}

	public static File getTemporaryFile(Context context, String ext)
	{
		File file = null;

		if (ext != null && !ext.equals(""))
			ext = "." + ext;

		try
		{
			if (android.os.Build.VERSION.SDK_INT < 8)
				file = File.createTempFile("pic", ext);
			else
				file = File.createTempFile("pic", ext, context.getExternalCacheDir());
		}
		catch (IOException e)
		{
			Log.debug(e.getMessage());
		}

		return file;
	}

	/*
	 * Gets the last image's path from Image Gallery
	 */
	public static String getLastImage(Context context)
	{
		final String[] imageColumns =
		{
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA
		};

		String path = "";
		final String imageOrderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";

		Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		 imageColumns, null, null, imageOrderBy);
		try
		{
			if (imageCursor.moveToFirst())
				path = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
		}
		finally
		{
			imageCursor.close();
		}

		return path;
	}

	public static String getVersion(Context context)
	{
		try
		{
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

			return pInfo.versionName + "-" + getRevision(context);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			Log.debug(e.getMessage());
		}

		return "";
	}

	public static String getOSVersion()
	{
		return "Android " + android.os.Build.VERSION.RELEASE;
	}

	public static Boolean HasPermition(Context context, String permission)
	{
		PackageManager pm = context.getPackageManager();

		return pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
	}

	public static void showMessage(Context context, String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(message)
		 .setCancelable(false)
		 .setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
