//
//  BackgroundGeolocation.m
//  Cordova Background GeoLocation
//
//  Created by Christopher Scott on 2015-04-23.
//
//
#import "BackgroundGeolocation.h"

// Debug sounds for bg-geolocation life-cycle events.
// http://iphonedevwiki.net/index.php/AudioServices
#define exitRegionSound         1005
#define locationSyncSound       1004
#define paceChangeYesSound      1110
#define paceChangeNoSound       1112
#define acquiringLocationSound  1103
#define acquiredLocationSound   1052
#define locationErrorSound      1073


@implementation BackgroundGeolocation  {
    BOOL isDebugging;
    BOOL enabled;
    BOOL isUpdatingLocation;
    BOOL stopOnTerminate;
    
    NSString *token;
    NSString *url;
    UIBackgroundTaskIdentifier bgTask;
    NSDate *lastBgTaskAt;
    
    NSError *locationError;
    
    BOOL isMoving;
    
    NSNumber *maxBackgroundHours;
    CLLocationManager *locationManager;
    UILocalNotification *localNotification;
    
    NSDictionary *locationData;
    CLLocation *lastLocation;
    NSMutableArray *locationQueue;
    
    NSDate *suspendedAt;
    
    CLLocation *stationaryLocation;
    CLCircularRegion *stationaryRegion;
    NSInteger locationAcquisitionAttempts;
    
    BOOL isAcquiringStationaryLocation;
    NSInteger maxStationaryLocationAttempts;
    
    BOOL isAcquiringSpeed;
    NSInteger maxSpeedAcquistionAttempts;
    
    // @config params
    NSInteger stationaryRadius;
    NSInteger distanceFilter;
    CLLocationAccuracy desiredAccuracy;
    CLActivityType activityType;
    BOOL disableElasticity;
}

- (id) init
{
    locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    
    localNotification = [[UILocalNotification alloc] init];
    localNotification.timeZone = [NSTimeZone defaultTimeZone];
    
    locationQueue = [[NSMutableArray alloc] init];
    
    // @config params
    isDebugging         = NO;
    stopOnTerminate     = NO;
    stationaryRadius    = 50;
    distanceFilter      = 50;
    desiredAccuracy     = kCLLocationAccuracyBest;
    disableElasticity   = NO;
    
    // Flags
    isMoving = NO;
    isUpdatingLocation = NO;
    stationaryLocation = nil;
    stationaryRegion = nil;
    maxStationaryLocationAttempts   = 4;
    maxSpeedAcquistionAttempts      = 3;
    
    // Listen to suspend/resume events
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onSuspend:) name:UIApplicationDidEnterBackgroundNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onResume:) name:UIApplicationWillEnterForegroundNotification object:nil];
    
    bgTask = UIBackgroundTaskInvalid;
    
    return [super init];
}

- (void) configure:(NSDictionary*)config
{
    [self setConfig:config];
    
    locationManager.activityType = activityType;
    locationManager.pausesLocationUpdatesAutomatically = YES;
    locationManager.distanceFilter = distanceFilter; // meters
    locationManager.desiredAccuracy = desiredAccuracy;
    
    [self log: @"CDVBackgroundGeoLocation configure %@", config];
    
    // ios 8 requires permissions to send local-notifications
    if (isDebugging) {
        UIApplication *app = [UIApplication sharedApplication];
        if ([app respondsToSelector:@selector(registerUserNotificationSettings:)]) {
            [app registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert|UIUserNotificationTypeBadge|UIUserNotificationTypeSound categories:nil]];
        }
    }
}

