var plugin = function () {
    return window.BackgroundGeolocation;
};
var BackgroundGeolocation = /** @class */ (function () {
    function BackgroundGeolocation() {
    }
    Object.defineProperty(BackgroundGeolocation, "LOG_LEVEL_OFF", {
        get: function () { return plugin().LOG_LEVEL_OFF; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "LOG_LEVEL_ERROR", {
        get: function () { return plugin().LOG_LEVEL_ERROR; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "LOG_LEVEL_WARNING", {
        get: function () { return plugin().LOG_LEVEL_WARNING; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "LOG_LEVEL_INFO", {
        get: function () { return plugin().LOG_LEVEL_INFO; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "LOG_LEVEL_DEBUG", {
        get: function () { return plugin().LOG_LEVEL_DEBUG; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "LOG_LEVEL_VERBOSE", {
        get: function () { return plugin().LOG_LEVEL_VERBOSE; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "DESIRED_ACCURACY_NAVIGATION", {
        get: function () { return plugin().DESIRED_ACCURACY_NAVIGATION; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "DESIRED_ACCURACY_HIGH", {
        get: function () { return plugin().DESIRED_ACCURACY_HIGH; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "DESIRED_ACCURACY_MEDIUM", {
        get: function () { return plugin().DESIRED_ACCURACY_MEDIUM; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "DESIRED_ACCURACY_LOW", {
        get: function () { return plugin().DESIRED_ACCURACY_LOW; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "DESIRED_ACCURACY_VERY_LOW", {
        get: function () { return plugin().DESIRED_ACCURACY_VERY_LOW; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "DESIRED_ACCURACY_THREE_KILOMETER", {
        get: function () { return plugin().DESIRED_ACCURACY_THREE_KILOMETER; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "AUTHORIZATION_STATUS_NOT_DETERMINED", {
        get: function () { return plugin().AUTHORIZATION_STATUS_NOT_DETERMINED; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "AUTHORIZATION_STATUS_RESTRICTED", {
        get: function () { return plugin().AUTHORIZATION_STATUS_RESTRICTED; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "AUTHORIZATION_STATUS_DENIED", {
        get: function () { return plugin().AUTHORIZATION_STATUS_DENIED; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "AUTHORIZATION_STATUS_ALWAYS", {
        get: function () { return plugin().AUTHORIZATION_STATUS_ALWAYS; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "AUTHORIZATION_STATUS_WHEN_IN_USE", {
        get: function () { return plugin().AUTHORIZATION_STATUS_WHEN_IN_USE; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "NOTIFICATION_PRIORITY_DEFAULT", {
        get: function () { return plugin().NOTIFICATION_PRIORITY_DEFAULT; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "NOTIFICATION_PRIORITY_HIGH", {
        get: function () { return plugin().NOTIFICATION_PRIORITY_HIGH; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "NOTIFICATION_PRIORITY_LOW", {
        get: function () { return plugin().NOTIFICATION_PRIORITY_LOW; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "NOTIFICATION_PRIORITY_MAX", {
        get: function () { return plugin().NOTIFICATION_PRIORITY_MAX; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "NOTIFICATION_PRIORITY_MIN", {
        get: function () { return plugin().NOTIFICATION_PRIORITY_MIN; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "ACTIVITY_TYPE_OTHER", {
        get: function () { return plugin().ACTIVITY_TYPE_OTHER; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "ACTIVITY_TYPE_AUTOMOTIVE_NAVIGATION", {
        get: function () { return plugin().ACTIVITY_TYPE_AUTOMOTIVE_NAVIGATION; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "ACTIVITY_TYPE_FITNESS", {
        get: function () { return plugin().ACTIVITY_TYPE_FITNESS; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "ACTIVITY_TYPE_OTHER_NAVIGATION", {
        get: function () { return plugin().ACTIVITY_TYPE_OTHER_NAVIGATION; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(BackgroundGeolocation, "logger", {
        get: function () { return plugin().logger; },
        enumerable: true,
        configurable: true
    });
    BackgroundGeolocation.ready = function (config, success, failure) { return plugin().ready.apply(this, arguments); };
    BackgroundGeolocation.configure = function () { return plugin().configure.apply(this, arguments); };
    BackgroundGeolocation.reset = function () { return plugin().reset.apply(this, arguments); };
    BackgroundGeolocation.onLocation = function () {
        plugin().onLocation.apply(this, arguments);
    };
    BackgroundGeolocation.onMotionChange = function () {
        plugin().onMotionChange.apply(this, arguments);
    };
    BackgroundGeolocation.onHttp = function () {
        plugin().onHttp.apply(this, arguments);
    };
    BackgroundGeolocation.onHeartbeat = function () {
        plugin().onHeartbeat.apply(this, arguments);
    };
    BackgroundGeolocation.onProviderChange = function () {
        plugin().onProviderChange.apply(this, arguments);
    };
    BackgroundGeolocation.onActivityChange = function () {
        plugin().onActivityChange.apply(this, arguments);
    };
    BackgroundGeolocation.onGeofence = function () {
        plugin().onGeofence.apply(this, arguments);
    };
    BackgroundGeolocation.onGeofencesChange = function () {
        plugin().onGeofencesChange.apply(this, arguments);
    };
    BackgroundGeolocation.onSchedule = function () {
        plugin().onSchedule.apply(this, arguments);
    };
    BackgroundGeolocation.onEnabledChange = function (callback) {
        plugin().onEnabledChange.apply(this, arguments);
    };
    BackgroundGeolocation.onConnectivityChange = function (callback) {
        plugin().onConnectivityChange.apply(this, arguments);
    };
    BackgroundGeolocation.onPowerSaveChange = function (callback) {
        plugin().onPowerSaveChange.apply(this, arguments);
    };
    BackgroundGeolocation.on = function () { return plugin().on.apply(this, arguments); };
    BackgroundGeolocation.un = function () { return plugin().un.apply(this, arguments); };
    BackgroundGeolocation.removeListener = function () { return plugin().removeListener.apply(this, arguments); };
    BackgroundGeolocation.removeListeners = function () { return plugin().removeListeners.apply(this, arguments); };
    BackgroundGeolocation.getState = function () { return plugin().getState.apply(this, arguments); };
    BackgroundGeolocation.start = function () { return plugin().start.apply(this, arguments); };
    BackgroundGeolocation.stop = function () { return plugin().stop.apply(this, arguments); };
    BackgroundGeolocation.startSchedule = function () { return plugin().startSchedule.apply(this, arguments); };
    BackgroundGeolocation.stopSchedule = function () { return plugin().stopSchedule.apply(this, arguments); };
    BackgroundGeolocation.startGeofences = function () { return plugin().startGeofences.apply(this, arguments); };
    BackgroundGeolocation.startBackgroundTask = function () { return plugin().startBackgroundTask.apply(this, arguments); };
    BackgroundGeolocation.finish = function () { return plugin().finish.apply(this, arguments); };
    BackgroundGeolocation.changePace = function () { return plugin().changePace.apply(this, arguments); };
    BackgroundGeolocation.setConfig = function () { return plugin().setConfig.apply(this, arguments); };
    BackgroundGeolocation.getLocations = function () { return plugin().getLocations.apply(this, arguments); };
    BackgroundGeolocation.getCount = function () { return plugin().getCount.apply(this, arguments); };
    BackgroundGeolocation.destroyLocations = function () { return plugin().destroyLocations.apply(this, arguments); };
    BackgroundGeolocation.insertLocation = function () { return plugin().insertLocation.apply(this, arguments); };
    BackgroundGeolocation.sync = function () { return plugin().sync.apply(this, arguments); };
    BackgroundGeolocation.getOdometer = function () { return plugin().getOdometer.apply(this, arguments); };
    BackgroundGeolocation.resetOdometer = function () { return plugin().resetOdometer.apply(this, arguments); };
    BackgroundGeolocation.setOdometer = function () { return plugin().setOdometer.apply(this, arguments); };
    BackgroundGeolocation.addGeofence = function () { return plugin().addGeofence.apply(this, arguments); };
    BackgroundGeolocation.removeGeofence = function () { return plugin().removeGeofence.apply(this, arguments); };
    BackgroundGeolocation.addGeofences = function () { return plugin().addGeofences.apply(this, arguments); };
    BackgroundGeolocation.removeGeofences = function () { return plugin().removeGeofences.apply(this, arguments); };
    BackgroundGeolocation.getGeofences = function () { return plugin().getGeofences.apply(this, arguments); };
    BackgroundGeolocation.getCurrentPosition = function () { return plugin().getCurrentPosition.apply(this, arguments); };
    BackgroundGeolocation.watchPosition = function () { return plugin().watchPosition.apply(this, arguments); };
    BackgroundGeolocation.stopWatchPosition = function () { return plugin().stopWatchPosition.apply(this, arguments); };
    BackgroundGeolocation.registerHeadlessTask = function () { return plugin().registerHeadlessTask.apply(this, arguments); };
    BackgroundGeolocation.setLogLevel = function () { return plugin().setLogLevel.apply(this, arguments); };
    BackgroundGeolocation.getLog = function () { return plugin().getLog.apply(this, arguments); };
    BackgroundGeolocation.destroyLog = function () { return plugin().destroyLog.apply(this, arguments); };
    BackgroundGeolocation.emailLog = function () { return plugin().emailLog.apply(this, arguments); };
    BackgroundGeolocation.isPowerSaveMode = function () { return plugin().isPowerSaveMode.apply(this, arguments); };
    BackgroundGeolocation.getSensors = function () { return plugin().getSensors.apply(this, arguments); };
    BackgroundGeolocation.playSound = function () { return plugin().playSound.apply(this, arguments); };
    return BackgroundGeolocation;
}());
export default BackgroundGeolocation;
