package com.transistorsoft.cordova.bggeo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * This boot receiver is meant to handle the case where device is first booted after power up.  
 * This boot the headless BackgroundGeolocationService as configured by this class.
 * @author chris scott
 *
 */
public class BootReceiver extends BroadcastReceiver {   
	private static final String TAG = "BackgroundGeolocation";
	
	/**
	 * Background Geolocation Configuration params.
	 * If you're auto-running the service on BOOT, you need to manually configure the params here since the foreground app will not have been booted.
	 */
	private float 	distanceFilter 					= 50;
	private Integer desiredAccuracy 				= 0;
	private Integer locationUpdateInterval 				= 5000;
	private Integer activityRecognitionInterval 			= 10000;
	private long 	stopTimeout 					= 0;
	private boolean debug 						= true;
	private boolean stopOnTerminate 				= false;
	private boolean forceReload 					= false;
	private String 	url 						= "http://posttestserver.com/post.php?dir=cordova-background-geolocation";
	private String 	params 						= "{'foo':'bar'}";
	private String 	headers 					= "{'X-FOO':'BAR'}";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "- BootReceiver booting service");
    	
    	Intent backgroundServiceIntent = new Intent(context, BackgroundGeolocationService.class);
    	
    	// Configure background geolocation service params.
        backgroundServiceIntent.putExtra("distanceFilter", distanceFilter);
        backgroundServiceIntent.putExtra("desiredAccuracy", desiredAccuracy);
        backgroundServiceIntent.putExtra("locationUpdateInterval", locationUpdateInterval);
        backgroundServiceIntent.putExtra("activityRecognitionInterval", activityRecognitionInterval);
        backgroundServiceIntent.putExtra("stopTimeout", stopTimeout);
        backgroundServiceIntent.putExtra("debug", debug);
        backgroundServiceIntent.putExtra("stopOnTerminate", stopOnTerminate);
        backgroundServiceIntent.putExtra("forceReload", forceReload);
        backgroundServiceIntent.putExtra("url", url);
        backgroundServiceIntent.putExtra("params", params);        
        backgroundServiceIntent.putExtra("headers", headers);
        
        // Start the service.
        context.startService(backgroundServiceIntent);
    	
    }
}