- (void) setConfig:(NSDictionary*)config
{
    if (config[@"debug"]) {
        isDebugging = [[config objectForKey:@"debug"] boolValue];
    }
    if (config[@"stationaryRadius"]) {
        stationaryRadius = [[config objectForKey:@"stationaryRadius"] intValue];
    }
    if (config[@"distanceFilter"]) {
        distanceFilter = [[config objectForKey:@"distanceFilter"] intValue];
    }
    if (config[@"desiredAccuracy"]) {
        desiredAccuracy = [self decodeDesiredAccuracy:[[config objectForKey:@"desiredAccuracy"] intValue]];
    }
    if (config[@"activityType"]) {
        activityType = [self decodeActivityType:[config objectForKey:@"activityType"]];
    }
    if (config[@"stopOnTerminate"]) {
        stopOnTerminate = [[config objectForKey:@"stopOnTerminate"] boolValue];
    }
    if (config[@"disableElasticity"]) {
        disableElasticity = [[config objectForKey:@"disableElasticity"] boolValue];
    }
}

-(NSInteger)decodeDesiredAccuracy:(NSInteger)accuracy
{
    switch (accuracy) {
        case 1000:
            accuracy = kCLLocationAccuracyKilometer;
            break;
        case 100:
            accuracy = kCLLocationAccuracyHundredMeters;
            break;
        case 10:
            accuracy = kCLLocationAccuracyNearestTenMeters;
            break;
        case 0:
            accuracy = kCLLocationAccuracyBest;
            break;
        case -1:
            accuracy = kCLLocationAccuracyBestForNavigation;
            break;
        default:
            accuracy = kCLLocationAccuracyBest;
    }
    return accuracy;
}

-(CLActivityType)decodeActivityType:(NSString*)name
{
    if ([name caseInsensitiveCompare:@"AutomotiveNavigation"]) {
        return CLActivityTypeAutomotiveNavigation;
    } else if ([name caseInsensitiveCompare:@"OtherNavigation"]) {
        return CLActivityTypeOtherNavigation;
    } else if ([name caseInsensitiveCompare:@"Fitness"]) {
        return CLActivityTypeFitness;
    } else {
        return CLActivityTypeOther;
    }
}


/**
 * Turn on background geolocation
 */
- (void) start
{
    enabled = YES;
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    
    [self log: @"- CDVBackgroundGeoLocation start (background? %d)", state];
    
    [locationManager startMonitoringSignificantLocationChanges];
    if (state == UIApplicationStateBackground) {
        [self setPace:isMoving];
    }
}
/**
 * Turn it off
 */
- (void) stop
{
    [self log: @"- CDVBackgroundGeoLocation stop"];
    enabled = NO;
    isMoving = NO;
    
    [self stopUpdatingLocation];
    [locationManager stopMonitoringSignificantLocationChanges];
    if (stationaryRegion != nil) {
        [locationManager stopMonitoringForRegion:stationaryRegion];
        stationaryRegion = nil;
    }
}

/**
 * Change pace to moving/stopped
 * @param {Boolean} isMoving
 */
- (void) onPaceChange:(BOOL)value
{
    isMoving = value;
    [self log: @"- CDVBackgroundGeoLocation onPaceChange %d", isMoving];
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    if (state == UIApplicationStateBackground) {
        [self setPace:isMoving];
    }
}

/**
 * Suspend.  Turn on passive location services
 */
-(void) onSuspend:(NSNotification *) notification
{
    [self log: @"- CDVBackgroundGeoLocation suspend (enabled? %d)", enabled];
    suspendedAt = [NSDate date];
    
    if (enabled) {
        // Sample incoming stationary-location candidate:  Is it within the current stationary-region?  If not, I guess we're moving.
        if (!isMoving && stationaryRegion) {
            if ([self locationAge:stationaryLocation] < (5 * 60.0)) {
                if (isDebugging) {
                    AudioServicesPlaySystemSound (acquiredLocationSound);
                    [self notify:[NSString stringWithFormat:@"Continue stationary\n%f,%f", [stationaryLocation coordinate].latitude, [stationaryLocation coordinate].longitude]];
                }
                [self queue:stationaryLocation type:@"stationary"];
                return;
            }
        }
        [self setPace: isMoving];
    }
}
/**@
 * Resume.  Turn background off
 */
-(void) onResume:(NSNotification *) notification
{
    [self log: @"- CDVBackgroundGeoLocation resume"];
    if (enabled) {
        [self stopUpdatingLocation];
    }
}

/**
 * toggle passive or aggressive location services
 */
