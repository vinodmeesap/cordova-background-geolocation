# CHANGELOG

## 3.6.1 - 2020-03-20
- [Added] [Android] Add an boolean extra `TSLocationManager: true` to the launch Intent of the foreground-notification, allowing application developers to determine when their app was launched due to a click on the foreground-notification.
- [Fixed] `Authorization` bug in refresh-url response-data recursive iterator.  Do not recurse into arrays in token-refresh response from server (`tokens` are not likely to be found there, anyway).

## 3.6.0 - 2020-02-25
- [Added] iOS `Config.showsBackgroundLocationIndicator`, A Boolean indicating whether the status bar changes its appearance when an app uses location services in the background.
- [Changed] `cordova-plugin-background-fetch` dependency updated to `3.x` with new iOS 13 `BGTaskScheduler` API.
- [Fixed] iOS bug related to significant-location-changes (SLC) API.  In a previous version, the plugin's geofence manager would stop monitoring SLC if the number of added geofences was < the maximum (20) (in order to not show the new iOS 13 dialog reporting background location usage when infinite-geofencing is not required).  The background-geolocation SDK uses several `CLLocationManager` instances and its `GeofenceManager` maintains its own instance.  However, it turns out that when *any* `CLLocationManager` instance stops monitoring the SLC API, then **ALL** instances stop monitoring SLC, which is highly unexpected and undocumented.  As a result, the plugin would lose its safety mechanism should the stationary geofence fail to trigger and iOS tracking could fail to start in some circumstances.
- [Fixed] `synchronize` methods in `TSLocationManager` to address Android NPE related to `buildTSLocation`.
- [Fixed] Typescript declaration for `Location.isMoving` should be `Location.is_moving`.
- [Fixed] iOS:  Bug in `accessToken` RegExp in Authorization token-refresh handler.
- [Fixed] Part of the raw Javascript API contained typescript, which would cause an error on older mobile browser versions.

## 3.5.1 - 2020-01-17
- [Added] Implement four new RPC commands `addGeofence`, `removeGeofence`, `addGeofences`, `removeGeofences`.  Document available RPC commands in "HttpGuide".

## 3.5.0 - 2020-01-15
- [Fixed] Android: launch-Intent for foreground-service notification was causing notification-click to re-launch the Activity rather than show existing.
- [Changed] Android: Modify behaviour of geofences-only mode to not periodically request location-updates.  Will use a stationary-geofence of radius geofenceProximityRadius/2 as a trigger to re-evaluate geofences in proximity.
- [Changed] iOS: Prefix FMDB method-names `databasePool` -> `ts_databasePool` after reports of apps being falsely rejected by Apple for "private API usage".
- [Fixed] Android: Ensure that `location.hasSpeed()` before attempting to use it for distanceFilter elasticity calculations.  There was a report of a Device returning `Nan` for speed.
- [Fixed] Android:  Do not throttle http requests after http connect failure when configured with `maxRecordsToPersist`.
- [Fixed] Android: Respect `disableLocationAuthorizationAlert` for all cases, including `getCurrentPosition`.
- [Changed] Android: Modify behaviour of geofences-only mode to not periodically request location-updates.  Will use a stationary-geofence of radius geofenceProximityRadius/2 as a trigger to re-evaluate geofences in proximity.
- [Changed] Authorization refreshUrl will post as application/x-www-form-urlencoded instead of form/multipart
- [Changed] iOS geofencing mode will not engage Significant Location Changes API when total geofence count <= 18 in order to prevent new iOS 13 "Location summary" popup from showing frequent location access.
- [Fixed] Android:  Add hack for older devices to fix "GPS Week Rollover" bug where incorrect timestamp is recorded from GPS (typically where year is older by 20 years).
- [Fixed] When determining geofences within `geofenceProximityRadius`, add the `location.accuracy` as a buffer against low-accuracy locations.
- [Changed] Increase default `geofenceProximityRadius: 2000`.

## 3.4.2 - 2019-12-03
- [Fixed] iOS crash when launching first time `-[__NSDictionaryM setObject:forKey:]: object cannot be nil (key: authorization)'`
- [Changed] Remove Android warning `In order to enable encryption, you must provide the com.transistorsoft.locationmanager.ENCRYPTION_PASSWORD` when using `encrypt: false`.
- [Fixed] Added headless implementation for `geofenceschange` event.

## 3.4.1 - 2019-12-02
- [Fixed] Android bug rendering `Authorization.toJson` when no `Config.authorization` defined.

## 3.4.0 - 2019-12-02
- [Added] New `Config.authorization` option for automated authorization-token support.  If the SDK receives an HTTP response status `401 Unauthorized` and you've provided an `authorization` config, the plugin will automatically send a request to your configured `refreshUrl` to request a new token.  The SDK will take care of adding the required `Authorization` HTTP header with `Bearer accessToken`.  In the past, one would manage token-refresh by listening to the SDK's `onHttp` listener for HTTP `401`.  This can now all be managed by the SDK by providing a `Config.authorization`.
- [Added] Implemented strong encryption support via `Config.encrypt`.  When enabled, the SDK will encrypt location data in its SQLite datbase, as well as the payload in HTTP requests.  See API docs `Config.encrypt` for more information, including the configuration of encryption password.
- [Added] New JSON Web Token API for the Demo server at http://tracker.transistorsoft.com.  It's now easier than ever to configure the plugin to post to the demo server.  See API docs `Config.transistorAuthorizationToken`.  The old method using `BackgroundGeolocation.transistorTrackerParams` is now deprecated.
- [Added] New `DeviceInfo` module for providing simple device-info (`model`, `manufacturer`, `version`, `platform`).

## 3.3.2 - 2019-10-31
- [Added] New HTTP config `disableAutoSyncOnCellular`.  Set `true` to allow `autoSync` only when device is connected to Wifi.
- [Changed] Re-factor iOS HTTP Service to be more robust; Replace deprecated `NSURLConnection` with `NSURLSession`.
- [Changed] Remove dependency `cordova-plugin-cocoalumberjack`.  `CocoaLumberjack` is now included as a `Cocoapod`.  Since Cordova will now generate a `Podfile`, when opening your project in XCode you must now open `YourProject.xcworkspace` (Not `YourProject.xcodeproj`).

## 3.3.1 - 2019-10-23
- [Fixed] Android NPE
```
Caused by: java.lang.NullPointerException:
  at com.transistorsoft.locationmanager.service.TrackingService.b (TrackingService.java:172)
  at com.transistorsoft.locationmanager.service.TrackingService.onStartCommand (TrackingService.java:135)
```
- [Added] new `uploadLog` feature for uploading logs directly to a server.  This is an alternative to `emailLog`.
- [Changed] Migrated logging methods `getLog`, `destroyLog`, `emailLog` to new `Logger` module available at `BackgroundGeolocation.logger`.  See docs for more information.  Existing log methods on `BackgroundGeolocation` are now `@deprecated`.
- [Changed] All logging methods (`getLog`, `emailLog` and `uploadLog`) now accept an optional `SQLQuery`.  Eg:
```javascript
let query = {
  start: Date.parse('2019-10-23 09:00'),
  end: Date.parse('2019-10-23 19:00'),
  limit: 1000,
  order: Logger.ORDER_ASC
};
let Logger = BackgroundGeolocation.logger;

let log = await Logger.getLog(query)
Logger.emailLog('foo@bar.com', query);
Logger.uploadLoad('http://your.server.com/logs', query);
```

