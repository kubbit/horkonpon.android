package com.kubbit.horkonpon;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class About extends ActionBarActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);
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
