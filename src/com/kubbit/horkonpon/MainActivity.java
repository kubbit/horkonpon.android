package com.kubbit.horkonpon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import com.kubbit.location.Location;
import com.kubbit.media.Image;
import com.kubbit.net.HttpPost;
import com.kubbit.net.Net;
import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public class MainActivity extends ActionBarActivity implements HttpPost.httpPostResult, SharedPreferences.OnSharedPreferenceChangeListener
{
	final int TAKE_PICTURE = 2;
	Gertakaria gertakaria = new Gertakaria();
	Location location = null;
	File picture;
	Context context;
	protected ImageView ivPhoto;
	protected EditText edComment;
	protected Button btSend;
	ProgressDialog httpPostWait;
	boolean firstStart = true;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.context = this;

		Preferences.load(this.context);
		// check for language preference change
		Preferences.settings.registerOnSharedPreferenceChangeListener(this);
		// set language for displaying from preferences
		Utils.setLanguage(this, Preferences.getLanguage());

		this.refresh();

		//this.checkFirstTime();
	}

	public void refresh()
	{
		setContentView(R.layout.main);

		this.ivPhoto = (ImageView)findViewById(R.id.img);
		this.edComment = (EditText)findViewById(R.id.comment);
		this.btSend = (Button)findViewById(R.id.send);

		this.location = new Location(this);
		this.location.onChanged = this.Location_Changed;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Check if it is the first time this app is run and open preferences
	 * after a delay in that case
	 */
	private void checkFirstTime()
	{
		if (!Preferences.getFirstTime())
			return;

		Handler handler = new Handler();

		handler.postDelayed(new Runnable()
		{
			public void run()
			{
				showPreferences();
			}
		}, Constants.PREFERENCES_DIALOG_DELAY);
	}

	private void showPreferences()
	{
		Intent intent = new Intent(this, Preferences.class);
		startActivity(intent);

		overridePendingTransition(R.anim.right_slide_in_bounce, R.anim.stay);
	}

	private void showDisclaimer()
	{
		Intent intent = new Intent(this, Disclaimer.class);
		startActivity(intent);

		// disabled as it slows down webview's page loading until animation is over
		//overridePendingTransition(R.anim.right_slide_in, R.anim.stay);
	}

	private void showAbout()
	{
		Intent intent = new Intent(this, About.class);
		startActivity(intent);

		overridePendingTransition(R.anim.right_slide_in, R.anim.stay);
	}

	private void showActivateGPS()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);

		alertDialogBuilder.setMessage(R.string.gps_disabled);
		alertDialogBuilder.setCancelable(false);

		alertDialogBuilder.setPositiveButton(R.string.button_positive, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(callGPSSettingIntent);
			}
		});

		alertDialogBuilder.setNegativeButton(R.string.button_negative, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		});

		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

	private void takePicture()
	{
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		this.picture = Utils.getTemporaryFile(this.context, "jpg");
		Uri tmpFileUri = Uri.fromFile(this.picture);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFileUri);
		startActivityForResult(intent, TAKE_PICTURE);
	}

	private void storeValues()
	{
		this.gertakaria.setLatitudea(this.location.getLatitude());
		this.gertakaria.setLongitudea(this.location.getLongitude());
		this.gertakaria.setZehaztasuna(this.location.getAccuracy());
		this.gertakaria.setHerria(this.location.getLocality());

		if (!Preferences.getAnonymous())
		{
			this.gertakaria.setIzena(Preferences.getFullName());
			this.gertakaria.setTelefonoa(Preferences.getPhone());
			this.gertakaria.setPosta(Preferences.getMail());
			this.gertakaria.setOhartarazi(Preferences.getNotify());
		}
		else
		{
			this.gertakaria.setIzena("");
			this.gertakaria.setTelefonoa("");
			this.gertakaria.setPosta("");
			this.gertakaria.setOhartarazi(false);
		}

		this.gertakaria.setHizkuntza(Utils.getLanguage(this.context));

		this.gertakaria.setOharrak(this.edComment.getText().toString());
	}

	private void clear()
	{
		this.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);
		this.ivPhoto.setImageResource(R.drawable.camera);

		this.edComment.setText("");

		this.gertakaria.clear();
	}

	public void preferencesOnClick(MenuItem item)
	{
		this.showPreferences();
	}

	public void disclaimerOnClick(MenuItem item)
	{
		this.showDisclaimer();
	}

	public void aboutOnClick(MenuItem item)
	{
		this.showAbout();
	}

	public void imgOnClick(View v)
	{
		this.takePicture();
	}

	public void sendOnClick(View v)
	{
		this.btSend.setEnabled(false);

		// hide virtual keyboard
		InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		this.storeValues();

		if (!this.gertakaria.validate(this.context))
		{
			this.btSend.setEnabled(true);
			return;
		}

		if (!Net.isNetworkAvailable(this.context))
		{
			Toast.makeText(this.context, R.string.error_no_internet, Toast.LENGTH_LONG).show();
			this.btSend.setEnabled(true);
			return;
		}

		try
		{
			NameValuePair param = new BasicNameValuePair("datuak", this.gertakaria.asJSON(this.context));
			HttpPost httpPost = new HttpPost(this.context, Constants.API_URL);
			httpPost.onResult = this;

			this.httpPostWait = ProgressDialog.show(this.context, null, getString(R.string.send_progress), true);
			httpPost.execute(param);
		}
		catch (Exception e)
		{
			Log.debug(e.getMessage());

			this.httpPostWait.dismiss();
			this.btSend.setEnabled(true);

			Utils.showMessage(this.context, e.getLocalizedMessage());
		}
	}

	public void onHttpPostResult(String result)
	{
		this.httpPostWait.dismiss();
		this.btSend.setEnabled(true);

		try
		{
			Log.debug(result);

			JSONObject jsResult = new JSONObject(result);

			Log.info(jsResult.getString("mezua"));
			Utils.showMessage(this.context, jsResult.getString("mezua"));

			if (jsResult.getInt("kodea") >= 0)
				this.clear();
		}
		catch (JSONException e)
		{
			Log.debug(e.getMessage());
			Utils.showMessage(this.context, getString(R.string.error_http));
		}
	}

	private final Location.Changed Location_Changed = new Location.Changed()
	{
		public void onLocationChanged(Object sender)
		{
			String title = getString(R.string.validate_gertakaria_no_gps);

			switch (location.getStatus())
			{
				case Location.Status.GPS_DISABLED:
					title = getString(R.string.no_gps);
					break;
				case Location.Status.ADDRESS_SET:
					title = location.getLocality();
					break;
				case Location.Status.LOCATION_SET:
					title = getString(R.string.location_set);
					break;
				case Location.Status.SEARCHING_POSITION:
				case Location.Status.SEARCHING_ADDRESS:
					title = getString(R.string.searching_location);
					break;
			}

			getSupportActionBar().setTitle(title);
		}
	};

	@Override
	protected void onStart()
	{
		super.onStart();

		if (this.location == null)
			return;

		if (this.firstStart && this.location.getStatus() == Location.Status.GPS_DISABLED)
			this.showActivateGPS();

		this.location.checkPosition();

		this.firstStart = false;
	}

	@Override
	protected void onStop()
	{
		if (this.location != null)
			this.location.stop();

		super.onStop();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Preferences.KEY_PREF_LANGUAGE))
		{
			Utils.setLanguage(this, Preferences.getLanguage(), true);

			this.refresh();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		try
		{
			if (resultCode != Activity.RESULT_OK)
				return;

			switch (requestCode)
			{
				case TAKE_PICTURE:
					Image img = new Image(this.picture);
					img.rotateToOrigin();

					this.ivPhoto.setImageBitmap(img.getBitmap());
					this.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

					try
					{
						this.gertakaria.setArgazkia(img.getJPG(), this.picture.getName());
					}
					catch (Exception e)
					{
						Log.debug(e.getMessage());
					}

					break;
			}
		}
		finally
		{
			// always delete temporary file
			if (this.picture != null && this.picture.exists())
			{
				this.picture.delete();
				this.picture = null;
			}
		}
	}
}
