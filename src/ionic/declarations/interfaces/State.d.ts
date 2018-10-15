declare module "cordova-background-geolocation" {
  interface State extends Config {
    enabled: boolean;
    schedulerEnabled: boolean;
    trackingMode: TrackingMode;
    odometer: number;
  }
}