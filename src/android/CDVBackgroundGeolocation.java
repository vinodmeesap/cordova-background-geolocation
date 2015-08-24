package com.transistorsoft.cordova.bggeo;

import java.util.List;
import java.util.ArrayList;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.os.Bundle;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.transistorsoft.locationmanager.BackgroundGeolocationService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import android.app.AlertDialog;
import android.content.DialogInterface;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.media.AudioManager;
import android.media.ToneGenerator;

public class CDVBackgroundGeolocation extends CordovaPlugin {
    private static final String TAG = "TSLocationManager";
    private static CordovaWebView gWebView;
    public static Boolean forceReload = false;

    /**
    * Timeout in millis for a getCurrentPosition request to give up.
    * TODO make configurable.
    */
    private static final long GET_CURRENT_POSITION_TIMEOUT = 30000;
    
    public static final String ACTION_START             = "start";
    public static final String ACTION_STOP              = "stop";
    public static final String ACTION_FINISH            = "finish";
    public static final String ACTION_ERROR             = "error";
    public static final String ACTION_CHANGE_PACE       = "changePace";
    public static final String ACTION_CONFIGURE         = "configure";
    public static final String ACTION_SET_CONFIG        = "setConfig";
    public static final String ACTION_ON_STATIONARY     = "addStationaryRegionListener";
    public static final String ACTION_ADD_MOTION_CHANGE_LISTENER    = "addMotionChangeListener";
    public static final String ACTION_ON_MOTION_CHANGE    = "onMotionChange";
    public static final String ACTION_GET_LOCATIONS     = "getLocations";
    public static final String ACTION_SYNC              = "sync";
    public static final String ACTION_GET_ODOMETER      = "getOdometer";
    public static final String ACTION_RESET_ODOMETER    = "resetOdometer";
    public static final String ACTION_ADD_GEOFENCE      = "addGeofence";
    public static final String ACTION_REMOVE_GEOFENCE   = "removeGeofence";
    public static final String ACTION_GET_GEOFENCES     = "getGeofences";
    public static final String ACTION_ON_GEOFENCE       = "onGeofence";
    public static final String ACTION_PLAY_SOUND        = "playSound";
    public static final String ACTION_ACTIVITY_RELOAD   = "activityReload";
    
    private Boolean isEnabled           = false;
    private Boolean stopOnTerminate     = false;
    private Boolean isMoving            = false;
    private Boolean isAcquiringCurrentPosition = false;
    private long isAcquiringCurrentPositionSince;
    private Intent backgroundServiceIntent;
    
    private DetectedActivity currentActivity;

    // Geolocation callback
    private CallbackContext locationCallback;
    private CallbackContext stationaryCallback;
    private CallbackContext getLocationsCallback;
    private CallbackContext syncCallback;
    private CallbackContext getOdometerCallback;
    private CallbackContext resetOdometerCallback;
    private CallbackContext paceChangeCallback;
    private CallbackContext getGeofencesCallback;

    private ToneGenerator toneGenerator;

    private List<CallbackContext> motionChangeCallbacks = new ArrayList<CallbackContext>();
    private List<CallbackContext> geofenceCallbacks = new ArrayList<CallbackContext>();
    private List<CallbackContext> currentPositionCallbacks = new ArrayList<CallbackContext>();

    public static boolean isActive() {
        return gWebView != null;
    }
    