## 3.3.0 - 2019-10-17
- [Fixed] Android: Fixed issue executing `#changePace` immediately after `#start`.
- [Fixed] Android:  Add guard against NPR in `calculateMedianAccuracy`
- [Added] Add new Geofencing methods: `#getGeofence(identifier)` and `#geofenceExists(identifier)`.
- [Fixed] iOS issue using `disableMotionActivityUpdates: false` with `useSignificantChangesOnly: true` and `reset: true`.  Plugin will accidentally ask for Motion Permission.  Fixes #1992.
- [Fixed] Resolved a number of Android issues exposed by booting the app in [StrictMode](https://developer.android.com/reference/android/os/StrictMode).  This should definitely help alleviate ANR issues related to `Context.startForegroundService`.
- [Added] Android now supports `disableMotionActivityUpdates` for Android 10 which now requires run-time permission for "Physical Activity".  Setting to `true` will not ask user for this permission.  The plugin will fallback to using the "stationary geofence" triggering, like iOS.
- [Changed] Android:  Ensure all code that accesses the database is performed in background-threads, including all logging (addresses `Context.startForegroundService` ANR issue).
- [Changed] Android:  Ensure all geofence event-handling is performed in background-threads (addresses `Context.startForegroundService` ANR issue).
- [Added] Android: implement logic to handle operation without Motion API on Android 10.  v3 has always used a "stationary geofence" like iOS as a fail-safe, but this is now crucial for Android 10 which now requires run-time permission for "Physical Activity".  For those users who [Deny] this permission, Android will trigger tracking in a manner similar to iOS (ie: requiring movement of about 200 meters).  This also requires handling to detect when the device has become stationary.

## [3.2.3] - 2019-09-30
- [Added] Implement `disableMotionActivityUpdates` for Android where device SDK > `Q`.  Android Q now requires run-time permissions for "Physical Activity".  Configuring `disableMotionActivityUpdates: true` will disable asking the user for this permission.  However, the plugin performance will be *seriously* degraded; it will take movement of 200-1000 meters before tracking will initiate.
- [Fixed] Android issue with `stopOnTerminate: true`.  Plugin mistakenly clears event-listeners on re-launch of app.
- [Added] Implement an HTTP "back-off" mechanism for both iOS and Android.  If the device is **on a Cell connection** and the server returns an HTTP response of `0` (could not connect), the plugin will not re-attempt an HTTP upload until **another** `autoSyncThreshold` records are recorded.  If you have not configured an `autoSyncThreshold`, the plugin will automatically halt HTTP attempts until another `10` records are recorded.  The HTTP back-off mechnism does not apply for `motionchange` events or if the device is connected to a Wifi network.
- [Fixed] Android NPE on `LocationRequest.getSmallestDisplacement` / `LocationRequest.setSmallestDisplacement`.  I suspect this was caused due to moving some code into background-thread while addressing ANR issues related to `Context.startForegroundService`.


## [3.2.2] - 2019-09-18
- [Changed] Android:  move more location-handling code into background-threads to help mitigate against ANR referencing `Context.startForegroundService`
- [Changed] Android:  If BackgroundGeolocation adapter is instantiated headless and is enabled, force ActivityRecognitionService to start.
- [Added] Add `mock` to `locationTemplate` data.

## [3.2.1] - 2019-08-05
- [Added] Added android script to purge SDK's debug sound-files from release build.
- [Changed] Rebuild iOS `TSLocationManager.framework` with XCode 10.  Replace `@available` macro with `SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO`.
- [Fixed] iOS 13 preventSuspend was not working with iOS 13.  iOS has once again decreased the max time for UIApplication beginBackgroundTask from 180s down to 30s.
- [Added] Implement Cordova "browser" platform with a Mock implementation of BackgroundGeolocation for use while developing in browser.
- [Changed] Upgrade `android-logback` dependency to `2.0.0`
- [Changed] Android: move some plugin initialization into background-threads (eg: `performLogCleanup`) to help mitigate against ANR "`Context.startForegroundService` did not then call `Service.startForeground`".
- [Added] Capacitor Support.  See README for Capacitor Setup Instructions.
- [Fixed] Android Initial headless events can be missed when app booted due to motion transition event.
- [Fixed] Android crash with EventBus `Subscriber already registered error`.
- [Fixed] iOS `Crash: [TSHttpService postBatch:error:] + 6335064 (TSHttpService.m:253)`

## [3.2.0] - 2019-08-17
- [Added] iOS 13 support.

## [3.1.0] - 2019-08-07
- [Fixed] Android Geofence `DWELL` transition (`notifyOnDwell: true`) not firing.
- [Fixed] iOS `logMaxDays` was hard-coded to `7`; Config option not being respected.
- [Added] Android `Q` support (API 29) with new iOS-like location permission model which now requests `When In Use` or `Always`.  Android now supports the config option `locationAuthorizationRequest` which was traditionally iOS-only.  Also, Android Q now requires runtime permission from user for `ACTIVITY_RECOGNITION`.
- [Changed] Another Android tweak to mitigate against error `Context.startForegroundService() did not then call Service.startForeground()`.

## [3.0.8] - 2019-06-28
- [Fixed] iOS / Android issues with odometer and `getCurrentPosition` when used with `maximumAge` constraint.  Incorrect, old location was being returned instead of latest available.
- [Fixed] Some Android methods were executing the callback in background-thread, exposed when using flutter dev channel (`#insertLocation`, `#getLocations`, `#getGeofences`, `#sync`).
- [Fixed] Add `intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)` to `DeviceSettings` request for Android 9 compatibility.
- [Changed] Tweaks to Android services code to help guard against error `Context.startForegroundService() did not then call Service.startForeground()`.
- [Fixed] iOS manual `sync` crash in simulator while executing callback when error response is returned from server.

## [3.0.7] - 2019-06-17
- [Fixed] iOS & Android:  Odometer issues:  clear odometer reference location on `#stop`, `#startGeofences`.
- [Fixed] Odometer issues: Android must persist its odometer reference location since the foreground-service is no longer long-lived and the app may be terminated between motionchange events.
- [Fixed] Return `Service.START_REDELIVER_INTENT` from `HeartbeatService` to prevent `null` Intent being delivered to `HeartbeatService`, causing a crash.
- [Added] Implement Android [LocationSettingsRequest](https://developer.android.com/training/location/change-location-settings#get-settings).  Determines if device settings is currently configured according to the plugin's desired-settings (eg: gps enabled, location-services enabled).  If the device settings differs, an automatic dialog will perform the required settings changes automatically when user clicks [OK].
- [Fixed] Android `triggerActivities` was not implemented refactor of `3.x`.

## [3.0.6] - 2019-06-04
- [Fixed] Android `destroyLocations` callback was being executed in background-thread.
- [Fixed] When Android geofence API receives a `GEOFENCE_NOT_AVAILABLE` error (can occur if Wifi is disabled), geofences must be re-registered.
- [Fixed] Android `Config.disableStopDetection` was not implemented.
- [Added] Add new Android Config options `scheduleUseAlarmManager` for forcing scheduler to use `AlarmManager` insead of `JobService` for more precise scheduling.

## [3.0.5] &mdash; 2019-05-14
--------------------------------------------------------------------
### :warning: Breaking Changes

### Android License Configuration

The Android license configuration mechanism of the plugin using `--variable LICENSE` is one of the biggest recurring support requests received.  From now on, Android `license_key` will be configured using a `<config-file />` block in your `config.xml` file (See updated README [Configuring the plugin](./README.md#large_blue_diamond-configuring-the-plugin)):

With this change, your license key will **never be deleted** when you remove the plugin and you'll never have to resort to the Wiki *License Validation Failure*.

- Open `config.xml`:  Add the following *namespace* attribute to the top-level `<widget>` element:

```diff
<widget
  id="com.foo.bar"
  version="1.0.0"
  xmlns="http://www.w3.org/ns/widgets"
+ xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:cdv="http://cordova.apache.org/ns/1.0">
```

- Within the `<platform name="android">` container, add the `license_key` key using a `<config-file />` element:

```xml
<platform name="android">
      <!-- background-geolocation -->
      <config-file parent="/manifest/application" target="app/src/main/AndroidManifest.xml">
          <meta-data
            android:name="com.transistorsoft.locationmanager.license_key"
            android:value="YOUR_LICENSE_KEY_HERE" />
      </config-file>
      <!-- /background-geolocation -->
</platform>
```

:warning: On older version of Cordova, If you **change your license key** after building android, you might receive an error:
```diff
BUILD FAILED in 1s

-Element meta-data#com.transistorsoft.locationmanager.license at AndroidManifest.xml duplicated
-with element declared at AndroidManifest.xml duplicated with element declared at AndroidManifest.xml
```

Simply remove and re-add the android platform:

```
$ cordova platform remove android
$ cordova platform add android
```

### iOS Location Authorization Strings

The iOS *Location Authorization Strings* have been migrated to the same `<config-file> />` mechanism.  The following plugin config `--variable` have been removed:
- `LOCATION_ALWAYS_AND_WHEN_IN_USE_USAGE_DESCRIPTION`
- `LOCATION_ALWAYS_USAGE_DESCRIPTION`
- `LOCATION_WHEN_IN_USE_USAGE_DESCRIPTION`
- `MOTION_USAGE_DESCRIPTION`

You can now manage these strings in `config.xml` using the following `<config-file />` elements:

```xml
<platform name="ios">
    <!-- background-geolocation -->
    <config-file parent="NSLocationAlwaysAndWhenInUseUsageDescription" target="*-Info.plist">
        <string>[CHANGEME] Background location tracking is required for our app so we can...</string>
    </config-file>
    <config-file parent="NSLocationAlwaysUsageDescription" target="*-Info.plist">
        <string>[CHANGEME pre-iOS11.  No longer used with iOS 12] Background location tracking is required for our app so we can...</string>
    </config-file>
    <config-file parent="NSLocationWhenInUseUsageDescription" target="*-Info.plist">
        <string>[CHANGEME].  Background location tracking is required for our app so we can...</string>
    </config-file>
    <config-file parent="NSMotionUsageDescription" target="*-Info.plist">
        <string>[CHANGEME] Device motion updates help determine when the device is stationary so the app can save power by turning off location-updates</string>
    </config-file>
    <!-- /background-geolocation -->
</platform>
```


--------------------------------------------------------------------


## [3.0.4] &mdash; 2019-05-10
- [Changed] Rollback `android-permissions` version back to `0.1.8`.  It relies on `support-annotations@28`.  This isn't a problem if one simply upgrades their `targetSdkVersion` but the support calls aren't worth the hassle, since the latest version doesn't offer anything the plugin needs.

## [3.0.3] - 2019-05-08
- [Fixed] iOS: changing `pauseslocationUpdatesAutomatically` was not being applied.
- [Changed] `reset` parameter provided to `#ready` has now been default to `true`.  This causes too many support issues for people using the plugin the first time.
- [Fixed] Android threading issue where 2 distinct `SingleLocationRequest` were issued the same id.  This could result in the foreground service quickly starting/stopping until `locationTimeout` expired.
- [Fixed] Android issue where geofences could fail to query for new geofences-in-proximity after a restart.
- [Fixed] Android issues re-booting device with location-services disabled or location-authorization revoked.
- [Added] Implement support for [Custom Android Notification Layouts](/../../wiki/Android-Custom-Notification-Layout).

## [3.0.2] - 2019-04-25
- [Fixed] Android bug where service repeatedly starts/stops after rebooting the device with plugin in *moving* state.
- [Fixed] Android headless `heartbeat` events were failing (incorrect `Context` was supplied to the event).

## [3.0.1] - 2019-04-17
- [Fixed] Typescript API missing methods `getProviderState`, `requestPermission`.
- [Added] Expose plugin config `--variable` `OKHTTP_VERSION`.  People are reporting conflicts with `cordova-plugin-advanced-http`, which also imports `okhttp`.

## [3.0.0] - 2019-04-10

## [3.0.0-rc.4] - 2019-03-31
- [Fixed] Android: Another `NullPointerException` with `Bundle#getExtras`.

## [3.0.0-rc.3] - 2019-03-29

- [Fixed] Android `NullPointerException` with `Bundle#getExtras` (#674).
- [Fixed] Android not persisting `providerchange` location when location-services re-enabled.

## [3.0.0-rc.2] - 2019-03-27
- [Fixed] An Android foreground-service is launched on first install and fails to stop.

## [3.0.0-rc.1] - 2019-03-26

------------------------------------------------------------------------------
### :warning: Breaking Changes

#### [Changed] The license format has changed.  New `3.0.0` licenses are now available for customers in the [product dashboard](https://www.transistorsoft.com/shop/customers).
![](https://dl.dropbox.com/s/3ohnvl9go4mi30t/Screenshot%202019-03-26%2023.07.46.png?dl=1)

- For versions `< 3.0.0`, use *old* license keys.
- For versions `>= 3.0.0`, use *new* license keys.
------------------------------------------------------------------------------

### Fixes
- [Fixed] Logic bugs in MotionActivity triggering between *stationary* / *moving* states.

### New Features

- [Added] Android implementation for `useSignificantChangesOnly` Config option.  Will request Android locations **without the persistent foreground service**.  You will receive location updates only a few times per hour:

#### `useSignificantChangesOnly: true`:
![](https://dl.dropboxusercontent.com/s/wdl9e156myv5b34/useSignificantChangesOnly.png?dl=1)

#### `useSignificantChangesOnly: false`:
![](https://dl.dropboxusercontent.com/s/hcxby3sujqanv9q/useSignificantChangesOnly-false.png?dl=1)

- [Added] Android now implements a "stationary geofence", just like iOS.  It currently acts as a secondary triggering mechanism along with the current motion-activity API.  You will hear the "zap" sound effect when it triggers.  This also has the fortunate consequence of allowing mock-location apps (eg: Lockito) of being able to trigger tracking automatically.

- [Added] The SDK detects mock locations and skips trigging the `stopTimeout` system, improving location simulation workflow.
- [Added] Android-only Config option `geofenceModeHighAccuracy` for more control over geofence triggering responsiveness.  Runs a foreground-service during geofences-only mode (`#startGeofences`).  This will, of course, consume more power.
```dart
await BackgroundGeolocation.ready({
  geofenceModeHighAccuracy: true,
  desiredAccuracy: BackgroundGeolocation.DESIRED_ACCURACY_MEDIUM,
  locationUpdateInterval: 5000,
  distanceFilter: 50
));

BackgroundGeolocation.startGeofences();
```

#### `geofenceModeHighAccuracy: false` (Default)

- Transition events are delayed in favour of lower power consumption.

![](https://dl.dropboxusercontent.com/s/6nxbuersjcdqa8b/geofenceModeHighAccuracy-false.png?dl=1)

#### `geofenceModeHighAccuracy: true`

- Transition events are nearly instantaneous at the cost of higher power consumption.

![](https://dl.dropbox.com/s/w53hqn7f7n1ug1o/geofenceModeHighAccuracy-true.png?dl=1)

- [Added] Android implementation of `startBackgroundTask` / `stopBackgroundTask`.
```dart
  int taskId = await BackgroundGeolocation.startBackgroundTask();

  // Do any work you like -- it's guaranteed to run, regardless of background/terminated.
  // Your task has exactly 30s to do work before the service auto-stops itself.

  getDataFromServer('https://foo.bar.com').then((result) => {
    // Be sure to always signal completion of your taskId.
    BackgroundGeolocation.stopBackgroundTask(taskId);
  }).catch((error) => {
    // Be sure to always signal completion of your taskId.
    BackgroundGeolocation.stopBackgroundTask(taskId);
  });
```
Logging for Android background-tasks looks like this (when you see an hourglass, a foreground-service is active)
```
 [BackgroundTaskManager onStartJob] ⏳ startBackgroundTask: 6
 .
 .
 .
 [BackgroundTaskManager$Task stop] ⏳ stopBackgroundTask: 6
```
- [Added] New custom Android debug sound FX.  See the [Config.debug](https://transistorsoft.github.io/cordova-background-geolocation/interfaces/_cordova_background_geolocation_.config.html#debug) for a new decription of iOS / Android sound FX **including a media player to play each.**
![](https://dl.dropbox.com/s/zomejlm9egm1ujl/Screenshot%202019-03-26%2023.10.50.png?dl=1)

:warning: These debug sound FX consume about **1.4MB** in the plugin's `tslocationmanager.aar`.  These assets can easily be stripped in your `release` builds by adding the following gradle task to your `app/build.gradle` (I'm working on an automated solution within the context of the plugin's `build.gradle`; so far, no luck).  [Big thanks](https://github.com/transistorsoft/react-native-background-geolocation-android/issues/667#issuecomment-475928108) to @mikehardy.
```gradle
/**
 * Purge Background Geolocation debug sounds from release build.
 */
def purgeBackgroundGeolocationDebugResources(applicationVariants) {
    applicationVariants.all { variant ->
        if (variant.buildType.name == 'release') {
            variant.mergeResources.doLast {
                delete(fileTree(dir: variant.mergeResources.outputDir, includes: ['raw_tslocationmanager*']))

            }
        }
    }
}

android {
    //Remove debug sounds from BackgroundGeolocation plugin
    purgeBackgroundGeolocationDebugResources(applicationVariants)

    compileSdkVersion rootProject.ext.compileSdkVersion
    .
    .
    .
}
```

### Removed
- [Changed] Removed Android config option **`activityRecognitionInterval`** and **`minimumActivityRecognitionConfidence`**.  The addition of the new "stationary geofence" for Android should alleviate issues with poor devices failing to initiate tracking.  The Android SDK now uses the more modern [ActivityTransistionClient](https://medium.com/life360-engineering/beta-testing-googles-new-activity-transition-api-c9c418d4b553) API which is a higher level wrapper for the traditional [ActivityReconitionClient](https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognitionClient).  `AcitvityTransitionClient` does not accept a polling `interval`, thus `actiivtyRecognitionInterval` is now unused.  Also, `ActivityTransitionClient` emits similar `on_foot`, `in_vehicle` events but no longer provides a `confidence`, thus `confidence` is now reported always as `100`.  If you've been implementing your own custom triggering logic based upon `confidence`, it's now pointless.  The `ActivityTransitionClient` will open doors for new features based upon transitions between activity states.

```
╔═════════════════════════════════════════════
║ Motion Transition Result
╠═════════════════════════════════════════════
╟─ 🔴  EXIT: walking
╟─ 🎾  ENTER: still
╚═════════════════════════════════════════════
```

### Maintenance
- [Changed] Update `android-permissions` dependency to `0.1.8`.

## [3.0.0-beta.5] - 2019-03-20
- [Fixed] Logic bugs in MotionActivity triggering between *stationary* / *moving* states.
- [Added] Android-only Config option `geofenceModeHighAccuracy` for more control over geofence triggering accuracy.  Runs a foreground-service during geofences-only mode (`#startGeofences`).
- [Added] Android implementation for `useSignificantChangesOnly` Config option.  Will request Android locations **without the persistent foreground service**.  You will receive location updates only a few times per hour.
- [Changed] Update `android-permissions` dependency to `0.1.8`.

## [3.0.0-beta.4] - 2019-03-02
- [Fixed] Android bug in Config dirty-fields mechanism.

## [3.0.0-beta.3] - 2019-03-02
- [Changed] Improve trackingMode state-changes between location -> geofences-only.
- [Changed] Improvements to geofences-only tracking.
- [Changed] Improvements to stationary-geofence monitoring, detecting mock locations to prevent stopTimeout triggering.

## [3.0.0-beta.2] - 2019-02-28
- [Changed] Tweaking stationary region monitoring.
- [Changed] Tweaking bad vendor detection to force stopTimeout timer when device is stationary for long periods and motion api hasn't responded.

## [3.0.0-beta.1] - 2019-02-27
- [Changed] Major refactor of Android Service architecture.  The SDK no longer requires a foreground-service active at all times.  The foreground-service (and cooresponding persistent notification) will only be active while the SDK is in the *moving* state.  No breaking dart api changes.
- [Changed] Improved Android debug notifications.

## [2.15.1] 2019-02-20
- [Added] Added new Config options `persistMode` for specifying exactly which events get persisted: location | geofence | all | none.
- [Added] Experimental Android-only Config option `speedJumpFilter (default 300 meters/second)` for detecting location anomalies.  The plugin will measure the distance and apparent speed of the current location relative to last location.  If the apparent speed is > `speedJumpFilter`, the location will be ignored.  Some users, particularly in Australia, curiously, have had locations suddenly jump hundreds of kilometers away, into the ocean.
- [Changed] iOS and Android will not perform odometer updates when the calculated distance is less than the average accuracy of the current and previous location.  This is to prevent small odometer changes when the device is lingering around the same position.

## [2.15.0] 2019-02-07
- [Added] New `DeviceSettings` API for redirecting user to Android Settings screens, including vendor-specific screens (eg: Huawei, OnePlus, Xiaomi, etc).  This is an attempt to help direct the user to appropriate device-settings screens for poor Android vendors as detailed in the site [Don't kill my app](https://dontkillmyapp.com/).
- [Added] `schedule` can now be configured to optionally execute geofences-only mode (ie: `#startGeofences`) per schedule entry.  See `schedule` docs.
- [Changed] Upgrade to Gradle `implementation` mechanism instead of deprecated `compile`.

## [2.14.6] 2019-01-11
- [Changed] Android Service: Return `START_STICKY` instead of `START_REDELIVER_INTENT`.
- [Changed] Android: `setShowBadge(false)` on Android `NotificationChannel`.  Some users reporting that Android shows a badge-count on app icon when service is started / stopped.

## [2.14.5] 2018-12-18
- [Fixed] Android `extras` provided to `watchPosition` were not being appended to location data.

## [2.14.4] 2018-12-13
- [Fixed] Android NPE in `watchPosition`
- [Added] Added method `getProviderState` for querying current state of location-services.
- [Added] Added method `requestPermission` for manually requesting location-permission (`#start`, `#getCurrentPosition`, `#watchPosition` etc, will already automatically request permission.

## [2.14.3] 2018-12-06
- [Changed] Upgrade Android logger dependency to latest version (`logback`).
- [Fixed] Prevent Android foreground-service from auto-starting when location permission is revoked via Settings screen.
- [Fixed] NPE in Android HTTP Service when manual sync is called.  Probably a threading issue with multiple sync operations executed simultaneously.

## [2.14.2] 2018-11-22
- [Fixed] Typescript definitions not loading with Ionic 4.  Requires `"types":"./src/ionic/index.d.ts"` in `package.json`.

## [2.14.1] 2018-11-20
- [Added] Android SDK 28 requires new permission to use foreground-service.
- [Fixed] Do not calculate odometer with geofence events.  Android geofence event's location timestamp does not correspond with the actual time the geofence fires since Android is performing some heuristics in the background to ensure the potential geofence event is not a false positive.  The actual geofence event can fire some minutes in the future (ie: the location timestamp will be some minutues in the past).  Using geofence location in odometer calculations will corrupt the odometer value.
- [Fixed] Android could not dynamically update the `locationTemplate` / `geofenceTemplate` without `#stop` and application restart.
- [Fixed] Android `startGeofences` after revoking & re-granting permission would fail to launch the plugin's Service.

## [2.14.0] 2018-10-29
- [Fixed] Android `NullPointerException` on `WatchPositionCallback` with `watchPosition`.

## [2.14.0-beta.2] 2018-10-23
- [Breaking] Change signature of `#getCurrentPosition` method:  Options `{}` is now first argument rather than last:
- [iOS] Catch `NSInvalidArgumentException` when decoding `TSConfig`.  References #1781.

## [2.14.0-beta.1] 2018-10-16

- [Added] Implement Typescript API.  No more `let bgGeo = (<any>window).BackgroundGeolocation`!

```typescript
// Import the SDK in addition to any desired interfaces:
import BackgroundGeolocation, {
  State,
  Config,
  Location,
  LocationError,
  Geofence,
  HttpEvent,
  MotionActivityEvent,
  ProviderChangeEvent,
  MotionChangeEvent,
  GeofenceEvent,
  GeofencesChangeEvent,
  HeartbeatEvent,
  ConnectivityChangeEvent
} from "cordova-background-geolocation";

```

- [Added] Refactor documentation.  Now auto-generated from Typescript api with [Typedoc](https://typedoc.org/) and served from https://transistorsoft.github.io/cordova-background-geolocation
- [Added] With the new Typescript API, it's necessary to add dedicated listener-methods for each method (in order for code-assist to work).
```javascript
// Old:  No code-assist for event-signature with new Typescript API
BackgroundGeolocation.on('location', (location) => {}, (error) => {});
// New:  use dedicated listener-method #onLocation
BackgroundGeolocation.onLocation((location) => {}, (error) => {});
// And every other event:
BackgroundGeolocation.onMotionChange(callback);
BackgroundGeolocation.onMotionProviderChange(callback);
BackgroundGeolocation.onActivityChange(callback);
BackgroundGeolocation.onHttp(callback);
BackgroundGeolocation.onGeofence(callback);
BackgroundGeolocation.onGeofencesChange(callback);
BackgroundGeolocation.onSchedule(callback);
BackgroundGeolocation.onConnectivityChange(callback);
BackgroundGeolocation.onPowerSaveChange(callback);
BackgroundGeolocation.onEnabledChange(callback);
```
- [Breaking] Change event-signature of `enabledchange` event to return simple `boolean` instead of `{enabled: true}`:  It was pointless to return an `{}` for this event.
```javascript
// Old
BackgroundGeolocation.onEnabledChange((enabledChangeEvent) => {
  console.log('[enabledchange] -' enabledChangeEvent.enabled);
})
// New
BackgroundGeolocation.onEnabledChange((enabled) => {
  console.log('[enabledchange] -' enabled);
})
```
- [Breaking] Changed event-signature of `http` event.  There is no more `failure` callback -- HTTP failures will be provided to your single `callback`.
```
// Old
BackgroundGeolocation.on('http', (response) => {
  console.log('[http] success -', response);
}, (response) => {
  console.log('[http] FAILURE -', response);
})

// New
BackgroundGeolocation.onHttp((response) => {
  if (response.success) {
  	console.log('[http] success -', response);
  } else {
  	console.log('[http] FAILURE -', response);
  }
})
```

## [2.13.2] - 2018-10-01
- [Fixed] iOS was missing Firebase adapter hook for persisting geofences.
- [Changed] Android headless events are now posted with using `EventBus` instead of `JobScheduler`.  Events posted via Android `JobScheduler` are subject to time-slicing by the OS so events could arrive late.

## [2.13.1] - 2018-08-29
- [Fixed] iOS scheduler not being initialized in `#ready` after reboot.

## [2.13.0]
- [Added] New Android config-option `notificationChannelName` for configuring the notification-channel required by the foreground-service notification.  See *Settings->Apps & Notificaitions->Your App->App Notifications*.
- [Added] Support for new [Firebase Adapter](https://github.com/transistorsoft/cordova-background-geolocation-firebase)
- [Added] iOS support for HTTP method `PATCH` (Android already supports it).
- [Fixed] Android was not using `httpTimeout` with latest `okhttp3`.
- [Fixed] Android issue not firing `providerchange` on boot when configured with `stopOnTerminate: true`
- [Fixed] Android `headlessJobService` class could fail to be applied when upgrading from previous version.  Ensure always applied.
- [Fixed] Android `httpTimeout` was not being applied to new `okhttp3.Client#connectionTimeout`
- [Fixed] iOS `preventSuspend` was not working with `useSignificantChangesOnly`
- [Changed] iOS disable encryption on SQLite database file when "Data Protection" capability is enabled with `NSFileProtectionNone` so that plugin can continue to insert records while device is locked.

## [2.12.2] - 2018-05-25
- [Changed] Reduce required `cordova` version from `8.0.0` to `7.1.0`.
- [Fixed] iOS issue when plugin is booted in background in geofences-only mode, could engage location-tracking mode.
- [Fixed] Android `getCurrentPosition` was not respecting `persist: true` when executed in geofences-only mode.

## [2.12.1] - 2018-05-17
- [Fixed] iOS geofence exit was being ignored in a specific case where (1) geofence was configured with `notifyOnDwell: true` AND (2) the app was booted in the background *due to* a geofence exit event.

## [2.12.0] - 2018-05-16
- [Added] `cordova-android@7.0.0` support.  Cordova 8 is now required.  This is the same library versions for both iOS and Android as used in `2.11.0`.

## [2.11.0] - 2018-05-09
- [Fixed] Android bug where plugin could fail to translate iOS desiredAccuracy value to Android value, resulting in incorrect `desiredAccuracy` value for Android, probably defaulting to `DESIRED_ACCURACY_LOWEST`.

## [2.11.0-beta.14] - 2018-05-02
- [Fixed] iOS was not persiting odometer.
- [Fixed] iOS geofence exit event not being executed due to a condition where a stationary event occurs while geofence exit events are awaiting their location to be returned.
- [Added] iOS config `disableLocationAuthorizationAlert` for disabling automatic location-authorization alert when location-services are disabled or user changes toggles location access (eg: `Always` -> `WhenInUse`).
- [Fixed] Fixed issue executing `#getCurrentPosition` from Headless mode while plugin is current disabled.
- [Added] Add new iOS `locationAuthorizationRequest: "Any"` for allowing the plugin to operate in either `Always` or `WhenInUse` without being spammed by location-authorization dialog.
- [Fixed] iOS `stopAfterElapsedMinutes` was not being evaluated during `heartbeat` event.
- [Changed] Re-structure Android lib `tslocationmanager.aar` as a Maven repo.
- [Added] Added new initialization method `#ready`, desigend to replace `#configure` (which is now deprectated).  The new `#ready` method operates in the same manner as `#configure` with a crucial difference -- the plugin will only apply the supplied configuration `{}` at the first launch of your app &mdash; thereafter, it will automatically load the last-known config from persistent storage.
- [Added] Add new method `#reset` for resetting the plugin configuration to documented defaults.
- [Added] Refactor Javascript API to use Promises.  Only `#watchPosition` and adding event-listeners with `#on` will not use promises.
- [Fixed] iOS issue not turning of "keepAlive" system when `#stop` method is executed while stop-detection system is engaged.
- [Added] Android will fire `providerchange` event after the result of user location-authorization (accept/deny).  The result will be available in the `status` key of the event-object.
- [Changed] Refactor native configuration system for both iOS and Android with more traditional Obj-c / Java API.
- [Changed] Create improved Obj-c / Java APIs for location-requests (`#getCurrentPosition`, `#watchPosition`) and geofencing.
- [Added] Added new event `connectivitychange` for detecting network connectivity state-changes.
- [Added] Added new event `enabledchange`, fired with the plugin enabled state changes.  Executing `#start` / `#stop` will cause this event to fire.  This is primarily designed for use with `stopAfterElapsedMinutes`.

## [2.10.1] - 2018-02-04
- [Fixed] Android `enableHeadless: true`:  The plugin cannot include a default `BackgroundGeolocationHeadlessTask.java` with a `<source-file />` element, since adding plugin and platform on initial install places the `<resource-file />`  from application's `plugin.xml` first, causing the `cordova plugin add` to complain about an existing file `BackgroundGeolocationHeadlessTask.java`.  Have to not place the default source-file and use refelction instead.  This is unfortunate since removing the plugin won't remove the user's custom BackgroundGeolocationHeadlessTask.java from the src tree, causing compilation errors.  Simply removing / re-adding the android platform will solve this issue.

## [2.10.0] - 2018-02-03
- [Fixed] Guard usage of `powersavechange` event for iOS < 9
- [Added] Android permissions are now handled completely within `tslocationmanager` library rather than within Cordova Activity.
- [Fixed] iOS `emailLog` issues:  sanity check existence of email client, ensure we have reference to topMost `UIViewController`.
- [Added] New Android "Headless" mechanism allowing you provide a simple custom Java class to receive all events from the plugin when your app is terminated (with `stopOnTerminate: false`).  The headless mechanism is enabled with new `@config {Boolean} enableHeadless`.  See the Wiki "Headless Mode" for details.
- [Fixed] iOS `getCurrentPosition` was applying entire options `{}` as `extras`.
- [Fixed] iOS `watchPosition` / `getCurrentPosition` `@option persist` was being ignored when plugin was disabled (ie: `#stop`ped).
- [Fixed] Implement Android `JobScheduler` API for scheduler (where API_LEVEL) allows it.  Will fallback to existing `AlarmManager` implementation where API_LEVEL doesn't allow `JobScheduler`.  This fixes issues scheduler issues with strict new Android 8 background-operation rules.
- [Added] Added new Android `@config {Boolean} allowIdenticalLocations [false]` for overriding the default behaviour of ignoring locations which are identical to the last location.

## [2.9.1] - 2017-11-12
- [Fixed] Rare issue with iOS where **rapidly** toggling executing `start` with `changePace(true)` in the callback followed by `stop`, over and over again, would lock up the main thread.
- [Changed] Android `GEOFENCE_INITIAL_TRIGGER_DWELL` defaulted to `true`.
- [Fixed] `Proguard-Rules` were not ignoring the new `LogFileProvider` used for `#emailLog` method.
- [Fixed] Android issue on some device where callback to `#configure` would not be executed in certain cases.

## [2.9.0] - 2017-11-09
- [Fixed] Android NPE on `Settings.getForegroundService()` when using `foregroundService: false`
- [Fixed] Android 8 error with `emailLog`.  Crash due to `SecurityException` when writing the log-file.  Fixed by implementing `FileProvider` (storage permissions no longer necessary).
- [Fixed] iOS bug when providing non-string `#header` values.  Ensure casted to String.
- [Changed] Android minimum required play-services version is `11.2.0` (required for new `play-services` APis.  Anything less and plugin will crash.
- [Changed] Update Android to use new [`FusedLocationProviderClient`](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) instead of now-deprectated `FusedLocationProviderAPI`.  It's the same underlying play-services location API -- just with a much simpler, less error-prone interface to implement.
- [Fixed] On Android, when `changePace(true)` is executed while device is currently `still` (and remains `still`), `stopTimeout` timer would never initiate until device movement is detected.
- [Fixed] iOS manual `#sync` was not executing *any* callback if database was empty.
- [Added] Expose Android variable `APPCOMPAT_VERSION` allowing customization of the plugin's required dependency `com.android.support:appcompat-v7` (default `27.0.0`).  This dependency is required for Android 8 API support.
- [Added] Implement new Android 8 `NotificationChannel` which is now required for displaying the `foregroundService` notification.
- [Added] New Android `<variable name="GOOGLE_API_VERSION" />` in `config.xml`.  This new `<variable />` is only possible to use in Cordova version `>= 7.1.0`.  This new variable helps to solve the old problem when multiple plugins require `play-services` of a different version, causing build failures.  The `GOOGLE_API_VERSION` allows you to configure the `play-services-location` version to align with the version used by other plugins (eg: `cordova-plugin-googlemaps`, `phonegap-plugin-push`, etc).
- [Added] Android foreground-service notification now uses `id: 9942585`.  If you wish to interact with the foreground-service notification in native code, this is the `id`.
- [Fixed] iOS not always firing location `failure` callback.
- [Fixed] iOS was not forcing an HTTP flush on `motionchange` event when `autoSyncThreshold` was used.
- [Fixed] iOS Add sanity-check for Settings `boolean` type.  It was possible to corrupt the Settings when a `boolean`-type setting was provided with a non-boolean value (eg: `{}`, `[]`).
- [Fixed] Android `getState` could cause an NPE if executed before `#configure`.
- [Fixed] Work around iOS 11 bug with `CLLocationManager#stopMonitoringSignificantLocationChanges` (SLC):  When this method is called upon *any* single `CLLocationManager` instance, it would cause *all* instances to `#stopMonitoringSignificantLocationChanges`.  This caused problems with Scheduler evaluation, since SLC is required to periodically evaluate the schedule.

## [2.8.5] - 2017-09-25
- [Added] Build for iOS 11, XCode 9.
- [Added] Implement new `powersavechange` event in addition to `isPowerSaveMode` method for determining if OS "Power saving" mode is enabled.
- [Added] New config `elasticityMultiplier` for controlling the scale of `distanceFilter` elasticity calculation.
- [Fixed] Android bug not firing `schedule` Javascript listeners
- [Fixed] Android crash `onGooglePlayServicesConnectError` when Google Play Services needs to be updated on device.

## [2.8.4] - 2017-09-14

- [Changed] Refactor Android `onDestroy` mechanism attempting to solve nagging and un-reproducible null pointer exceptions.
- [Fixed] Fixed bug not where `stopAfterElapsedMinutes` is not evaluated when executing `#getCurrentPosition`.
- [Fixed] Modifications for Android O.  For now, `foregroundService: true` will be enforced when running on Android O (api 26).

## [2.8.2] - 2017-08-28
- [Fixed] Android `stopOnTerminate` was not setting the `enabled` value to `false` when terminated.  This caused the plugin to automatically `#start` the first time the app was booted (it would work correctly every boot thereafter).
- [Changed] iOS `motionchange` position will be fetch by `CLLocationManager#startUpdatingLocation` rather than `#requestLocation`, since `#requestLocation` cannot keep the app alive in the background.  This could cause the app to be suspended when `motionchange` position was requested due to a background-fetch event.
- [Changed] Change Android HTTP layer to use more modern library `OkHttp3` instead of `Volley`.  Some users reported weird issues with some devices on some servers.  `OkHttp` seems to have solved it for them.  `OkHttp` is a much simpler library to use than `Volley`
- [Changed] `play-services-location` dependency pinned to `:11.+` instead of `:+` in order to prevent build-issues with plugin's using Google's `fcm`.  I've created a new plugin to solve Google API conflicts (eg: `play-services`): [`cordova-google-api-version`](https://github.com/transistorsoft/cordova-google-api-version)

## [2.8.1] - 2017-08-21
- [Changed] Reference latest `cordova-plugin-background-fetch` version `5.0.0`
- [Added] Javascript API to plugin's logging system.
- [Fixed] Minor issue with iOS flush where multiple threads might create multiple background-tasks, leaving some unfinished.

## [2.8.0] - 2017-08-15
- [Changed] Refactor iOS/Android core library event-subscription API.
- [Changed] Removed `taskId` supplied to all event-callbacks.  You no longer have to call `bgGeo.finish(taskId)` in your event-callbacks (though the method will still operate as a `noop` for backwards compatibility).  You will now be responsible for creating your own iOS background-tasks using the method `#startBackgroundTask` when performing long-running tasks in your event-callbacks.
- [Added] iOS and Android now support ability to remove single event-listeners with method `#un`

## [2.7.5] - 2017-07-27
- [Changed] Remove dependency `cordova-plugin-dialogs`.  It's not required.
- [Changed] Improve iOS/Android acquisition of `motionchange` location to ensure a recent location is fetched.
- [Changed] Implement `#getSensors` method for both iOS & Android.  Returns an object indicating the presense of *accelerometer*, *gyroscope* and *magnetometer*.  If any of these sensors are missing, the motion-detection system for that device will poor.
- [Changed] The `activitychange` success callback method signature has been changed from `{String} activityName` -> `{Object}` containing both `activityName` as well as `confidence`.  This event only used to fire after the `activityName` changed (eg: `on_foot` -> `in_vehicle`), regardless of `confidence`.  This event will now fire for *any* change in activity, including `confidence` changes.
- [Changed] iOS `emailLog` will gzip the attached log file.
- [Added] Implement new Android config `notificationPriority` for controlling the behaviour of the `foregroundService` notification and notification-bar icon.
- [Fixed] Android was creating a foreground notification even when `foregroundService: false`
- [Changed] Tweak iOS Location Authorization to not show locationAuthorizationAlert if user initially denies location permission.
- [Fixed] Android:  Remove isMoving condition from geofence proximity evaluator.
- [Fixed] iOS 11 fix:  Added new location-authorization string `NSLocationAlwaysAndWhenInUseUsageDescription`.  iOS 11 now requires location-authorization popup to allow user to select either `Always` or `WhenInUse`.

## [2.7.4] - 2017-07-10
- [Fixed] Android & iOS will ensure old location samples are ignored with `getCurrentPosition`
- [Fixed] Android `providerchange` event would continue to persist a providerchange location even when plugin was disabled for the case where location-services is disabled by user.
- [Fixed] Don't mutate iOS `url` to lowercase.  Just lowercase the comparison when checking for `301` redirects.
- [Changed] Android will attempt up to 5 motionchange samples instead of 3.
- [Changed] Android foregroundService notification priority set to `PRIORITY_MIN` so that notification doesn't always appear on top.
- [Fixed] Android plugin was not nullifying the odometer reference location when `#stop` method is executed, resulting in erroneous odometer calculations if plugin was stopped, moved some distance then started again.
- [Added] Android plugin will detect presense of Sensors `ACCELEROMETER`, `GYROSCOPE`, `MAGNETOMETER` and `SIGNIFICANT_MOTION`.  If any of these sensors are missing, the Android `ActivityRecognitionAPI` is considered non-optimal and plugin will add extra intelligence to assist determining when device is moving.
- [Fixed] Bug in broadcast event `GEOFENCE` not being fired when `MainActivity` is terminated (only applies to those using a `BroadcastReceiver`).
- [Fixed] Android scheduler issue when device is rebooted and plugin is currently within a scheduled ON period (fails to start)
- [Fixed] (Android) Fix error calling `stopWatchPosition` before `#configure` callback has executed.  Also add support for executing `#getCurrentPosition` before `#configure` callback has fired.
- [Added] (Android) Listen to LocationResult while stopTimeout is engaged and perform manual motion-detection by checking if location distance from stoppedAtLocation is > stationaryRadius
- [Fixed] Bug in literal schedule parsing for both iOS and Android
- [Fixed] Bug in Android scheduler after app terminated.  Configured schedules were not having their `onTime` and `offTime` zeroed, resulting in incorrect time comparison.

## [2.7.3] - 2017-06-15
- [Fixed] Bug in Android scheduler after app terminated.  Configured schedules were not having their `SECOND` and `MILLISECOND` zeroed resulting in incorrect time comparison.

## [2.7.2] - 2017-06-14
- [Added] New config `stopOnStationary` for both iOS and Android.  Allows you to automatically `#stop` tracking when the `stopTimeout` timer elapses.
- [Added] Support for configuring the "Large Icon" (`notificationLargeIcon`) on Android `foregroundService` notification.  `notificationIcon` has now been aliased -> `notificationSmallIcon`.
- [Fixed] iOS timing issue when fetching `motionchange` position after initial `#start` -- since the significant-location-changes API (SLC) is engaged in the `#stop` method and eagerly returns a location ASAP, that first SLC location could sometimes be several minutes old and come from cell-tower triangulation (ie: ~1000m accuracy).  The plugin could mistakenly capture this location as the `motionchange` location instead of waiting for the highest possible accuracy location that was requested.  SLC API will be engaged only after the `motionchange` location has been received.
- [Fixed] On Android, when adding a *massive* number of geofences (ie: *thousands*), it can take several minutes to perform all `INSERT` queries.  There was a threading issue which could cause the main-thread to be blocked while waiting for the database lock from the geofence queries to be released, resulting in an ANR (app isn't responding) warning.
- [Changed] Changing the Android foreground-service notification is now supported (you no longer need to `#stop` / `#start` the plugin for changes to take effect).
- [Fixed] Improved Android handling of simultaneous `#getCurrentPosition`, `#start`, `#configure` requests when location-services are not yet authorized by the user (the plugin will buffer all these requests and execute them in order once location-services are authorized).
- [Added] New config option `httpTimeout` (milliseconds) for configuring the timeout where the plugin will give up on sending an HTTP request.
- [Fixed] When iOS engages the `stopTimeout` timer, the OS will pause background-execution if there's no work being performed, in spite of `startBackgroundTask`, preventing the `stopTimeout` timer from running.  iOS will now keep location updates running at minimum accuracy during `stopTimeout` to prevent this.
- [Fixed] Ensure iOS background "location" capability is enabled before asking `CLLocationManager` to `setBackgroundLocationEnabled`.
- [Added] Implement ability to provide literal dates to schedule (eg: `2017-06-01 09:00-17:00`)
- [Added] When Android motion-activity handler detects `stopTimeout` has expired, it will initiate a `motionchange` without waiting for the `stopTimeout` timer to expire (there were cases where the `stopTimeout` timer could be delayed from firing due likely to vendor-based battery-saving software)
- [Fixed] Android `emailLog` method was using old `adb logcat` method of fetching logs rather than fetching from `#getLog`

## [2.7.1] - 2017-05-12
- [Fixed] iOS has a new hook to execute an HTTP flush when network reachability is detected.  However, it was not checking if `autoSync: true` or state of `autoSyncThreshold`.

## [2.7.0] - 2017-05-08
- [Added] When iOS detects a network connection with `autoSync: true`, an HTTP flush will be initiated.
- [Fixed] Improve switching between tracking-mode location and geofence.  It's not necessary to call `#stop` before executing `#start` / `#startGeofences`.
- [Fixed] iOS issue with `cordova-plugin-cocoalumberjack` dependency issue with Cordova 7.0:  plugin version (should be `~0.0.2`, not `^0.0.2`)
- [Fixed] iOS `maximumAge` with `getCurrentPosition` wasn't clearing the callbacks when current-location-age was `<= maximumAge`
- [Fixed] iOS when `#stop` is executed, nullify the odometer reference location.
- [Fixed] iOS issue with `preventSuspend: true`:  When a `motionchange` event with `is_moving: false` occurred, the event was incorrectly set to `heartbeat` instead of `motionchange`.
- [Fixed] Android null pointer exception when using `startOnBoot: true, forceReloadOnBoot: true`:  there was a case where last known location failed to return a location.  The lib will check for null location in this case.
- [Changed] iOS minimum version is now `8.4`.  Plugin will log an error when used on versions of iOS that don't implement the method `CLLocationManager#requestLocation`
- [Fixed] iOS bug executing `#setConfig` multiple times too quickly can crash the plugin when multiple threads attempt to modify an `NSMutableDictionary`

## [2.6.1] - 2017-04-18
- [Fixed] Android was rounding `battery_level` to 1 decimal place
- [Fixed] iOS geofences-only mode was not using significant-location-change events to evaluate geofences within proximity.
- [Changed] iOS now uses `CLLocationManager requestLocation` to request the `motionchange` position, rather than counting samples.  This is a more robust way to get a single location
- [Fixed] iOS crash when providing `null` values in `Object` config options (ie: `#extras`, `#params`, `#headers`, etc)
- [Added] New config param `locationsOrderDirection [ASC|DESC]` for controlling the order that locations are selected from the database (and syned to your server)
- [Added] iOS now supports geofence `DWELL` with `loiteringDelay` with my own custom implementation, just as Android does natively.
- [Fixed] iOS was creating `backgroundTask` in `location` listener even if no listeners were registered, resulting in growing list of background-tasks which would eventually be `FORCE KILLED`.

## [2.6.0] - 2017-03-09
- [Fixed] iOS bug when composing geofence data for peristence.  Sometimes it appended a `location.geofence.location` due to a shared `NSDictionary`
- [Fixed] Android issue with applying default settings the first time an app boots.  If you execute `#getState` before `#configure` is called, `#getState` would return an empty `{}`.
- [Changed] The licensing model of Android now enforces license only for **release** builds.  If an invalid license is configured while runningin **debug** mode, a Toast warning will appear **"BackgroundGeolocation is running in evaluation mode."**, but the plugin *will* work.
- [Fixed] iOS bug with HTTP `401` handling.
- [Added] The Android plugin now broadcasts all its events using the Android `BroadcastReceiver` mechanism.  You're free to implement your own native Android handler to receive and react to these events as you wish.

## [2.5.3] - 2017-03-01
- [Changed] Refactor Android settings-management.  Plugin will always load previously known state as soon as plugin comes alive.  `#configure` will reset all settings to default before applying supplied `{Config}`.
- [Fixed] Android database migration issue when upgrading from a very old version missed `geofences` table migration.

## [2.5.1] - 2017-02-26
- [Changed] Refactor iOS settings-management.  Plugin will always load previously known state as soon as plugin comes alive.  `#configure` will reset all settings to default before applying supplied `{Config}`.
- [Fixed] iOS Schedule evaluation edge-case when a current-schedule is referenced but expired: When evaulating, always check if current-schedule is expired; query for next if so.
- [Fixed] GeofenceManager edge-case:  GeofenceManager should not receive current location when plugin is disabled (eg: executing `#getCurrentPosition` when plugin is disabled).

- [Fixed] `geofence` event not passing configured geofence `#extras`.
- [Changed] Removed `taskId` from `geofence` event callback.  This change is backwards compatible.  If you want to do a long-running task, create your own `bgTask` with `#startBackgroundTask` (the plan is to remove `taskId` from **all** callbacks. Eg:

```javascript
bgGeo.on('geofence', function(geofence) {  // <-- taskId no longer provided!
  // Start your own bgTask:
  bgGeo.startBackgroundTask(function(taskId) {
    performLongRunningTask(function() {
      bgGeo.finish(taskId);
    });
  });
});
```

## [2.5.0] - 2017-02-21
- [Fixed] iOS geofence identifiers containing ":" character were split and only the last chunk returned.  The plugin itself prefixes all geofences it creates with the string `TSGeofenceManager:` and the string-splitter was too naive.  Uses a `RegExp` replace to clear the plugin's internal prefix.
- [Changed] Refactored API Documentation
- [Added] HTTP JSON template features.  See [HTTP Features](./docs/http.md).  You can now template your entire JSON request data sent to the server by the plugin's HTTP layer.

## [2.4.0] - 2017-02-08
- [Changed] **BREAKING** I've *finally* figured out how to configure a number of key variables required by the plugin within your `config.xml` file, namely the `NSLocationAlwaysUsageDescription`, `NSLocationWhenInUseUsageDescription`, `NSMotionUsageDescription`.  The plugin now requires a `<plugin />` config within your `config.xml`.  **BREAKING:** With the introduction of this new config mechanism, I decided to use this also for the Android `license` config.  You will no longer provide the `<parameter name="cordova-background-geolocation-license" />`.  See the [README](https://github.com/transistorsoft/cordova-background-geolocation/tree/config-xml-variables#configuring-the-plugin) for details.

```xml
<widget id="com.your.company.app.id">
  <plugin name="cordova-background-geolocation" spec="^2.4.0">
    <variable name="LOCATION_ALWAYS_USAGE_DESCRIPTION" value="Background location-tracking is required" />
    <variable name="LOCATION_WHEN_IN_USE_USAGE_DESCRIPTION" value="Background location-tracking is required" />
    <variable name="MOTION_USAGE_DESCRIPTION" value="Using the accelerometer increases battery-efficiency by intelligently toggling location-tracking only when the device is detected to be moving" />
    <variable name="BACKGROUND_MODE_LOCATION" value="location" />
    <variable name="BACKGROUND_MODE_AUDIO" value="" />
  </plugin>
```
- [Fixed] Migrate Android `providerchange` mechanism out of the `Service` (which only runs when the plugin is `#start`ed) to a place where it will be monitored all the time, regardless if the plugin is enabled or not.
- [Fixed] Catch `IllegalStateException` reported when using `#getLog`
- [Changed] With new Android "Doze-mode", override "idle" on `stopTimeout` and `schedule` alarms
- [Changed] Tweak iOS accelerometer-only motion-detection system.
- [Fixed] Location-authorization alert being popped up after a `suspend` event because the plugin always attempts to ensure it has a stationary-region here.  Simply check current authorization-status is not == `Denied`.

## [2.3.1] - 2017-01-13
- [Fixed] iOS Location Authorization alert is shown multiple time.  Also discovered a bug where the `providerchange` `enabled` value was calculated based upon hard-coded `Always` where it should have compared to the configured `locationAuthorizationRequest`.
- [Added] If plugin's `#stop` method is called, the Location Authorization Alert will be hidden (if currently visible).

## [2.3.0] - 2017-01-09
- [Fixed] Locale issue when formatting Floats.  Some locale use "," as decimal separator.  Force Locale -> US when performing rounding.  Proper locale will be applied during the JSON encoding.
- [Added] Ability to provide optional arbitrary meta-data `extras` on geofences.
- [Changed] Location parameters `heading`, `accuracy`, `odometer`, `speed`, `altitude`, `altitudeAccuracy` are now fixed at 2 decimal places.
- [Fixed] Bug reported with `EventBus already registered` error.  Found a few cases where `EventBus.isRegistered` was not being used.
- [Added] Android will attempt to auto-sync on heartbeat events.
- [Changed] permission `android.hardware.location.gps" **android:required="false"**`
- [Added] Implement `IntentFilter` to capture `MY_PACKAGE_REPLACED`, broadcast when user upgrades the app.  If you've configured `startOnBoot: true, stopOnTerminate: false` and optionally `foreceRelaodOnBoot: true`, the plugin will automatically restart when user upgrades the app.
- [Changed] When adding a geofence (either `#addGeofence` or `#addGeofences`), if a geofence already exists with the provided `identifier`, the plugin will first destroy the existing one before creating the new one.
- [Changed] When iOS Scheduler is engaged and a scheduled OFF event occurs, the plugin will continue to monitor significant-changes, since background-fetch events alone cannot be counted on.  This will guarantee the plugin evaluates the schedule each time the device moves ~ 1km.  This will have little impact on power consumption, since these sig.change events will not be persisted or `POST`ed, nor will they even be provided to Javascript.
- [Changed] Android Scheduler will `setExact` Alarm triggers (only works for API `>= KITKAT` or if OEM's OS doesn't override it (ie: Samsung)).
- [Fixed] iOS Scheduler was not listening to `BackgroundFetch` events while plugin was disabled, preventing schedule evaluation from fetch-events (user would have to open the app for scheduler to evaluate).
- [Fixed] `stopWatchPostion` callbacks not being called.
- [Fixed] Use more precise Alarm mechanism for `stopTimeout`
- [Fixed] Improve odometer accuracy.  Introduce `desiredOdometerAccuracy` for setting a threshold of location accuracy for calculating odometer.  Any location having `accuracy > desiredOdometerAccuracy` will not be used for odometer calculation.
- [Fixed] When configured with a schedule, the Schedule parser wasn't ordering the schedule entries by start-time.
- [Fixed] Had a report of null-pointer exception when processing an HTTP error response.  I could not reproduce the issue but find a case where accessing a `String` could produce a NPE.
- [Changed] Add ability to set odometer to any arbitrary value.  Before, odometer could only be reset to `0` via `resetOdometer`.  The plugin now uses `setOdometer(Float, successFn, failureFn`.  `resetOdometer` is now just an alias for `setOdometer(0)`.  `setOdometer` will now internally perform a `#getCurrentPosition`, so it can know the exact location where the odometer was set at.  As a result, using `#setOdometer` is exactly like performing a `#getCurrentPosition` and the `success` / `failure` callbacks use the same method-signature, where the `success` callback is provided the `location`.
- [Added] Added ability to create your own arbitrary **background tasks** with new `#startBackgroundTask` method.  Some of the plugin's methods receive a `taskId` which you've had to call `bgGeo.finish(taskId)` upon.  These automatically created `taskId` will soon be removed.  It will be **up to you** to create your own as desired, when you need to perform any long-running task in any of the plugin's callbacks.  `#finish` operates in the same manner as before.

## [2.2.0] - 2016-11-21
- [Fixed] Issue #1025 Bug with Android geofences not posting `event: geofence` and the actual `geofence` data was missing (The data sent to Javascript callback was ok, just the data sent to HTTP.
- [Fixed] Issue #1023 Logic bug in `TSGeofenceManager`; was not performing geospatial query when changing state from **MOVING -> STATIONARY**.
- [Added] Geofences-only mode for both iOS and Android **BETA**.  Start geofences-only mode with method `#startGeofences`.
- [Changed] Add some intelligence to iOS motion-detection system:  Use a Timer of `activityRecognitionInterval` seconds before engaging location-services after motion is detected.  This helps to reduce false-positives, particularly when using `preventSuspend` while walking around one's house or office.
- [Changed] Add more intelligence to iOS motion-detection system:  The plugin will be **eager** to engage the stop-detection, as soon as it detects `still`, regardless of confidence.  When the plugin is currently in the **moving** state and detects `still`, it will engage a timer of `activityRecognitionInterval` milliseconds -- when this timer expires and the motion-detector still reports `still`, the stop-detection system will be engaged.  If any *moving* type activity occurs during this time, the timer will be cancelled.  References #1002.
- [Fixed] Bug in Android Scheduler, failing to `startOnBoot`.  Issue #985
- [Added] `#removeListeners` method.  Removes all listeners registered with plugin via `#on` method.
- [Changed] With `preventSuspend: true`, the plugin will no longer immediately engage location-services as soon as it sees a "moving"-type motion-activity:  it will now calculate if the current position is beyond stationary geofence. This helps reduce false-positives engaging location-services while simply walking around one's home or office.
- [Fixed] iOS `batchSync`: When only 1 record in batch, iOS fails to pack the records in a JSON `location: []`, appending to a `location: {}` instead.  Fixes #1042

## [2.1.6] - 2016-11-08
- [Fixed] Android was only handling the first geofence event when multiple geofences fire simultaneously.  Issue #1004
- [Changed] The plugin will ignore `autoSyncThreshold` when a `motionchange` event occurs.
- [Fixed] Fixed ui-blocking issue when plugin boots with locations in its database with `autoSync: true`.  Found a case where the plugin was executing HTTP Service on the UI thread.  Fixes #995.
- [Fixed] Return current `state {Object}` in callback to `setConfig` (issue #985)
- [Fixed] iOS Scheduler puked when provided with a `null` or `[]` schedule.
- [Changed] iOS Scheduler behaviour changed to match Android, where `#stopSchedule` does **not** execute `#stop` on the plugin itself.

## [2.1.5] - 2016-11-04
- [Fixed] Issue #998.  FMDB [has issues](https://github.com/ccgus/fmdb/pull/180) binding array arguments (eg: DELETE FROM locations WHERE id IN(?)).  Solution is to simply compose the query string with concatenation.  Sanitization isn't required here anyway, since the ids come directly from my own query.

## [2.1.4] - 2016-11-02
- [Changed] Extract `CococaLumberjack` static-libary from compiled binary TSLocationManager.  It causes problems if other libs also use this dependency.  Extracted CocoaLumberjack to its own distinct plugin `cordova-plugin-cocoalumberjack`, which background-geolocation installs as a dependency.  This change should be completely transparent.

## [2.1.3] - 2016-10-19
- [Changed] Introduce database-logging for Android with [logback-android](https://github.com/tony19/logback-android).  Same API as iOS (@see `2.1.0`)
- [Fixed] iOS geofencing issue where multiple geofences trigger simultaneously, only the last geofence event would be transmitted to the client and persisted to database.
- [Fixed] Remove iOS motion-activity-based filtering of locations.  If a location was recorded while the motion-recognition system said the device was `still`, the location was ignored.  Fixes issue #954.
- [Changed] Implemented ability for iOS to trigger a geofence `ENTER` event immediately when device is already inside the geofence (Android has always done this).  This behaviour can be controlled with the new config `@param {Boolean} geofenceInitialTriggerEntry [true]`.  This behaviour defaults to `true`.

## [2.1.2] - 2016-10-17
- [Changed] Android will filter-out received locations detected to be same-as-last by comparing `latitude`, `longitude`, `speed` & `bearing`.
- [Fixed] Bug in `stopDetectionDelay` logic
- [Fixed] Geofencing transistion event logging wouldn't occur when configured for `debug: false`

## [2.1.1] - 2016-10-12
- [Fixed] Bug in Android geofencing

## [2.1.0] - 2016-10-10
- [Changed] Refactor iOS Logging system to use popular CocoaLumberjack library.  iOS logs are now stored in the database!  By default, logs are stored for 3 days, but is configurable with `logMaxDays`.  Logs can now be filtered by logLevel:

| logLevel | Label |
|---|---|
|`0`|`LOG_LEVEL_OFF`|
|`1`|`LOG_LEVEL_ERROR`|
|`2`|`LOG_LEVEL_WARNING`|
|`3`|`LOG_LEVEL_INFO`|
|`4`|`LOG_LEVEL_DEBUG`|
|`5`|`LOG_LEVEL_VERBOSE`|

`#getLog`, `#emailLog` operate in the same manner as before.

- [Fixed] If user declines "Motion Activity" permission, plugin failed to detect this authorization failure and fallback to the accelerometer-based motion-detection system.

- [Changed] Refactored Geolocation system.  The plugin is no longer bound by native platform limits on number of geofences which can be monitored (iOS: 20; Android: 100).  You may now monitor infinite geofences.  The plugin now stores geofences in its SQLite db and performs a geospatial query, activating only those geofences in proximity of the device (@config #geofenceProximityRadius, @event `geofenceschange`).  See the new [Geofencing Guide](./docs/geofencing.md)

## [2.0.13] - 2016-09-25
- [Fixed] Background-fetch event when causing app to boot in background, left plugin in preventSuspend mode when not configured to do so.

## [2.0.12] - 2016-09-25
- [Fixed] Bug in prevent-suspend where background-fetch operation where plugin was left in preventSuspend mode when not configured to do do

## [2.0.11] - 2016-09-22
- [Fixed] Bug in prevent-suspend where the plugin failed to re-start its prevent-suspend timer if no MotionActivity event occurred during that interval.  Prevent-suspend system should now operate completely independently of MotionDetector.
- [Fixed] `#stop` method wasn't calling `stopMonitoringSignificantChanges`, resulting in location-services icon failing to toggle OFF.  Fixes issue #908

## [2.0.10] - 2016-09-22
- [Fixed] Issue where iOS crashes when configured with null url.
- [Added] iOS `watchPosition` mechanism.
- [Changed] Refactored iOS motion-detection system.  Improved iOS motion-triggering when using `CMMotionActivityManager` (ie: when not using `disableMotionActivityUpdates: true`).  iOS can now trigger out of stationary-mode just like android, where it sees a 'moving-type' motion-activity (eg: 'on_foot', 'in_vehicle', etc).  Note: this will still occur only when your app isn't suspended (eg: app is in foreground, `preventSuspend: true`, or `#watchPosition` is engaged).
- [Changed] Refactored iOS "prevent suspend" system to be more robust.
- [Fixed] iOS locations sent to Javascript client had a different `uuid` than the one persisted to database (and synced to server).
-[Added] new iOS 10 .plist required key for accelerometer updates `NSMmotionUsageDescription` to `config.xml`.  Fixes #880
- [Added] New required android permission `<uses-feature android:name="android.hardware.location.gps" />`.  Fixes #896

## [2.0.9] - 2016-08-29
- [Fixed] `removeGeofences` was removing stationary-region.  This would prevent stationary-exit if called while device is in stationary-mode
- [Fixed] Issue #830 Android pukes when it receives an empty schedule `[]`.
- [Fixed] Issue #824.  Android when configured with `batchSync: true, autoSync: true` was failing because the plugin automatically tweaked `autoSync: false` but failed to reset it to the configured value.  This behaviour was obsolete and has been removed.
- [Added] Add new config `@param {Integer} autoSyncThreshold [0]`.  Allows you to specify a minimum number of persisted records to trigger an auto-sync action.
- [Fixed] Issue #837.  Android `SimpleDateFormat` used for rendering location timestamp was not being used in a thread-safe manner, resulting in corrupted timestamps for some

## [2.0.8] - 2016-08-17
- [Fixed] Issue #804, null pointer exeception on mGoogleApiClient
- [Fixed] Issue #806.  PlayServices connect error event was fired before listeners arrive; Dialog to fix problem was never shown.
- [Changed] Removed `app-compat` from Gradle dependencies.
- [Changed] Fire http error callback when HTTP request is not 200ish (ie: 200, 201, 204).  Fixes issue #819.  Contradicts #774.
- [Changed] Remove `play-services:app-compat-v7` from Gradle dependencies
- [Fixed] Android heartbeat location wasn't having its meta-data updated (ie: `event: 'heartbeat', battery:<current-data>, uuid: <new uuid>`)
- [Changed] Reduce Android `minimumActivityRecognitionConfidence` default from `80` to `75` (issue #825)
- [Changed] Android will ask for location-permission when `#configure` is executed, rather than waiting for `#start`.
- [Changeed] Android will catch `java.lang.SecurityException` when attempting to request location-updates without "Location Permission"

## [2.0.7] - 2016-08-08
- [Fixed] Scheduler parsing issue #785.

## [2.0.6] Skipped to sync version with Lite version
## [2.0.5] - 2016-08-07
- [Fixed] `addGeofences` issue #778
- [Fixed] iOS setting `method` not being respected (was always doing `POST`).  Issue #770
- [Changed] Implement latest version of `cordova-plugin-background-fetch` dependency (v4.0.0)
- [Changed] iOS Plugin will perform HTTP sync on background-fetch event.
- [Changed] Implemented latest version of `cordova-plugin-background-fetch@4.0.0`.

## [2.0.4] - 2016-07-28
- [Changed] Major Android refactor with significant architectural changes.  Introduce new `adapter.BackgroundGeolocation`, a proxy between the Cordova plugin and `BackgroundGeolocationService`.  Up until now, the functionality of the plugin has been contained within a large, monolithic Android Service class.  This monolithic functionality has mostly been moved into the proxy object now, in addition to spreading among a number of new Helper classes.  The functionality of the HTTP, SQLite, Location and ActivityRecognition layers are largely unchanged, just re-orgnanized.  This new structure will make it much easier going forward with adding new features.
- [Changed] SQLite & HTTP layers have been moved from the BackgroundGeolocationService -> the proxy.  This means that database & http operations can now be performed without enabling the plugin with start() method.
- [Changed] Upgrade EventBus to latest.
- [Changed] Implement new watchPosition method (Android-only), allowing you to get a continues stream of locations without using Javascript setInterval (Android-only currently)
- [Added] Implement new event "providerchange" allowing you to listen to Location-services change events (eg: user turns off GPS, user turns off location services).  Whenever a "providerchange" event occurs, the plugin will automatically fetch the current position and persist the location adding the event: "providerchange" as well as append the provider state-object to the location.
- [Changed] Significantly simplified Cordova plugin (CDVBackgroundGeolocation.java) by moving boiler-plate code into the Proxy object.  This significantly simplifies the Cordova plugin, making it much easier to support all the different frameworks the plugin has been ported to (ie: React Native, NativeScript).
- [Changed] Disable iOS start-detection system when no accelerometer detected (ie: when running in simulator, fixes issue #767)
- [Changed] Refactor iOS location-authorization request system.  The plugin will now constantly check the status of location-authorization and show an Alert popup directing the user to the **Settings** screen if the user changes the state to anything other than what you requested (eg: user changes authorization-request to `WhenInUse` or `Never` when you requestd `Always`)
- [Fixed] `stopOnTerminate` on iOS was broken when app is closed while in background.

## [1.7.0] - 2016-07-18
- [Changed] `Scheduler` will use `Locale.US` in its Calendar operations, such that the days-of-week correspond to Sunday=1..Saturday=7.  Fixes issue #659
- [Changed] Refactor odometer calculation for both iOS and Android.  No longer filters out locations based upon average location accuracy of previous 10 locations; instead, it will only use the current location for odometer calculation if it has accuracy < 100.
- [Fixed] Missing iOS setting `locationAuthorizationRequest after Settings service refactor
- [Added] **Android-only currently, beta** New geofence-only tracking-mode, where the plugin will not actively track location, only geofences.  This mode is engaged with the new API method `#startGeofences` (instead of `#start`, which engages the usual location-tracking mode).  `#getState` returns a new param `#trackingMode [location|geofences]`
- [Changed] **Android** Refactor the Android "Location Request" system to be more robust and better handle "single-location" requests / asynchronous requests, such as `#getCurrentPosition` and stationary-position to fetch multiple samples (3), selecting the most accurate (ios has always done this, heard with the debug sound `tick`).  In debug mode, you'll hear these location-samples with new debug sound "US dialtone".  Just like iOS, these location-samples will be returned to your `#onLocation` event-listener with the property `{sample: true}`.  If you're doing your own HTTP in Javascript, you should **NOT** send these locations to your server.  Use them instead to simply update the current position on the Map, if you're using one.
- [Added] new `#getCurrentPosition` options `#samples` and `#desiredAccuracy`. `#samples` allows you to configure how many location samples to fetch before settling sending the most accurate to your `callbackFn`.  `#desiredAccuracy` will keep sampling until an location having accuracy `<= desiredAccuracy` is achieved (or `#timeout` elapses).
- [Added] new `#event` type `heartbeat` added to `location` params (`#is_heartbeat` is **@deprecated**).
- [Fixed] Issue #676.  Don't engage foreground-service when executing `#getCurrentPosition` while plugin is in disabled state.
- [Fixed] When enabling iOS battery-state monitoring, use setter method `setBatteryMonitoringEnabled` rather than setting property.  This seems to have changed with latest iOS
- [Fixed] error-callback incorrectly fired when `getCurrentPosition` is called while service is stopped (Fixes issue #691)
- [Added] Implement `disableStopDetection` for Android (Fixes issue #692)
- [Changed] `android.permission.GET_TASKS` changed to `android.permission.GET_REAL_TASKS`.  Hoping this removes deprecation warning.  This permission is required for Android `#forceReload` configs.
- [Added] New Anddroid config `#notificationIcon`, allowing you to customize the icon shown on notification when using `foregroundServcie: true`.
- [Changed] Take better care with applying `DEFAULT` settings for both iOS & Android.
- [Changed] Default settings: `startOnBoot: false`, `stopOnTerminate: true`, `distanceFilter: 10`.
- [Added] Allow setting `isMoving` as a config param to `#configure`.  Allows the plugin to automatically do a `#changePace` to your desired value when the plugin is first `#configure`d.
- [Added] New event `activitychange` for listening to changes from the Activit Recognition system.  See **Events** section in API docs for details.  Fixes issue #703.
- [Added] Allow Android `foregroundService` config to be changed dynamically with `#setConfig` (used to have to restart the start to apply this).
- [Added] Implement the new `#getCurrentPosition` options `#samples` and `#desiredAccuracy` for iOS.
- [Changed] Revert GET_REAL_TASKS back to GET_TASKS
## [1.6.3] - 2016-05-25
- [Fixed] Rebuild binary `tslocationmanager.aar` excluding dependencies `appcompat-v7` and `play-services`.  I was experiencing build-failures with react-native since other libs may include these dependencies:

## [1.6.2] - 2016-05-24
- [Fixed] Android `GeofenceService` namespace was changed but the `plugin.xml` file was not updated with the new namespace.

## [1.6.1] - 2016-05-22
- [Changed] Refactor iOS motion-detection system.  When not set to `disableMotionActivityUpdates` (default), the  plugin will not activate the accelerometer and will rely instead purely upon updates from the **M7** chip.  When `disableMotionActivityUpdates` **is** set to `false`, the pure acceleromoeter based activity-detection has been improved to give more accurate results of the detected activity (ie: `on_foot, walking, stationary`)
- [Fixed] Fixed configuration bug which could incorrectly set `pausesLocationUpdatesAutomatically` to false, preventing the plugin from automatically turning off location-updates.

## [1.6.0] - 2016-05-17
- [Added] Implement new Scheduling feature for iOS and Android.
- [Fixed] Bugs in iOS option `useSignificantChangesOnly`
- [Changed] Refactor HTTP Layer for both iOS and Android to stop spamming server when it returns an error (used to keep iterating through the entire queue).  It will now stop syncing as soon as server returns an error (good for throttling servers).
- [Added] Migrate iOS settings-management to new Settings service
- [Fixed] bugs in Scheduler
- [Fixed] Fix bug where Android was not JSON decoding the location object sent to heartbeat event #646
- [Added] Better BackgroundFetch plugin integration.  Background-fetch will retrieve the location-state when a fetch event fires.  This can help to trigger stationary-exit since bg-fetch typically fires about every 15min.
- [Added] Improved functionality with `stopOnTerminate: false`.  Ensure a stationary-geofence is created when plugin is closed while in **moving** state; this seems to improve the time it takes to trigger the iOS app awake after terminate.  When plugin *is* rebooted in background due to geofence-exit, the plugin will briefly sample the accelerometer to see if device is currently moving.

## [1.5.1] - 2016-04-12
- [Added] ios logic to handle being launched in the background (by a background-fetch event, for example).  When launched in the background, iOS will essentially do a `changePace(true)` upon itself and let the stop-detection system determine engage stationary-mode as detected.
- [Changed] ios halt stop-detection distance was using `distanceFilter`; changed to use `stationaryRadius`.  This effects users using the accelerometer-based stop-detection system:  after stop is detected, the device must move `stationaryRadius` meters away from location where stop was detected.
- [Fixed] Android `addGeofences` callbacks not being called.
- [Changed] When `maxRecordsToPersist == 0`, don't persist any record.
- [Added] Implement `startOnBoot` param for iOS.  iOS always ignored `startOnBoot`.  If you set `startOnBoot: false` now, iOS will not begin tracking when launched in background after device is rebooted (eg: from a background-fetch event, geofence exit or significant-change event)
- [Changed] Modified the method signature of `#configure`.  The config `{}` will now be the 1st param and the `callbackFn` will now signal successful configuration.  The `locationCallback` and `locationErrorCallback` must now be provided with a separate event-listener `#onLocation`.
- [Fixed] Bug in `getCurrentPosition` timeout when plugin is disabled.

## [1.5.0] - 2016-04-04
- [Fixed] Fixed issue #597, `NullPointerException` after `startOnBoot`
- [Fixed] Refactor `startOnBoot` system.  Moved `BootReceiver` to `com.transistorsoft.cordova.bggeo` namespace.
- [Added] New config option `#forceReloadOnBoot` for specifying the MainActivity should launch after a `startOnBoot` occurs.
## [1.5.0] - 2016-04-04
- [Added] Intelligence for `stopTimeout`.  When stop-timer is initiated, save a reference to the current-location.  If another location is recorded while during stop-timer, calculate the distance from location when stop-timer initiated:  if `distance > stationaryRadius`, cancel the stop-timer and stay in "moving" state.
- [Added] New debug sound **"booooop"**:  Signals the initiation of stop-timer of `stopTimeout` minutes.
- [Added] New debug sound **"boop-boop-boop"**: Signals the cancelation of stop-timer due to movement beyond `stationaryRadius`.
- [Fixed] When using setConfig to change `distanceFilter` while location-updates are already engaged, was mistakenly using value for `desiredAccuracy`!
- [Fixed] iOS Issue with timers not running on main-thread.
- [Fixed] iOS Issue with acquriring stationary-location on a stale location.  Ensure the selected stationary-location is no older than 1s.
- [Fixed] iOS Removed some log messages appearing when `{debug: false}`
- [Fixed] Android 5 geofence limit.  Refactored Geofence system.  Was using a `PendingIntent` per geofence; now uses a single `PendingIntent` for all geofences.
- [Fixed] Edge-case issue #540 when executing `#getCurrentPosition` followed immediately by `#start` in cases where location-timeout occurs.
- [Changed] Update Android Volley dependency to official version
- [Changed] When Android #stop is called, update `isMoving` state to `false`
- [Fixed] ios `stopOnTerminate` was defaulting to `false`.  Docs say default is `true`.
- [Fixed] ios `useSignificantChangesOnly` was broken.
- [Fixed] If location-request times-out while acquiring stationary-location, try to use last-known-location
- [Added] Add odometer to ios location JSON schema
- [Added] ios Log network reachability flags on connection-type changes.
- [Added] `maxRecordsToPersist` to limit the max number of records persisted in plugin's SQLite database.
- [Added] API methods `#addGeofences` (for adding a list-of-geofences), `#removeGeofences`
- [Changed] The plugin will no longer delete geofences when `#stop` is called; it will merely stop monitoring them.  When the plugin is `#start`ed again, it will start monitoringt any geofences it holds in memory.  To completely delete geofences, use new method `#removeGeofences`.
- [Fixed] iOS battery `is_charging` was rendering as `1/0` instead of boolean `true/false`
- [Fixed] Issue with `forceReloadOnX` params. These were not forcing the activity to reload on device reboot when configured with `startOnBoot: true`.  Implemented a better method for detecting when main Activity is active.
- [Fixed] `getOdometer` callabck wasn't running if BackgroundGeolocationService wasn't running.
- [Changed] When Activity is `forceReload`ed (eg: via `forceReloadOnLocationChange`), the Activity will automatically be minimized when launched now!

## [1.4.0] - 2016-03-08
- [Changed] Introduce new per-app licensing scheme.  I'm phasing out the unlimited 'god-mode' license in favour of generating a distinct license-key for each bundle ID.  This cooresponds with new Customer Dashboard for generating application keys and managing team-members.

## [1.3.0]

- [Changed] Upgrade `emailLog` method to attach log as email-attachment rather than rendering to email-body.  The result of `#getState` is now rendered to the

##0.6.4

- pin google-play-services at 7.5.0 to help people suffering from Google Play Services bug
- Tweak stop-detection system.  add `#stopDetectionDelay`

![](https://camo.githubusercontent.com/76f20a03f04f455f8219dbbca2a40631cd009fb4/68747470733a2f2f646c2e64726f70626f7875736572636f6e74656e742e636f6d2f752f323331393735352f636f72646f76612d6261636b67726f756e642d67656f6c6f636169746f6e2f696f732d73746f702d646574656374696f6e2d74696d696e672e706e67)

## 0.6.3
- Introduce accelerometer-base stop-detection for ios.
- `@config {Boolean} useSignificantChangesOnly`
- When a geofence event occurs, the associated location will have geolocation meta-data attached to the associated location.  Find it in the POST data.  See Wiki Location Data Schema for details.