- (void)setPace:(BOOL)value
{
    [self log: @"- CDVBackgroundGeoLocation setPace %d, stationaryRegion? %d", value, stationaryRegion!=nil];
    isMoving                        = value;
    isAcquiringStationaryLocation   = NO;
    isAcquiringSpeed                = NO;
    locationAcquisitionAttempts     = 0;
    stationaryLocation              = nil;
    
    if (isDebugging) {
        AudioServicesPlaySystemSound (isMoving ? paceChangeYesSound : paceChangeNoSound);
    }
    if (isMoving) {
        if (stationaryRegion) {
            [locationManager stopMonitoringForRegion:stationaryRegion];
            stationaryRegion = nil;
        }
        isAcquiringSpeed = YES;
    } else {
        isAcquiringStationaryLocation   = YES;
    }
    if (isAcquiringSpeed || isAcquiringStationaryLocation) {
        // Crank up the GPS power temporarily to get a good fix on our current location
        [self stopUpdatingLocation];
        locationManager.distanceFilter = kCLDistanceFilterNone;
        locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation;
        [self startUpdatingLocation];
    }
}

-(void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    [self log: @"- CDVBackgroundGeoLocation didUpdateLocations (isMoving: %d)", isMoving];
    
    locationError = nil;
    if (isMoving && !isUpdatingLocation) {
        [self startUpdatingLocation];
    }
    
    CLLocation *location = [locations lastObject];
    
    if (!isMoving && !isAcquiringStationaryLocation && !stationaryLocation) {
        // Perhaps our GPS signal was interupted, re-acquire a stationaryLocation now.
        [self setPace: NO];
    }
    
    // test the age of the location measurement to determine if the measurement is cached
    // in most cases you will not want to rely on cached measurements
    if ([self locationAge:location] > 5.0) return;
    
    // test that the horizontal accuracy does not indicate an invalid measurement
    if (location.horizontalAccuracy < 0) return;
    
    lastLocation = location;
    
    // test the measurement to see if it is more accurate than the previous measurement
    if (isAcquiringStationaryLocation) {
        [self log: @"- Acquiring stationary location, accuracy: %f", location.horizontalAccuracy];
        if (isDebugging) {
            AudioServicesPlaySystemSound (acquiringLocationSound);
        }
        if (stationaryLocation == nil || stationaryLocation.horizontalAccuracy > location.horizontalAccuracy) {
            stationaryLocation = location;
        }
        if (++locationAcquisitionAttempts == maxStationaryLocationAttempts) {
            isAcquiringStationaryLocation = NO;
            [self startMonitoringStationaryRegion:stationaryLocation];
        } else {
            // Unacceptable stationary-location: bail-out and wait for another.
            return;
        }
    } else if (isAcquiringSpeed) {
        if (isDebugging) {
            AudioServicesPlaySystemSound (acquiringLocationSound);
        }
        if (++locationAcquisitionAttempts == maxSpeedAcquistionAttempts) {
            if (isDebugging) {
                [self notify:@"Aggressive monitoring engaged"];
            }
            // We should have a good sample for speed now, power down our GPS as configured by user.
            isAcquiringSpeed = NO;
            [locationManager setDesiredAccuracy:desiredAccuracy];
            [locationManager setDistanceFilter:[self calculateDistanceFilter:location.speed]];
            [self startUpdatingLocation];
        } else {
            return;
        }
    } else if (isMoving) {
        // Adjust distanceFilter incrementally based upon current speed
        float newDistanceFilter = [self calculateDistanceFilter:location.speed];
        if (newDistanceFilter != locationManager.distanceFilter) {
            [self log: @"- CDVBackgroundGeoLocation updated distanceFilter, new: %f, old: %f", newDistanceFilter, locationManager.distanceFilter];
            [locationManager setDistanceFilter:newDistanceFilter];
            [self startUpdatingLocation];
        }
    } else if ([self locationIsBeyondStationaryRegion:location]) {
        if (isDebugging) {
            [self notify:@"Manual stationary exit-detection"];
        }
        [self setPace:YES];
    }
    [self queue:location type:@"current"];
}

