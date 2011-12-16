package com.morgan.design.android.service;

import static com.morgan.design.android.util.ObjectUtils.isNotNull;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.morgan.design.android.util.Logger;

public class LocationLookupService extends Service {

	private static final String LOG_TAG = "LocationLookupService";

	public static final String GET_CURRENT_LOCATION_LOOKUP = "com.morgan.design.android.service.LocationLookupService";
	public static final String LOCATION_CHANGED_BROADCAST = "com.morgan.design.broadcast.LOCATION_CHANGED";

	public static final String PROVIDERS_FOUND = "PROVIDERS_FOUND";
	public static final String CURRENT_LOCAION = "CURRENT_LOCATION";
	public static final String LOCATION_LOOKUP_TIMEOUT = "LOCATION_LOOKUP_TIMEOUT";

	// 30 seconds
	public static final int DEFAULT_LOCATION_TIMEOUT = 30000;

	LocationManager locationManager;
	Timer timer;

	boolean gpsEnabled = false;
	boolean networkEnabled = false;

	@Override
	public void onCreate() {
		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onDestroy() {
		if (null != this.timer) {
			this.timer.cancel();
		}
		removetLocationListeners();
	}

	LocationListener networkLocationListener = new LocationListenerFacade() {
		@Override
		public void onLocationChanged(final Location location) {
			onCurrentLocationFound(location);
		}
	};

	LocationListener gpsLocationListener = new LocationListenerFacade() {
		@Override
		public void onLocationChanged(final Location location) {
			onCurrentLocationFound(location);
		}
	};

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Logger.d(LOG_TAG, "Starting lookup location service");

		int locationTimeOut = DEFAULT_LOCATION_TIMEOUT;

		// Allow for default to be overridden if required
		final Bundle extras = intent.getExtras();
		if (isNotNull(extras)) {
			if (intent.hasExtra(LOCATION_LOOKUP_TIMEOUT)) {
				locationTimeOut = intent.getIntExtra(LOCATION_LOOKUP_TIMEOUT, DEFAULT_LOCATION_TIMEOUT);
			}
		}

		determineProvidersEnabled();

		// don't start listeners if no provider is enabled
		if (!this.gpsEnabled && !this.networkEnabled) {
			onNoProvidersFound();
			stopSelf();
		}

		bindLocationListeners();

		// Start the fall back option, get the last know location result
		this.timer = new Timer();
		this.timer.schedule(new GetLastKnownLocation(), locationTimeOut);

		return START_NOT_STICKY;
	};

	private void determineProvidersEnabled() {
		try {
			this.gpsEnabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}
		catch (final Exception ex) {}
		try {
			this.networkEnabled = this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (final Exception ex) {}
	}

	private void bindLocationListeners() {
		if (this.gpsEnabled) {
			this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.gpsLocationListener);
		}
		if (this.networkEnabled) {
			this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this.networkLocationListener);
		}
	}

	public void onNoProvidersFound() {
		final Bundle extras = new Bundle();
		extras.putBoolean(PROVIDERS_FOUND, false);
		sendBroadcast(new Intent(LOCATION_CHANGED_BROADCAST).putExtras(extras));
		stopSelf();
	}

	public void onCurrentLocationFound(final Location location) {
		final Bundle extras = new Bundle();

		if (null != location) {
			extras.putParcelable(CURRENT_LOCAION, location);
		}
		else {
			extras.putBoolean(PROVIDERS_FOUND, true);
		}
		sendBroadcast(new Intent(LOCATION_CHANGED_BROADCAST).putExtras(extras));
		stopSelf();
	}

	protected void removetLocationListeners() {
		if (null != this.networkLocationListener) {
			this.locationManager.removeUpdates(this.networkLocationListener);
		}
		if (null != this.gpsLocationListener) {
			this.locationManager.removeUpdates(this.gpsLocationListener);
		}
	}

	class GetLastKnownLocation extends TimerTask {

		@Override
		public void run() {
			removetLocationListeners();

			Location netLocation = null;
			Location gpsLocation = null;

			if (LocationLookupService.this.gpsEnabled) {
				gpsLocation = LocationLookupService.this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if (LocationLookupService.this.networkEnabled) {
				netLocation = LocationLookupService.this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

			if (gpsLocation != null && netLocation != null) {

				if (gpsLocation.getTime() > netLocation.getTime()) {
					onCurrentLocationFound(gpsLocation);
				}
				else {
					onCurrentLocationFound(netLocation);
				}
				return;
			}

			if (gpsLocation != null) {
				onCurrentLocationFound(gpsLocation);
				return;
			}
			if (netLocation != null) {
				onCurrentLocationFound(netLocation);
				return;
			}
			onCurrentLocationFound(null);
		}
	}

	/**
	 * Simply exposes only the required methods, others can be override if requried
	 */
	abstract class LocationListenerFacade implements LocationListener {

		@Override
		public void onProviderDisabled(final String provider) {
		}

		@Override
		public void onProviderEnabled(final String provider) {
		}

		@Override
		public void onStatusChanged(final String provider, final int status, final Bundle extras) {
		}
	}

	@Override
	public IBinder onBind(final Intent intent) {
		// No binder is required
		return null;
	}
}
