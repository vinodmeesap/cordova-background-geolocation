#import <CoreLocation/CoreLocation.h>
#import <AudioToolbox/AudioToolbox.h>
#import <sqlite3.h>

@interface TSLocationManager : NSObject <CLLocationManagerDelegate>

- (void) configure:(NSDictionary*)config;
- (void) start;
- (void) stop;
- (void) finish;
- (NSArray*) sync;
- (NSArray*) getLocations;
- (void) stopBackgroundTask;
- (void) onPaceChange:(BOOL)value;
- (void) setConfig:(NSDictionary*)command;
- (NSDictionary*) getStationaryLocation;
- (void) onSuspend:(NSNotification *)notification;
- (void) onResume:(NSNotification *)notification;
- (void) onAppTerminate;
- (BOOL) isEnabled;
- (NSDictionary*) locationToDictionary:(CLLocation*)location;
@end

