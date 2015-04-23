#import <CoreLocation/CoreLocation.h>
#import <AudioToolbox/AudioToolbox.h>
#import <Cordova/CDVCommandDelegate.h>

@interface BackgroundGeolocation : NSObject <CLLocationManagerDelegate>

@property (nonatomic, weak) id <CDVCommandDelegate> commandDelegate;

- (void) configure:(NSDictionary*)config;
- (void) start;
- (void) stop;
- (void) finish;
- (void) stopBackgroundTask;
- (void) onPaceChange:(BOOL)value;
- (void) setConfig:(NSDictionary*)command;
- (NSDictionary*) getStationaryLocation;
- (void) onSuspend:(NSNotification *)notification;
- (void) onResume:(NSNotification *)notification;
- (void) onAppTerminate;

@end