/**
 * Creates a new circle around user and region-monitors it for exit
 */
- (void) startMonitoringStationaryRegion:(CLLocation*)location {
    stationaryLocation = location;
    
    // fire onStationary @event for Javascript.
    [self queue:location type:@"stationary"];
    
    CLLocationCoordinate2D coord = [location coordinate];
    [self log: @"- CDVBackgroundGeoLocation createStationaryRegion (%f,%f)", coord.latitude, coord.longitude];
    
    if (isDebugging) {
        AudioServicesPlaySystemSound (acquiredLocationSound);
        [self notify:[NSString stringWithFormat:@"Acquired stationary location\n%f, %f", location.coordinate.latitude,location.coordinate.longitude]];
    }
    if (stationaryRegion != nil) {
        [locationManager stopMonitoringForRegion:stationaryRegion];
    }
    isAcquiringStationaryLocation = NO;
    stationaryRegion = [[CLCircularRegion alloc] initWithCenter: coord radius:stationaryRadius identifier:@"BackgroundGeoLocation stationary region"];
    stationaryRegion.notifyOnExit = YES;
    [locationManager startMonitoringForRegion:stationaryRegion];
    
    [self stopUpdatingLocation];
    locationManager.distanceFilter = distanceFilter;
    locationManager.desiredAccuracy = desiredAccuracy;
}

/**
 * Manual stationary location his-testing.  This seems to help stationary-exit detection in some places where the automatic geo-fencing soesn't
 */
-(bool)locationIsBeyondStationaryRegion:(CLLocation*)location
{
    [self log: @"- CDVBackgroundGeoLocation locationIsBeyondStationaryRegion"];
    if (![stationaryRegion containsCoordinate:[location coordinate]]) {
        double pointDistance = [stationaryLocation distanceFromLocation:location];
        return (pointDistance - stationaryLocation.horizontalAccuracy - location.horizontalAccuracy) > stationaryRadius;
    } else {
        return NO;
    }
}


/**
 * Calculates distanceFilter by rounding speed to nearest 5 and multiplying by 10.
 * - Clamped at 1km max.
 * - Disabled by #disableElasticity
 */
-(float) calculateDistanceFilter:(float)speed
{
    if (disableElasticity == YES) {
        return distanceFilter;
    }
    float newDistanceFilter = distanceFilter;
    if (speed < 100) {
        // (rounded-speed-to-nearest-5) / 2)^2
        // eg 5.2 becomes (5/2)^2
        newDistanceFilter = pow((5.0 * floorf(fabsf(speed) / 5.0 + 0.5f)), 2) + distanceFilter;
    }
    return (newDistanceFilter < 1000) ? newDistanceFilter : 1000;
}

-(void) queue:(CLLocation*)location type:(id)type
{
    [self log: @"- CDVBackgroundGeoLocation queue %@", type];
    NSMutableDictionary *data = [self locationToHash:location];
    [data setObject:type forKey:@"location_type"];
    [locationQueue addObject:data];
    [self flushQueue];
}

- (void) flushQueue
{
    // Sanity-check the duration of last bgTask:  If greater than 30s, kill it.
    if (bgTask != UIBackgroundTaskInvalid) {
        if (-[lastBgTaskAt timeIntervalSinceNow] > 30.0) {
            [self log: @"- CDVBackgroundGeoLocation#flushQueue has to kill an out-standing background-task!"];
            [self stopBackgroundTask];
        }
        return;
    }
    if ([locationQueue count] > 0) {
        NSMutableDictionary *data = [locationQueue lastObject];
        [locationQueue removeObject:data];
        
        // Create a background-task and delegate to Javascript for syncing location
        bgTask = [self createBackgroundTask];
        [self runInBackground:^{
            [self sync:data];
        }];
    }
}

- (void)runInBackground:(void (^)())block
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), block);
}

/**
 * We are running in the background if this is being executed.
 * We can't assume normal network access.
 * bgTask is defined as an instance variable of type UIBackgroundTaskIdentifier
 */
