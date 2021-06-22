fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew install fastlane`

# Available Actions
### screenshots
```
fastlane screenshots
```

### capture_screen
```
fastlane capture_screen
```
Capture Screen
### build_for_screengrab
```
fastlane build_for_screengrab
```
Build debug and test APK for screenshots
### release
```
fastlane release
```
Create a release and upload to internal channel of playstore
### finalize_sentry
```
fastlane finalize_sentry
```
Check the version currently in production and mark it as "finalized"

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
