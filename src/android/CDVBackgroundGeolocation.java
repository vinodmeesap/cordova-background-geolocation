package com.transistorsoft.cordova.bggeo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.transistorsoft.locationmanager.BackgroundGeolocationService;
import com.transistorsoft.locationmanager.BackgroundGeolocationService.PaceChangeEvent;
import com.transistorsoft.locationmanager.BackgroundGeolocationService.PausedEvent;
//import com.transistorsoft.locationmanager.BackgroundGeolocationService.StationaryLocation;

import de.greenrobot.event.EventBus;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

public class CDVBackgroundGeolocation extends CordovaPlugin {
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
    private Boolean isMoving            = false;
    
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
        
        backgroundServiceIntent = new Intent(this.cordova.getActivity(), BackgroundGeolocationService.class);

        // Register for events fired by our IntentService "LocationService"
        EventBus.getDefault().register(this);
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute / action : " + action);

        Boolean result      = false;
        
        if (ACTION_START.equalsIgnoreCase(action) && !isEnabled) {
            result      = true;
            this.setEnabled(true);
            callbackContext.success();
        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
            result      = true;
            this.setEnabled(false);
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
    
    private void setEnabled(boolean value) {
        isEnabled = value;
        
        Activity activity = this.cordova.getActivity();
        
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enabled", isEnabled);
        editor.commit();
        
        if (isEnabled) {
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                activity.startService(backgroundServiceIntent);
            }
        } else {
            activity.stopService(backgroundServiceIntent);
        }
    }
    
    private boolean applyConfig(JSONArray data) {
        Activity activity = this.cordova.getActivity();
        
        try {
            JSONObject config = data.getJSONObject(0);
            Log.i(TAG, "- configure: " + config.toString());
            
            SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
            SharedPreferences.Editor editor = settings.edit();
            
            editor.putBoolean("activityIsActive", true);
            editor.putBoolean("isMoving", isMoving);
            
            if (config.has("distanceFilter")) {
                editor.putFloat("distanceFilter", config.getInt("distanceFilter"));
            }
            if (config.has("desiredAccuracy")) {
                editor.putInt("desiredAccuracy", config.getInt("desiredAccuracy"));
            }
            if (config.has("locationUpdateInterval")) {
                editor.putInt("locationUpdateInterval", config.getInt("locationUpdateInterval"));
            }
            if (config.has("activityRecognitionInterval")) {
                editor.putLong("activityRecognitionInterval", config.getLong("activityRecognitionInterval"));
            }
            if (config.has("stopTimeout")) {
                editor.putLong("stopTimeout", config.getLong("stopTimeout"));
            }
            if (config.has("debug")) {
                editor.putBoolean("debug", config.getBoolean("debug"));
            }
            if (config.has("stopOnTerminate")) {
                editor.putBoolean("stopOnTerminate", config.getBoolean("stopOnTerminate"));
            }
            if (config.has("startOnBoot")) {
                editor.putBoolean("startOnBoot", config.getBoolean("startOnBoot"));
            }
            if (config.has("forceReload")) {
                editor.putBoolean("forceReload", config.getBoolean("forceReload"));
            }
            if (config.has("url")) {
                editor.putString("url", config.getString("url"));
            }
            if (config.has("params")) {
                try {
                    editor.putString("params", config.getJSONObject("params").toString());
                } catch (JSONException e) {
                    Log.w(TAG, "- Failed to parse #params to JSONObject.  Ignored");
                }
            }
            if (config.has("headers")) {
                try {
                    editor.putString("headers", config.getJSONObject("headers").toString());
                } catch (JSONException e) {
                    Log.w(TAG, "- Failed to parse #headers to JSONObject.  Ignored");
                }
            }
            editor.commit();
            return true;
        } catch (JSONException e) {
            Log.w(TAG, e);
            return false;
        }
    }    

    public void onPause(boolean multitasking) {
        Log.i(TAG, "- onPause");
        if (isEnabled) {
            
        }
    }
    public void onResume(boolean multitasking) {
        Log.i(TAG, "- onResume");
        if (isEnabled) {
            
        }
    }
    
    /**
     * EventBus listener
     * @param {Location} location
     */
    public void onEventMainThread(Location location) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, BackgroundGeolocationService.locationToJson(location));
        result.setKeepCallback(true);

        if (location instanceof com.transistorsoft.locationmanager.BackgroundGeolocationService.StationaryLocation) {
            isMoving = false;
            if (stationaryCallback != null) {
                runInBackground(stationaryCallback, result);
            }
        } else {
            isMoving = true;
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
        
        Activity activity = this.cordova.getActivity();

        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("activityIsActive", false);
        editor.commit();

        if(isEnabled && stopOnTerminate) {
            this.cordova.getActivity().stopService(backgroundServiceIntent);
        }
    }    
}
