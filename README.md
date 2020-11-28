# hubitat-honeywell
A habitat app and driver for the Honeywell Home thermostats (Lyric etc...).

## Notes
This is very much an alpha at this point - I've done very little testing and there are a number of areas that need work (a few listed below).

## Install
- Import the driver and the app.
- Create a new instance of the app.
- Authenticate with Honeywell
- Discover devices

### Not Yet Implemented/Tested
- Multiple Locations
- Events (honeywell supports Azure Event Grid, that needs to be implemented and might require per user accounts)
- Schedules (if anyone has a sample for this it would be great to see)
- Celsius (I put some code in for this but didn't test it)
- Name changes (if you change the name of the device)
- Likely a bug if you have more than just thermostats
- Tokens seems to require forced refresh from time to time (there is a scheduled refresh)


## Credits
I got a lot of example and inspiration from @dkilgore90 (https://github.com/dkilgore90/google-sdm-api/blob/develop/sdm-api-app.groovy)
