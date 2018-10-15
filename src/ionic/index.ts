const plugin = function() {
	return (<any>window).BackgroundGeolocation;
}

export default class BackgroundGeolocation {

  static get LOG_LEVEL_OFF() { return plugin().LOG_LEVEL_OFF; }
  static get LOG_LEVEL_ERROR() { return plugin().LOG_LEVEL_ERROR; }
  static get LOG_LEVEL_WARNING() { return plugin().LOG_LEVEL_WARNING; }
  static get LOG_LEVEL_INFO() { return plugin().LOG_LEVEL_INFO; }
  static get LOG_LEVEL_DEBUG() { return plugin().LOG_LEVEL_DEBUG; }
  static get LOG_LEVEL_VERBOSE() { return plugin().LOG_LEVEL_VERBOSE; }

  static get DESIRED_ACCURACY_NAVIGATION() { return plugin().DESIRED_ACCURACY_NAVIGATION; }
  static get DESIRED_ACCURACY_HIGH() { return plugin().DESIRED_ACCURACY_HIGH; }
  static get DESIRED_ACCURACY_MEDIUM() { return plugin().DESIRED_ACCURACY_MEDIUM; }
  static get DESIRED_ACCURACY_LOW() { return plugin().DESIRED_ACCURACY_LOW; }
  static get DESIRED_ACCURACY_VERY_LOW() { return plugin().DESIRED_ACCURACY_VERY_LOW; }
  static get DESIRED_ACCURACY_THREE_KILOMETER() { return plugin().DESIRED_ACCURACY_THREE_KILOMETER; }

  static get AUTHORIZATION_STATUS_NOT_DETERMINED() { return plugin().AUTHORIZATION_STATUS_NOT_DETERMINED; }
  static get AUTHORIZATION_STATUS_RESTRICTED() { return plugin().AUTHORIZATION_STATUS_RESTRICTED; }
  static get AUTHORIZATION_STATUS_DENIED() { return plugin().AUTHORIZATION_STATUS_DENIED; }
  static get AUTHORIZATION_STATUS_ALWAYS() { return plugin().AUTHORIZATION_STATUS_ALWAYS; }
  static get AUTHORIZATION_STATUS_WHEN_IN_USE() { return plugin().AUTHORIZATION_STATUS_WHEN_IN_USE; }

  static get NOTIFICATION_PRIORITY_DEFAULT() { return plugin().NOTIFICATION_PRIORITY_DEFAULT; }
  static get NOTIFICATION_PRIORITY_HIGH() { return plugin().NOTIFICATION_PRIORITY_HIGH; }
  static get NOTIFICATION_PRIORITY_LOW() { return plugin().NOTIFICATION_PRIORITY_LOW; }
  static get NOTIFICATION_PRIORITY_MAX() { return plugin().NOTIFICATION_PRIORITY_MAX; }
  static get NOTIFICATION_PRIORITY_MIN() { return plugin().NOTIFICATION_PRIORITY_MIN; }

  static get ACTIVITY_TYPE_OTHER() { return plugin().ACTIVITY_TYPE_OTHER; }
  static get ACTIVITY_TYPE_AUTOMOTIVE_NAVIGATION() { return plugin().ACTIVITY_TYPE_AUTOMOTIVE_NAVIGATION; }
  static get ACTIVITY_TYPE_FITNESS() { return plugin().ACTIVITY_TYPE_FITNESS; }
  static get ACTIVITY_TYPE_OTHER_NAVIGATION() { return plugin().ACTIVITY_TYPE_OTHER_NAVIGATION; }

  static get logger() { return plugin().logger; }

  static ready(config:any, success?:Function, failure?:Function) { return plugin().ready.apply(this, arguments); }
  static configure() { return plugin().configure.apply(this, arguments); }
  static reset() { return plugin().reset.apply(this, arguments); }

