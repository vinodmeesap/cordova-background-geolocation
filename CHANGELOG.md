
# Change Log

## [Unreleased]
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
