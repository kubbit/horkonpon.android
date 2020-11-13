package com.kubbit.horkonpon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.URLUtil;

import org.json.JSONObject;

import java.util.Objects;

import com.kubbit.utils.Utils;
import com.kubbit.utils.Log;

public class Settings
{
	public static final String KEY_PREF_APP_VERSION = "pref_app_version";
	public static final String KEY_PREF_USER_ID = "pref_user_id";
	public static final String KEY_PREF_USER_KEY = "pref_user_key";
	public static final String KEY_PREF_FULLNAME = "pref_fullname";
	public static final String KEY_PREF_PHONE = "pref_phone";
	public static final String KEY_PREF_MAIL = "pref_mail";
	public static final String KEY_PREF_ANONYMOUS = "pref_anomymous";
	public static final String KEY_PREF_LANGUAGE = "pref_language";
	public static final String KEY_PREF_NOTIFY = "pref_notify";
	public static final String KEY_PREF_ADVANCED = "pref_advanced";
	public static final String KEY_PREF_CUSTOM_API_URL = "pref_api_url";
	public static final String KEY_PREF_CUSTOM_API_USERNAME = "pref_api_auth_user";
	public static final String KEY_PREF_CUSTOM_API_PASSWORD = "pref_api_auth_pwd";
	public static final String KEY_PREF_MODIFIED = "pref_modified";

	final static int USER_KEY_BYTES = 16;

	private static SharedPreferences settings = null;
	private static SharedPreferences.OnSharedPreferenceChangeListener listener;

	public static String getLanguage()
	{
		return settings.getString(KEY_PREF_LANGUAGE, null);
	}
	public static String getFullName()
	{
		return settings.getString(KEY_PREF_FULLNAME, null);
	}
	public static void setFullName(String value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putString(KEY_PREF_FULLNAME, value);

		editor.apply();
	}
	public static String getPhone()
	{
		return settings.getString(KEY_PREF_PHONE, null);
	}
	public static void setPhone(String value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putString(KEY_PREF_PHONE, value);

		editor.apply();
	}
	public static String getMail()
	{
		return settings.getString(KEY_PREF_MAIL, null);
	}
	public static void setMail(String value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putString(KEY_PREF_MAIL, value);

		editor.apply();
	}
	public static Boolean getAnonymous()
	{
//		return settings.getBoolean(KEY_PREF_ANONYMOUS, true);
		return true;
	}
	public static void setAnonymous(Boolean value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putBoolean(KEY_PREF_ANONYMOUS, value);

		editor.apply();
	}
	public static Boolean getNotify()
	{
		return settings.getBoolean(KEY_PREF_NOTIFY, false);
	}
	public static void setNotify(Boolean value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putBoolean(KEY_PREF_NOTIFY, value);

		editor.apply();
	}
	public static Boolean getAdvanced()
	{
		return settings.getBoolean(KEY_PREF_ADVANCED, false);
	}
	public static String getApiUrl()
	{
		String url = Constants.API_URL;

		if (settings.getBoolean(KEY_PREF_ADVANCED, false))
			url = settings.getString(KEY_PREF_CUSTOM_API_URL, Constants.API_URL);

		if (!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url))
			url = Constants.API_URL;

		return url;
	}
	public static Boolean isCustomApi()
	{
		return !Constants.API_URL.equals(getApiUrl());
	}
	public static String getCustomUserId()
	{
		return settings.getString(KEY_PREF_CUSTOM_API_USERNAME, null);
	}
	public static String getUserId()
	{
		return settings.getString(KEY_PREF_USER_ID, null);
	}
	public static void setUserId(String value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putString(KEY_PREF_USER_ID, value);

		editor.apply();
	}
	public static Boolean isUserRegistered()
	{
		return  getUserId() != null;
	}
	public static String getCustomUserPass()
	{
		return settings.getString(KEY_PREF_CUSTOM_API_PASSWORD, null);
	}
	public static String getUserKey()
	{
		if (settings.getString(KEY_PREF_USER_KEY, null) == null)
			Settings.setUserKey(Utils.getRandomString(USER_KEY_BYTES));

		return settings.getString(KEY_PREF_USER_KEY, null);
	}
	protected static void setUserKey(String value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putString(KEY_PREF_USER_KEY, value);

		editor.apply();
	}
	public static Boolean getModified()
	{
		return settings.getBoolean(KEY_PREF_MODIFIED, false);
	}
	public static void setModified(Boolean value)
	{
		SharedPreferences.Editor editor = Settings.settings.edit();

		editor.putBoolean(KEY_PREF_MODIFIED, value);

		editor.apply();
	}

	public static Boolean hasContactInfo()
	{
		if (getPhone() != null && !getPhone().equals(""))
			return true;
		else if (getMail() != null && !getMail().equals(""))
			return true;

		return false;
	}

	protected static void clearContactInfo()
	{
		setFullName(null);
		setMail(null);
		setPhone(null);
		setNotify(false);
	}

	public static void load(Context context)
	{
		if (settings != null)
			return;

		settings = PreferenceManager.getDefaultSharedPreferences(context);

		listener = new SharedPreferences.OnSharedPreferenceChangeListener()
		{
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
			{
				if (key.equals(KEY_PREF_MODIFIED))
					return;

				if (!getModified())
				{
					SharedPreferences.Editor editor = Settings.settings.edit();

					editor.putBoolean(KEY_PREF_MODIFIED, true);

					editor.apply();
				}
			}
		};
		Settings.registerOnSharedPreferenceChangeListener(listener);

		checkVersion(context);
	}

	protected static void checkVersion(Context context)
	{
		Integer newVersion = Utils.getVersionCode(context);
		if (newVersion == null)
			return;

		Integer oldVersion = settings.getInt(KEY_PREF_APP_VERSION, 0);

		if (!oldVersion.equals(newVersion))
		{
			Log.debug(Utils.NLStringFormat("App version change detected: %d => %d.", oldVersion, newVersion));

			SharedPreferences.Editor editor = Settings.settings.edit();

			editor.putInt(KEY_PREF_APP_VERSION, newVersion);

			editor.apply();

			updateSettings(oldVersion, newVersion);
		}
	}

	protected static void updateSettings(Integer oldVersion, Integer newVersion)
	{
		if (oldVersion < Constants.AppVersion.V2_0)
		{
			clearContactInfo();

			setAnonymous(true);
		}
	}

	public static JSONObject asJSON() throws Exception
	{
		JSONObject user = new JSONObject();

		user.put("password", Settings.getUserKey());

		if (Settings.getFullName() != null)
			user.put("fullname", Settings.getFullName());
		if (Settings.getMail() != null)
			user.put("mail", Settings.getMail());
		if (Settings.getPhone() != null)
			user.put("phone", Settings.getPhone());

		user.put("notify", Settings.getNotify());

		if (Settings.getLanguage() != null)
			user.put("language", Settings.getLanguage());

		return user;
	}

	public static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
	{
		settings.registerOnSharedPreferenceChangeListener(listener);
	}

	public static void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
	{
		if (settings != null)
			settings.unregisterOnSharedPreferenceChangeListener(listener);
	}
}
