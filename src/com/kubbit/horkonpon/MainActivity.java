package com.kubbit.horkonpon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kubbit.horkonpon.screens.*;
import com.kubbit.location.Location;
import com.kubbit.media.Camera;
import com.kubbit.media.Image;
import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public class MainActivity extends Activity implements com.kubbit.location.Location.Changed, ActivityCompat.OnRequestPermissionsResultCallback
{
	protected Camera camera;

	Issue issue = null;
	Location location = null;
	protected ImageView ivPhoto;
	protected EditText edComment;
	protected FloatingActionButton btSend;
	boolean firstStart = true;

	@Override
	public Integer drawerId()
	{
		return R.id.drawer_layout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// set language for displaying from preferences
		Utils.setLanguage(this, Settings.getLanguage());

		this.refresh();

		// if application gets killed temporary files are not deleted, so empty them at start
		Utils.emptyTempDir(this.context);

		this.clear();
	}

	public void refresh()
	{
		setContentView(R.layout.main);

		this.ivPhoto = (ImageView)findViewById(R.id.img);
		this.edComment = (EditText)findViewById(R.id.comment);
		this.btSend = (FloatingActionButton)findViewById(R.id.send);

		this.location = new Location(this);
		this.location.onChanged = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	private void showActivateGPS()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);

		alertDialogBuilder.setMessage(R.string.gps_disabled);
		alertDialogBuilder.setCancelable(false);

		alertDialogBuilder.setPositiveButton(R.string.button_positive, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(callGPSSettingIntent);
			}
		});

		alertDialogBuilder.setNegativeButton(R.string.button_negative, new DialogInterface.OnClickListener()
		{
			@Override
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
		try
		{
			this.camera = new Camera(this);
			this.camera.open();
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
			Toast.makeText(this.context, R.string.error_while_loading_picture, Toast.LENGTH_LONG).show();
		}
	}

	private void storeGPSValues()
	{
		this.issue.setLatitude(this.location.getLatitude());
		this.issue.setLongitude(this.location.getLongitude());
		this.issue.setAccuracy(this.location.getAccuracy());
		this.issue.setLocality(this.location.getLocality());
	}

	private void storeValues()
	{
		this.storeGPSValues();

		this.issue.getMessages().clear();
		this.issue.addMessage(this.edComment.getText().toString());
	}

	private void syncIssues()
	{
		IssueList.getInstance(this.context).sync();
	}

	private void send()
	{
		this.storeValues();

		if (!this.issue.validate())
			return;

		this.issue.setStatus(Constants.IssueStatus.NEW);
		this.issue.save();

		Toast.makeText(this.context, R.string.sync_progress, Toast.LENGTH_SHORT).show();

		// open issue visualization activity
		IssueList.getInstance(this.context).setSelected(this.issue);
		this.showScreen(IssueShow.class);

		this.clear();
	}

	private void clear()
	{
		this.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);
		this.ivPhoto.setImageResource(R.drawable.camera);

		this.edComment.setText("");

		this.issue = new Issue(this.context);
	}

	public void menuItemOnClick(MenuItem item)
	{
		if (item.getItemId() == R.id.action_list)
			this.showScreen(IssueBrowse.class);
		else if (item.getItemId() == R.id.action_preferences)
			this.showScreen(Preferences.class, R.anim.right_slide_in_bounce);
		else if (item.getItemId() == R.id.action_disclaimer)
			this.showScreen(Disclaimer.class);
		else if (item.getItemId() == R.id.action_about)
			this.showScreen(About.class);
	}

	public void imgOnClick(View v)
	{
		this.takePicture();
	}

	public void sendOnClick(View v)
	{
		this.btSend.setEnabled(false);

		this.hideKeyboard();

		this.send();

		this.btSend.setEnabled(true);
	}

	private void checkLocation(Boolean force)
	{
		if (this.location == null)
			return;

		if (!force && this.location.isSet() && this.issue.hasPhoto())
			return;

		if (this.firstStart && this.location.getStatus() == Location.Status.GPS_DISABLED)
			this.showActivateGPS();

		if (!Utils.checkPermission(this.context, android.Manifest.permission.ACCESS_FINE_LOCATION))
		{
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.AppPermission.LOCATION);
			return;
		}

		this.location.checkPosition();
	}

	@Override
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

		this.getToolBar().setTitle(title);

		this.storeGPSValues();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		// check for request cancelled
		if (grantResults.length == 0)
			return;

		switch (requestCode)
		{
			case Constants.AppPermission.LOCATION:
			{
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
					return;

				this.location.checkPosition();
				break;
			}
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		this.checkLocation(false);

		this.syncIssues();

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
	protected void onDestroy()
	{
		this.clear();

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		try
		{
			if (resultCode != Activity.RESULT_OK)
				return;

			switch (requestCode)
			{
				case Constants.ActivityResultCode.TAKE_PICTURE:
					try
					{
						Image img = this.camera.getImage();
						img.loadScaledBitmap(Constants.IMAGE_MIN_SIZE);

						this.ivPhoto.setImageBitmap(img.getBitmap());
						this.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

						DBFile file = new DBFile(this.context);
						img.writeJPG(file.getFile(), Constants.IMAGE_QUALITY);
						this.issue.setImage(file);

						this.checkLocation(true);
					}
					catch (Exception e)
					{
						Log.error(e.getMessage());
						Toast.makeText(this.context, R.string.error_while_loading_picture, Toast.LENGTH_LONG).show();
					}

					break;
			}
		}
		finally
		{
			if (this.camera == null)
				return;

			this.camera.clear();
			this.camera = null;
		}
	}
}
