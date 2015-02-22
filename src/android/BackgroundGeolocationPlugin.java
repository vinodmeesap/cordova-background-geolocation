package com.transistorsoft.cordova.bggeo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import de.greenrobot.event.EventBus;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.location.LocationListener;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class BackgroundGeolocationPlugin extends CordovaPlugin implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BackgroundGeolocation";

    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_CONFIGURE = "configure";
    public static final String ACTION_SET_CONFIG = "setConfig";

    private PendingIntent locationUpdateService;

    private Boolean isEnabled       = false;
    private Boolean isMoving        = false;
    
    // Common config
    private Integer desiredAccuracy     = 10;
    private Integer stationaryRadius    = 50;
    private Float distanceFilter        = (float) 50;
    private Boolean isDebugging         = false;
    private Boolean stopOnTerminate     = false;
    
    // Android-only config
    private Integer locationUpdateInterval      = 60000;
    private Integer activityDetectionInterval   = 60000;
    
    
    private CallbackContext callback;

    private GoogleApiClient googleApiClient;    
    private DetectedActivity currentActivity;
    
    private static CordovaWebView gWebView;    
    private ToneGenerator toneGenerator;
    
    @Override
    protected void pluginInitialize() {
        Log.d("BUS","registering");
        
        gWebView = this.webView;
        
        Activity activity = this.cordova.getActivity();
        
        // Connect to google-play services.
        if (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity)) {
            Log.i(TAG, "- Connecting to GooglePlayServices...");
            
            googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

            googleApiClient.connect();
        } else {
            Log.e(TAG,  "- GooglePlayServices unavailable");
        }
        
        // This is the IntentService we'll provide to google-play API.
        locationUpdateService = PendingIntent.getService(activity, 0, new Intent(activity, BackgroundGeolocationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Register for events fired by our IntentService "LocationService"
        EventBus.getDefault().register(this);
        
        if (isDebugging) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        }
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        Log.d(TAG, "execute / action : " + action);

        Boolean result = false;
        
        if (ACTION_START.equalsIgnoreCase(action) && !isEnabled) {
            result      = true;
            isEnabled   = true;
            isMoving    = false;
            
            requestActivityUpdates();

        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
            result      = true;
            isEnabled   = false;
            isMoving    = false;
            
            removeLocationUpdates();
            removeActivityUpdates();
            callbackContext.success();
        } else if (ACTION_CONFIGURE.equalsIgnoreCase(action)) {
            result = true;
            try {
                JSONObject config = data.getJSONObject(0);
                Log.i(TAG, "- configure: " + config.toString());
                
                this.stationaryRadius           = config.getInt("stationaryRadius");
                this.distanceFilter             = (float) config.getInt("distanceFilter");
                this.desiredAccuracy            = config.getInt("desiredAccuracy");
                this.locationUpdateInterval     = config.getInt("locationUpdateInterval");
                this.activityDetectionInterval  = config.getInt("activityDetectionInterval");
                this.isDebugging                = config.getBoolean("debug");
                this.stopOnTerminate            = config.getBoolean("stopOnTerminate");
                
                this.callback = callbackContext;
            } catch (JSONException e) {
                callbackContext.error("Configuration error " + e.getMessage());
            }
        } else if (ACTION_SET_CONFIG.equalsIgnoreCase(action)) {
            result = true;
            // TODO reconfigure Service
            callbackContext.success();
        }

        return result;
    }
    
    /**
     * Translates a number representing desired accuracy of GeoLocation system from set [0, 10, 100, 1000].
     * 0:  most aggressive, most accurate, worst battery drain
     * 1000:  least aggressive, least accurate, best for battery.
     */
    private Integer translateDesiredAccuracy(Integer accuracy) {
        switch (accuracy) {
            case 1000:
                accuracy = LocationRequest.PRIORITY_NO_POWER;
                break;
            case 100:
                accuracy = LocationRequest.PRIORITY_LOW_POWER;
                break;
            case 10:
                accuracy = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                break;
            case 0:
                accuracy = LocationRequest.PRIORITY_HIGH_ACCURACY;
                break;
            default:
                accuracy = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
        }
        return accuracy;
    }
     
    public static boolean isActive() {
        return gWebView != null;
    }
    
    public void onPause(boolean multitasking) {
        Log.i(TAG, "- onPause");
        if (isEnabled) {
            setPace(isMoving);
        }
    }
    public void onResume(boolean multitasking) {
        Log.i(TAG, "- onResume");
        if (isEnabled) {
            removeLocationUpdates();
        }
    }
    
    private void setPace(Boolean moving) {
        if (isMoving) {
            LocationRequest request = LocationRequest.create()
                .setPriority(translateDesiredAccuracy(desiredAccuracy))
                .setInterval(this.locationUpdateInterval)
                .setFastestInterval(30000)
                .setSmallestDisplacement(distanceFilter);
            
            requestLocationUpdates(request);
        } else {
            removeLocationUpdates();
        }
    }
    
    public void onEventMainThread(DetectedActivity probableActivity) {
        currentActivity = probableActivity;
        
        Boolean wasMoving = isMoving;
        switch (probableActivity.getType()) {
            case DetectedActivity.IN_VEHICLE:
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.ON_FOOT:
                isMoving = true;
                break;
            case DetectedActivity.STILL:
                isMoving = false;
                break;
            case DetectedActivity.UNKNOWN:
                break;
            case DetectedActivity.TILTING:
                break;
        }
        
        if (!wasMoving && isMoving) {
            startTone("doodly_doo");
            setPace(isMoving);
        } else if (wasMoving && !isMoving) {
            startTone("long_beep");
            setPace(isMoving);
        }

        String probableActivityName = getActivityName(probableActivity.getType());
        Log.w(TAG, "- DetectedActivity: " + probableActivityName + ", confidence: " + probableActivity.getConfidence());
    }
    
    public void onEventMainThread(Location location) {
        Log.i(TAG, "BUS Rx:" + location.toString());
        startTone("beep");
        try {
            JSONObject loc = new JSONObject();
            loc.put("latitude", location.getLatitude());
            loc.put("longitude", location.getLongitude());
            loc.put("accuracy", location.getAccuracy());
            loc.put("speed", location.getSpeed());
            loc.put("bearing", location.getBearing());
            loc.put("altitude", location.getAltitude());
            loc.put("timestamp", location.getTime());
            
            PluginResult result = new PluginResult(PluginResult.Status.OK, loc);
            result.setKeepCallback(true);
            if(callback != null){
                callback.sendPluginResult(result);    
            }
        } catch (JSONException e) {
            Log.e(TAG, "could not parse location");
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
    
    private void requestActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, activityDetectionInterval, locationUpdateService);
    }
    
    private void removeActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, locationUpdateService);
    }
    
    private void requestLocationUpdates(LocationRequest request) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, locationUpdateService);
    }
    
    private void removeLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationUpdateService);
    }
    
    
    
    /**
     * Plays debug sound
     * @param name
     */
    private void startTone(String name) {
        int tone = 0;
        int duration = 1000;

        if (name.equals("beep")) {
            tone = ToneGenerator.TONE_PROP_BEEP;
        } else if (name.equals("beep_beep_beep")) {
            tone = ToneGenerator.TONE_CDMA_CONFIRM;
        } else if (name.equals("long_beep")) {
            tone = ToneGenerator.TONE_CDMA_ABBR_ALERT;
        } else if (name.equals("doodly_doo")) {
            tone = ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE;
        } else if (name.equals("chirp_chirp_chirp")) {
            tone = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
        } else if (name.equals("dialtone")) {
            tone = ToneGenerator.TONE_SUP_RINGTONE;
        }
        if (isDebugging) {
            toneGenerator.startTone(tone, duration);
        }
    }
    
    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        Log.i(TAG, "- onConnected");
    }


    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Override method in CordovaPlugin.
     * Checks to see if it should turn off
     */
    public void onDestroy() {
        if(isEnabled && stopOnTerminate || !isEnabled) {
            removeActivityUpdates();
            removeLocationUpdates();
        }
    }    
}
