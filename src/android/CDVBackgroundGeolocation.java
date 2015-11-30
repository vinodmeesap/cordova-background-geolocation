package com.transistorsoft.cordova.bggeo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.transistorsoft.locationmanager.BackgroundGeolocationService;
import com.transistorsoft.locationmanager.Settings;
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
import android.widget.Toast;

public class CDVBackgroundGeolocation extends CordovaPlugin {
    private static final String TAG = "TSLocationManager";

    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    public static final int REQUEST_ACTION_START = 1;
    public static final int REQUEST_ACTION_GET_CURRENT_POSITION = 2;


    private static CordovaWebView gWebView;
    public static Boolean forceReload = false;

    /**
     * Timeout in millis for a getCurrentPosition request to give up.
     * TODO make configurable.
     */
    private static final long GET_CURRENT_POSITION_TIMEOUT = 30000;

    public static final String ACTION_FINISH            = "finish";
    public static final String ACTION_ERROR             = "error";
    public static final String ACTION_CONFIGURE         = "configure";
    public static final String ACTION_SET_CONFIG        = "setConfig";
    public static final String ACTION_ADD_MOTION_CHANGE_LISTENER    = "addMotionChangeListener";
    public static final String ACTION_ON_GEOFENCE       = "onGeofence";
    public static final String ACTION_PLAY_SOUND        = "playSound";
    public static final String ACTION_ACTIVITY_RELOAD   = "activityReload";
    public static final String ACTION_GET_STATE         = "getState";

    private Boolean isEnabled           = false;
    private Boolean stopOnTerminate     = false;
    private Boolean isMoving            = false;
    private Boolean isAcquiringCurrentPosition = false;
    private long isAcquiringCurrentPositionSince;
    private Intent backgroundServiceIntent;
    private JSONObject mConfig;

    private DetectedActivity currentActivity;

    private CallbackContext startCallback;
    // Geolocation callback
    private CallbackContext locationCallback;
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

