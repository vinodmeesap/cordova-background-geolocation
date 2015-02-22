package com.transistorsoft.cordova.bggeo;

import de.greenrobot.event.EventBus;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.ActivityRecognitionResult;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

public class BackgroundGeolocationService extends IntentService {

	private static final String TAG = "BackgroundGeolocationService";
	
	public BackgroundGeolocationService() {
		super("com.transistorsoft.cordova.bggeo.BackgroundGeolocationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			DetectedActivity probableActivity = result.getMostProbableActivity();
			
			Log.i(TAG, "Activity detected:" + getActivityName(probableActivity.getType()) + ", confidence:" + probableActivity.getConfidence());
			if (probableActivity.getConfidence() < 80) {
				return;
			}

			Boolean isMoving = false;
			switch (probableActivity.getType()) {
				case DetectedActivity.IN_VEHICLE:
				case DetectedActivity.ON_BICYCLE:
				case DetectedActivity.ON_FOOT:
					isMoving = true;
					break;
				case DetectedActivity.STILL:
					break;
				case DetectedActivity.UNKNOWN:
					break;
				case DetectedActivity.TILTING:
					break;
			}
			
			boolean isPushPluginActive = BackgroundGeolocationPlugin.isActive();
			if (isMoving && !isPushPluginActive) {
				forceMainActivityReload();
		    }
			EventBus.getDefault().post(probableActivity);
		} else {
			final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
			if (location != null) {
				Log.i(TAG, "Location received: " + location.toString());
				boolean isPushPluginActive = BackgroundGeolocationPlugin.isActive();
				if (!isPushPluginActive) {
					forceMainActivityReload();
			    }
				EventBus.getDefault().post(location);	
			}
		}
	}
	
	private String getActivityName(int activityType) {
		switch (activityType) {
			case DetectedActivity.IN_VEHICLE:
				return "in_vehicle";
			case DetectedActivity.ON_BICYCLE:
				return "on_bicycle";
			case DetectedActivity.ON_FOOT:
				return "on_foot";
			case DetectedActivity.STILL:
				return "still";
			case DetectedActivity.UNKNOWN:
				return "unknown";
			case DetectedActivity.TILTING:
				return "tilting";
		}
		return "unknown";
	}
	
	/**
	 * Forces the main activity to re-launch if it's unloaded.
	 */
	private void forceMainActivityReload() {
		Log.w(TAG, "- Forcing main-activity reload");
		PackageManager pm = getPackageManager();
		Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
		launchIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(launchIntent);
	}
}