-(void) sync:(NSMutableDictionary*)data
{
    [self log: @"- CDVBackgroundGeoLocation#sync"];
    [self log: @"  type: %@, position: %@,%@ speed: %@", [data objectForKey:@"location_type"], [data objectForKey:@"latitude"], [data objectForKey:@"longitude"], [data objectForKey:@"speed"]];
    if (isDebugging) {
        [self notify:[NSString stringWithFormat:@"Location update: %s\nSPD: %0.0f | DF: %ld | ACY: %0.0f",
                      ((isMoving) ? "MOVING" : "STATIONARY"),
                      [[data objectForKey:@"speed"] doubleValue],
                      (long) locationManager.distanceFilter,
                      [[data objectForKey:@"accuracy"] doubleValue]]];
        
        AudioServicesPlaySystemSound (locationSyncSound);
    }
    
    // Build a resultset for javascript callback.
    NSString *locationType = [data objectForKey:@"location_type"];
    if ([locationType isEqualToString:@"stationary"]) {
        // Any javascript stationaryRegion event-listeners?
        [data setObject:[NSNumber numberWithDouble:stationaryRadius] forKey:@"radius"];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"BackgroundGeolocation.stationarylocation" object:data];
        //[self fireStationaryRegionListeners:data];
    } else if ([locationType isEqualToString:@"current"]) {
        [[NSNotificationCenter defaultCenter] postNotificationName:@"BackgroundGeolocation.location" object:data];
    } else {
        [self log: @"- CDVBackgroundGeoLocation#sync could not determine location_type."];
        [self stopBackgroundTask];
    }
}

/**
 * Called by js to signify the end of a background-geolocation event
 */
-(void) finish
{
     [self log: @"- CDVBackgroundGeoLocation finish"];
     [self stopBackgroundTask];
}


/**
 * Fetches current stationaryLocation
 */
- (NSDictionary*) getStationaryLocation
{
    NSDictionary *data = [self locationToHash:stationaryLocation];
    return data;
}

- (bool) stationaryRegionContainsLocation:(CLLocation*)location {
    CLCircularRegion *region = [locationManager.monitoredRegions member:stationaryRegion];
    return ([region containsCoordinate:location.coordinate]) ? YES : NO;
}

-(UIBackgroundTaskIdentifier) createBackgroundTask
{
    lastBgTaskAt = [NSDate date];
    return [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
        [self stopBackgroundTask];
    }];
}

- (void) stopBackgroundTask
{
    UIApplication *app = [UIApplication sharedApplication];
    [self log: @"- CDVBackgroundGeoLocation stopBackgroundTask (remaining t: %f)", app.backgroundTimeRemaining];
    if (bgTask != UIBackgroundTaskInvalid)
    {
        [app endBackgroundTask:bgTask];
        bgTask = UIBackgroundTaskInvalid;
    }
    [self flushQueue];
}


-(NSMutableDictionary*) locationToHash:(CLLocation*)location
{
    NSMutableDictionary *returnInfo;
    returnInfo = [NSMutableDictionary dictionaryWithCapacity:10];
    
    NSNumber* timestamp = [NSNumber numberWithDouble:([location.timestamp timeIntervalSince1970] * 1000)];
    [returnInfo setObject:timestamp forKey:@"timestamp"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.speed] forKey:@"speed"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.verticalAccuracy] forKey:@"altitudeAccuracy"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.horizontalAccuracy] forKey:@"accuracy"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.course] forKey:@"heading"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.altitude] forKey:@"altitude"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.coordinate.latitude] forKey:@"latitude"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.coordinate.longitude] forKey:@"longitude"];
    
    return returnInfo;
}

- (NSTimeInterval) locationAge:(CLLocation*)location
{
    return -[location.timestamp timeIntervalSinceNow];
}


- (void) stopUpdatingLocation
{
    [locationManager stopUpdatingLocation];
    isUpdatingLocation = NO;
}

