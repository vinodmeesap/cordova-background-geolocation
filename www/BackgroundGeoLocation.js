/**
* cordova-background-geolocation
* Copyright (c) 2015, Transistor Software (9224-2932 Quebec Inc)
* All rights reserved.
* sales@transistorsoft.com
* http://transistorsoft.com
* @see LICENSE
*/
var exec = require("cordova/exec");
module.exports = {
    /**
    * @property {Object} stationaryLocation
    */
    stationaryLocation: null,
    /**
    * @property {Object} config
    */
    config: {},

    configure: function(success, failure, config) {
        var me = this;
        config = config || {};
        this.config = config;
        success = success || function(location, taskId) {
            me.finish(taskId);
        };
        
        var mySuccess = function(params) {
            var location    = params.location || params;
            var taskId      = params.taskId || 'task-id-undefined';
            // Transform timestamp to Date instance.
            if (location.timestamp) {
                location.timestamp = new Date(location.timestamp);
            }
            success.call(this, location, taskId);
        }
        exec(mySuccess,
             failure || function() {},
             'BackgroundGeoLocation',
             'configure',
             [config]
        );
    },
    start: function(success, failure, config) {
        exec(success || function() {},
             failure || function() {},
             'BackgroundGeoLocation',
             'start',
             []);
    },
    stop: function(success, failure, config) {
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'stop',
            []);
    },
    finish: function(taskId, success, failure) {
        if (!taskId) {
            throw "BackgroundGeolocation#finish must now be provided with a taskId as 1st param, eg: bgGeo.finish(taskId).  taskId is provided by 2nd param in callback";
        }
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'finish',
            [taskId]);
    },
    changePace: function(isMoving, success, failure) {
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'onPaceChange',
            [isMoving]);
    },
    /**
    * @param {Integer} stationaryRadius
    * @param {Integer} desiredAccuracy
    * @param {Integer} distanceFilter
    * @param {Integer} timeout
    */
    setConfig: function(success, failure, config) {
        this.apply(this.config, config);
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'setConfig',
            [config]);
    },
    /**
    * Returns current stationaryLocation if available.  null if not
    */
    getStationaryLocation: function(success, failure) {
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'getStationaryLocation',
            []);
    },
    /**
    * Add a stationary-region listener.  Whenever the devices enters "stationary-mode", your #success callback will be executed with #location param containing #radius of region
    * @param {Function} success
    * @param {Function} failure [optional] NOT IMPLEMENTED
    */
    onStationary: function(success, failure) {
        var me = this;
        success = success || function(location, taskId) {
            me.finish(taskId);
        };
        var callback = function(params) {
            var location    = params.location || params,
                taskId      = params.taskId || 'task-id-undefined';
            
            me.stationaryLocation = location;
            success.call(me, location, taskId);
        };
        exec(callback,
            failure || function() {},
            'BackgroundGeoLocation',
            'addStationaryRegionListener',
            []);
    },
    getLocations: function(success, failure) {
        if (typeof(success) !== 'function') {
            throw "BackgroundGeolocation#getLocations requires a success callback";
        }
        var me = this;
        var mySuccess = function(locations) {
            success.call(this, me._setTimestamp(locations));
        }
        exec(mySuccess,
            failure || function() {},
            'BackgroundGeoLocation',
            'getLocations',
            []);
    },
    /**
    * Signal native plugin to sync locations queue to HTTP
    */
    sync: function(success, failure) {
        if (typeof(success) !== 'function') {
            throw "BackgroundGeolocation#sync requires a success callback";
        }
        var me = this;
        var mySuccess = function(locations) {
            success.call(this, me._setTimestamp(locations));
        }
        exec(mySuccess,
            failure || function() {},
            'BackgroundGeoLocation',
            'sync',
            []);
    },
    /**
    * Fetch current odometer value
    */
    getOdometer: function(success, failure) {
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'getOdometer',
            []);
    },
    /**
    * Reset Odometer to 0
    */
    resetOdometer: function(success, failure) {
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'resetOdometer',
            []);
    },
    /**
    * add geofence
    */
    addGeofence: function(config, success, failure) {
        config = config || {};
        if (!config.identifier) {
            throw "#addGeofence requires an 'identifier'";
        }
        if (!(config.latitude && config.longitude)) {
            throw "#addGeofence requires a #latitude and #longitude";
        } 
        if (!config.radius) {
            throw "#addGeofence requires a #radius";
        }
        exec(success || function() {},
            failure || function() {},
            'BackgroundGeoLocation',
            'addGeofence',
            [config]);
    },
    onGeofence: function(success, failure) {
        if (!typeof(success) === 'function') {
            throw "#onGeofence requires a success callback";
        }
        var me = this;
        var mySuccess = function(params) {
            var identifier  = params.identifier,
                taskId      = params.taskId || 'task-id-undefined';
            success.call(me, identifier, taskId);
        }
        exec(mySuccess,
            failure || function() {},
            'BackgroundGeoLocation',
            'onGeofence',
            []);
    },
    _setTimestamp: function(rs) {
        // Transform timestamp to Date instance.
        if (typeof(rs) === 'object') {
            for (var n=0,len=rs.length;n<len;n++) {
                rs[n].timestamp = new Date(rs[n].timestamp);
            }
        }
        return rs;
    },
    apply: function(destination, source) {
        source = source || {};
        for (var property in source) {
            if (source.hasOwnProperty(property)) {
                destination[property] = source[property];
            }
        }
        return destination;
    }
};
