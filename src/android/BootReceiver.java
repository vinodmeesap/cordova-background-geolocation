package com.transistorsoft.cordova.bggeo;
import com.transistorsoft.locationmanager.*;
import com.transistorsoft.locationmanager.scheduler.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;

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
        Log.i(TAG, "BootReceiver fired");
        
        Settings.init(context.getSharedPreferences(TAG, 0));
        Settings.load();

        boolean startOnBoot     = Settings.getStartOnBoot();
        boolean enabled         = Settings.getEnabled();

        // Start scheduler service
        JSONArray schedule = Settings.getSchedule();
        if (schedule.length() > 0) {
            context.startService(new Intent(context, ScheduleService.class));
        }

        if (!startOnBoot || !enabled) {
            return;
        }

        // Start the service.
        Intent launchIntent = new Intent(context, BackgroundGeolocationService.class);
        Bundle event = new Bundle();
        event.putString("command", BackgroundGeolocationService.ACTION_START_ON_BOOT);
        launchIntent.putExtras(event);
        context.startService(launchIntent);    
    }
}

