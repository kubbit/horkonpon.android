package com.kubbit.horkonpon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.kubbit.profile.Profile;
import com.kubbit.utils.Utils;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	public static final String KEY_PREF_FIRST_TIME = "pref_first_time";
	public static final String KEY_PREF_FULLNAME = "pref_fullname";
	public static final String KEY_PREF_PHONE = "pref_phone";
	public static final String KEY_PREF_MAIL = "pref_mail";
	public static final String KEY_PREF_ANONYMOUS = "pref_anomymous";
	public static final String KEY_PREF_LANGUAGE = "pref_language";
	public static final String KEY_PREF_NOTIFY = "pref_notify";

	public static SharedPreferences settings;

	public static String getLanguage()
	{
		return settings.getString(KEY_PREF_LANGUAGE, null);
	}
	public static Boolean getFirstTime()
	{
		return settings.getBoolean(KEY_PREF_FIRST_TIME, true);
	}
	public static String getFullName()
	{
		return settings.getString(KEY_PREF_FULLNAME, null);
	}
	public static String getPhone()
	{
		return settings.getString(KEY_PREF_PHONE, null);
	}
	public static String getMail()
	{
		return settings.getString(KEY_PREF_MAIL, null);
	}
	public static Boolean getAnonymous()
	{
		return settings.getBoolean(KEY_PREF_ANONYMOUS, true);
	}
	public static Boolean getNotify()
	{
		return settings.getBoolean(KEY_PREF_NOTIFY, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		load(this);
		settings.registerOnSharedPreferenceChangeListener(this);
		Utils.setLanguage(this, getLanguage());

		this.refresh();

		SharedPreferences.Editor editor = settings.edit();

		if (getFirstTime())
			editor.putBoolean(KEY_PREF_FIRST_TIME, false);

		if (getPhone() == null)
			editor.putString(KEY_PREF_PHONE, Profile.getPhone(this));

		if (getMail() == null)
			editor.putString(KEY_PREF_MAIL, Profile.getMail(this));

		editor.commit();
	}

	public void refresh()
	{
		this.setPreferenceScreen(null);
		this.addPreferencesFromResource(R.xml.preferences);
	}

	public static void load(Context context)
	{
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		overridePendingTransition(0, R.anim.right_slide_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			this.onBackPressed();

			return true;
		}

		return false;
	}

	@Override
	protected void onDestroy()
	{
		if (settings != null)
			settings.unregisterOnSharedPreferenceChangeListener(this);

		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(KEY_PREF_LANGUAGE))
			Utils.setLanguage(this, getLanguage(), true);

		this.refresh();
	}
}
