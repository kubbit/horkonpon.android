package com.kubbit.horkonpon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity
{
	final int SPLASH_DIALOG_DELAY = 1 * 1000;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Preferences.load(this);

		setContentView(R.layout.splash);

//		if (Preferences.getFirstTime())
			this.delayNext();
//		else
//			this.openNext();
	}

	private void delayNext()
	{
		Handler handler = new Handler();

		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				openNext();
			}
		}, SPLASH_DIALOG_DELAY);
	}

	private void openNext()
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);

		this.finish();
	}
}
