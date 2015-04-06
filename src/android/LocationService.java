package com.transistorsoft.cordova.bggeo;

import de.greenrobot.event.EventBus;

import com.google.android.gms.location.FusedLocationProviderApi;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

public class LocationService extends IntentService {

	private static final String TAG = "BackgroundGeolocation";
	
	public LocationService() {
		super("com.transistorsoft.cordova.bggeo.LocationUpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
		
		if (location != null) {
			Log.i(TAG, "Location received: " + location.toString());
			EventBus.getDefault().post(location);	
		}
	}
	
}