////
//  CDVBackgroundGeoLocation
//
//  Created by Chris Scott <chris@transistorsoft.com> on 2013-06-15
//
#import "CDVBackgroundGeoLocation.h"

@implementation CDVBackgroundGeolocation {
    TSLocationManager *bgGeo;
    NSDictionary *config;
}

@synthesize syncCallbackId, stationaryRegionListeners;

- (void)pluginInitialize
{
    bgGeo = [[TSLocationManager alloc] init];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onLocationChanged:) name:@"TSLocationManager.location" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onStationaryLocation:) name:@"TSLocationManager.stationary" object:nil];
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
    self.syncCallbackId = command.callbackId;
    
    config = [command.arguments objectAtIndex:0];

    [bgGeo configure:config];
}

- (void) setConfig:(CDVInvokedUrlCommand*)command
{
    NSDictionary *cfg  = [command.arguments objectAtIndex:0];
    [bgGeo setConfig:cfg];
    
    CDVPluginResult* result = nil;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * Turn on background geolocation
 */
- (void) start:(CDVInvokedUrlCommand*)command
{
    [bgGeo start];
}
/**
 * Turn it off
 */
- (void) stop:(CDVInvokedUrlCommand*)command
{
    [bgGeo stop];
}

/**
 * Change pace to moving/stopped
 * @param {Boolean} isMoving
 */
- (void) onPaceChange:(CDVInvokedUrlCommand *)command
{
    BOOL moving = [[command.arguments objectAtIndex: 0] boolValue];
    [bgGeo onPaceChange:moving];
}

/**
 * location handler from BackgroundGeolocation
 */
- (void)onLocationChanged:(NSNotification*)notification {
    CLLocation *location = notification.object;
    NSDictionary *locationData = [bgGeo locationToDictionary:location];
    
    CDVPluginResult* result = nil;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:locationData];
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.syncCallbackId];
}

- (void) onStationaryLocation:(NSNotification*)notification
{
    CLLocation *location = notification.object;
    NSDictionary *locationData = [bgGeo locationToDictionary:location];

    if (![self.stationaryRegionListeners count]) {
        [bgGeo stopBackgroundTask];
        return;
    }
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:locationData];
    [result setKeepCallbackAsBool:YES];
    for (NSString *callbackId in self.stationaryRegionListeners)
    {
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
    [bgGeo stopBackgroundTask];
}

/**
 * Fetches current stationaryLocation
 */
- (void) getStationaryLocation:(CDVInvokedUrlCommand *)command
{
    NSDictionary* location = [bgGeo getStationaryLocation];
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:location];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * Fetches current stationaryLocation
 */
- (void) getLocations:(CDVInvokedUrlCommand *)command
{
    NSArray* locations = [bgGeo getLocations];
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:locations];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * Fetches current stationaryLocation
 */
- (void) sync:(CDVInvokedUrlCommand *)command
{
    NSArray* locations = [bgGeo sync];
    
    if (locations != nil) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:locations];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } else {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:@"Failed to sync to server.  Is there a network connection?"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}

- (void) addStationaryRegionListener:(CDVInvokedUrlCommand*)command
{
    if (self.stationaryRegionListeners == nil) {
        self.stationaryRegionListeners = [[NSMutableArray alloc] init];
    }
    [self.stationaryRegionListeners addObject:command.callbackId];
}

/**
 * Called by js to signify the end of a background-geolocation event
 */
-(void) finish:(CDVInvokedUrlCommand*)command
{
    [bgGeo finish];
}
/**
 * If you don't stopMonitoring when application terminates, the app will be awoken still when a
 * new location arrives, essentially monitoring the user's location even when they've killed the app.
 * Might be desirable in certain apps.
 */
- (void)applicationWillTerminate:(UIApplication *)application {
}

@end
