package com.kubbit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

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
		Locale.setDefault(config.locale);

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

	public static File getTempDir(Context context)
	{
		File tempDir = new File(context.getExternalCacheDir().getPath() + File.separator + "temp");
		if (!tempDir.exists())
			tempDir.mkdirs();
		
		return tempDir;
	}

	public static File getTemporaryFile(Context context, String ext) throws IOException
	{
		File file = null;

		if (ext != null && !ext.equals(""))
			ext = "." + ext;

		file = File.createTempFile("pic", ext, getTempDir(context));

		return file;
	}

	public static File renameFile(File file, String newPath)
	{
		if (file == null || !file.exists())
		{
			Log.error("Cannot rename non existing file.");
			return null;
		}

		if (file.getPath().equals(newPath))
			return file;

		File newFile = new File(newPath);
		File parentDir = newFile.getParentFile();
		if (!parentDir.exists())
			parentDir.mkdirs();

		if (!file.renameTo(newFile))
			Log.error("Could not rename file.");

		return newFile;
	}

	public static Uri getFileUri(Context context, File file)
	{
		return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
	}

	public static void emptyTempDir(Context context)
	{
		File tempDir = getTempDir(context);

		deleteSubtree(tempDir);
	}

	/*
	 * Deletes a directory and all its contents
	 *
	 * @param subtree File or directory to be deleted
	 */
	public static void deleteSubtree(File subtree)
	{
		if (subtree.isDirectory())
			for (File child: subtree.listFiles())
				deleteSubtree(child);

		subtree.delete();
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

	public static String toBase64(byte[] file)
	{
		return Base64.encodeToString(file, Base64.DEFAULT);
	}
	public static String toBase64(File file) throws Exception
	{
		byte[] b = readFileToByteArray(file);

		return toBase64(b);
	}
	private static byte[] readFileToByteArray(File file) throws Exception
	{
		FileInputStream fi = new FileInputStream(file);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int bytesRead;

		while ((bytesRead = fi.read(b)) != -1)
		{
			os.write(b, 0, bytesRead);
		}

		return os.toByteArray();
	}

	public static void writeToStream(String text, OutputStream stream, String charset) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
		try
		{
			writer.write(text);
			writer.flush();
		}
		finally
		{
			writer.close();
		}
	}

	public static String streamToString(InputStream stream, String charset) throws IOException, UnsupportedEncodingException
	{
		final int BUF_SIZE = 1024;

		InputStream input = new BufferedInputStream(stream);
		ByteArrayOutputStream array = new ByteArrayOutputStream();

		byte[] buffer = new byte[BUF_SIZE];
		int length;
		while ((length = input.read(buffer)) != -1)
			array.write(buffer, 0, length);

		return array.toString(charset);
	}

	public static void writeToFile(byte[] data, File file) throws Exception
	{
		// create parent directories if needed
		File directory = new File(file.getParent());
		if (!directory.exists())
			directory.mkdirs();

		FileOutputStream output = new FileOutputStream(file);
		output.write(data);
		output.close();
	}

	/*
	 * String.Format() with implied US locale
	 */
	public static String NLStringFormat(String format, Object... args)
	{
		return String.format(Locale.US, format, args);
	}

	/*
	 * SimpleDateFormat() with implied US locale
	 */
	public static SimpleDateFormat NLSimpleDateFormat(String format)
	{
		return new SimpleDateFormat(format, Locale.US);
	}

	public static Date strToDate(String date, String format)
	{
		SimpleDateFormat formatter = NLSimpleDateFormat(format);
		try
		{
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

			return formatter.parse(date);
		}
		catch (ParseException e)
		{
			Log.debug(e.getMessage());
			return null;
		}
	}
	public static String dateToStr(Date date, String format)
	{
		SimpleDateFormat formatter = NLSimpleDateFormat(format);

		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		return formatter.format(date);
	}
	public static String dateToStrLocalized(Context context, Date date)
	{
		return DateUtils.formatDateTime(context, date.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME);
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
	public static Integer getVersionCode(Context context)
	{
		try
		{
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

			return pInfo.versionCode;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			Log.debug(e.getMessage());
		}

		return null;
	}

	public static String getOSVersion()
	{
		return "Android " + android.os.Build.VERSION.RELEASE;
	}

	public static Boolean checkPermission(Context context, String permission)
	{
		PackageManager pm = context.getPackageManager();

		return pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED
		 && ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
	}

	public static void showMessage(Context context, String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(message)
		 .setCancelable(false)
		 .setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static Boolean equals(Object obj1, Object obj2)
	{
		return obj1 == null ? obj2 == null : obj1.equals(obj2);
	}

	public static String getRandomString(int byteLength)
	{
		SecureRandom rand = new SecureRandom();

		byte[] token = new byte[byteLength];
		rand.nextBytes(token);

		// hex encoding
		return new BigInteger(1, token).toString(16);
	}
}
