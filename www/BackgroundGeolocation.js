/**
* cordova-background-geolocation
* Copyright (c) 2015, Transistor Software (9224-2932 Quebec Inc)
* All rights reserved.
* sales@transistorsoft.com
* http://transistorsoft.com
* @see LICENSE
*/

var API = require('./API');

var emptyFn = function() {};

module.exports = {
    LOG_LEVEL_OFF: 0,
    LOG_LEVEL_ERROR: 1,
    LOG_LEVEL_WARNING: 2,
    LOG_LEVEL_INFO: 3,
    LOG_LEVEL_DEBUG: 4,
    LOG_LEVEL_VERBOSE: 5,

    // For #desiredAccuracy
    DESIRED_ACCURACY_NAVIGATION: -2,
    DESIRED_ACCURACY_HIGH: -1,
    DESIRED_ACCURACY_MEDIUM: 10,
    DESIRED_ACCURACY_LOW: 100,
    DESIRED_ACCURACY_VERY_LOW: 1000,
    DESIRED_ACCURACY_THREE_KILOMETER: 3000,

    // For providerchange event
    AUTHORIZATION_STATUS_NOT_DETERMINED: 0,
    AUTHORIZATION_STATUS_RESTRICTED: 1,
    AUTHORIZATION_STATUS_DENIED: 2,
    AUTHORIZATION_STATUS_ALWAYS: 3,
    AUTHORIZATION_STATUS_WHEN_IN_USE: 4,

    // For android #notificationPriority
    NOTIFICATION_PRIORITY_DEFAULT: 0,
    NOTIFICATION_PRIORITY_HIGH: 1,
    NOTIFICATION_PRIORITY_LOW: -1,
    NOTIFICATION_PRIORITY_MAX: 2,
    NOTIFICATION_PRIORITY_MIN: -2,

    // For iOS #activityType
    ACTIVITY_TYPE_OTHER: 1,
    ACTIVITY_TYPE_AUTOMOTIVE_NAVIGATION: 2,
    ACTIVITY_TYPE_FITNESS: 3,
    ACTIVITY_TYPE_OTHER_NAVIGATION: 4,

    ready: function(success, failure, defaultConfig) {
        if ((arguments.length == 1) && (typeof(success) === 'object')) {
            return API.ready.apply(API, arguments);
        } else {
            API.ready(defaultConfig).then(success).catch(failure);
        }
    },
    configure: function(config, success, failure) {
        config = config || {};
        if (arguments.length == 1) {
            return API.configure(config);
        } else {
            API.configure(config).then(success).catch(failure);
        }        
    },
    reset: function(config, success, failure) {
        if ((typeof(config) === 'function') ||  (typeof(success) === 'function')) {
            if (typeof(config) === 'function') {
                success = config;
                config = undefined;
            }
            API.reset(config).then(success).catch(failure);
        } else {
            return API.reset(config);
        }        
    },
    on: function(event, success, failure) {
        if (typeof(success) !== 'function') {
            throw "Event '" + event + "' was not provided with a success callback";
        }
        failure = failure || emptyFn;
        API.on(event, success, failure);
    },
    /**
    * @alias #removeListener
    */
    un: function() {
        return this.removeListener.apply(this, arguments);
    },    
    removeListener: function(event, handler, success, failure) {
        if (arguments.length == 2) {
            return API.removeListener(event, handler);
        } else {
            API.removeListener(event, handler).then(success).catch(failure);
        }
    },
    removeListeners: function(success, failure) {
        if (!arguments.length) {
            return API.removeListeners();
        } else {
            API.removeListeners().then(success).catch(failure);
        }
    },
    getState: function(success, failure) {
        if (!arguments.length) {
            return API.getState();
        } else {
            API.getState().then(success).catch(failure);
        }
    },
    start: function(success, failure) {
        if (!arguments.length) {
            return API.start();
        } else {
            API.start().then(success).catch(failure);
        }        
    },
    stop: function(success, failure) {
        if (!arguments.length) {
            return API.stop();
        } else {
            API.stop().then(success).catch(failure);
        }
    },
    startSchedule: function(success, failure) {
        if (!arguments.length) {
            return API.startSchedule();
        } else {
            API.startSchedule().then(success).catch(failure);
        }
    },
    stopSchedule: function(successs, failure) {
        if (!arguments.length) {
            return API.stopSchedule();
        } else {
            API.stopSchedule().then(success).catch(failure);
        }
    },
    startGeofences: function(success, failure) {
        if (!arguments.length) {
            return API.startGeofences(); 
        } else {
            API.startGeofences().then(success).catch(failure);
        }
    },
    startBackgroundTask: function(success, failure) {
        if (!arguments.length) {
            return API.startBackgroundTask();
        } else {
            API.startBackgroundTask().then(success).catch(failure);
        }
    },
    finish: function(taskId, success, failure) {
        if (arguments.length == 1) {
            return API.finish(taskId);
        } else {
            API.finish(taskId).then(success).catch(failure);
        }
    },
    changePace: function(isMoving, success, failure) {
        if (arguments.length == 1) {
            return API.changePace(isMoving);
        } else {
            API.changePace(isMoving).then(success).catch(failure);
        }
    },
    setConfig: function(config, success, failure) {
        if (arguments.length == 1) {
            return API.setConfig(config);
        } else {
            API.setConfig(config).then(success).catch(failure);
        }
    },
    getLocations: function(success, failure) {
        if (!arguments.length) {
            return API.getLocations();
        } else {
            API.getLocations().then(success).catch(failure);
        }
    },
    getCount: function(success, failure) {
        if (!arguments.length) {
            return API.getCount();
        } else {
            API.getCount().then(success).catch(failure);
        }
    },
    destroyLocations: function(success, failure) {
        if (!arguments.length) {
            return API.destroyLocations();
        } else {
            API.destroyLocations().then(success).catch(failure);
        }
    },
    // @deprecated
    clearDatabase: function() {
        this.destroyLocations.apply(this, arguments);
    },
    insertLocation: function(location, success, failure) {
        if (arguments.length == 1) {
            return API.insertLocation(location);
        } else {
            API.insertLocation(location).then(success).catch(failure);
        }
    },
    sync: function(success, failure) {
        if (!arguments.length) {
            return API.sync();
        } else{
            API.sync().then(success).catch(failure);
        }
    },
    getOdometer: function(success, failure) {
        if (!arguments.length) {
            return API.getOdometer();
        } else {
            API.getOdometer().then(success).catch(failure);
        }
    },
    resetOdometer: function(success, failure) {
        this.setOdometer(0, success, failure);
    },
    setOdometer: function(value, success, failure) {
        if (arguments.length == 1) {
            return API.setOdometer(value);
        } else {
            API.setOdometer(value).then(success).catch(failure);
        }
    },
    addGeofence: function(config, success, failure) {
        if (arguments.length == 1) {
            return API.addGeofence(config);
        } else {
            API.addGeofence(config).then(success).catch(failure);
        }
    },
    removeGeofence: function(identifier, success, failure) {
        if (arguments.length == 1) {
            return API.removeGeofence(identifier);
        } else {
            API.removeGeofence(identifier).then(success).catch(failure);
        }
    },
    addGeofences: function(geofences, success, failure) {
        if (arguments.length == 1) {
            return API.addGeofences(geofences);
        } else {
            API.addGeofences(geofences).then(success).catch(failure);
        }
    },
    /**
    * 1. removeGeofences() <-- Promise
    * 2. removeGeofences(['foo'])  <-- Promise
    *
    * 3. removeGeofences(success, [failure])    
    * 4. removeGeofences(['foo'], success, [failure])
    */
    removeGeofences(identifiers, success, failure) {        
        if ((arguments.length <= 1) && (typeof(identifiers) !== 'function'))  {
            return API.removeGeofences(identifiers);
        } else {            
            if (typeof(identifiers) === 'function') {
                // 3. -> removeGeofences(success, failure?)
                failure = success || emptyFn;
                success = identifiers;
                identifiers = [];
            }            
            API.removeGeofences(identifiers).then(success).catch(failure);
        }
    },
    getGeofences: function(success, failure) {
        if (!arguments.length) {
            return API.getGeofences();
        } else {
            API.getGeofences().then(success).catch(failure);
        }
    },
    getCurrentPosition: function(success, failure, options) {
        if (typeof(success) === 'function') {
            if (typeof(failure) === 'object') {
                // Allow -> #getCurrentPosition(success, options)
                options = failure;
                failure = emptyFn;
            }
            options = options || {};
            API.getCurrentPosition(options).then(success).catch(failure);
        } else {
            return API.getCurrentPosition.apply(API, arguments);
        }        
    },
    watchPosition: function(success, failure, options) {
        if (typeof(success) === 'function') {
            if (typeof(failure) === 'object') {
                // Allow -> #watchPosition(success, options)
                options = failure;
                failure = emptyFn;
            }
            API.watchPosition.apply(API, arguments);
        } else {            
            throw "BackgroundGeolocation#watchPosition does not support Promise API, since Promises cannot resolve multiple times.  The #watchPosition callback *will* be run multiple times.  Use the #watchPosition(success, failure, options) API.";
        }
    },
    stopWatchPosition: function(success, failure) {
        if (!arguments.length) {
            return API.stopWatchPosition();
        } else {
            API.stopWatchPosition().then(success).catch(failure);
        }
    },
    setLogLevel: function(logLevel, success, failure) {
        if (arguments.length == 1) {
            return API.setLogLevel(logLevel);
        } else {
            API.setLogLevel(logLevel).then(success).catch(failure);
        }
    },
    getLog: function(success, failure) {
        if (!arguments.length) {
            return API.getLog();
        } else {
            API.getLog().then(success).catch(failure);
        }
    },
    destroyLog: function(success, failure) {
        if (!arguments.length) {
            return API.destroyLog();
        } else {
            API.destroyLog().then(success).catch(failure);
        }
    },
    emailLog: function(email, success, failure) {
        if (arguments.length == 1) {
            return API.emailLog(email);
        } else {
            API.emailLog(email).then(success).catch(failure);
        }
    },
    isPowerSaveMode: function(success, failure) {
        if (!arguments.length) {
            return API.isPowerSaveMode();
        } else {
            API.isPowerSaveMode().then(success).catch(failure);
        }
    },
    getSensors: function(success, failure) {
        if (!arguments.length) {
            return API.getSensors();
        } else {
            API.getSensors.then(success).catch(failure);
        }
    },
    /**
    * Play a system sound.  This is totally experimental.
    * iOS http://iphonedevwiki.net/index.php/AudioServices
    * Android:
    */
    playSound: function(soundId) {
        API.playSound(soundId);
    },
    logger: {
        error: function(msg) {
            API.log('error', msg);
        },
        warn: function(msg) {
            API.log('warn', msg);
        },
        debug: function(msg) {
            API.log('debug', msg);
        },
        info: function(msg) {
            API.log('info', msg);
        },        
        notice: function(msg) {
            API.log('notice', msg);
        },        
        header: function(msg) {
            API.log('header', msg);
        },
        on: function(msg) {
            API.log('on', msg);
        },
        off: function(msg) {
            API.log('off', msg);
        },
        ok: function(msg) {
            API.log('ok', msg);
        }
    }
};

