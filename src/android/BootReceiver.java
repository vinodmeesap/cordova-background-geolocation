package com.transistorsoft.locationmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
/**
 * This boot receiver is meant to handle the case where device is first booted after power up.  
 * This boot the headless BackgroundGeolocationService as configured by this class.
 * @author chris scott
 *
 */
public class BootReceiver extends BroadcastReceiver {   
    private static final String TAG = "TSLocationManager";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences(TAG, 0);
        Settings.init(context.getSharedPreferences(TAG, 0));
        Settings.load();

        boolean startOnBoot     = Settings.values.getBoolean("startOnBoot", false);
        boolean enabled         = preferences.getBoolean("enabled", false);

        if (!startOnBoot || !enabled) {
            return;
        }
        Log.i(TAG, "- BootReceiver booting service");
        // Start the service.
        context.startService(new Intent(context, BackgroundGeolocationService.class));
    }
}