    @Override
    protected void pluginInitialize() {        
        gWebView = this.webView;
        
        backgroundServiceIntent = new Intent(this.cordova.getActivity(), BackgroundGeolocationService.class);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        // Register for events fired by our IntentService "LocationService"
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute / action : " + action);

        Boolean result      = false;
        
        if (ACTION_START.equalsIgnoreCase(action)) {
            result      = true;
            this.setEnabled(true);
            callbackContext.success(1);
        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
            // No implementation to stop background-tasks with Android.  Just say "success"
            result      = true;
            this.setEnabled(false);
            callbackContext.success(0);
        } else if (ACTION_FINISH.equalsIgnoreCase(action)) {
            result = true;
            callbackContext.success();
        } else if (ACTION_ERROR.equalsIgnoreCase(action)) {
            result = true;
            this.onError(data.getString(1));
            callbackContext.success();
        } else if (ACTION_CONFIGURE.equalsIgnoreCase(action)) {
            result = applyConfig(data);
            if (result) {
                this.locationCallback = callbackContext;
            } else {
                callbackContext.error("- Configuration error!");
            }
        } else if (BackgroundGeolocationService.ACTION_CHANGE_PACE.equalsIgnoreCase(action)) {
            if (!isEnabled) {
                Log.w(TAG, "- Cannot change pace while disabled");
                result = false;
                callbackContext.error("Cannot #changePace while disabled");
            } else {
                result = true;
                isMoving = data.getBoolean(0);
                paceChangeCallback = callbackContext; 
                Bundle event = new Bundle();
                event.putString("name", action);
                event.putBoolean("request", true);
                event.putBoolean("isMoving", isMoving);
                EventBus.getDefault().post(event);
            }
        } else if (ACTION_SET_CONFIG.equalsIgnoreCase(action)) {
            result = applyConfig(data);
            if (result) {
                Bundle event = new Bundle();
                event.putString("name", action);
                event.putBoolean("request", true);
                EventBus.getDefault().post(event);
                callbackContext.success();
            } else {
                callbackContext.error("- Configuration error!");
            }
        } else if (ACTION_ON_STATIONARY.equalsIgnoreCase(action)) {
            result = true;
            this.stationaryCallback = callbackContext;  
        } else if (ACTION_ADD_MOTION_CHANGE_LISTENER.equalsIgnoreCase(action)) {
            result = true;
            this.addMotionChangeListener(callbackContext);
        } else if (ACTION_GET_LOCATIONS.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            getLocationsCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (ACTION_SYNC.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            syncCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (ACTION_GET_ODOMETER.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            getOdometerCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (ACTION_RESET_ODOMETER.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            resetOdometerCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (ACTION_ADD_GEOFENCE.equalsIgnoreCase(action)) {
            result = onAddGeofence(data.getJSONObject(0));
            if (result) {
                callbackContext.success();
            } else {
                callbackContext.error("Failed to add geofence");
            }
        } else if (ACTION_REMOVE_GEOFENCE.equalsIgnoreCase(action)) {
            result = onRemoveGeofence(data.getString(0));
            if (result) {
                callbackContext.success();
            }  else {
                callbackContext.error("Failed to add geofence");
            }
        } else if (ACTION_ON_GEOFENCE.equalsIgnoreCase(action)) {
            result = true;
            addGeofenceListener(callbackContext);
        } else if (ACTION_GET_GEOFENCES.equalsIgnoreCase(action)) {
            result = true;
            getGeofencesCallback = callbackContext;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            EventBus.getDefault().post(event);
        } else if (ACTION_PLAY_SOUND.equalsIgnoreCase(action)) {
            result = true;
            playSound(data.getInt(0));
            callbackContext.success();
        } else if (BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION.equalsIgnoreCase(action)) {
            result = true;
            JSONObject options = data.getJSONObject(0);
            onGetCurrentPosition(callbackContext, options);
        }
        return result;
    }

    private void onGetCurrentPosition(CallbackContext callbackContext, JSONObject options) {
        isAcquiringCurrentPosition = true;
        isAcquiringCurrentPositionSince = System.nanoTime();
        addCurrentPositionListener(callbackContext);

        if (!isEnabled) {
            EventBus.getDefault().register(this);
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                backgroundServiceIntent.putExtra("command", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
                this.cordova.getActivity().startService(backgroundServiceIntent);
            }
        } else {
            Bundle event = new Bundle();
            event.putString("name", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
            event.putBoolean("request", true);
            if (options.has("timeout")) {
                try {
                    event.putInt("timeout", options.getInt("timeout"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            EventBus.getDefault().post(event);
        }
    }
    private Boolean onAddGeofence(JSONObject config) {
        try {
            Bundle event = new Bundle();
            event.putString("name", ACTION_ADD_GEOFENCE);
            event.putBoolean("request", true);
            event.putFloat("radius", (float) config.getLong("radius"));
            event.putDouble("latitude", config.getDouble("latitude"));
            event.putDouble("longitude", config.getDouble("longitude"));
            event.putString("identifier", config.getString("identifier"));
            if (config.has("notifyOnEntry")) {
                event.putBoolean("notifyOnEntry", config.getBoolean("notifyOnEntry"));
            }
            if (config.has("notifyOnExit")) {
                event.putBoolean("notifyOnExit", config.getBoolean("notifyOnExit"));
            }
            EventBus.getDefault().post(event);
            return true;
        } catch (JSONException e) {
            Log.w(TAG, e);
            return false;
        }
    }

    private void addGeofenceListener(CallbackContext callbackContext) {
        geofenceCallbacks.add(callbackContext);

        Activity activity   = this.cordova.getActivity();
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra("forceReload") && launchIntent.hasExtra("geofencingEvent")) {
            try {
                JSONObject geofencingEvent  = new JSONObject(launchIntent.getStringExtra("geofencingEvent"));
                handleGeofencingEvent(geofencingEvent);
            } catch (JSONException e) {
                Log.w(TAG, e);
            }
        }
    }
    private void addCurrentPositionListener(CallbackContext callbackContext) {
        currentPositionCallbacks.add(callbackContext);
    }
    private void addMotionChangeListener(CallbackContext callbackContext) {
        motionChangeCallbacks.add(callbackContext);

        Activity activity = this.cordova.getActivity();
        Intent launchIntent = activity.getIntent();

        if (launchIntent.hasExtra("forceReload")) {
            if (launchIntent.getStringExtra("name").equalsIgnoreCase(ACTION_ON_MOTION_CHANGE)) {
                Bundle event = launchIntent.getExtras();
                this.onEventMainThread(event);
            }
        }
    }
    private Boolean onRemoveGeofence(String identifier) {
        Bundle event = new Bundle();
        event.putString("name", ACTION_REMOVE_GEOFENCE);
        event.putBoolean("request", true);
        event.putString("identifier", identifier);
        EventBus.getDefault().post(event);
        return true;
    }

    private void setEnabled(boolean value) {
        // Don't set a state that we're already in.
        if (value == isEnabled) {
            return;
        }
        isEnabled = value;
        
        Intent launchIntent = this.cordova.getActivity().getIntent();
        if (launchIntent.hasExtra("forceReload") && launchIntent.hasExtra("location")) {
            try {
                JSONObject location = new JSONObject(launchIntent.getStringExtra("location"));
                onLocationChange(location);
                launchIntent.removeExtra("forceReload");
                launchIntent.removeExtra("location");
            } catch (JSONException e) {
                Log.w(TAG, e);
            }
            
        }
        
        Activity activity = this.cordova.getActivity();
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enabled", isEnabled);
        editor.commit();
        
        if (isEnabled) {
            EventBus.getDefault().register(this);
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                activity.startService(backgroundServiceIntent);
            }
        } else {
            EventBus.getDefault().unregister(this);
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
            
            if (config.has("distanceFilter")) {
                editor.putFloat("distanceFilter", config.getInt("distanceFilter"));
            }
            if (config.has("desiredAccuracy")) {
                editor.putInt("desiredAccuracy", config.getInt("desiredAccuracy"));
            }
            if (config.has("locationUpdateInterval")) {
                editor.putInt("locationUpdateInterval", config.getInt("locationUpdateInterval"));
            }
            if (config.has("fastestLocationUpdateInterval")) {
                editor.putInt("fastestLocationUpdateInterval", config.getInt("fastestLocationUpdateInterval"));
            }
            if (config.has("activityRecognitionInterval")) {
                editor.putLong("activityRecognitionInterval", config.getLong("activityRecognitionInterval"));
            }
            if (config.has("minimumActivityRecognitionConfidence")) {
                editor.putInt("minimumActivityRecognitionConfidence", config.getInt("minimumActivityRecognitionConfidence"));
            }
            if (config.has("triggerActivities")) {
                editor.putString("triggerActivities", config.getString("triggerActivities"));
            }
            if (config.has("stopTimeout")) {
                editor.putLong("stopTimeout", config.getLong("stopTimeout"));
            }
            if (config.has("debug")) {
                editor.putBoolean("debug", config.getBoolean("debug"));
            }
            if (config.has("stopAfterElapsedMinutes")) {
                editor.putInt("stopAfterElapsedMinutes", config.getInt("stopAfterElapsedMinutes"));
            }
            if (config.has("stopOnTerminate")) {
                stopOnTerminate = config.getBoolean("stopOnTerminate");
                editor.putBoolean("stopOnTerminate", stopOnTerminate);
            }
            if (config.has("startOnBoot")) {
                editor.putBoolean("startOnBoot", config.getBoolean("startOnBoot"));
            }
            if (config.has("forceReloadOnLocationChange")) {
                editor.putBoolean("forceReloadOnLocationChange", config.getBoolean("forceReloadOnLocationChange"));
            }
            if (config.has("forceReload")) { // @deprecated, alias to #forceReloadOnLocationChange
                editor.putBoolean("forceReloadOnLocationChange", config.getBoolean("forceReload"));
            }
            if (config.has("forceReloadOnMotionChange")) {
                editor.putBoolean("forceReloadOnMotionChange", config.getBoolean("forceReloadOnMotionChange"));
            }
            if (config.has("forceReloadOnGeofence")) {
                editor.putBoolean("forceReloadOnGeofence", config.getBoolean("forceReloadOnGeofence"));
            }
            if (config.has("url")) {
                editor.putString("url", config.getString("url"));
            }
            if (config.has("method")) {
                editor.putString("method", config.getString("method"));
            }
            if (config.has("autoSync")) {
                editor.putBoolean("autoSync", config.getBoolean("autoSync"));
            }
            if (config.has("batchSync")) {
                editor.putBoolean("batchSync", config.getBoolean("batchSync"));
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
     * EventBus listener for Event Bundle
     * @param {Bundle} event
     */
    @Subscribe
    public void onEventMainThread(Bundle event) {
        if (event.containsKey("request")) {
            return;
        }
        String name = event.getString("name");

        if (ACTION_GET_LOCATIONS.equalsIgnoreCase(name)) {
            try {
                JSONObject params = new JSONObject();
                params.put("locations", new JSONArray(event.getString("data")));
                params.put("taskId", "android-bg-task-id");
                PluginResult result = new PluginResult(PluginResult.Status.OK, params);
                runInBackground(getLocationsCallback, result);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                PluginResult result = new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());
                runInBackground(getLocationsCallback, result);
            }
        } else if (ACTION_SYNC.equalsIgnoreCase(name)) {
            Boolean success = event.getBoolean("success");
            if (success) {
                try {
                    JSONObject params       = new JSONObject();
                    params.put("locations", new JSONArray(event.getString("data")));
                    params.put("taskId", "android-bg-task-id");
                    PluginResult result = new PluginResult(PluginResult.Status.OK, params);
                    runInBackground(syncCallback, result);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                PluginResult result = new PluginResult(PluginResult.Status.IO_EXCEPTION, event.getString("message"));
                runInBackground(syncCallback, result);
            }
        } else if (ACTION_GET_ODOMETER.equalsIgnoreCase(name)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, event.getFloat("data"));
            runInBackground(getOdometerCallback, result);
        } else if (ACTION_RESET_ODOMETER.equalsIgnoreCase(name)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            resetOdometerCallback.sendPluginResult(result);
        } else if (BackgroundGeolocationService.ACTION_CHANGE_PACE.equalsIgnoreCase(name)) {
            //PluginResult result = new PluginResult(PluginResult.Status.OK);
            //paceChangeCallback.sendPluginResult(result);
            int state = event.getBoolean("isMoving") ? 1 : 0;
            paceChangeCallback.success(state);
        } else if (ACTION_GET_GEOFENCES.equalsIgnoreCase(name)) {
            try {
                JSONArray json      = new JSONArray(event.getString("data"));
                PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                runInBackground(getGeofencesCallback, result);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                PluginResult result = new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());
                runInBackground(getGeofencesCallback, result);
            }
        } else if (ACTION_ON_MOTION_CHANGE.equalsIgnoreCase(name)) {
            this.onMotionChange(event);
        } else if (name.equalsIgnoreCase(BackgroundGeolocationService.ACTION_GOOGLE_PLAY_SERVICES_CONNECT_ERROR)) {
            GooglePlayServicesUtil.getErrorDialog(event.getInt("errorCode"), this.cordova.getActivity(), 1001).show();
        } else if (name.equalsIgnoreCase(BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION)) {
            this.onCurrentPositionTimeout(event);
        }
    }

    private void onCurrentPositionTimeout(Bundle event) {
        this.finishAcquiringCurrentPosition(false);
        // Execute callbacks.
        for (CallbackContext callback : currentPositionCallbacks) {
            Log.i(TAG, "- send callback");
            
            PluginResult result = new PluginResult(PluginResult.Status.ERROR);
            result.setKeepCallback(false);
            runInBackground(callback, result);
        }
        currentPositionCallbacks.clear();

    }
    private void finishAcquiringCurrentPosition(boolean success) {
        // Current position has arrived:  release the hounds.
        isAcquiringCurrentPosition = false;
        // When currentPosition is explicitly requested while plugin is stopped, shut Service down again and stop listening to EventBus
        if (!isEnabled) {
            backgroundServiceIntent.removeExtra("command");
            EventBus.getDefault().unregister(this);
            this.cordova.getActivity().stopService(backgroundServiceIntent);
        }
    }
    private void onMotionChange(Bundle event) {
        PluginResult result;
        isMoving = event.getBoolean("isMoving");
        try {
            JSONObject params = new JSONObject();
            params.put("location", new JSONObject(event.getString("location")));
            params.put("isMoving", isMoving);
            params.put("taskId", "android-bg-task-id");
            result = new PluginResult(PluginResult.Status.OK, params);
        } catch (JSONException e) {
            e.printStackTrace();
            result = new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());
        }
        result.setKeepCallback(true);
        for (CallbackContext callback : motionChangeCallbacks) {
            runInBackground(callback, result);
        }
    }
    /**
     * EventBus listener for ARS
     * @param {ActivityRecognitionResult} result
     */
    @Subscribe
    public void onEventMainThread(ActivityRecognitionResult result) {
        currentActivity = result.getMostProbableActivity();
        
        if (isAcquiringCurrentPosition) {
            long elapsedMillis = (System.nanoTime() - isAcquiringCurrentPositionSince) / 1000000;
            if (elapsedMillis > GET_CURRENT_POSITION_TIMEOUT) {
                isAcquiringCurrentPosition = false;
                Log.i(TAG, "- getCurrentPosition timeout, giving up");
                for (CallbackContext callback : currentPositionCallbacks) {
                    callback.error(408); // aka HTTP 408 Request Timeout
                }
                currentPositionCallbacks.clear();
            }
        }
    }
    /**
     * EventBus listener
     * @param {Location} location
     */
    @Subscribe
    public void onEventMainThread(Location location) {
        JSONObject locationData = BackgroundGeolocationService.locationToJson(location, currentActivity, isMoving);
        this.onLocationChange(locationData);
    }
    private void onLocationChange(JSONObject location) {
        Log.i(TAG, "- CDVBackgroundGeolocation Rx Location: " + isEnabled);

        PluginResult result = new PluginResult(PluginResult.Status.OK, location);
        result.setKeepCallback(true);

        runInBackground(locationCallback, result);

        if (isAcquiringCurrentPosition) {
            finishAcquiringCurrentPosition(true);
            // Execute callbacks.
            for (CallbackContext callback : currentPositionCallbacks) {
                result = new PluginResult(PluginResult.Status.OK, location);
                result.setKeepCallback(false);
                runInBackground(callback, result);
            }
            currentPositionCallbacks.clear();
        }
    }
    /**
    * EventBus handler for Geofencing events
    */
    @Subscribe
    public void onEventMainThread(GeofencingEvent geofenceEvent) {
        Log.i(TAG, "- Rx GeofencingEvent: " + geofenceEvent);

        if (!geofenceCallbacks.isEmpty()) {
            for (Geofence geofence : geofenceEvent.getTriggeringGeofences()) {
                JSONObject params = BackgroundGeolocationService.geofencingEventToJson(geofenceEvent, geofence);
                handleGeofencingEvent(params);
            }
        }
    }
    private void handleGeofencingEvent(JSONObject params) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, params);
        result.setKeepCallback(true);
        for (CallbackContext callback : geofenceCallbacks) {
            runInBackground(callback, result);
        }
    }

    private void playSound(int soundId) {
        int duration = 1000;
        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGenerator.startTone(soundId, duration);
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

    private void onError(String error) {        
        String message = "BG Geolocation caught a Javascript exception while running in background-thread:\n".concat(error);
        Log.e(TAG, message);
        
        SharedPreferences settings = this.cordova.getActivity().getSharedPreferences("TSLocationManager", 0);

        // Show alert popup with js error
        if (settings.contains("debug") && settings.getBoolean("debug", false)) {
            playSound(68);
            AlertDialog.Builder builder = new AlertDialog.Builder(this.cordova.getActivity());
            builder.setMessage(message)
            .setCancelable(false)
            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //do things
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
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

        EventBus.getDefault().unregister(this);

        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("activityIsActive", false);
        editor.commit();

        if(isEnabled && stopOnTerminate) {
            this.cordova.getActivity().stopService(backgroundServiceIntent);
        }
    }    
}
