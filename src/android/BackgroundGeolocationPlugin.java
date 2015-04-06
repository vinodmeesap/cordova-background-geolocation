package com.transistorsoft.cordova.bggeo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.transistorsoft.cordova.bggeo.BackgroundGeolocationService.PaceChangeEvent;
import com.transistorsoft.cordova.bggeo.BackgroundGeolocationService.PausedEvent;
import com.transistorsoft.cordova.bggeo.BackgroundGeolocationService.StationaryLocation;

import de.greenrobot.event.EventBus;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

public class BackgroundGeolocationPlugin extends CordovaPlugin {
    private static final String TAG = "BackgroundGeolocation";
    private static CordovaWebView gWebView;
    public static Boolean forceReload = false;
    
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_ON_PACE_CHANGE = "onPaceChange";
    public static final String ACTION_CONFIGURE = "configure";
    public static final String ACTION_SET_CONFIG = "setConfig";
    public static final String ACTION_ON_STATIONARY = "addStationaryRegionListener";
    
    
    private Boolean isEnabled           = false;
    private Boolean stopOnTerminate     = false;
    
    private Intent backgroundServiceIntent;
    
    // Geolocation callback
    private CallbackContext locationCallback;
    // Called when DetectedActivity is STILL
    private CallbackContext stationaryCallback;
        
    public static boolean isActive() {
        return gWebView != null;
    }
    
    @Override
    protected void pluginInitialize() {        
        gWebView = this.webView;

        // Register for events fired by our IntentService "LocationService"
        EventBus.getDefault().register(this);
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute / action : " + action);
        
        Activity activity   = this.cordova.getActivity();
        Boolean result      = false;

        if (ACTION_START.equalsIgnoreCase(action) && !isEnabled) {
            result      = true;
            isEnabled   = true;
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                activity.startService(backgroundServiceIntent);
            }
        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
            result      = true;
            isEnabled = false;
            activity.stopService(backgroundServiceIntent);
            callbackContext.success();
        } else if (ACTION_CONFIGURE.equalsIgnoreCase(action)) {
            result = applyConfig(data);
            if (result) {
                this.locationCallback = callbackContext;
            } else {
                callbackContext.error("- Configuration error!");
            }
        } else if (ACTION_ON_PACE_CHANGE.equalsIgnoreCase(action)) {
            if (!isEnabled) {
                Log.w(TAG, "- Cannot change pace while disabled");
                result = false;
                callbackContext.error("Cannot #changePace while disabled");
            } else { 
                PaceChangeEvent event = new PaceChangeEvent(data.getBoolean(0));
                EventBus.getDefault().post(event);

                result = true;
                callbackContext.success();
            }
        } else if (ACTION_SET_CONFIG.equalsIgnoreCase(action)) {
            activity.stopService(backgroundServiceIntent);
            result = applyConfig(data);
            // TODO reconfigure Service
            if (result) {
                activity.stopService(backgroundServiceIntent);
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
        // This is the IntentService we'll provide to google-play API.
        Activity activity = this.cordova.getActivity();

        backgroundServiceIntent = new Intent(activity, BackgroundGeolocationService.class);
        
        try {
            JSONObject config = data.getJSONObject(0);
            Log.i(TAG, "- configure: " + config.toString());
            
            if (config.has("distanceFilter")) {
                backgroundServiceIntent.putExtra("distanceFilter", (float) config.getInt("distanceFilter"));
            }
            if (config.has("desiredAccuracy")) {
                backgroundServiceIntent.putExtra("desiredAccuracy", config.getInt("desiredAccuracy"));
            }
            if (config.has("locationUpdateInterval")) {
                backgroundServiceIntent.putExtra("locationUpdateInterval", config.getInt("locationUpdateInterval"));
            }
            if (config.has("activityRecognitionInterval")) {
                backgroundServiceIntent.putExtra("activityRecognitionInterval", config.getInt("activityRecognitionInterval"));
            }
            if (config.has("stopTimeout")) {
                backgroundServiceIntent.putExtra("stopTimeout", config.getLong("stopTimeout"));
            }
            if (config.has("debug")) {
                backgroundServiceIntent.putExtra("debug", config.getBoolean("debug"));
            }
            if (config.has("stopOnTerminate")) {
                stopOnTerminate = config.getBoolean("stopOnTerminate");
                backgroundServiceIntent.putExtra("stopOnTerminate", config.getBoolean("stopOnTerminate"));
            }
            if (config.has("forceReload")) {
                backgroundServiceIntent.putExtra("forceReload", config.getBoolean("forceReload"));
            }
            if (config.has("url")) {
                backgroundServiceIntent.putExtra("url", config.getString("url"));
            }
            if (config.has("params")) {
                backgroundServiceIntent.putExtra("params", config.getString("params"));
            }
            if (config.has("headers")) {
                backgroundServiceIntent.putExtra("headers", config.getString("headers"));
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }    

    public void onPause(boolean multitasking) {
        Log.i(TAG, "- onPause");
        if (isEnabled) {
            //setPace(isMoving);
            EventBus.getDefault().post(new PausedEvent(true));
        }
    }
    public void onResume(boolean multitasking) {
        Log.i(TAG, "- onResume");
        if (isEnabled) {
            EventBus.getDefault().post(new PausedEvent(false));
        }
    }
    
    /**
     * EventBus listener
     * @param {Location} location
     */
    public void onEventMainThread(Location location) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, BackgroundGeolocationService.locationToJson(location));
        result.setKeepCallback(true);
        
        if (location instanceof StationaryLocation) {
            if (stationaryCallback != null) {
                runInBackground(stationaryCallback, result);
            }
        } else {
            result.setKeepCallback(true);
            runInBackground(locationCallback, result);
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
    
    /**
     * Override method in CordovaPlugin.
     * Checks to see if it should turn off
     */
    public void onDestroy() {
        Log.i(TAG, "- onDestroy");
        Log.i(TAG, "  stopOnTerminate: " + stopOnTerminate);
        Log.i(TAG, "  isEnabled: " + isEnabled);
        
        if(isEnabled && stopOnTerminate) {
            this.cordova.getActivity().stopService(backgroundServiceIntent);
        }
    }    
}
