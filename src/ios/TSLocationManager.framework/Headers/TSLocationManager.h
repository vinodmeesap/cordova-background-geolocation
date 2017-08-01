#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import <CoreLocation/CoreLocation.h>
#import <AudioToolbox/AudioToolbox.h>
#import "TSSchedule.h"
#import "TSLocation.h"
#import "TSActivityChangeEvent.h"
#import "TSProviderChangeEvent.h"
#import "TSHttpEvent.h"
#import "TSHeartbeatEvent.h"
#import "TSScheduleEvent.h"
#import "TSGeofencesChangeEvent.h"
#import "TSGeofenceEvent.h"
#import "LocationManager.h"

@interface TSLocationManager : NSObject <CLLocationManagerDelegate>

@property (nonatomic) UIViewController* viewController;
@property (nonatomic, strong) CLLocationManager* locationManager;
@property (nonatomic) NSDate *stoppedAt;
@property (nonatomic) UIBackgroundTaskIdentifier preventSuspendTask;

// Blocks

+ (TSLocationManager *)sharedInstance;


// Methods
- (NSDictionary*) configure:(NSDictionary*)config;
- (void) on:(NSString*)event success:(void (^)(id))success failure:(void(^)(id))failure;
- (void) onLocation:(void(^)(TSLocation*))success failure:(void(^)(NSError*))failure;
- (void) onHttp:(void(^)(TSHttpEvent*))success;
- (void) onGeofence:(void(^)(TSGeofenceEvent*))success;
- (void) onHeartbeat:(void(^)(TSHeartbeatEvent*))success;
- (void) onMotionChange:(void(^)(TSLocation*))success;
- (void) onActivityChange:(void(^)(TSActivityChangeEvent*))success;
- (void) onProviderChange:(void(^)(TSProviderChangeEvent*))success;
- (void) onGeofencesChange:(void(^)(TSGeofencesChangeEvent*))success;
- (void) onSchedule:(void(^)(TSScheduleEvent*))success;
- (void) un:(NSString*)event callback:(void(^)(id))callback;
- (void) removeListener:(NSString*)event callback:(void(^)(id))callback;
- (void) removeListeners;

- (void) start;
- (void) stop;
- (void) startSchedule;
- (void) stopSchedule;
- (void) startGeofences;
- (void) sync:(void(^)(NSArray*))success failure:(void(^)(NSError*))failure;
- (NSArray*) getLocations;
- (UIBackgroundTaskIdentifier) createBackgroundTask;
- (void) stopBackgroundTask:(UIBackgroundTaskIdentifier)taskId;
- (void) error:(UIBackgroundTaskIdentifier)taskId message:(NSString*)message;
- (void) changePace:(BOOL)value;
- (NSDictionary*) setConfig:(NSDictionary*)command;
- (NSMutableDictionary*) getState;
- (NSDictionary*) getStationaryLocation;
- (void) onSuspend:(NSNotification *)notification;
- (void) onResume:(NSNotification *)notification;
- (void) onAppTerminate;
- (void) addGeofence:(NSDictionary*)params success:(void (^)(NSString*))success error:(void (^)(NSString*))error;
- (void) addGeofences:(NSArray*)geofences success:(void (^)(NSString*))success error:(void (^)(NSString*))error;
- (void) removeGeofence:(NSString*)identifier success:(void (^)(NSString*))success error:(void (^)(NSString*))error;
- (void) removeGeofences:(NSArray*)identifiers success:(void (^)(NSString*))success error:(void (^)(NSString*))error;;
- (NSArray*) getGeofences;
- (void) getCurrentPosition:(NSDictionary*)options success:(void (^)(TSLocation*))success failure:(void (^)(NSError*))failure;
- (void) watchPosition:(NSDictionary*)options success:(void (^)(TSLocation*))success failure:(void (^)(NSError*))failure;
- (void) stopWatchPosition;
- (void) playSound:(SystemSoundID)soundId;
- (BOOL) clearDatabase;
- (BOOL) destroyLocations;
- (BOOL) insertLocation:(NSDictionary*)params;
- (int) getCount;
- (NSString*) getLog;
- (BOOL) destroyLog;
- (void) emailLog:(NSString*)to;
- (void) setLogLevel:(NSInteger)level;
- (CLLocationDistance)getOdometer;
- (void) setOdometer:(CLLocationDistance)odometer success:(void (^)(TSLocation*))success failure:(void (^)(NSError*))failure;
// Sensor methods
-(BOOL) isMotionHardwareAvailable;
-(BOOL) isDeviceMotionAvailable;
-(BOOL) isAccelerometerAvailable;
-(BOOL) isGyroAvailable;
-(BOOL) isMagnetometerAvailable;

@end

