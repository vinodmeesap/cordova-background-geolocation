declare module "cordova-background-geolocation" {
	interface WatchPositionRequest {
    interval?: number;
    desiredAccuracy?: number;
    persist?: boolean;
    extras?: Object;
    timeout?: number;
  }
}
