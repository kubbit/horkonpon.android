package com.kubbit.location;

import android.content.Context;
import android.location.Address;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Date;

import com.kubbit.net.Net;
import com.kubbit.utils.Timer;
import com.kubbit.utils.Utils;

public class Location implements LocationListener, Geocoder.addressSet
{
	public class Status
	{
		public static final int UNKNOWN = 0;
		public static final int GPS_DISABLED = 1;
		public static final int NOT_ACTIVE = 2;
		public static final int SEARCHING_POSITION = 3;
		public static final int SEARCHING_ADDRESS = 4;
		public static final int LOCATION_SET = 5;
		public static final int ADDRESS_SET = 6;
	}

	private static final int MIN_ACCURACY = 50;
	private static final int UPDATE_FREQUENCY = 1 * 1000;
	private static final float UPDATE_DISTANCE = 1 / 4;
	private static final int NEGLIGIBLE_DISTANCE = 5;
	private static final int MIN_ATTEMPTS = 3;
	private static final int MAX_ATTEMPTS = 25;
	private static final int GPS_TIMEOUT = 30 * 1000;
	private static final int MIN_LOCATION_REFRESH_TIME = 30 * 1000;

	private final Context _context;
	private android.location.Location location = null;
	private Address address = null;
	private boolean singleCall;
	private boolean active = false;
	private int counter;
	private int attempt;
	private Date timestamp;

	private final LocationManager locationManager;

	private final Timer timer;

	public double getLongitude()
	{
		if (this.location != null)
			return this.location.getLongitude();
		else
			return 0;
	}
	public double getLatitude()
	{
		if (this.location != null)
			return this.location.getLatitude();
		else
			return 0;
	}
	public String getLocality()
	{
		if (this.address != null)
			return this.address.getLocality();
		else
			return "";
	}
	public float getAccuracy()
	{
		if (this.location != null)
			return this.location.getAccuracy();
		else
			return Float.MAX_VALUE;
	}
	public int getStatus()
	{
		if (!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return Status.GPS_DISABLED;

		if (this.address != null)
			return Status.ADDRESS_SET;

		if (this.location != null)
			return Status.LOCATION_SET;

		if (!this.active)
			return Status.NOT_ACTIVE;

		if (this.location == null)
			return Status.SEARCHING_POSITION;

		if (this.address == null)
			return Status.SEARCHING_ADDRESS;

		return Status.UNKNOWN;
	}

	public Changed onChanged;

	public interface Changed
	{
		void onLocationChanged(Object sender);
	}

	public Location(Context context)
	{
		this._context = context;

		this.locationManager = (LocationManager)this._context.getSystemService(Context.LOCATION_SERVICE);

		this.timer = new Timer(GPS_TIMEOUT);
		this.timer.onFinish = timer_onFinish;
	}

	public Boolean isSet()
	{
		if (this.location == null)
			return false;
		else if (this.getLatitude() == 0 && this.getLongitude() == 0)
			return false;

		return true;
	}

	public Boolean checkPosition()
	{
		if (!this.checkLastLocationTime())
			return false;

		return this.initialize(true);
	}

	private Boolean checkLastLocationTime()
	{
		Date now = new Date();

		return this.timestamp == null || now.getTime() - this.timestamp.getTime() > MIN_LOCATION_REFRESH_TIME;
	}

	public Boolean start()
	{
		return this.initialize(false);
	}

	private Boolean initialize(boolean singleCall)
	{
		if (this.onChanged != null)
			onChanged.onLocationChanged(this);

		if (this.active || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return false;

		this.counter = 0;
		this.attempt = 0;
		this.active = true;
		this.location = null;
		this.singleCall = singleCall;

		if (!Utils.checkPermission(this._context, android.Manifest.permission.ACCESS_FINE_LOCATION))
			return false;

		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_FREQUENCY, UPDATE_DISTANCE, this);

		if (Utils.checkPermission(this._context, android.Manifest.permission.ACCESS_COARSE_LOCATION))
			this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_FREQUENCY, UPDATE_DISTANCE, this);

		this.timer.start();

		if (this.onChanged != null)
			onChanged.onLocationChanged(this);

		return true;
	}

	public void stop()
	{
		if (!this.active)
			return;

		this.active = false;

		if (Utils.checkPermission(this._context, android.Manifest.permission.ACCESS_FINE_LOCATION))
			this.locationManager.removeUpdates(this);

		this.timer.stop();

		if (this.onChanged != null)
			onChanged.onLocationChanged(this);
	}

	public void checkAddress()
	{
		if (!Net.isNetworkAvailable(this._context))
			return;

		if (this.location == null)
			return;

		Geocoder geocoder = new Geocoder(this._context, this.location);
		geocoder.onResult = this;
		geocoder.execute();
	}

	@Override
	public void onLocationChanged(android.location.Location newLocation)
	{
		// restart gps timeout timer countdown
		this.timer.reset();

		this.counter++;

		// set the new location if there is no previous location
		// or the new location is more accurate
		// or the position has changed significantly
		if (this.location == null
		 || newLocation.getAccuracy() < this.location.getAccuracy()
		 || newLocation.distanceTo(this.location) - newLocation.getAccuracy() > NEGLIGIBLE_DISTANCE)
		{
			this.attempt = 0;

			this.location = newLocation;
			this.checkAddress();

			if (this.onChanged != null)
				onChanged.onLocationChanged(this);
		}
		// stop updates if it doesnt best previous one
		else if (this.singleCall
		 && this.location.getAccuracy() <= MIN_ACCURACY
		 && newLocation.getAccuracy() >= this.location.getAccuracy())
		{
			this.attempt++;

			if (this.attempt >= MIN_ATTEMPTS)
			{
				this.timestamp = new Date();
				this.stop();
			}
		}
		else if (this.singleCall && this.counter >= MAX_ATTEMPTS)
			this.stop();
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		if (this.onChanged != null)
			onChanged.onLocationChanged(this);
	}

	@Override
	public void onProviderEnabled(String provider)
	{
		if (this.onChanged != null)
			onChanged.onLocationChanged(this);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		if (this.onChanged != null)
			onChanged.onLocationChanged(this);
	}

	private final Timer.Finish timer_onFinish = new Timer.Finish()
	{
		@Override
		public void onTimerFinish(Object sender)
		{
			stop();
		}
	};

	@Override
	public void onAddressSet(Address result)
	{
		this.address = result;

		if (this.onChanged != null)
			onChanged.onLocationChanged(this);
	}
}