        Activity activity = this.cordova.getActivity();
        backgroundServiceIntent = new Intent(activity, BackgroundGeolocationService.class);
        SharedPreferences settings = activity.getSharedPreferences("TSLocationManager", 0);
        Settings.init(settings);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        // Register for events fired by our IntentService "LocationService"
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "execute / action : " + action);

        Boolean result      = false;

        if (BackgroundGeolocationService.ACTION_START.equalsIgnoreCase(action)) {
            result      = true;
            if (startCallback == null) {
                this.start(callbackContext);
            } else {
                result = false;
                callbackContext.error("waiting for previous start to finish");
            }
        } else if (BackgroundGeolocationService.ACTION_STOP.equalsIgnoreCase(action)) {
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
            mConfig = data.getJSONObject(0);
            result = applyConfig();
            SharedPreferences settings = this.cordova.getActivity().getSharedPreferences("TSLocationManager", 0);
            setEnabled(settings.getBoolean("enabled", isEnabled));
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
        } else if (BackgroundGeolocationService.ACTION_SET_CONFIG.equalsIgnoreCase(action)) {
            result = onSetConfig(data.getJSONObject(0));
            if (result) {
                Bundle event = new Bundle();
                event.putString("name", BackgroundGeolocationService.ACTION_SET_CONFIG);
                event.putBoolean("request", true);
                EventBus.getDefault().post(event);
                callbackContext.success();
            } else {
                callbackContext.error("- Configuration error!");
            }
        } else if (ACTION_GET_STATE.equalsIgnoreCase(action)) {
            result = true;
            JSONObject state = this.getState();
            PluginResult response = new PluginResult(PluginResult.Status.OK, state);
            response.setKeepCallback(false);
            runInBackground(callbackContext, response);
        } else if (ACTION_ADD_MOTION_CHANGE_LISTENER.equalsIgnoreCase(action)) {
            result = true;
            this.addMotionChangeListener(callbackContext);
        } else if (BackgroundGeolocationService.ACTION_GET_LOCATIONS.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            getLocationsCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (BackgroundGeolocationService.ACTION_SYNC.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            syncCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (BackgroundGeolocationService.ACTION_GET_ODOMETER.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            getOdometerCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (BackgroundGeolocationService.ACTION_RESET_ODOMETER.equalsIgnoreCase(action)) {
            result = true;
            Bundle event = new Bundle();
            event.putString("name", action);
            event.putBoolean("request", true);
            resetOdometerCallback = callbackContext;
            EventBus.getDefault().post(event);
        } else if (BackgroundGeolocationService.ACTION_ADD_GEOFENCE.equalsIgnoreCase(action)) {
            result = onAddGeofence(data.getJSONObject(0));
            if (result) {
                callbackContext.success();
            } else {
                callbackContext.error("Failed to add geofence");
            }
        } else if (BackgroundGeolocationService.ACTION_REMOVE_GEOFENCE.equalsIgnoreCase(action)) {
            result = onRemoveGeofence(data.getString(0));
            if (result) {
                callbackContext.success();
            }  else {
                callbackContext.error("Failed to add geofence");
            }
        } else if (BackgroundGeolocationService.ACTION_ON_GEOFENCE.equalsIgnoreCase(action)) {
            result = true;
            addGeofenceListener(callbackContext);
        } else if (BackgroundGeolocationService.ACTION_GET_GEOFENCES.equalsIgnoreCase(action)) {
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
        } else if (BackgroundGeolocationService.ACTION_BEGIN_BACKGROUND_TASK.equalsIgnoreCase(action)) {
            // Android doesn't do background-tasks.  This is an iOS thing.  Just return a number.
            result = true;
            callbackContext.success(1);
        } else if (BackgroundGeolocationService.ACTION_CLEAR_DATABASE.equalsIgnoreCase(action)) {
            result = clearDatabase();
            if (result) {
                callbackContext.success();
            } else {
                callbackContext.error("failed to clear database");
            }
        }
        return result;
    }

    private void start(CallbackContext callback) {
        startCallback = callback;
        if (hasPermission(ACCESS_COARSE_LOCATION) && hasPermission(ACCESS_FINE_LOCATION)) {
            setEnabled(true);
        } else {
            String[] permissions = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
            requestPermissions(REQUEST_ACTION_START, permissions);
        }
    }

    private void startService(int requestCode) {
        if (hasPermission(ACCESS_FINE_LOCATION) && hasPermission(ACCESS_COARSE_LOCATION)) {
            this.cordova.getActivity().startService(backgroundServiceIntent);
        } else {
            String[] permissions = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
            requestPermissions(requestCode, permissions);
        }
    }

    private void onGetCurrentPosition(CallbackContext callbackContext, JSONObject options) {
        isAcquiringCurrentPosition = true;
        isAcquiringCurrentPositionSince = System.nanoTime();
        addCurrentPositionListener(callbackContext);

        if (!isEnabled) {
            EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(this)) {
                eventBus.register(this);
            }
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                backgroundServiceIntent.putExtra("command", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
                startService(REQUEST_ACTION_GET_CURRENT_POSITION);
            }
        } else {
            Bundle event = new Bundle();
            event.putString("name", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
            event.putBoolean("request", true);
            event.putString("options", options.toString());
            EventBus.getDefault().post(event);
        }
    }

    private Boolean onAddGeofence(JSONObject config) {
        try {
            Bundle event = new Bundle();
            event.putString("name", BackgroundGeolocationService.ACTION_ADD_GEOFENCE);
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
            if (launchIntent.getStringExtra("name").equalsIgnoreCase(BackgroundGeolocationService.ACTION_ON_MOTION_CHANGE)) {
                Bundle event = launchIntent.getExtras();
                this.onEventMainThread(event);
            }
        }
    }

    private Boolean onRemoveGeofence(String identifier) {
        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_REMOVE_GEOFENCE);
        event.putBoolean("request", true);
        event.putString("identifier", identifier);
        EventBus.getDefault().post(event);
        return true;
    }

    private void setEnabled(boolean value) {
        // Don't set a state that we're already in.
        Log.i(TAG, "- setEnabled: " + value + ", current value: " + isEnabled);

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
            EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(this)) {
                eventBus.register(this);
            }
            if (!BackgroundGeolocationService.isInstanceCreated()) {
                activity.startService(backgroundServiceIntent);
            } else {
                Bundle event = new Bundle();
                event.putString("name", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
                event.putBoolean("request", true);
                EventBus.getDefault().post(event);
            }
        } else {
            EventBus.getDefault().unregister(this);
            activity.stopService(backgroundServiceIntent);
        }
    }
    
    private boolean onSetConfig(JSONObject config) {
        try {
            JSONObject merged = new JSONObject();
            JSONObject[] objs = new JSONObject[] { mConfig, config };
            for (JSONObject obj : objs) {
                Iterator it = obj.keys();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    merged.put(key, obj.get(key));
                }
            }
            mConfig = merged;
            return applyConfig();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean applyConfig() {
        if (mConfig.has("stopOnTerminate")) {
            try {
                stopOnTerminate = mConfig.getBoolean("stopOnTerminate");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences settings = this.cordova.getActivity().getSharedPreferences("TSLocationManager", 0);
        SharedPreferences.Editor editor = settings.edit();

        try {
            if (preferences.contains("cordova-background-geolocation-license")) {
                mConfig.put("license", preferences.getString("cordova-background-geolocation-license", null));
            }
            if (preferences.contains("cordova-background-geolocation-orderId")) {
                mConfig.put("orderId", preferences.getString("cordova-background-geolocation-orderId", null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.w(TAG, "- Failed to apply license");
        }

        editor.putString("config", mConfig.toString());
        editor.putBoolean("activityIsActive", true);
        editor.commit();

        return true;
    }

    private boolean clearDatabase() {
        Bundle event = new Bundle();
        event.putString("name", BackgroundGeolocationService.ACTION_CLEAR_DATABASE);
        event.putBoolean("request", true);
        EventBus.getDefault().post(event);
        return true;
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

        if (BackgroundGeolocationService.ACTION_START.equalsIgnoreCase(name)) {
            startCallback.success();
            startCallback = null;
        } else if (BackgroundGeolocationService.ACTION_GET_LOCATIONS.equalsIgnoreCase(name)) {
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
        } else if (BackgroundGeolocationService.ACTION_SYNC.equalsIgnoreCase(name)) {
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
        } else if (BackgroundGeolocationService.ACTION_GET_ODOMETER.equalsIgnoreCase(name)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, event.getFloat("data"));
            runInBackground(getOdometerCallback, result);
        } else if (BackgroundGeolocationService.ACTION_RESET_ODOMETER.equalsIgnoreCase(name)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            resetOdometerCallback.sendPluginResult(result);
        } else if (BackgroundGeolocationService.ACTION_CHANGE_PACE.equalsIgnoreCase(name)) {
            this.onChangePace(event);
        } else if (BackgroundGeolocationService.ACTION_GET_GEOFENCES.equalsIgnoreCase(name)) {
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
        } else if (BackgroundGeolocationService.ACTION_ON_MOTION_CHANGE.equalsIgnoreCase(name)) {
            this.onMotionChange(event);
        } else if (name.equalsIgnoreCase(BackgroundGeolocationService.ACTION_GOOGLE_PLAY_SERVICES_CONNECT_ERROR)) {
            GoogleApiAvailability.getInstance().getErrorDialog(this.cordova.getActivity(), event.getInt("errorCode"), 1001).show();
        } else if (name.equalsIgnoreCase(BackgroundGeolocationService.ACTION_LOCATION_ERROR)) {
            this.onLocationError(event);
        }
    }

    private void finishAcquiringCurrentPosition(boolean success) {
        // Current position has arrived:  release the hounds.
        isAcquiringCurrentPosition = false;
        // When currentPosition is explicitly requested while plugin is stopped, shut Service down again and stop listening to EventBus
        backgroundServiceIntent.removeExtra("command");
        if (!isEnabled) {
            EventBus.getDefault().unregister(this);
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
    private void onChangePace(Bundle event) {
        Boolean success = event.getBoolean("success");
        if (success) {
            int state = event.getBoolean("isMoving") ? 1 : 0;
            paceChangeCallback.success(state);
        } else {
            paceChangeCallback.error(event.getInt("code"));
        }
    }
    private JSONObject getState() {
        SharedPreferences settings = this.cordova.getActivity().getSharedPreferences("TSLocationManager", 0);

        Bundle values = Settings.values;
        JSONObject state = new JSONObject();
        Set<String> keys = values.keySet();

        try {
            state.put("enabled", isEnabled);
            state.put("isMoving", isMoving);
            for (String key : keys) {
                state.put(key, values.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
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
            JSONObject params = BackgroundGeolocationService.geofencingEventToJson(geofenceEvent, currentActivity, isMoving);
            handleGeofencingEvent(params);
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

    private Boolean isDebugging() {
        SharedPreferences settings = this.cordova.getActivity().getSharedPreferences("TSLocationManager", 0);
        return settings.contains("debug") && settings.getBoolean("debug", false);
    }

    private void onError(String error) {
        String message = "BG Geolocation caught a Javascript exception while running in background-thread:\n".concat(error);
        Log.e(TAG, message);

        // Show alert popup with js error
        if (isDebugging()) {
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

    private void onLocationError(Bundle event) {
        Integer code = event.getInt("code");
        if (code == BackgroundGeolocationService.LOCATION_ERROR_DENIED) {
            if (isDebugging()) {
                Toast.makeText(this.cordova.getActivity(), "Location services disabled!", Toast.LENGTH_SHORT).show();
            }
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, code);
        result.setKeepCallback(true);

        runInBackground(locationCallback, result);

        if (isAcquiringCurrentPosition) {
            finishAcquiringCurrentPosition(true);
            for (CallbackContext callback : currentPositionCallbacks) {
                result = new PluginResult(PluginResult.Status.ERROR, code);
                result.setKeepCallback(false);
                runInBackground(callback, result);
            }
            currentPositionCallbacks.clear();
        }
    }

    private boolean hasPermission(String action) {
        try {
            Method methodToFind = cordova.getClass().getMethod("hasPermission", String.class);
            if (methodToFind != null) {
                try {
                    return (Boolean) methodToFind.invoke(cordova, action);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch(NoSuchMethodException e) {
            // Probably SDK < 23 (MARSHMALLOW implmements fine-grained, user-controlled permissions).
            return true;
        }
        return true;
    }

    private void requestPermissions(int requestCode, String[] action) {
        try {
            Method methodToFind = cordova.getClass().getMethod("requestPermissions", CordovaPlugin.class, int.class, String[].class);
            if (methodToFind != null) {
                try {
                    methodToFind.invoke(cordova, this, requestCode, action);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r:grantResults) {
            if(r == PackageManager.PERMISSION_DENIED) {
                int errorCode = BackgroundGeolocationService.LOCATION_ERROR_DENIED;
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, errorCode);
                if (requestCode == REQUEST_ACTION_START) {
                    startCallback.sendPluginResult(result);
                    startCallback = null;
                } else if (requestCode == REQUEST_ACTION_GET_CURRENT_POSITION) {
                    Bundle event = new Bundle();
                    event.putString("name", BackgroundGeolocationService.ACTION_GET_CURRENT_POSITION);
                    event.putInt("code", errorCode);
                    onLocationError(event);
                }
                return;
            }
        }
        switch(requestCode)
        {
            case REQUEST_ACTION_START:
                setEnabled(true);
                break;
            case REQUEST_ACTION_GET_CURRENT_POSITION:
                startService(requestCode);
                break;
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
