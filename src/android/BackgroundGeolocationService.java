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
		// Determine whether the fore-ground Activity is running.  If it's not, we'll reboot it.
		boolean isPluginActive = BackgroundGeolocationPlugin.isActive();
		
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
				case DetectedActivity.WALKING:
				case DetectedActivity.RUNNING:
					isMoving = true;
					break;
				case DetectedActivity.STILL:
					break;
				case DetectedActivity.UNKNOWN:
					break;
				case DetectedActivity.TILTING:
					break;
			}
			
			// Force main-activity reload (if not running) if we're detected to be moving.
			if (isMoving && !isPluginActive) {
				forceMainActivityReload();
		    }
			
			// Post activity to the bus.
			EventBus.getDefault().post(probableActivity);
		} else {
			final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
			if (location != null) {
				Log.i(TAG, "Location received: " + location.toString());
				
				// Force main-activity reload when a location comes in.
				if (!isPluginActive) {
					forceMainActivityReload();
			    }
				EventBus.getDefault().post(location);	
			}
		}
	}
	
	/**
	 * This method has no other purpose than formatting the Activity for log-messages 
	 */
	private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.RUNNING:
            	return "running";
            case DetectedActivity.WALKING:
            	return "walking";
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
	 * Forces the main activity to re-launch if it's unloaded.  This is how we're able to rely upon Javascript
	 * running always, since we for the app to boot.
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