package com.kubbit.horkonpon.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.kubbit.horkonpon.*;
import com.kubbit.utils.Utils;

public class Preferences extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Settings.load(this);

		Utils.setLanguage(this, Settings.getLanguage());

		if (savedInstanceState == null)
			getFragmentManager().beginTransaction().add(android.R.id.content, new PrefFragment()).commit();
	}

	public static class PrefFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

			this.loadPreferences();
		}

		protected void loadPreferences()
		{
			addPreferencesFromResource(R.xml.preferences);

			/*Preference myPref = (Preference)findPreference(Settings.KEY_PREF_ANONYMOUS);
			myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				public boolean onPreferenceClick(Preference preference)
				{
					if (!Settings.getAnonymous())
					{
						new AlertDialog.Builder(getActivity())
						 .setTitle(R.string.anonymous_confirm_title)
						 .setMessage(R.string.anonymous_deskribapena)
						 .setCancelable(false)
						 .setPositiveButton(android.R.string.yes, null)
						 .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{
								Settings.setAnonymous(true);

								getPreferenceScreen().removeAll();
								loadPreferences();
							}
						}).show();
					}

					return true;
				}
			});*/
		}
	}
}