  static onLocation() {
    plugin().onLocation.apply(this, arguments);
  }
  static onMotionChange() {
    plugin().onMotionChange.apply(this, arguments);
  }
  static onHttp() {
    plugin().onHttp.apply(this, arguments);
  }
  static onHeartbeat() {
    plugin().onHeartbeat.apply(this, arguments);
  }
  static onProviderChange() {
    plugin().onProviderChange.apply(this, arguments);
  }
  static onActivityChange() {
    plugin().onActivityChange.apply(this, arguments);
  }
  static onGeofence() {
    plugin().onGeofence.apply(this, arguments);
  }
  static onGeofencesChange() {
    plugin().onGeofencesChange.apply(this, arguments);
  }
  static onSchedule() {
    plugin().onSchedule.apply(this, arguments);
  }
  static onEnabledChange(callback:Function) {
    plugin().onEnabledChange.apply(this, arguments);
  }
  static onConnectivityChange(callback:Function) {
    plugin().onConnectivityChange.apply(this, arguments);
  }
  static onPowerSaveChange(callback:Function) {
    plugin().onPowerSaveChange.apply(this, arguments);
  }

  static on() { return plugin().on.apply(this, arguments); }
  static un() { return plugin().un.apply(this, arguments); }
  static removeListener() { return plugin().removeListener.apply(this, arguments); }
  static removeListeners() { return plugin().removeListeners.apply(this, arguments); }
  static getState() { return plugin().getState.apply(this, arguments); }
  static start() { return plugin().start.apply(this, arguments); }
  static stop() { return plugin().stop.apply(this, arguments); }
  static startSchedule() { return plugin().startSchedule.apply(this, arguments); }
  static stopSchedule() { return plugin().stopSchedule.apply(this, arguments); }
  static startGeofences() { return plugin().startGeofences.apply(this, arguments); }
  static startBackgroundTask() { return plugin().startBackgroundTask.apply(this, arguments); }
  static finish() { return plugin().finish.apply(this, arguments); }
  static changePace() { return plugin().changePace.apply(this, arguments); }
  static setConfig() { return plugin().setConfig.apply(this, arguments); }
  static getLocations() { return plugin().getLocations.apply(this, arguments); }
  static getCount() { return plugin().getCount.apply(this, arguments); }
  static destroyLocations() { return plugin().destroyLocations.apply(this, arguments); }
  static insertLocation() { return plugin().insertLocation.apply(this, arguments); }
  static sync() { return plugin().sync.apply(this, arguments); }
  static getOdometer() { return plugin().getOdometer.apply(this, arguments); }
  static resetOdometer() { return plugin().resetOdometer.apply(this, arguments); }
  static setOdometer() { return plugin().setOdometer.apply(this, arguments); }
  static addGeofence() { return plugin().addGeofence.apply(this, arguments); }
  static removeGeofence() { return plugin().removeGeofence.apply(this, arguments); }
  static addGeofences() { return plugin().addGeofences.apply(this, arguments); }
  static removeGeofences() { return plugin().removeGeofences.apply(this, arguments); }
  static getGeofences() { return plugin().getGeofences.apply(this, arguments); }
  static getCurrentPosition() { return plugin().getCurrentPosition.apply(this, arguments); }
  static watchPosition() { return plugin().watchPosition.apply(this, arguments); }
  static stopWatchPosition() { return plugin().stopWatchPosition.apply(this, arguments); }
  static registerHeadlessTask() { return plugin().registerHeadlessTask.apply(this, arguments); }
  static setLogLevel() { return plugin().setLogLevel.apply(this, arguments); }
  static getLog() { return plugin().getLog.apply(this, arguments); }
  static destroyLog() { return plugin().destroyLog.apply(this, arguments); }
  static emailLog() { return plugin().emailLog.apply(this, arguments); }
  static isPowerSaveMode() { return plugin().isPowerSaveMode.apply(this, arguments); }
  static getSensors() { return plugin().getSensors.apply(this, arguments); }
  static playSound() { return plugin().playSound.apply(this, arguments); }
}