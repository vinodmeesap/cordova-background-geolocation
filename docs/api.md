# API Documentation

## Geolocation Options

| Option | Type | Opt/Required | Default | Note |
|---|---|---|---|---|
| [`desiredAccuracy`](#param-integer-desiredaccuracy-0-10-100-1000-in-meters) | `Integer` | Required | 0 | Specify the desired-accuracy of the geolocation system with 1 of 4 values, `0`, `10`, `100`, `1000` where `0` means **HIGHEST POWER, HIGHEST ACCURACY** and `1000` means **LOWEST POWER, LOWEST ACCURACY** |
| [`distanceFilter`](#param-integer-distancefilter) | `Integer` | Required | `30`| The minimum distance (measured in meters) a device must move horizontally before an update event is generated. @see Apple docs. However, #distanceFilter is elastically auto-calculated by the plugin: When speed increases, #distanceFilter increases; when speed decreases, so does distanceFilter (disabled with `disableElasticity: true`) |
| [`stopAfterElapsedMinutes`](#param-integer-stopafterelapsedminutes) | `Integer`  |  Optional | `0`  | The plugin can optionally auto-stop monitoring location when some number of minutes elapse after being the #start method was called. |
| [`stationaryRadius`](#param-integer-stationaryradius-meters) | `Integer`  |  Required (**iOS**)| `20`  | When stopped, the minimum distance the device must move beyond the stationary location for aggressive background-tracking to engage. Note, since the plugin uses iOS significant-changes API, the plugin cannot detect the exact moment the device moves out of the stationary-radius. In normal conditions, it can take as much as 3 city-blocks to 1/2 km before staionary-region exit is detected. |
| [`disableElasticity`](#param-boolean-disableelasticity-false) | `bool`  |  Optional (**iOS**)| `false`  | Set true to disable automatic speed-based `#distanceFilter` elasticity. eg: When device is moving at highway speeds, locations are returned at ~ 1 / km. |
| [`activityType`](#param-string-activitytype-automotivenavigation-othernavigation-fitness-other) | `String` | Required (**iOS**)| `Other` | Presumably, this affects ios GPS algorithm.  See [Apple docs](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instp/CLLocationManager/activityType) for more information | Set the desired interval for active location updates, in milliseconds. |
| [`useSignificantChangesOnly`](#param-boolean-usesignificantchangesonly-false) | `Boolean` | Optional (**iOS**)| `false` | Defaults to `false`.  Set `true` in order to disable constant background-tracking and use only the iOS [Significant Changes API](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/index.html#//apple_ref/occ/instm/CLLocationManager/startMonitoringSignificantLocationChanges).  If Apple has denied your application due to background-tracking, this can be a solution.  **NOTE** The Significant Changes API will report a location only when a significant change from the last location has occurred.  Many of the configuration parameters **will be ignored**, such as `#distanceFilter`, `#stationaryRadius`, `#activityType`, etc. |

## Activity Recognition Options

| Option | Type | Opt/Required | Default | Note |
|---|---|---|---|---|
| [`activityRecognitionInterval`](#param-integer-millis-10000-activityrecognitioninterval) | `Integer` | Required | `10000` | The desired time between activity detections. Larger values will result in fewer activity detections while improving battery life. A value of 0 will result in activity detections at the fastest possible rate. |
| [`stopTimeout`](#param-integer-minutes-stoptimeout) | `Integer` | Required | `5 minutes` | The number of miutes to wait before turning off the GPS after the ActivityRecognition System (ARS) detects the device is `STILL` (**Android:** defaults to 0, no timeout, **iOS:** defaults to 5min).  If you don't set a value, the plugin is eager to turn off the GPS ASAP.  An example use-case for this configuration is to delay GPS OFF while in a car waiting at a traffic light. |
| [`minimumActivityRecognitionConfidence`](#param-integer-millis-minimumactivityrecognitionconfidence) | `Integer` | Optional (**Android**)| `80` | Each activity-recognition-result returned by the API is tagged with a "confidence" level expressed as a %.  You can set your desired confidence to trigger a state-change.  Defaults to `80`.|
| [`stopDetectionDelay`](#param-integer-minutes-stopdetectiondelay-0) | `Integer` | Optional (**iOS**)| 0 | Allows the stop-detection system to be delayed from activating.  When the stop-detection system is engaged, the GPS is off and only the accelerometer is monitored.  Stop-detection will only engage if this timer expires.  The timer is cancelled if any movement is detected before expiration | 

## HTTP / Persistence Options

| Option | Type | Opt/Required | Default | Note |
|---|---|---|---|---|
| [`url`](#param-string-url) | `String` | Optional | - | Your server url where you wish to HTTP POST recorded locations to |
| [`params`](#param-object-params) | `Object` | Optional | `{}` | Optional HTTP params sent along in HTTP request to above `#url` |
| [`headers`](#param-object-headers) | `Object` | Optional | `{}` | Optional HTTP headers sent along in HTTP request to above `#url` |
| [`method`](#param-string-method-post) | `String` | Optional | `POST` | The HTTP method.  Defaults to `POST`.  Some servers require `PUT`.
| [`autoSync`](#param-string-autosync-true) | `Boolean` | Optional | `true` | If you've enabeld HTTP feature by configuring an `#url`, the plugin will attempt to HTTP POST each location to your server **as it is recorded**.  If you set `autoSync: false`, it's up to you to **manually** execute the `#sync` method to initate the HTTP POST (**NOTE** The plugin will continue to persist **every** recorded location in the SQLite database until you execute `#sync`). |
| [`batchSync`](#param-string-batchsync-false) | `Boolean` | Optional | `false` | Default is `false`.  If you've enabled HTTP feature by configuring an `#url`, `batchSync: true` will POST all the locations currently stored in native SQLite datbase to your server in a single HTTP POST request.  With `batchSync: false`, an HTTP POST request will be initiated for **each** location in database. |
| [`maxDaysToPersist`](#param-integer-maxdaystopersist) | `Integer` | Optional | `1` |  Maximum number of days to store a geolocation in plugin's SQLite database when your server fails to respond with `HTTP 200 OK`.  The plugin will continue attempting to sync with your server until `maxDaysToPersist` when it will give up and remove the location from the database. |

## Application Options

| Option | Type | Opt/Required | Default | Note |
|---|---|---|---|---|
| [`debug`](#param-boolean-debug) | `Boolean` | Optional | `false` | When enabled, the plugin will emit sounds for life-cycle events of background-geolocation!  **NOTE iOS**:  In addition, you must manually enable the *Audio and Airplay* background mode in *Background Capabilities* to hear these debugging sounds. |
| [`stopOnTerminate`](#param-boolean-stoponterminate) | `Boolean` | Optional | `true` | Enable this in order to force a stop() when the application terminated (e.g. on iOS, double-tap home button, swipe away the app). On Android, stopOnTerminate: false will cause the plugin to operate as a headless background-service (in this case, you should configure an #url in order for the background-service to send the location to your server) |

## Events

| Event Name | Returns | Notes
|---|---|---|
| `onMotionChange` | `{location}, `taskId` | Fired when the device changes stationary / moving state. |
| `onGeofence` | `{geofence}`, `taskId` | Fired when a geofence crossing event occurs |
| `onHttp` | `{response}`, `taskId` | Fired after a successful HTTP response. `response` object is provided with `status` and `responseText`|

## [Methods](#methods-1)

| Method Name | Arguments | Notes
|---|---|---|
| `configure` | `{config}` | Configures the plugin's parameters (@see following Config section for accepted config params. The locationCallback will be executed each time a new Geolocation is recorded and provided with the following parameters |
| `setConfig` | `{config}` | Re-configure the plugin with new values |
| `start` | `callbackFn`| Enable location tracking.  Supplied `callbackFn` will be executed when tracking is successfully engaged |
| `stop` | `callbackFn` | Disable location tracking.  Supplied `callbackFn` will be executed when tracking is successfully engaged |
| `getState` | `callbackFn` | Fetch the current-state of the plugin, including `enabled`, `isMoving`, as well as all other config params |
| `getCurrentPosition` | `callbackFn` | Retrieves the current position. This method instructs the native code to fetch exactly one location using maximum power & accuracy. |
| `changePace` | `isMoving` | Initiate or cancel immediate background tracking. When set to true, the plugin will begin aggressively tracking the devices Geolocation, bypassing stationary monitoring. If you were making a "Jogging" application, this would be your [Start Workout] button to immediately begin GPS tracking. Send false to disable aggressive GPS monitoring and return to stationary-monitoring mode. |
| `getLocations` | `callbackFn` | Fetch all the locations currently stored in native plugin's SQLite database. Your callbackFn`` will receive an `Array` of locations in the 1st parameter |
| `sync` | - | If the plugin is configured for HTTP with an `#url` and `#autoSync: false`, this method will initiate POSTing the locations currently stored in the native SQLite database to your configured `#url`|
| `getOdometer` | `callbackFn` | The plugin constantly tracks distance travelled. The supplied callback will be executed and provided with a `distance` as the 1st parameter.|
| `resetOdometer` | `callbackFn` | Reset the **odometer** to `0`.  The plugin never automatically resets the odometer -- this is **up to you** |
| `playSound` | `soundId` | Here's a fun one.  The plugin can play a number of OS system sounds for each platform.  For [IOS](http://iphonedevwiki.net/index.php/AudioServices) and [Android](http://developer.android.com/reference/android/media/ToneGenerator.html).  I offer this API as-is, it's up to you to figure out how this works. |
| `addGeofence` | `{config}` | Adds a geofence to be monitored by the native plugin. Monitoring of a geofence is halted after a crossing occurs.|
| `removeGeofence` | `identifier` | Removes a geofence identified by the provided `identifier` |
| `getGeofences` | `callbackFn` | Fetch the list of monitored geofences. Your callbackFn will be provided with an Array of geofences. If there are no geofences being monitored, you'll receive an empty `Array []`.|


## Geolocation Options

### Common

####`@param {Integer} desiredAccuracy [0, 10, 100, 1000] in meters`

Specify the desired-accuracy of the geolocation system with 1 of 4 values, ```0, 10, 100, 1000``` where ```0``` means HIGHEST POWER, HIGHEST ACCURACY and ```1000``` means LOWEST POWER, LOWEST ACCURACY

- [Android](https://developer.android.com/reference/com/google/android/gms/location/LocationRequest.html#PRIORITY_BALANCED_POWER_ACCURACY)
- [iOS](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/index.html#//apple_ref/occ/instp/CLLocationManager/desiredAccuracy) 

####`@param {Integer} distanceFilter`

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

![distanceFilter at highway speed](https://dl.dropboxusercontent.com/u/2319755/cordova-background-geolocaiton/distance-filter-highway.png)

Compare now background-geolocation in the scope of a city.  In this image, the left-hand track is from a cab-ride, while the right-hand track is walking speed.

![distanceFilter at city scale](https://dl.dropboxusercontent.com/u/2319755/cordova-background-geolocaiton/distance-filter-city.png)

####`@param {Integer} stopAfterElapsedMinutes`

The plugin can optionally auto-stop monitoring location when some number of minutes elapse after being the #start method was called.

### iOS

####`@param {Integer} stationaryRadius (meters)`

When stopped, the minimum distance the device must move beyond the stationary location for aggressive background-tracking to engage.  Note, since the plugin uses iOS significant-changes API, the plugin cannot detect the exact moment the device moves out of the stationary-radius.  In normal conditions, it can take as much as 3 city-blocks to 1/2 km before staionary-region exit is detected.

####`@param {Boolean} disableElasticity [false]`

Defaults to ```false```.  Set ```true``` to disable automatic speed-based ```#distanceFilter``` elasticity.  eg:  When device is moving at highway speeds, locations are returned at ~ 1 / km.

####`@param {String} activityType [AutomotiveNavigation, OtherNavigation, Fitness, Other]`

Presumably, this affects ios GPS algorithm.  See [Apple docs](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/CLLocationManager/CLLocationManager.html#//apple_ref/occ/instp/CLLocationManager/activityType) for more information

####`@param {Boolean} useSignificantChangesOnly [false]`

Defaults to `false`.  Set `true` in order to disable constant background-tracking and use only the iOS [Significant Changes API](https://developer.apple.com/library/ios/documentation/CoreLocation/Reference/CLLocationManager_Class/index.html#//apple_ref/occ/instm/CLLocationManager/startMonitoringSignificantLocationChanges).  If Apple has denied your application due to background-tracking, this can be a solution.  **NOTE** The Significant Changes API will report a location only when a significant change from the last location has occurred.  Many of the configuration parameters **will be ignored**, such as `#distanceFilter`, `#stationaryRadius`, `#activityType`, etc.

Set `true` to disable iOS `CMMotionActivity` updates (eg: walking, running, vehicle, biking, stationary)

### Android

####`@param {Integer millis} locationUpdateInterval`

Set the desired interval for active location updates, in milliseconds.

The location client will actively try to obtain location updates for your application at this interval, so it has a direct influence on the amount of power used by your application. Choose your interval wisely.

This interval is inexact. You may not receive updates at all (if no location sources are available), or you may receive them slower than requested. You may also receive them faster than requested (if other applications are requesting location at a faster interval). 

Applications with only the coarse location permission may have their interval silently throttled.

####`@param {Integer millis} fastestLocationUpdateInterval`

Explicitly set the fastest interval for location updates, in milliseconds.

This controls the fastest rate at which your application will receive location updates, which might be faster than ```#locationUpdateInterval``` in some situations (for example, if other applications are triggering location updates).

This allows your application to passively acquire locations at a rate faster than it actively acquires locations, saving power.

Unlike ```#locationUpdateInterval```, this parameter is exact. Your application will never receive updates faster than this value.

If you don't call this method, a fastest interval will be set to **30000 (30s)**. 

An interval of 0 is allowed, but not recommended, since location updates may be extremely fast on future implementations.

If ```#fastestLocationUpdateInterval``` is set slower than ```#locationUpdateInterval```, then your effective fastest interval is ```#locationUpdateInterval```.

========
An interval of 0 is allowed, but not recommended, since location updates may be extremely fast on future implementations.

####`@param {String} triggerActivities`

These are the comma-delimited list of [activity-names](https://developers.google.com/android/reference/com/google/android/gms/location/DetectedActivity) returned by the `ActivityRecognition` API which will trigger a state-change from **stationary** to **moving**.  By default, this list is set to all five **moving-states**:  `"in_vehicle, on_bicycle, on_foot, running, walking"`.  If you wish, you could configure the plugin to only engage **moving-mode** for vehicles by providing only `"in_vehicle"`.

# Activity Recognition Options

### Common

####`@param {Integer millis} [10000] activityRecognitionInterval`

Defaults to `10000` (10 seconds).  The desired time between activity detections. Larger values will result in fewer activity detections while improving battery life. A value of 0 will result in activity detections at the fastest possible rate.

####`@param {Integer millis} minimumActivityRecognitionConfidence` 

Each activity-recognition-result returned by the API is tagged with a "confidence" level expressed as a %.  You can set your desired confidence to trigger a state-change.  Defaults to `80`.

####`@param {Integer minutes} stopTimeout`

The number of miutes to wait before turning off the GPS after the ActivityRecognition System (ARS) detects the device is `STILL` (**Android:** defaults to 0, no timeout, **iOS:** defaults to 5min).  If you don't set a value, the plugin is eager to turn off the GPS ASAP.  An example use-case for this configuration is to delay GPS OFF while in a car waiting at a traffic light.  **iOS Stop-detection timing**
![](https://dl.dropboxusercontent.com/u/2319755/cordova-background-geolocaiton/ios-stop-detection-timing.png)

### iOS

####`@param {Integer minutes} stopDetectionDelay [0]` 

Allows the stop-detection system to be delayed from activating.  When the stop-detection system is engaged, the GPS is off and only the accelerometer is monitored.  Stop-detection will only engage if this timer expires.  The timer is cancelled if any movement is detected before expiration.  If a value of `0` is specified, the stop-detection system will engage as soon as the device is detected to be stationary.

####`@param {Boolan} disableMotionActivityUpdates [false]`


# HTTP / Persistence Options

####`@param {String} url`

Your server url where you wish to HTTP POST location data to.

####`@param {String} method [POST]`

The HTTP method to use when creating an HTTP request to your configured `#url`.  Defaults to `POST`.  Valid values are `POST`, `PUT` and `OPTIONS`.

####`@param {String} batchSync [false]`

Default is ```false```.  If you've enabled HTTP feature by configuring an ```#url```, ```batchSync: true``` will POST all the locations currently stored in native SQLite datbase to your server in a single HTTP POST request.  With ```batchSync: false```, an HTTP POST request will be initiated for **each** location in database.

####`@param {String} autoSync [true]`

Default is ```true```.  If you've enabeld HTTP feature by configuring an ```#url```, the plugin will attempt to HTTP POST each location to your server **as it is recorded**.  If you set ```autoSync: false```, it's up to you to **manually** execute the ```#sync``` method to initate the HTTP POST (**NOTE** The plugin will continue to persist **every** recorded location in the SQLite database until you execute ```#sync```).

####`@param {Object} params`

Optional HTTP params sent along in HTTP request to above ```#url```.

####`@param {Object} headers`

Optional HTTP params sent along in HTTP request to above ```#url```.

####`@param {Integer} maxDaysToPersist`

Maximum number of days to store a geolocation in plugin's SQLite database when your server fails to respond with ```HTTP 200 OK```.  The plugin will continue attempting to sync with your server until ```maxDaysToPersist``` when it will give up and remove the location from the database.

# Application Options

####`@param {Boolean} debug`

When enabled, the plugin will emit sounds for life-cycle events of background-geolocation!  **NOTE iOS**:  In addition, you must manually enable the *Audio and Airplay* background mode in *Background Capabilities* to hear these [debugging sounds](wiki/Debug-Sounds).  See the wiki [Debug Sounds](wiki/Debug-Sounds) for a detailed description of these sounds.

####`@param {Boolean} stopOnTerminate`
Enable this in order to force a stop() when the application terminated (e.g. on iOS, double-tap home button, swipe away the app).  On Android, ```stopOnTerminate: false``` will cause the plugin to operate as a headless background-service (in this case, you should configure an #url in order for the background-service to send the location to your server)


### Android

####`@param {Boolean} forceReloadOnMotionChange`

If the user closes the application while the background-tracking has been started,  location-tracking will continue on if ```stopOnTerminate: false```.  You may choose to force the foreground application to reload (since this is where your Javascript runs).  `forceReloadOnMotionChange: true` will reload the app only when a state-change occurs from **stationary -> moving** or vice-versa. (**WARNING** possibly disruptive to user).

####`@param {Boolean} forceReloadOnLocationChange`

If the user closes the application while the background-tracking has been started,  location-tracking will continue on if ```stopOnTerminate: false```.  You may choose to force the foreground application to reload (since this is where your Javascript runs).  `forceReloadOnLocationChange: true` will reload the app when a new location is recorded.

####`@param {Boolean} forceReloadOnGeofence`

If the user closes the application while the background-tracking has been started,  location-tracking will continue on if ```stopOnTerminate: false```.  You may choose to force the foreground application to reload (since this is where your Javascript runs).  `forceReloadOnGeolocation: true` will reload the app only when a geofence crossing event has occurred.

####`@param {Boolean} startOnBoot`

Set to ```true``` to start the background-service whenever the device boots.  Unless you configure the plugin to ```forceReload``` (ie: boot your app), you should configure the plugin's HTTP features so it can POST to your server in "headless" mode.



