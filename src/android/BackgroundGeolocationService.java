package com.transistorsoft.cordova.bggeo;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import de.greenrobot.event.EventBus;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class BackgroundGeolocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "BackgroundGeolocation";
    
    private static BackgroundGeolocationService instance = null;
    
    public static boolean isInstanceCreated() {
       return instance != null;
    }
    
    private GoogleApiClient googleApiClient;
    private WakeLock wakeLock;
    private ToneGenerator toneGenerator;
    
    private PendingIntent activityRecognitionPI;
    private PendingIntent locationUpdatePI;
    private LocationRequest locationRequest;
    
    // Common config
    /**
     * @config {Integer} desiredAccuracy
     */
    private Integer desiredAccuracy     = 10;
    /**
     * @config {Float} distanceFilter
     */
    private Float distanceFilter        = (float) 50;
    /**
     * @config {Boolean} isDebugging
     */
    private Boolean isDebugging         = false;
    /**
     * @config {Boolean} stopOnTerminate
     */
    private Boolean stopOnTerminate     = false;
    
    // Android-only config
    /**
     * @config {Integer} locationUpdateInterval (ms)
     */
    private Integer locationUpdateInterval          = 60000;
    /**
     * @config {Integer{ activityRecognitionInterval (ms)
     */
    private Integer activityRecognitionInterval     = 60000;
    /*
     * @config {Boolean} forceReload Whether to reboot the Android Activity when detected to have closed
     */
    private Boolean forceReload                     = false;
    /**
     * @config {Integer} stopTimeout The time to wait after ARS STILL to turn of GPS
     */
     private long stopTimeout                     = 0;
     
    // HTTP config
    /**
     * @config {String} url For sending location to your server
     */
    private String url                  = null;
    /**
     * @config {JSONObject} params For sending location to your server
     */
    private JSONObject params           = new JSONObject();
    /**
     * @config {JSONObject} headers For sending location to your server
     */
    private JSONObject headers          = null;
    
    // Flags
    private Boolean isEnabled           = false;
    private Boolean isMoving            = false;
    private long stoppedAt              = 0;
    
    private Location stationaryLocation;
    private DetectedActivity currentActivity;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "- Start BackgroundGeolocationService");
        instance = this;
        
        EventBus.getDefault().register(this);

        isEnabled = true;
        stopOnTerminate             = intent.getBooleanExtra("stopOnTerminate", true);
        isDebugging                 = intent.getBooleanExtra("debug", false);
        distanceFilter              = intent.getFloatExtra("distanceFilter", 50);
        desiredAccuracy             = intent.getIntExtra("desiredAccuracy", 10);
        locationUpdateInterval      = intent.getIntExtra("locationUpdateInterval", 30000);
        activityRecognitionInterval = intent.getIntExtra("activityRecognitionInterval", 10000);
        stopTimeout                 = intent.getLongExtra("stopTimeout", 0);
        forceReload                 = intent.getBooleanExtra("forceReload", false);

        // HTTP Configuration
        url = intent.getStringExtra("url");
        try {
            if (intent.hasExtra("params")) {
                params = new JSONObject(intent.getStringExtra("params"));
            }
            if (intent.hasExtra("headers")) {
                headers = new JSONObject(intent.getStringExtra("headers"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // For debug sounds, turn on ToneGenerator.
        if (isDebugging) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        }

        // Configure FusedLocationProvider
        locationRequest = LocationRequest.create()
            .setPriority(translateDesiredAccuracy(desiredAccuracy))
            .setInterval(this.locationUpdateInterval)
            .setFastestInterval(30000)
            .setSmallestDisplacement(distanceFilter);
        
        // Connect to google-play services.
        if (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) {
            Log.i(TAG, "- Connecting to GooglePlayServices...");

            googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
            
            googleApiClient.connect();
        } else {
            Log.e(TAG,  "- GooglePlayServices unavailable");
        }
        
        /*
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock        = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
        */
        
        return Service.START_REDELIVER_INTENT;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "- GooglePlayServices connected");
        
        Intent arsIntent = new Intent(this, ActivityRecognitionService.class);
        activityRecognitionPI = PendingIntent.getService(this, 0, arsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        Intent locationIntent = new Intent(this, LocationService.class);
        locationUpdatePI = PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         
        // Start monitoring ARS
        if (googleApiClient.isConnected()) {
            requestActivityUpdates();
        }
    }
    
    /**
     * EventBus listener for ARS
     * @param {ActivityRecognitionResult} result
     */
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
                // We're not interested in these modes.
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
            if (elapsedMinutes >= stopTimeout) {
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
        if (location instanceof StationaryLocation) {
            return;
        }
        Log.i(TAG, "BUS Rx:" + location.toString());
        startTone("beep");
        
        // Force main-activity reload (if not running) if we're detected to be moving.
        boolean isPluginActive = BackgroundGeolocationPlugin.isActive();
        
        if (!isPluginActive && forceReload) {
            forceMainActivityReload();
        }
        if (url != null) {
            if (isNetworkAvailable()) {
                schedulePostLocation(location);
            } else {
                Log.i(TAG, "- No network detected");
                // TODO no in-plugin persistence
            }
        }
    }
    
    private void setPace(Boolean moving) {
        Log.i(TAG, "- setPace: " + moving);
        boolean wasMoving   = isMoving;
        isMoving            = moving;
        
        if (moving && isEnabled) {
            if (!wasMoving) {
                startTone("doodly_doo");
            }
            stationaryLocation = null;
            requestLocationUpdates();
        } else {
            removeLocationUpdates();
            if (stationaryLocation == null) {
                startTone("long_beep");
                // set our stationaryLocation
                stationaryLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                EventBus.getDefault().post(new BackgroundGeolocationService.StationaryLocation(stationaryLocation));
            }
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
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    private void schedulePostLocation(Location location) {
        PostLocationTask task = new BackgroundGeolocationService.PostLocationTask();
        task.setLocation(location);
        
        Log.d(TAG, "beforeexecute " +  task.getStatus());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
        Log.d(TAG, "afterexecute " +  task.getStatus());
    }
    
    private boolean postLocation(Location location) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost request = new HttpPost(url);

            JSONObject data = locationToJson(location);
            
            params.put("location", data);

            Log.i(TAG, "data: " + params.toString());

            StringEntity se = new StringEntity(params.toString());
            request.setEntity(se);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            
            Iterator<String> keys = headers.keys();
            while( keys.hasNext() ){
                String key = keys.next();
                if(key != null) {
                    request.setHeader(key, (String)headers.getString(key));
                }
            }

            Log.d(TAG, "Posting to " + request.getURI().toString());
            HttpResponse response = httpClient.execute(request);
            Log.i(TAG, "Response received: " + response.getStatusLine());
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            Log.w(TAG, "Exception posting location: " + e);
            e.printStackTrace();
            return false;
        }
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
    
    private void requestActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, activityRecognitionInterval, activityRecognitionPI);
    }
    
    private void removeActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, activityRecognitionPI);
    }
    
    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationUpdatePI);
    }
    
    private void removeLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationUpdatePI);
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
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onDestroy() {
        Log.w(TAG, "--------------------------------------");
        Log.w(TAG, "- Destroy service");
        Log.w(TAG, "--------------------------------------");
        
        cleanUp();
        super.onDestroy();
    }
    private void cleanUp() {
        instance = null;
        EventBus.getDefault().unregister(this);
        removeActivityUpdates();
        removeLocationUpdates();
        stopSelf();
        wakeLock.release();
    }
    
    /**
     * Convert a Location instance to JSONObject
     * @param Location
     * @return JSONObject
     */
    public static JSONObject locationToJson(Location l) {
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
    
    class StationaryLocation extends Location {

        public StationaryLocation(Location l) {
            super(l);
        }
    }
    
    private class PostLocationTask extends AsyncTask<Object, Integer, Boolean> {
        private Location location;
        
        public void setLocation(Location l) {
            location = l;
        }
        @Override
        protected Boolean doInBackground(Object...objects) {
            Log.d(TAG, "Executing PostLocationTask#doInBackground");
            
            if (postLocation(location)) {
                location = null;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "PostLocationTask#onPostExecture");
        }
    }
    
}
