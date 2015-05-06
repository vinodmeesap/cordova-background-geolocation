BackgroundGeoLocation
==============================

Cross-platform background geolocation for Cordova with battery-saving "circular region monitoring" and "stop detection".

Follows the [Cordova Plugin spec](https://github.com/apache/cordova-plugman/blob/master/plugin_spec.md), so that it works with [Plugman](https://github.com/apache/cordova-plugman).

This plugin leverages Cordova/PhoneGap's [require/define functionality used for plugins](http://simonmacdonald.blogspot.ca/2012/08/so-you-wanna-write-phonegap-200-android.html).

## Using the plugin ##
The plugin creates the object `window.plugins.backgroundGeoLocation` with the methods

  `configure(success, fail, option)`,
	
  `setConfig(success, fail, config) // reconfigure`,
  
  `start(success, fail)`

  `stop(success, fail)`.

  `changePace(true) // engages aggressive monitoring immediately`
  
  `onStationary(callback, fail)`
  

## Installing the plugin ##

```

   cordova plugin add https://github.com/christocracy/cordova-background-geolocation.git
```

## Help

[See the Wiki](https://github.com/christocracy/cordova-background-geolocation/wiki)

## Example

```

////
// As with all Cordova plugins, you must configure within an #deviceready callback.
//
function onDeviceReady() {
    /**
    * This would be your own callback for Ajax-requests after POSTing background geolocation to your server.
    */
    var yourAjaxCallback = function(response) {
        ////
        // IMPORTANT:  You must execute the #finish method here to inform the native plugin that you're finished,
        //  and the background-task may be completed.  You must do this regardless if your HTTP request is successful or not.
        // IF YOU DON'T, ios will CRASH YOUR APP for spending too much time in the background.
        //
        //
        bgGeo.finish();
    };

    /**
    * This callback will be executed every time a geolocation is recorded in the background.
    */
    var callbackFn = function(location) {
        console.log('[js] BackgroundGeoLocation callback:  ' + location.latitude + ',' + location.longitude);
        // Do your HTTP request here to POST location to your server.
        //
        //
        yourAjaxCallback.call(this);
    };

    var failureFn = function(error) {
        console.log('BackgroundGeoLocation error');
    }

    // BackgroundGeoLocation is highly configurable.
    bgGeo.configure(callbackFn, failureFn, {
        debug: true, // <-- enable this hear sounds for background-geolocation life-cycle.
        desiredAccuracy: 0,
        stationaryRadius: 50,
        distanceFilter: 50,
        disableElasticity: false, // <-- [iOS] Default is 'false'.  Set true to disable speed-based distanceFilter elasticity
        locationUpdateInterval: 5000,
        minimumActivityRecognitionConfidence: 80,   // percentage 
        fastestLocationUpdateInterval: 5000,
        activityRecognitionInterval: 10000,
        stopTimeout: 0,
        forceReload: true,      // <-- [Android] If the user closes the app **while location-tracking is started** , reboot app (WARNING: possibly distruptive to user) 
        stopOnTerminate: false, // <-- [Android] Allow the background-service to run headless when user closes the app.
        startOnBoot: true,      // <-- [Android] Auto start background-service in headless mode when device is powered-up.
        activityType: 'AutomotiveNavigation'
        /**
        * HTTP Feature:  set an url to allow the native background service to POST locations to your server
        */
        ,url: 'http://posttestserver.com/post.php?dir=cordova-background-geolocation',
        maxDaysToPersist: 1,    // <-- Maximum days to persist a location in plugin's SQLite database when HTTP fails
        headers: {
            "X-FOO": "bar"
        },
        params: {
            "auth_token": "maybe_your_server_authenticates_via_token_YES?"
        }
    });

    // Turn ON the background-geolocation system.  The user will be tracked whenever they suspend the app.
    bgGeo.start();

    // If you wish to turn OFF background-tracking, call the #stop method.
    // bgGeo.stop()
}


```

## Example Application

![SampleApp](/android-sample-app.png "SampleApp")

This plugin hosts a SampleApp in ```example/SampleApp``` folder.  This SampleApp contains no plugins so you must first start by adding its required plugins (most importantly, this one).  **NOTE** In order to use the SampleApp, it's important to make a copy of it outside of the plugin itself.

```
$ git clone git@github.com:christocracy/cordova-background-geolocation.git
$ mkdir tmp
$ cp -R cordova-background-geolocation/example/SampleApp tmp
$ cd tmp/SampleApp
$ cordova plugin add cordova-plugin-whitelist
$ cordova plugin add cordova-plugin-geolocation
$ cordova plugin add git@github.com:christocracy/cordova-background-geolocation.git
$ cordova platform add ios
$ cordova platform add android
$ cordova build ios
$ cordova build android

```

If you're using XCode, boot the SampleApp in the iOS Simulator and enable ```Debug->Location->City Drive```.

## Help!  It doesn't work!

Yes it does.  [See the Wiki](https://github.com/christocracy/cordova-background-geolocation/wiki)

- on iOS, background tracking won't be engaged until you travel about **2-3 city blocks**, so go for a walk or car-ride (or use the Simulator with ```Debug->Location->City Drive```)
- Android is much quicker detecting movements; typically several meters of walking will do it.
- When in doubt, **nuke everything**:  First delete the app from your device (or simulator)

```
$ cordova plugin remove com.transistorsoft.cordova.background-geolocation
$ cordova plugin add git@github.com:christocracy/cordova-background-geolocation.git

$ cordova platform remove ios
$ cordova platform add ios
$ cordova build ios

```

## Behaviour

The plugin has features allowing you to control the behaviour of background-tracking, striking a balance between accuracy and battery-usage.  In stationary-mode, the plugin attempts to descrease its power usage and accuracy by setting up a circular stationary-region of configurable #stationaryRadius.  iOS has a nice system  [Significant Changes API](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instm/CLLocationManager/startMonitoringSignificantLocationChanges), which allows the os to suspend your app until a cell-tower change is detected (typically 2-3 city-block change) Android uses the Google Play Services APIs [FusedLocationProvider API](https://developer.android.com/reference/com/google/android/gms/location/FusedLocationProviderApi.html) as well as the [ActivityRecognition API](https://developer.android.com/reference/com/google/android/gms/location/ActivityRecognitionApi.html) (for movement/stationary detection). Windows Phone does not have such a API.

The plugin will execute your configured ```callback``` provided to the ```#configure(callback, config)``` method. You must manually POST the received ```GeoLocation``` to your server using standard XHR, as well as manually cache a recieved location into ```localStorage``` if no network connection is available.

The function ```changePace(isMoving, success, failure)``` is provided to force the plugin to enter "moving" or "stationary" state.

## iOS

The plugin uses iOS Significant Changes API, and starts triggering your configured ```callback``` only when a cell-tower switch is detected (i.e. the device exits stationary radius). 

When the plugin detects the device has moved beyond its configured #stationaryRadius, it engages the native platform's geolocation system for aggressive monitoring according to the configured `#desiredAccuracy`, `#distanceFilter`.  The plugin attempts to intelligently scale `#distanceFilter` based upon the current reported speed.  Each time `#distanceFilter` is determined to have changed by 5m/s, it recalculates it by squaring the speed rounded-to-nearest-five and adding #distanceFilter (I arbitrarily came up with that formula.  Better ideas?).

  `(round(speed, 5))^2 + distanceFilter`

### Android

Using the [ActivityRecognition API](https://developer.android.com/reference/com/google/android/gms/location/ActivityRecognitionApi.html) provided by [Google Play Services](https://developer.android.com/google/play-services/index.html), Android will constantly monitor [the nature](https://developer.android.com/reference/com/google/android/gms/location/DetectedActivity.html) of the device's movement at a sampling-rate configured by ```#activityRecognitionRate```.  When the plugin sees a DetectedActivity of [STILL](https://developer.android.com/reference/com/google/android/gms/location/DetectedActivity.html), location-updates will be halted -- when it sees ```IN_VEHICLE, ON_BICYCLE, ON_FOOT, RUNNING, WALKING```, location-updates will be initiated.


### WP8

WP8 uses ```callbackFn``` the way iOS do. On WP8, however, the plugin does not support the Stationary location and does not implement ```getStationaryLocation()``` and ```onPaceChange()```.
Keep in mind that it is **not** possible to use ```start()``` at the ```pause``` event of Cordova/PhoneGap. WP8 suspend your app immediately and ```start()``` will not be executed. So make sure you fire ```start()``` before the app is closed/minimized.

## Methods

#####`configure(locationCallback, failureCallback, config)`

Configures the plugin's parameters (@see following [Config](https://github.com/christocracy/cordova-background-geolocation/blob/edge/README.md#config) section for accepted ```config``` params.  The ```locationCallback``` will be executed each time a new Geolocation is recorded.

#####`setConfig(successFn, failureFn, config)`
Reconfigure plugin's configuration (@see followign ##Config## section for accepted ```config``` params.  **NOTE** The plugin will continue to send recorded Geolocation to the ```locationCallback``` you provided to ```configure``` method -- use this method only to change configuration params (eg: ```distanceFilter```, ```stationaryRadius```, etc).

```
bgGeo.setConfig(function(){}, function(){}, {
    desiredAccuracy: 10,
    distanceFilter: 100
});
```

#####`start(successFn, failureFn)`

Enable background geolocation tracking.

```
bgGeo.start()
```

#####`stop(successFn, failureFn)`

Disable background geolocation tracking.

```
bgGeo.stop();
```

#####`changePace(enabled, successFn, failureFn)`
Initiate or cancel immediate background tracking.  When set to ```true```, the plugin will begin aggressively tracking the devices Geolocation, bypassing stationary monitoring.  If you were making a "Jogging" application, this would be your [Start Workout] button to immediately begin GPS tracking.  Send ```false``` to disable aggressive GPS monitoring and return to stationary-monitoring mode.

```
bgGeo.changePace(true);  // <-- Aggressive GPS monitoring immediately engaged.
bgGeo.changePace(false); // <-- Disable aggressive GPS monitoring.  Engages stationary-mode.
```

#####`onStationary(callbackFn, failureFn)`
Your ```callbackFn``` will be executed each time the device has entered stationary-monitoring mode.  The ```callbackFn``` will be provided with a ```Geolocation``` object as the 1st param, with the usual params (```latitude, longitude, accuracy, speed, bearing, altitude```).

```
bgGeo.onStationary(function(location) {
    console.log('- Device is stopped: ', location.latitude, location.longitude);
});
```

## Config

Use the following config-parameters with the #configure method:

#####`@param {Boolean} debug`

When enabled, the plugin will emit sounds for life-cycle events of background-geolocation!  **NOTE iOS**:  In addition, you must manually enable the *Audio and Airplay* background mode in *Background Capabilities* to hear these debugging sounds.

- Exit stationary region:  **[ios]** Calendar event notification sound
- GeoLocation recorded:  **[ios]** SMS-sent sound, **[android]** "blip", *[WP8]* High beep, 1 sec.
- Aggressive geolocation engaged:  **[ios]** SIRI listening sound, **[android]** "Doodly-doo"
- Acquiring stationary location sound: **[ios]** "tick,tick,tick" sound, *[android]* none
- Stationary location acquired sound:  **[ios]** "bloom" sound, **[android]** long "beeeeeep"

![Enable Background Audio](/enable-background-audio.png "Enable Background Audio")

#####`@param {Integer} desiredAccuracy [0, 10, 100, 1000] in meters`

Specify the desired-accuracy of the geolocation system with 1 of 4 values, ```0, 10, 100, 1000``` where ```0``` means HIGHEST POWER, HIGHEST ACCURACY and ```1000``` means LOWEST POWER, LOWEST ACCURACY

- [Android](https://developer.android.com/reference/com/google/android/gms/location/LocationRequest.html#PRIORITY_BALANCED_POWER_ACCURACY)
- [iOS](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/index.html#//apple_ref/occ/instp/CLLocationManager/desiredAccuracy) 

#####`@param {Integer} stationaryRadius (meters)`

When stopped, the minimum distance the device must move beyond the stationary location for aggressive background-tracking to engage.  Note, since the plugin uses iOS significant-changes API, the plugin cannot detect the exact moment the device moves out of the stationary-radius.  In normal conditions, it can take as much as 3 city-blocks to 1/2 km before staionary-region exit is detected.

#####`@param {Integer} distanceFilter`

The minimum distance (measured in meters) a device must move horizontally before an update event is generated.  @see [Apple docs](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instp/CLLocationManager/distanceFilter).  However, #distanceFilter is elastically auto-calculated by the plugin:  When speed increases, #distanceFilter increases;  when speed decreases, so does distanceFilter.

distanceFilter is calculated as the square of speed-rounded-to-nearest-5 and adding configured #distanceFilter.

  `(round(speed, 5))^2 + distanceFilter`

For example, at biking speed of 7.7 m/s with a configured distanceFilter of 30m:

  `=> round(7.7, 5)^2 + 30`
  `=> (10)^2 + 30`
  `=> 100 + 30`
  `=> 130`

A gps location will be recorded each time the device moves 130m.

At highway speed of 30 m/s with distanceFilter: 30,

  `=> round(30, 5)^2 + 30`
  `=> (30)^2 + 30`
  `=> 900 + 30`
  `=> 930`

A gps location will be recorded every 930m

Note the following real example of background-geolocation on highway 101 towards San Francisco as the driver slows down as he runs into slower traffic (geolocations become compressed as distanceFilter decreases)

![distanceFilter at highway speed](/distance-filter-highway.png "distanceFilter at highway speed")

Compare now background-geolocation in the scope of a city.  In this image, the left-hand track is from a cab-ride, while the right-hand track is walking speed.

![distanceFilter at city scale](/distance-filter-city.png "distanceFilter at city scale")

#####`@param {Boolean} stopOnTerminate`
Enable this in order to force a stop() when the application terminated (e.g. on iOS, double-tap home button, swipe away the app).  On Android, ```stopOnTerminate: false``` will cause the plugin to operate as a headless background-service (in this case, you should configure an #url in order for the background-service to send the location to your server)

#####`@param {Boolean} stopAfterElapsedMinutes`

The plugin can optionally auto-stop monitoring location when some number of minutes elapse after being the #start method was called.

#### HTTP Features

#####`@param {String} url`

By configuring an ```#url```, the  plugin will always attempt to HTTP POST the location to your server.

#####`@param {Object} params`

Optional HTTP params sent along in HTTP request to above ```#url```.

#####`@param {Object} headers`

Optional HTTP params sent along in HTTP request to above ```#url```.

#####`@param {Integer} maxDaysToPersist`

Maximum number of days to store a geolocation in plugin's SQLite database when your server fails to respond with ```HTTP 200 OK```.  The plugin will continue attempting to sync with your server until ```maxDaysToPersist``` when it will give up and remove the location from the database.

Both iOS and Android can send the Geolocation to your server simply by configuring an ```#url``` in addition to optional ```#headers``` and ```#params```.  This is the preferred way to send the Geolocation to your server, rather than doing it yourself with Ajax in your javascript.  

##### In-Plugin SQLite Storage

When you enable HTTP Feature by configuring an ```#url```, the plugin will cache every recorded geolocation to its internal SQLite database -- when your server responds with HTTP ```200, 201 or 204```, the plugin will DELETE the stored location from cache.  The plugin has a cache-pruning feature with ```@config {Integer} maxDaysToPersist``` -- If your server hasn't responded with 200 before ```maxDaysToPersist``` expires, the plugin will give up on it and that geolocation will be pruned from the database.

```
bgGeo.configure(callbackFn, failureFn, {
    .
    .
    .
    url: 'http://posttestserver.com/post.php?dir=cordova-background-geolocation',
    maxDaysToPersist: 1,
    headers: {
        "X-FOO": "bar"
    },
    params: {
        "auth_token": "maybe_your_server_authenticates_via_token_YES?"
    }
});

...

Headers (Some may be inserted by server)

REQUEST_URI = /post.php?dir=cordova-background-geolocation
QUERY_STRING = dir=cordova-background-geolocation
REQUEST_METHOD = POST
GATEWAY_INTERFACE = CGI/1.1
REMOTE_PORT = 38380
REMOTE_ADDR = 198.84.250.106
HTTP_USER_AGENT = Apache-HttpClient/UNAVAILABLE (java 1.4)
HTTP_CONNECTION = close
HTTP_HOST = posttestserver.com
CONTENT_LENGTH = 243
CONTENT_TYPE = application/json
HTTP_ACCEPT = application/json
UNIQUE_ID = VS-YI9Bx6hIAABctKDoAAAAB
REQUEST_TIME_FLOAT = 1429198883.9584
REQUEST_TIME = 1429198883

No Post Params.

== Begin post body ==
{
  "location":{
    "timestamp":"2015-05-05T04:31:54Z",  // <-- ISO-8601, UTC
    "coords":{
      "latitude":45.519282,
      "longitude":-73.6169562,
      "accuracy":12.850000381469727,
      "speed":0,
      "heading":0,
      "altitude":0
    },
    "activity":{  // <-- Android-only currently
      "type":"still",
      "confidence":48
    }
  },
  "android_id":"39dbac67e2c9d80"
}
== End post body ==
```

### Android Config

#####`@param {Integer millis} locationUpdateInterval`

Set the desired interval for active location updates, in milliseconds.

The location client will actively try to obtain location updates for your application at this interval, so it has a direct influence on the amount of power used by your application. Choose your interval wisely.

This interval is inexact. You may not receive updates at all (if no location sources are available), or you may receive them slower than requested. You may also receive them faster than requested (if other applications are requesting location at a faster interval). 

Applications with only the coarse location permission may have their interval silently throttled.

An interval of 0 is allowed, but not recommended, since location updates may be extremely fast on future implementations.

#####`@param {Integer millis} activityRecognitionInterval`

the desired time between activity detections. Larger values will result in fewer activity detections while improving battery life. A value of 0 will result in activity detections at the fastest possible rate.

#####`@param {Integer minutes} stopTimeout`

The number of miutes to wait before turning off the GPS after the ActivityRecognition System (ARS) detects the device is ```STILL``` (defaults to 0, no timeout).  If you don't set a value, the plugin is eager to turn off the GPS ASAP.  An example use-case for this configuration is to delay GPS OFF while in a car waiting at a traffic light.

#####`@param {Boolean} forceReload`

If the user closes the application while the background-tracking has been started,  location-tracking will continue on if ```stopOnTerminate: false```.  You may choose to force the foreground application to reload (since this is where your Javascript runs) by setting ```foreceReload: true```.  This will guarantee that locations are always sent to your Javascript callback (**WARNING** possibly disruptive to user).

#####`@param {Boolean} startOnBoot`

Set to ```true``` to start the background-service whenever the device boots.  Unless you configure the plugin to ```forceReload``` (ie: boot your app), you should configure the plugin's HTTP features so it can POST to your server in "headless" mode.



### iOS Config

#####`@param {Boolean} disableElasticity [false]`

Defaults to ```false```.  Set ```true``` to disable automatic speed-based ```#distanceFilter``` elasticity.  eg:  When device is moving at highway speeds, locations are returned at ~ 1 / km.

#####`@param {String} activityType [AutomotiveNavigation, OtherNavigation, Fitness, Other]`

Presumably, this affects ios GPS algorithm.  See [Apple docs](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instp/CLLocationManager/activityType) for more information

### WP8 Config

#####`{Integer [0, 10, 100, 1000]} desiredAccuracy`

###### Windows Phone
The underlying GeoLocator you can choose to use 'DesiredAccuracy' or 'DesiredAccuracyInMeters'. Since this plugins default configuration accepts meters, the default desiredAccuracy is mapped to the Windows Phone DesiredAccuracyInMeters leaving the DesiredAccuracy enum empty. For more info see the [MS docs](http://msdn.microsoft.com/en-us/library/windows/apps/windows.devices.geolocation.geolocator.desiredaccuracyinmeters) for more information.

## Licence ##
```
cordova-background-geolocation
Copyright (c) 2015, Transistor Software (9224-2932 Quebec Inc)
All rights reserved.
sales@transistorsoft.com
http://transistorsoft.com
```

1. Preamble:  This Agreement governs the relationship between YOU OR THE ORGANIZATION ON WHOSE BEHALF YOU ARE ENTERING INTO THIS AGREEMENT (hereinafter: Licensee) and Transistor Software, a LICENSOR AFFILIATION whose principal place of business is Montreal, Quebec, Canada (Hereinafter: Licensor). This Agreement sets the terms, rights, restrictions and obligations on using [{software}] (hereinafter: The Software) created and owned by Licensor, as detailed herein

2. License Grant: Licensor hereby grants Licensee a Personal, Non-assignable &amp; non-transferable, Commercial, Royalty free, Including the rights to create but not distribute derivative works, Non-exclusive license, all with accordance with the terms set forth and other legal restrictions set forth in 3rd party software used while running Software.

	2.1 Limited: Licensee may use Software for the purpose of:
		- Running Software on Licensee's Website[s] and Server[s];
		- Allowing 3rd Parties to run Software on Licensee's Website[s] and Server[s];
		- Publishing Software&rsquo;s output to Licensee and 3rd Parties;
		- Distribute verbatim copies of Software's output (including compiled binaries);
		- Modify Software to suit Licensee&rsquo;s needs and specifications.

	2.2 Binary Restricted: Licensee may sublicense Software as a part of a larger work containing more than Software, distributed solely in Object or Binary form under a personal, non-sublicensable, limited license. Such redistribution shall be limited to unlimited codebases.</li><li>

	2.3 Non Assignable &amp; Non-Transferable: Licensee may not assign or transfer his rights and duties under this license.

	2.4 Commercial, Royalty Free: Licensee may use Software for any purpose, including paid-services, without any royalties

	2.5 Including the Right to Create Derivative Works: </strong>Licensee may create derivative works based on Software, including amending Software&rsquo;s source code, modifying it, integrating it into a larger work or removing portions of Software, as long as no distribution of the derivative works is made.

3. Term & Termination:  The Term of this license shall be until terminated. Licensor may terminate this Agreement, including Licensee's license in the case where Licensee : 

	3.1 became insolvent or otherwise entered into any liquidation process; or

	3.2 exported The Software to any jurisdiction where licensor may not enforce his rights under this agreements in; or

	3.3 Licensee was in breach of any of this license's terms and conditions and such breach was not cured, immediately upon notification; or

	3.4 Licensee in breach of any of the terms of clause 2 to this license; or

	3.5 Licensee otherwise entered into any arrangement which caused Licensor to be unable to enforce his rights under this License.

4. Payment: In consideration of the License granted under clause 2, Licensee shall pay Licensor a FEE, via Credit-Card, PayPal or any other mean which Licensor may deem adequate. Failure to perform payment shall construe as material breach of this Agreement.

5. Upgrades, Updates and Fixes: Licensor may provide Licensee, from time to time, with Upgrades,  Updates or Fixes, as detailed herein and according to his sole discretion. Licensee hereby warrants to keep The Software up-to-date and install all relevant updates and fixes, and may, at his sole discretion, purchase upgrades, according to the rates set by Licensor. Licensor shall provide any update or Fix free of charge; however, nothing in this Agreement shall require Licensor to provide Updates or Fixes.

	5.1 Upgrades: for the purpose of this license, an Upgrade  shall be a material amendment in The Software, which contains new features   and or major performance improvements and shall be marked as a new version number. For example, should Licensee purchase The Software under   version 1.X.X, an upgrade shall commence under number 2.0.0.

	5.2 Updates: for the purpose of this license, an update shall be a minor amendment   in The Software, which may contain new features or minor improvements and   shall be marked as a new sub-version number. For example, should   Licensee purchase The Software under version 1.1.X, an upgrade shall   commence under number 1.2.0.

	5.3 Fix: for the purpose of this license, a fix shall be a minor amendment in   The Software, intended to remove bugs or alter minor features which impair   the The Software's functionality. A fix shall be marked as a new   sub-sub-version number. For example, should Licensee purchase Software   under version 1.1.1, an upgrade shall commence under number 1.1.2.

6. Support: Software is provided under an AS-IS basis and without any support, updates or maintenance. Nothing in this Agreement shall require Licensor to provide Licensee with support or fixes to any bug, failure, mis-performance or other defect in The Software.

	6.1 Bug Notification: Licensee may provide Licensor of details regarding any bug, defect or   failure in The Software promptly and with no delay from such event;  Licensee  shall comply with Licensor's request for information regarding  bugs,  defects or failures and furnish him with information,  screenshots and  try to reproduce such bugs, defects or failures.

	6.2 Feature Request: Licensee may request additional features in Software, provided, however, that (i) Licensee shall waive any claim or right in such feature should feature be developed by Licensor; (ii) Licensee shall be prohibited from developing the feature, or disclose such feature   request, or feature, to any 3rd party directly competing with Licensor or any 3rd party which may be, following the development of such feature, in direct competition with Licensor; (iii) Licensee warrants that feature does not infringe any 3rd party patent, trademark, trade-secret or any other intellectual property right; and (iv) Licensee developed, envisioned or created the feature solely by himself.

7. Liability: To the extent permitted under Law, The Software is provided under an   AS-IS basis. Licensor shall never, and without any limit, be liable for   any damage, cost, expense or any other payment incurred by Licensee as a   result of Software&rsquo;s actions, failure, bugs and/or any other  interaction  between The Software &nbsp;and Licensee&rsquo;s end-equipment, computers,  other  software or any 3rd party, end-equipment, computer or  services. Moreover, Licensor shall never be liable for any defect in  source code  written by Licensee when relying on The Software or using The Software&rsquo;s source  code.

8. Warranty: 

	8.1 Intellectual Property:  Licensor   hereby warrants that The Software does not violate or infringe any 3rd   party claims in regards to intellectual property, patents and/or   trademarks and that to the best of its knowledge no legal action has   been taken against it for any infringement or violation of any 3rd party   intellectual property rights.

	8.2 No-Warranty: The Software is provided without any warranty; Licensor hereby disclaims   any warranty that The Software shall be error free, without defects or code   which may cause damage to Licensee&rsquo;s computers or to Licensee, and  that  Software shall be functional. Licensee shall be solely liable to  any  damage, defect or loss incurred as a result of operating software  and  undertake the risks contained in running The Software on License&rsquo;s  Server[s]  and Website[s].

	8.3 Prior Inspection:  Licensee hereby states that he inspected The Software thoroughly and found   it satisfactory and adequate to his needs, that it does not interfere   with his regular operation and that it does meet the standards and  scope  of his computer systems and architecture. Licensee found that  The Software  interacts with his development, website and server environment  and that  it does not infringe any of End User License Agreement of any  software  Licensee may use in performing his services. Licensee hereby  waives any  claims regarding The Software's incompatibility, performance,  results and  features, and warrants that he inspected the The Software.</p>

9. No Refunds:  Licensee warrants that he inspected The Software according to clause 7(c)   and that it is adequate to his needs. Accordingly, as The Software is   intangible goods, Licensee shall not be, ever, entitled to any refund,   rebate, compensation or restitution for any reason whatsoever, even if   The Software contains material flaws.

10. Indemnification:  Licensee hereby warrants to hold Licensor harmless and indemnify Licensor for any lawsuit brought against it in regards to Licensee&rsquo;s use   of The Software in means that violate, breach or otherwise circumvent this   license, Licensor's intellectual property rights or Licensor's title  in  The Software. Licensor shall promptly notify Licensee in case of such  legal  action and request Licensee's consent prior to any settlement in relation to such lawsuit or claim.

11. Governing Law, Jurisdiction:  Licensee hereby agrees not to initiate class-action lawsuits against Licensor in relation to this license and to compensate Licensor for any legal fees, cost or attorney fees should any claim brought by Licensee against Licensor be denied, in part or in full.

