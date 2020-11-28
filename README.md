# hubitat-honeywell
A hubitate app and driver for the Honeywell Home thermostates (Lyric etc...).

## Notes
This is very much an alpha at this point - I've done very little testing and there are a number of areas that need work (a few listed below).

## Install
- Import the driver and the app.
- Create a new instance of the app.
- Authenticate with honeywell
- Discover devices

### Not Yet Implemented/Tested
- Multiple Locations
- Events (honeywell supports Azure Event Grid, that needs to be impelmented and might require per user accounts)
- Scheduels (if anyone has a sample for this it would be great to see)
- Celcius (I put some code in for this but didn't test it)
- Name changes (if you change the name of the device)
- Likly a bug if you have more than just thermistates
- Tokens seems to require forced refresh from time to time (there is a scheduled refresh)
