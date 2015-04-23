////
//  CDVBackgroundGeoLocation
//
//  Created by Chris Scott <chris@transistorsoft.com> on 2013-06-15
//
#import "CDVBackgroundGeoLocation.h"
#import "BackgroundGeolocation.h"

@implementation CDVBackgroundGeoLocation {
    BackgroundGeolocation *bgGeo;
}

- (void)pluginInitialize
{
    bgGeo = [[BackgroundGeolocation alloc] init];
    bgGeo.commandDelegate = self.commandDelegate;
}
/**
 * configure plugin
 * @param {String} token
 * @param {String} url
 * @param {Number} stationaryRadius
 * @param {Number} distanceFilter
 */
- (void) configure:(CDVInvokedUrlCommand*)command
{
    [bgGeo configure:command];
}

- (void) setConfig:(CDVInvokedUrlCommand*)command
{
    [bgGeo setConfig:command];
}

/**
 * Turn on background geolocation
 */
- (void) start:(CDVInvokedUrlCommand*)command
{
    [bgGeo start:command];
}
/**
 * Turn it off
 */
- (void) stop:(CDVInvokedUrlCommand*)command
{
    [bgGeo stop:command];
}

/**
 * Change pace to moving/stopped
 * @param {Boolean} isMoving
 */
- (void) onPaceChange:(CDVInvokedUrlCommand *)command
{
    [bgGeo onPaceChange:command];
}

/**
 * Fetches current stationaryLocation
 */
- (void) getStationaryLocation:(CDVInvokedUrlCommand *)command
{
    [bgGeo getStationaryLocation:command];
}

- (void) addStationaryRegionListener:(CDVInvokedUrlCommand*)command
{
    [bgGeo addStationaryRegionListener:command];
}

/**
 * Called by js to signify the end of a background-geolocation event
 */
-(void) finish:(CDVInvokedUrlCommand*)command
{
    [bgGeo finish:command];
}
/**
 * If you don't stopMonitoring when application terminates, the app will be awoken still when a
 * new location arrives, essentially monitoring the user's location even when they've killed the app.
 * Might be desirable in certain apps.
 */
- (void)applicationWillTerminate:(UIApplication *)application {
}

@end
