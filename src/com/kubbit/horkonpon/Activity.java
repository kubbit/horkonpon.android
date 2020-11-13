package com.kubbit.horkonpon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public abstract class Activity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	protected Context context;
	private DrawerLayout drawer;
	private ActionBarDrawerToggle drawerToggle;

	public Integer drawerId()
	{
		return null;
	}

	public ActionBar getToolBar()
	{
		return this.getSupportActionBar();
	}

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		this.context = this;

		Settings.load(this.context);

		Settings.registerOnSharedPreferenceChangeListener(this);

		Utils.setLanguage(this, Settings.getLanguage(), true);
		this.resetTitle();
	}

	@Override
	protected void onDestroy()
	{
		Settings.unregisterOnSharedPreferenceChangeListener(this);

		super.onDestroy();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		this.addAnimatedToggleButton();
	}

	private void addAnimatedToggleButton()
	{
		if (this.drawerId() == null)
			return;

		this.drawer = (DrawerLayout)findViewById(this.drawerId());
		if (this.drawer == null)
			return;

		// animated drawer toggle button
		this.drawerToggle = new ActionBarDrawerToggle(this, this.drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		this.drawer.setDrawerListener(this.drawerToggle);
		this.getToolBar().setDisplayHomeAsUpEnabled(true);
		this.getToolBar().setHomeButtonEnabled(true);
		this.drawerToggle.syncState();
	}

	protected void showScreen(Class<?> cls)
	{
		this.showScreen(cls, R.anim.right_slide_in);
	}

	protected void showScreen(Class<?> cls, int enterAnim)
	{
		// hide drawer if visible
		if (this.drawer != null && this.drawer.isDrawerOpen(GravityCompat.START))
			this.drawer.closeDrawer(GravityCompat.START);

		Intent intent = new Intent(this, cls);
		startActivity(intent);

		overridePendingTransition(enterAnim, R.anim.stay);
	}

	protected void hideKeyboard()
	{
		try
		{
			if (getCurrentFocus() != null)
				return;

			// hide virtual keyboard
			InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			if (inputManager != null)
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}
	}

	@Override
	public void onBackPressed()
	{
		if (this.drawer != null && this.drawer.isDrawerOpen(GravityCompat.START))
		{
			this.drawer.closeDrawer(GravityCompat.START);
			return;
		}

		super.onBackPressed();

		if (this.getClass() != MainActivity.class)
			overridePendingTransition(0, R.anim.right_slide_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (this.drawerToggle != null && this.drawerToggle.onOptionsItemSelected(item))
			return true;

		if (item.getItemId() == android.R.id.home)
		{
			this.onBackPressed();

			return true;
		}

		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Settings.KEY_PREF_LANGUAGE))
		{
			Utils.setLanguage(this, Settings.getLanguage(), true);

			this.recreate();
		}
	}

	/*
	 * Prevent activity title cache (problems when changing language)
	 */
	private void resetTitle()
	{
		try
		{
			android.content.pm.ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), android.content.pm.PackageManager.GET_META_DATA);

			int label = activityInfo.labelRes;
			if (label != 0)
				setTitle(label);
		}
		catch (Exception e)
		{
	
		}
	}
}