- (void) startUpdatingLocation
{
    SEL requestSelector = NSSelectorFromString(@"requestAlwaysAuthorization");
    if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusNotDetermined && [locationManager respondsToSelector:requestSelector]) {
        ((void (*)(id, SEL))[locationManager methodForSelector:requestSelector])(locationManager, requestSelector);
        [locationManager startUpdatingLocation];
        isUpdatingLocation = YES;
    } else {
        [locationManager startUpdatingLocation];
        isUpdatingLocation = YES;
    }
}
- (void) locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status
{
    [self log: @"- CDVBackgroundGeoLocation didChangeAuthorizationStatus %u", status];
    if (isDebugging) {
        [self notify:[NSString stringWithFormat:@"Authorization status changed %u", status]];
    }
}

- (void) notify:(NSString*)message
{
    localNotification.fireDate = [NSDate date];
    localNotification.alertBody = message;
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
}

/**
 * Log a message.  Only outputs when @config debug: true
 */
- (void) log:(NSString *)format, ...
{
    if (isDebugging) {
        va_list args;
        va_start(args, format);
        va_end(args);
        NSLogv(format, args);
    }
}


/**
 * Called when user exits their stationary radius (ie: they walked ~50m away from their last recorded location.
 * - turn on more aggressive location monitoring.
 */
- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region
{
    [self log: @"- CDVBackgroundGeoLocation exit region"];
    if (isDebugging) {
        AudioServicesPlaySystemSound (exitRegionSound);
        [self notify:@"Exit stationary region"];
    }
    [self setPace:YES];
}

/**
 * 1. turn off std location services
 * 2. turn on significantChanges API
 * 3. create a region and start monitoring exits.
 */
- (void)locationManagerDidPauseLocationUpdates:(CLLocationManager *)manager
{
    [self log: @"- CDVBackgroundGeoLocation paused location updates"];
    if (isDebugging) {
        [self notify:@"Stop detected"];
    }
    if (locationError) {
        isMoving = NO;
        [self startMonitoringStationaryRegion:lastLocation];
        [self stopUpdatingLocation];
    } else {
        [self setPace:NO];
    }
}

/**
 * 1. Turn off significantChanges ApI
 * 2. turn on std. location services
 * 3. nullify stationaryRegion
 */
- (void)locationManagerDidResumeLocationUpdates:(CLLocationManager *)manager
{
    [self log: @"- CDVBackgroundGeoLocation resume location updates"];
    if (isDebugging) {
        [self notify:@"Resume location updates"];
    }
    [self setPace:YES];
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    [self log: @"- CDVBackgroundGeoLocation locationManager failed:  %@", error];
    if (isDebugging) {
        AudioServicesPlaySystemSound (locationErrorSound);
        [self notify:[NSString stringWithFormat:@"Location error: %@", error.localizedDescription]];
    }
    
    locationError = error;
    
    switch(error.code) {
        case kCLErrorLocationUnknown:
        case kCLErrorNetwork:
        case kCLErrorRegionMonitoringDenied:
        case kCLErrorRegionMonitoringSetupDelayed:
        case kCLErrorRegionMonitoringResponseDelayed:
        case kCLErrorGeocodeFoundNoResult:
        case kCLErrorGeocodeFoundPartialResult:
        case kCLErrorGeocodeCanceled:
            break;
        case kCLErrorDenied:
            [self stopUpdatingLocation];
            break;
        default:
            [self stopUpdatingLocation];
    }
}


/**
 * Termination. Checks to see if it should turn off
 */
-(void) onAppTerminate
{
    [self log: @"- CDVBackgroundGeoLocation appTerminate"];
    if (enabled && stopOnTerminate) {
        [self log: @"- CDVBackgroundGeoLocation stoping on terminate"];
        
        enabled = NO;
        isMoving = NO;
        
        [self stopUpdatingLocation];
        [locationManager stopMonitoringSignificantLocationChanges];
        if (stationaryRegion != nil) {
            [locationManager stopMonitoringForRegion:stationaryRegion];
            stationaryRegion = nil;
        }
    }
}

- (void)dealloc
{
    locationManager.delegate = nil;
}

@end

