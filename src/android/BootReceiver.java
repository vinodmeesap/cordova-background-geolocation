package com.transistorsoft.cordova.bggeo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This boot receiver is meant to handle the case where device is first booted after power up.  This will initiate
 * Google Play's ActivityRecognition API, whose events will be sent to BackgroundGeolocationService as usual.
 * @author chris
 *
 */
public class BootReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {   
	private static final String TAG = "BackgroundGeolocation";
	
	private GoogleApiClient googleApiClient;
	private PendingIntent locationUpdateService;
	
	private Integer activityRecognitionInterval = 10000;
	
	private PendingResult pendingResult;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "- BootReceiver auto-running ActivityRecognition system");
    	
    	// GoogleApiClient connection is asynchronous process.  @see #onConnected
    	pendingResult = goAsync();
    	
    	// Connect to google-play services.
        if (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)) {
            Log.i(TAG, "- Connecting to GooglePlayServices...");
            
            googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

            googleApiClient.connect();
        } else {
            Log.e(TAG,  "- GooglePlayServices unavailable");
        }
        
        // This is the IntentService we'll provide to google-play API.
        locationUpdateService = PendingIntent.getService(context, 0, new Intent(context, LocationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private void requestActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, activityRecognitionInterval, locationUpdateService);
    }
    
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		pendingResult.finish();
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		requestActivityUpdates();
		pendingResult.finish();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		pendingResult.finish();
	}
}