package com.transistorsoft.cordova.bggeo;

import java.util.concurrent.TimeUnit;
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
import com.google.android.gms.location.ActivityRecognitionResult;
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
    public static final String ACTION_ON_PACE_CHANGE = "onPaceChange";
    public static final String ACTION_CONFIGURE = "configure";
    public static final String ACTION_SET_CONFIG = "setConfig";
    public static final String ACTION_ON_STATIONARY = "addStationaryRegionListener";
    
    private PendingIntent locationUpdateService;

    private Boolean isEnabled       = false;
    private Boolean isMoving        = false;
    
    // Common config
    private Integer desiredAccuracy     = 10;
    private Float distanceFilter        = (float) 50;
    private Boolean isDebugging         = false;
    private Boolean stopOnTerminate     = false;

    // Android-only config
    private Integer locationUpdateInterval          = 60000;
    private Integer activityRecognitionInterval     = 60000;
    /**
    * @config {Integer} stopTimeout The time to wait after ARS STILL to turn of GPS
    */
    private long stopTimeout                     = 0;
    
    // The elapsed millis when the ARS detected STILL
    private long stoppedAt                          = 0;
    
    // Geolocation callback
    private CallbackContext locationCallback;
    
    // Called when DetectedActivity is STILL
    private CallbackContext stationaryCallback;
    private Location stationaryLocation;
   
    private GoogleApiClient googleApiClient;    
    private DetectedActivity currentActivity;
    
    private static CordovaWebView gWebView;    
    private ToneGenerator toneGenerator;
    
    @Override
    protected void pluginInitialize() {        
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
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute / action : " + action);

        Boolean result = false;
        
        if (ACTION_START.equalsIgnoreCase(action) && !isEnabled) {
            result      = true;
            isEnabled   = true;

            if (googleApiClient.isConnected()) {
                requestActivityUpdates();
            }

        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
            result      = true;
            isEnabled   = false;
            isMoving    = false;
            
            removeLocationUpdates();
            removeActivityUpdates();
            callbackContext.success();
        } else if (ACTION_CONFIGURE.equalsIgnoreCase(action)) {
            result = applyConfig(data);
            if (result) {
                this.locationCallback = callbackContext;
                if (isDebugging) {
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                }
            } else {
                callbackContext.error("- Configuration error!");
            }
        } else if (ACTION_ON_PACE_CHANGE.equalsIgnoreCase(action)) {
            if (!isEnabled) {
                Log.w(TAG, "- Cannot change pace while in #stop mode");
                result = false;
                callbackContext.error("Cannot #changePace while in #stop mode");
            } else {
                result = true;
                isMoving = data.getBoolean(0);
                callbackContext.success();
            }
        } else if (ACTION_SET_CONFIG.equalsIgnoreCase(action)) {
            result = applyConfig(data);
            // TODO reconfigure Service
            if (result) {
                callbackContext.success();
            } else {
                callbackContext.error("- Configuration error!");
            }
        } else if (ACTION_ON_STATIONARY.equalsIgnoreCase(action)) {
            result = true;
            this.stationaryCallback = callbackContext;  
        }

        return result;
    }
    private boolean applyConfig(JSONArray data) {
        try {
            JSONObject config = data.getJSONObject(0);
            Log.i(TAG, "- configure: " + config.toString());
            
            if (config.has("distanceFilter")) {
                distanceFilter = (float) config.getInt("distanceFilter");
            }
            if (config.has("desiredAccuracy")) {
                desiredAccuracy = config.getInt("desiredAccuracy");
            }
            if (config.has("locationUpdateInterval")) {
                locationUpdateInterval = config.getInt("locationUpdateInterval");
            }
            if (config.has("activityRecognitionInterval")) {
                activityRecognitionInterval = config.getInt("activityRecognitionInterval");
            }
            if (config.has("stopTimeout")) {
                stopTimeout = config.getLong("stopTimeout");
            }
            if (config.has("debug")) {
                isDebugging = config.getBoolean("debug");
            }
            if (config.has("stopOnTerminate")) {
                stopOnTerminate = config.getBoolean("stopOnTerminate");
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
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
        Log.i(TAG, "- setPace: " + moving);
        boolean wasMoving = isMoving;
        isMoving = moving;
        if (moving && isEnabled) {
            if (!wasMoving) {
                startTone("doodly_doo");
            }
            stationaryLocation = null;
            
            // Here's where the FusedLocationProvider is controlled.
            LocationRequest request = LocationRequest.create()
                .setPriority(translateDesiredAccuracy(desiredAccuracy))
                .setInterval(this.locationUpdateInterval)
                .setFastestInterval(30000)
                .setSmallestDisplacement(distanceFilter);
            
            requestLocationUpdates(request);
        } else {
            removeLocationUpdates();
            if (stationaryLocation == null) {
                startTone("long_beep");
                
                // Re-set our stationaryLocation
                stationaryLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                
                // Inform Javascript of our stationaryLocation
                fireStationaryListener();
            }
        }
    }
    
    public void onEventMainThread(ActivityRecognitionResult result) {
        currentActivity = result.getMostProbableActivity();
        String probableActivityName = getActivityName(currentActivity.getType());
        Log.w(TAG, "- DetectedActivity: " + probableActivityName + ", confidence: " + currentActivity.getConfidence());
        
        boolean wasMoving = isMoving;
        boolean nowMoving = false;
       
        switch (currentActivity.getType()) {
            case DetectedActivity.IN_VEHICLE:
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.RUNNING:
            case DetectedActivity.WALKING:
                nowMoving = true;
                break;
            case DetectedActivity.STILL:
                nowMoving = false;
                break;
            case DetectedActivity.UNKNOWN:
            case DetectedActivity.TILTING:
                nowMoving = isMoving;
                return;
        }
        
        boolean startedMoving   = !wasMoving && nowMoving;
        boolean justStopped     = wasMoving && !nowMoving;
        boolean initialState    = !nowMoving && (stationaryLocation == null);
        
        // If we're using a stopTimeout, record the current activity's timestamp.
        if (justStopped && stopTimeout > 0 && stoppedAt == 0) {
            stoppedAt = result.getElapsedRealtimeMillis();
            return;
        }
        
        // If we're using a stopTimeout, compare the current activity's timestamp with the 1st recorded STILL event.
        if (!nowMoving && stoppedAt > 0) {
            long elapsedMillis = result.getElapsedRealtimeMillis() - stoppedAt;
            long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
            Log.i(TAG, "- Waiting for stopTimeout (" + stopTimeout + " min): elapsed min: " + elapsedMinutes);
            if (elapsedMinutes == stopTimeout) {
                justStopped = true;
            } else {
                return;
            }
        }
        stoppedAt = 0;
        if ( startedMoving || justStopped || initialState ) {
            setPace(nowMoving);
        }
    }
        
    public void onEventMainThread(Location location) {
        Log.i(TAG, "BUS Rx:" + location.toString());
        startTone("beep");

        PluginResult result = new PluginResult(PluginResult.Status.OK, locationToJson(location));
        result.setKeepCallback(true);
        runInBackground(locationCallback, result);
    }
    
    /**
     * Execute onStationary javascript callback when device is determined to have just stopped
     */
    private void fireStationaryListener() {
        Log.i(TAG, "- fire stationary listener");
        if ( (stationaryCallback != null)  && (stationaryLocation != null) ) {
            final PluginResult result = new PluginResult(PluginResult.Status.OK, locationToJson(stationaryLocation));
            result.setKeepCallback(true);
            runInBackground(stationaryCallback, result);
        }
    }
    
    /**
     * Convert a Location instance to JSONObject
     * @param Location
     * @return JSONObject
     */
    private JSONObject locationToJson(Location l) {
        try {
            JSONObject data = new JSONObject();
            data.put("latitude", l.getLatitude());
            data.put("longitude", l.getLongitude());
            data.put("accuracy", l.getAccuracy());
            data.put("speed", l.getSpeed());
            data.put("bearing", l.getBearing());
            data.put("altitude", l.getAltitude());
            data.put("timestamp", l.getTime());
            return data;
        } catch (JSONException e) {
            Log.e(TAG, "could not parse location");
            return null;
        }
    }
    
    /**
     * Run a javascript callback in Background
     * @param cb
     * @param result
     */
    private void runInBackground(final CallbackContext cb, final PluginResult result) {
        if(cb != null){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    cb.sendPluginResult(result);
                }
            });
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
    
    private void requestActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, activityRecognitionInterval, locationUpdateService);
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
        Log.i(TAG, "- onConnectionFailed");
        
    }
    
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        Log.i(TAG, "- onConnected");
        if (isEnabled) {
            requestActivityUpdates();
        }
    }


    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Override method in CordovaPlugin.
     * Checks to see if it should turn off
     */
    public void onDestroy() {
        Log.i(TAG, "- onDestroy");
        Log.i(TAG, "  stopOnTerminate: " + stopOnTerminate);
        Log.i(TAG, "  isEnabled: " + isEnabled);
        
        if(isEnabled && stopOnTerminate || !isEnabled) {
            removeActivityUpdates();
            removeLocationUpdates();
        }
    }    
}
