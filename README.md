# hubitat-honeywell
An app and driver for the Honeywell Home thermostats (Lyric, T5, T9 etc…). This uses the official Honeywell Home APIs and services.

## Notes
I would consider this beta code, there are definitely rough edges that still need to be worked out but it’s running in “production” at my house. Feel free to submit pull requests for improvements, on major improvements I will rev the release and the package.

## Install
- Import the driver and the app (or use Hubitat Package Manager)
- Create a new instance of the app.
- Authenticate with Honeywell (establish OAuth Link)
- Discover devices

### Not Yet Implemented/Tested
- Events (honeywell supports Azure Event Grid, that needs to be implemented and might require per user accounts)
- Schedules via Honeywelll (I am using the Hubitat Thermostat Scheduler personally)
- Celsius (I put some code in for this but didn't test it)
- Name changes (if you change the name of the device)
- Tokens seems to require forced refresh from time to time (there is a scheduled refresh and a force option in the debug menu).

## Credits
- I got a lot of example and inspiration from @dkilgore90 (https://github.com/dkilgore90/google-sdm-api/blob/develop/sdm-api-app.groovy)
- Credit to Tim in the Hubitat community for patently testing and improving this with me over days/weeks.
