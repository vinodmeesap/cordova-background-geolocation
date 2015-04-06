package com.transistorsoft.cordova.bggeo;

import de.greenrobot.event.EventBus;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.ActivityRecognitionResult;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ActivityRecognitionService extends IntentService {

    private static final String TAG = "BackgroundGeolocation";
    
    public ActivityRecognitionService() {
        super("com.transistorsoft.cordova.bggeo.ActivityRecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Determine whether the fore-ground Activity is running.  If it's not, we'll reboot it.
        
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity probableActivity = result.getMostProbableActivity();
            
            Log.w(TAG, "Activity detected:" + getActivityName(probableActivity.getType()) + ", confidence:" + probableActivity.getConfidence());
            if (probableActivity.getConfidence() < 80) {
                return;
            }

            switch (probableActivity.getType()) {
                case DetectedActivity.IN_VEHICLE:
                case DetectedActivity.ON_BICYCLE:
                case DetectedActivity.ON_FOOT:
                case DetectedActivity.WALKING:
                case DetectedActivity.RUNNING:
                case DetectedActivity.STILL:
                    EventBus.getDefault().post(result);
                    break;
                case DetectedActivity.UNKNOWN:
                    return;
                case DetectedActivity.TILTING:
                    return;
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
}