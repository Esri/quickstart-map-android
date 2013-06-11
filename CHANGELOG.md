# quickstart-map-android - Changelog

## Version 1.2 - June 11, 2013
Re-uploaded version 1.2. A permission error on my local machine prevented the correct files from being committed on June 5. All files 'should' be updated to the correct version. If something seems fishy let me know via the project comments. Special thanks to @elbarsal for catching the problem.


## Version 1.2 - June 5, 2013

- No breaking changes made.
- Fixed issue #2 when used in Europe NETWORK_PROVIDER could not be detected and threw a fatal error. These changes were made in setLocationListener(). It now should properly detect if network or gps providers are available and react appropriately. 
- Fixed issue #3 when drawing long polylines applications would crash. Changed the pattern to use unique segments for each move event rather than cummulatively adding onto a single segment.
- Fixed issue #6 by adding a method to the library that lets you clear all graphics.
- Fixed issue #7 quickstartsample application crashes after selecting a draw tool and hitting phone back button. Added an onBackPressed Listener in the EsriQuickStartSampleActivity.
- Cleaned up code in setLocationListener(). Removed old code that wasn't being used.
- Updated quickstart.jar so that name now contains version number (e.g. quickstart1.2.jar).
- Changed namespace of sample to be unique. This was causing some compile time duplication errors.
- Updated quickstart and quickstartsample manifest files to reflect proper versions.
- Fixed bug in quickstartsample shutdownAllLocation() that caused Illegal Argument Exception when shutting down GPS.
- Tweaked threading in quickstart.stopLocationService() and quickstart.delayedStartLocationService()

## Version 1.1 - December 5, 2012

- Various bug fixes and enhancements
- Added more help to README.


## Version 1.0 - October 17, 2012

- Initial commit