# Capacitor Setup

```
npm install https://github.com/transistorsoft/cordova-background-geolocation.git
npx cap sync
```
:information_source: Append optional `#version` tag (eg: `#3.2.1`) to url above (See [Releases](../../../releases))

# iOS

## Configure Background Capabilities

With `YourApp.xcworkspace` open in XCode, add the following **Background Modes Capabilities**:

- [x] Location updates
- [x] Background fetch
- [x] Audio (**optional for debug-mode sound FX**)

![](https://dl.dropbox.com/s/c3vm8x0wgrfn9f4/ios-setup-background-modes.png?dl=1)

## Info.plist

Edit **`Info.plist`**.  Add the following items (Set **Value** as desired):

| Key | Type | Value |
|-----|-------|-------------|
| *Privacy - Location Always and When in Use Usage Description* | `String` | *CHANGEME: Location required in background* |
| *Privacy - Location When in Use Usage Description* | `String` | *CHANGEME: Location required when app is in use* |
| *Privacy - Motion Usage Description* | `String` | *CHANGEME: Motion permission helps detect when device in in-motion* |

![](https://dl.dropbox.com/s/9non3j83jj0rimu/ios-setup-plist-strings.png?dl=1)

### `AppDelegate.swift`

The Background Geolocation SDK is integrated with the [iOS Background Fetch API](https://developer.apple.com/documentation/uikit/core_app/managing_your_app_s_life_cycle/preparing_your_app_to_run_in_the_background/updating_your_app_with_background_app_refresh).

In Your **`AppDelegate.swift`**, add the following code (just the **`+green`** lines):

```diff
import UIKit
import Capacitor
+import TSBackgroundFetch

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

  var window: UIWindow?

  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Override point for customization after application launch.
    return true
  }

+ //Added for cordova-plugin-background-fetch
+ func application(_ application: UIApplication, performFetchWithCompletionHandler completionHandler:@escaping (UIBackgroundFetchResult) -> Void) {
+   NSLog("AppDelegate received fetch event");
+   let fetchManager = TSBackgroundFetch.sharedInstance();
+   fetchManager?.perform(completionHandler: completionHandler, applicationState: application.applicationState);
+ }
  .
  .
  .
}
```

# Android

*Nothing else to perform*




