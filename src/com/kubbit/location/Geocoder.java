package com.kubbit.location;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.kubbit.utils.Log;

public class Geocoder extends AsyncTask<Void, Void, Address>
{
	final int MAX_RESULTS = 1;

	public addressSet onResult;
	private final android.location.Geocoder geocoder;
	private final android.location.Location location;

	public interface addressSet
	{
		void onAddressSet(Address result);
	}

	public Geocoder(Context context, android.location.Location location)
	{
		this.geocoder = new android.location.Geocoder(context, Locale.getDefault());

		this.location = location;
	}

	@Override
	protected Address doInBackground(Void... params)
	{
		List<Address> addresses;

		try
		{
			addresses = this.geocoder.getFromLocation(this.location.getLatitude(), this.location.getLongitude(), MAX_RESULTS);
			if (addresses.size() > 0)
				return addresses.get(0);
		}
		catch (IOException e)
		{
			Log.debug(e.getMessage());
		}

		return null;
	}

	@Override
	protected void onPostExecute(Address result)
	{
		if (this.onResult != null)
			onResult.onAddressSet(result);
	}
}
