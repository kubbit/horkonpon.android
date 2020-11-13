package com.kubbit.horkonpon.screens;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.kubbit.horkonpon.*;
import com.kubbit.utils.Utils;

public class Disclaimer extends Activity
{
	protected WebView tvDisclaimer;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.disclaimer);
		Utils.setLanguage(this, Settings.getLanguage(), true);

		this.tvDisclaimer = (WebView)findViewById(R.id.disclaimer);

		this.tvDisclaimer.loadUrl(String.format(Constants.DISCLAIMER_URL, Utils.getLanguage(this)));
		WebSettings settings = this.tvDisclaimer.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		// fix language reset in Android >= 7
		Utils.setLanguage(this, Settings.getLanguage(), true);
	}

	@Override
	public void onBackPressed()
	{
		// fix language reset in Android >= 7
		Utils.setLanguage(this, Settings.getLanguage(), true);

		super.onBackPressed();
	}
}
