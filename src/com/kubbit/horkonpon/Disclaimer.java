package com.kubbit.horkonpon;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Disclaimer extends ActionBarActivity
{
	protected WebView tvDisclaimer;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.disclaimer);

		this.tvDisclaimer = (WebView)findViewById(R.id.disclaimer);

		this.tvDisclaimer.loadData(getString(R.string.disclaimer_text), "text/html; charset=UTF-8", null);
		WebSettings settings = this.tvDisclaimer.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
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
}
