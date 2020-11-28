/*
Hubitat Driver For Honeywell Thermistate
(C) 2020 - Taylor Brown

11-25-2020 :  Initial 

Considerable Credit Goes To: https://github.com/dkilgore90/google-sdm-api/
*/


import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field


@Field static String global_apiURL = "https://api.honeywell.com"
@Field static String global_redirectURL = "https://cloud.hubitat.com/oauth/stateredirect"
@Field static String global_conusmerKey = "DEb39Y2eKMrv3fGpoKudWvLOZ9LDey6N"
@Field static String global_consumerSecret = "hGyrQFX5TU4frGG5"

definition(
        name: "Honeywell Home",
        namespace: "thecloudtaylor",
        author: "Taylor Brown",
        description: "Honeywell Home App and Driver",
        category: "Thermostate",
        iconUrl: "",
        iconX2Url: "")

preferences 
{
    page(name: "mainPage")
    page(name: "debugPage", title: "Debug Options", install: true)
}

mappings {
    path("/handleAuth") {
        action: [
            GET: "handleAuthRedirect"
        ]
    }
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Setup connection to Honeywell Home and Discover devices", install: true, uninstall: true) {

        connectToHoneywell()
        getDiscoverButton()
        
        section {
            input name: "debugOutput", type: "bool", title: "Enable Debug Logging?", defaultValue: false, submitOnChange: true
        }
        
        listDiscoveredDevices()
        
        getDebugLink()
    }
}


def debugPage() {
    dynamicPage(name:"debugPage", title: "Debug", install: false, uninstall: false) {
        section {
            paragraph "Debug buttons"
        }
        section {
            input 'refreshToken', 'button', title: 'Force Token Refresh', submitOnChange: true
        }
        section {
            input 'deleteDevices', 'button', title: 'Delete all devices', submitOnChange: true
        }
        section {
            input 'refreshDevices', 'button', title: 'Refresh all devices', submitOnChange: true
        }
    }
}

def LogDebug(logMessage)
{
    if(settings?.debugOutput)
    {
        log.debug "${logMessage}";
    }
}

def LogInfo(logMessage)
{
    log.info "${logMessage}";
}

def LogWarn(logMessage)
{
    log.warn "${logMessage}";
}

def LogError(logMessage)
{
    log.error "${logMessage}";
}

def installed()
{
    LogInfo("Installing Honeywell Home.");
    createAccessToken();
    
}

def initialize() 
{
    LogInfo("Initializing Honeywell Home.");
    unschedule()
    refreshToken()
    runEvery1Hour refreshToken
    runEvery15Minutes refreshAllThermostats
}

def updated() 
{
    LogDebug("Updated with config: ${settings}");
    initialize();
}

def uninstalled() 
{
    LogInfo("Uninstalling Honeywell Home.");
    unschedule()
    for (device in getChildDevices())
    {
        deleteChildDevice(device.deviceNetworkId)
    }
}

def connectToHoneywell() 
{
    LogDebug("connectToHoneywell()");

    //if this isn't defined early then the redirect fails for some reason...
    def redirectLocation = "http://www.bing.com";
    if (state.accessToken == null)
    {
        createAccessToken();
    }

    def state = java.net.URLEncoder.encode("${getHubUID()}/apps/${app.id}/handleAuth?access_token=${state.accessToken}", "UTF-8")
    def escapedRedirectURL = java.net.URLEncoder.encode(global_redirectURL, "UTF-8")
    def authQueryString = "response_type=code&redirect_uri=${escapedRedirectURL}&client_id=${global_conusmerKey}&state=${state}";

    def params = [
        uri: global_apiURL,
        path: "/oauth2/authorize",
        queryString: authQueryString.toString()
    ]
    LogDebug("honeywell_auth request params: ${params}");

    try {
        httpPost(params) { response -> 
            if (response.status == 302) 
            {
                LogDebug("Response 302, getting redirect")
                redirectLocation = response.headers.'Location'
                LogDebug("Redirect: ${redirectLocation}");
            }
            else
            {
                LogError("Auth request Returned Invalid HTTP Response: ${response.status}")
                return false;
            } 
        }
    }
    catch (groovyx.net.http.HttpResponseException e) 
    {
        LogError("API Auth failed -- ${e.getLocalizedMessage()}: ${e.response.data}")
        return false;
    }
    section("Honeywell Login")
    {
        paragraph "Click below to be redirected to Honeywall to authorize Hubitat access."
        href(
            name       : 'authHref',
            title      : 'Auth Link',
            url        : redirectLocation,
            description: 'Click this link to authorize with Honeywell Home'
        )
        //href url:redirectURL, external:true, required:false, title:"Connect to Honeywell:", description:description
    } 
}

def getDiscoverButton() 
{
    if (state.access_token == null) 
    {
        section 
        {
            paragraph "Device discovery button is hidden until authorization is completed."            
        }
    } 
    else 
    {
        section 
        {
            input 'discoverDevices', 'button', title: 'Discover', submitOnChange: true
        }
    }
}

def getDebugLink() {
    section{
        href(
            name       : 'debugHref',
            title      : 'Debug buttons',
            page       : 'debugPage',
            description: 'Access debug buttons (force Token refresh, delete child devices , refresh devices)'
        )
    }
}

def listDiscoveredDevices() {
    def children = getChildDevices()
    def builder = new StringBuilder()
    builder << "<ul>"
    children.each {
        if (it != null) {
            builder << "<li><a href='/device/edit/${it.getId()}'>${it.getLabel()}</a></li>"
        }
    }
    builder << "</ul>"
    def links = builder.toString()
    section {
        paragraph "Discovered devices are listed below:"
        paragraph links
    }
}



def appButtonHandler(btn) {
    switch (btn) {
    case 'discoverDevices':
        discoverDevices()
        break
    case 'refreshToken':
        refreshToken()
        break
    case 'deleteDevices':
        deleteDevices()
        break
    case 'refreshDevices':
        refreshAllThermostats()
        break
    }
}

def deleteDevices()
{
    def children = getChildDevices()
    LogInfo("Deleting all child devices: ${children}")
    children.each {
        if (it != null) {
            deleteChildDevice it.getDeviceNetworkId()
        }
    } 
}

def discoverDevices()
{
    LogDebug("discoverDevices()");

    def uri = global_apiURL + '/v2/locations' + "?apikey=" + global_conusmerKey
    def headers = [ Authorization: 'Bearer ' + state.access_token ]
    def contentType = 'application/json'
    def params = [ uri: uri, headers: headers, contentType: contentType ]
    LogDebug("Location Discovery-params ${params}")

    //add error checking
    def reJson =''
    try 
    {
        httpGet(params) { response ->
            def reCode = response.getStatus();
            reJson = response.getData();
            LogDebug("reCode: ${reCode}")
            LogDebug("reJson: ${reJson}")
        }
    }
    catch (groovyx.net.http.HttpResponseException e) 
    {
        LogError("Location Discover failed -- ${e.getLocalizedMessage()}: ${e.response.data}")
        return;
    }

    //BugBug: There could be multiple locations. I should be checking each.
    def locationID = reJson.locationID[0].toString()
    LogDebug("LocationID: ${locationID}");

    //BugBug: Need to Make Sure Each is a Thermosate
    reJson.devices.each {
        LogDebug("DeviceID: ${it.deviceID[0].toString()}")
        LogDebug("DeviceModel: ${it.deviceModel[0].toString()}")

        try 
        {
            addChildDevice(
                'thecloudtaylor',
                'Honeywell Home Thermostat',
                "${locationID} - ${it.deviceID[0].toString()}",
                [
                    name: "Honeywell - ${it.deviceModel[0].toString()} - ${it.deviceID[0].toString()}",
                    label: it.userDefinedDeviceName[0].toString()
                ])
            

        } 
        catch (com.hubitat.app.exception.UnknownDeviceTypeException e) 
        {
            "${e.message} - you need to install the appropriate driver: ${device.type}"
        } 
        catch (IllegalArgumentException ignored) 
        {
            //Intentionally ignored.  Expected if device id already exists in HE.
        }
    }
}

def discoverDevicesCallback(resp, data)
{
    LogDebug("discoverDevicesCallback()");

    def respCode = resp.getStatus()
    if (resp.hasError()) 
    {
        def respError = ''
        try 
        {
            respError = resp.getErrorJson()
        } 
        catch (Exception ignored) 
        {
            // no response body
        }
        if (respCode == 401 && !data.isRetry) 
        {
            LogWarn('Authorization token expired, will refresh and retry.')
            refreshToken()
            data.isRetry = true
            asynchttpGet(handleDeviceList, data.params, data)
        } 
        else 
        {
            LogWarn("Device-list response code: ${respCode}, body: ${respError}")
        }
    } 
    else 
    {
        def respJson = resp.getJson()
        LogDebug(respJson);
    }
}

def handleAuthRedirect() 
{
    LogDebug("handleAuthRedirect()");

    def authCode = params.code

    LogDebug("AuthCode: ${authCode}")
    def authorization = ("${global_conusmerKey}:${global_consumerSecret}").bytes.encodeBase64().toString()

    def headers = [
                    Authorization: authorization,
                    Accept: "application/json"
                ]
    def body = [
                    grant_type:"authorization_code",
                    code:authCode,
                    redirect_uri:global_redirectURL
    ]
    def params = [uri: global_apiURL, path: "/oauth2/token", headers: headers, body: body]
    
    try 
    {
        httpPost(params) { response -> loginResponse(response) }
    } 
    catch (groovyx.net.http.HttpResponseException e) 
    {
        LogError("Login failed -- ${e.getLocalizedMessage()}: ${e.response.data}")
    }

    def stringBuilder = new StringBuilder()
    stringBuilder << "<!DOCTYPE html><html><head><title>Honeywell Connected to Hubitat</title></head>"
    stringBuilder << "<body><p>Hubitate and Honeywell are now connected.</p>"
    stringBuilder << "<p><a href=http://${location.hub.localIP}/installedapp/configure/${app.id}/mainPage>Click here</a> to return to the App main page.</p></body></html>"
    
    def html = stringBuilder.toString()

    render contentType: "text/html", data: html, status: 200
}

def refreshToken()
{
    LogDebug("getToken()");

    if (state.refresh_token != null)
    {
        def authorization = ("${global_conusmerKey}:${global_consumerSecret}").bytes.encodeBase64().toString()

        def headers = [
                        Authorization: authorization,
                        Accept: "application/json"
                    ]
        def body = [
                        grant_type:"refresh_token",
                        refresh_token:state.refresh_token

        ]
        def params = [uri: global_apiURL, path: "/oauth2/token", headers: headers, body: body]
        
        try 
        {
            httpPost(params) { response -> loginResponse(response) }
        } 
        catch (groovyx.net.http.HttpResponseException e) 
        {
            LogError("Login failed -- ${e.getLocalizedMessage()}: ${e.response.data}")  
        }
    }
    else
    {
        LogError("Failed to refresh token, refresh token null.")
    }

    runEvery1Hour refreshToken
}

def loginResponse(response) 
{
    LogDebug("loginResponse()");

    def reCode = response.getStatus();
    def reJson = response.getData();
    LogDebug("reCode: {$reCode}")
    LogDebug("reJson: {$reJson}")

    if (reCode == 200)
    {
        state.access_token = reJson.access_token;
        state.refresh_token = reJson.refresh_token;
    }
    else
    {
        LogError("LoginResponse Failed HTTP Request Status: ${reCode}");
    }
}

def refreshAllThermostats()
{
    LogDebug("refreshAllThermostats()");

    def children = getChildDevices()
    children.each 
    {
        if (it != null) 
        {
            refreshThermosat(it);
        }
    }
}


def refreshThermosat(com.hubitat.app.DeviceWrapper device)
{
    LogDebug("refreshThermosat")
    def deviceID = device.getDeviceNetworkId();
    def locDelminator = deviceID.indexOf('-');
    def honeywellLocation = deviceID.substring(0, (locDelminator-1))
    def honewellDeviceID = deviceID.substring((locDelminator+2))

    LogDebug("Attempting to Update DeviceID: ${honewellDeviceID}, With LocationID: ${honeywellLocation}");

    def uri = global_apiURL + '/v2/devices/thermostats/'+ honewellDeviceID + '?apikey=' + global_conusmerKey + '&locationId=' + honeywellLocation
    def headers = [ Authorization: 'Bearer ' + state.access_token ]
    def contentType = 'application/json'
    def params = [ uri: uri, headers: headers, contentType: contentType ]
    LogDebug("Location Discovery-params ${params}")

    //add error checking
    def reJson =''
    try 
    {
        httpGet(params) 
        { 
            response ->
            def reCode = response.getStatus();
            reJson = response.getData();
            LogDebug("reCode: {$reCode}")
            LogDebug("reJson: {$reJson}")
        }
    }
    catch (groovyx.net.http.HttpResponseException e) 
    {
        LogError("Thermosate API failed -- ${e.getLocalizedMessage()}: ${e.response.data}")
    }

    def tempUnits = "F"
    if (reJson.units != "Fahrenheit")
    {
        tempUnits = "C"
    }
    LogDebug("updateThermostats-tempUnits: ${tempUnits}")

    def indoorTemperature = reJson.indoorTemperature
    LogDebug("updateThermostats-indoorTemperature: ${indoorTemperature}")
    sendEvent(device, [name: 'temperature', value: indoorTemperature, unit: tempUnits])

    def heatingSetpoint = reJson.changeableValues.heatSetpoint
    LogDebug("updateThermostats-heatSetpoint: ${heatingSetpoint}")
    sendEvent(device, [name: 'heatingSetpoint', value: heatingSetpoint, unit: tempUnits])

    def coolingSetpoint = reJson.changeableValues.coolSetpoint
    LogDebug("updateThermostats-coolingSetpoint: ${coolingSetpoint}")
    sendEvent(device, [name: 'coolingSetpoint', value: coolingSetpoint, unit: tempUnits])

    def supportedThermostatFanModes = reJson.settings.fan.allowedModes
    def lowerCaseSupportedFanModes = []
    supportedThermostatFanModes.each {m -> lowerCaseSupportedFanModes.add(m.toLowerCase())}
    LogDebug("updateThermostats-supportedThermostatFanModes: ${lowerCaseSupportedFanModes}")
    sendEvent(device, [name: 'supportedThermostatFanModes', value: lowerCaseSupportedFanModes])

    def supportedThermostatModes = reJson.allowedModes
    def lowerCaseSupportedThermostatModes = []
    supportedThermostatModes.each {m -> lowerCaseSupportedThermostatModes.add(m.toLowerCase())}
    LogDebug("updateThermostats-supportedThermostatModes: ${lowerCaseSupportedThermostatModes}")
    sendEvent(device, [name: 'supportedThermostatModes', value: lowerCaseSupportedThermostatModes])

    def thermostatMode = (reJson.changeableValues.mode).toLowerCase();
    LogDebug("updateThermostats-thermostatMode: ${thermostatMode}")
    sendEvent(device, [name: 'thermostatMode', value: thermostatMode])

    def thermostatFanMode = (reJson.settings.fan.changeableValues.mode).toLowerCase();
    LogDebug("updateThermostats-thermostatFanMode: ${thermostatFanMode}")
    sendEvent(device, [name: 'thermostatFanMode', value: thermostatFanMode])

    def operationStatus = reJson.operationStatus.mode
    def formatedOperationStatus =''
    if (operationStatus == "EquipmentOff")
    {
        formatedOperationStatus = "idle";
    }
    else if(operationStatus == "Heat")
    {
        formatedOperationStatus = "heating";
    }
    else if(operationStatus == "Cool")
    {
        formatedOperationStatus = "cooling";
    }
    else
    {
        LogError("Unexpected Operation Status: ${operationStatus}")
    }

    LogDebug("updateThermostats-thermostatOperatingState: ${formatedOperationStatus}")
    sendEvent(device, [name: 'thermostatOperatingState', value: formatedOperationStatus])

    def humidity = reJson.indoorHumidity
    LogDebug("updateThermostats-humidity: ${humidity}")
    sendEvent(device, [name: 'humidity', value: humidity])
}

def setThermosatSetPoint(com.hubitat.app.DeviceWrapper device, mode=null, autoChangeoverActive=false, heatPoint=null, coolPoint=null)
{
    LogDebug("setThermosatSetPoint")
    def deviceID = device.getDeviceNetworkId();
    def locDelminator = deviceID.indexOf('-');
    def honeywellLocation = deviceID.substring(0, (locDelminator-1))
    def honewellDeviceID = deviceID.substring((locDelminator+2))


    if (mode == null)
    {
        mode=device.currentValue('thermostatMode');
    }

    if (mode == "heat")
    {
        mode = "Heat"
    }
    else if (mode == "cool")
    {
        mode = "Cool"
    }
    else if (mode == "off")
    {
        mode = "Off"
    }
    else
    {
        LogError("Invalid Mode Specified: ${mode}")
        return false;
    }

    if (heatPoint == null)
    {
        heatPoint=device.currentValue('heatingSetpoint');
    }

    if (coolPoint == null)
    {
        coolPoint=device.currentValue('coolingSetpoint');
    }

    LogDebug("Attempting to Set DeviceID: ${honewellDeviceID}, With LocationID: ${honeywellLocation}");
    def uri = global_apiURL + '/v2/devices/thermostats/'+ honewellDeviceID + '?apikey=' + global_conusmerKey + '&locationId=' + honeywellLocation

    def headers = [
                    Authorization: 'Bearer ' + state.access_token,
                    "Content-Type": "application/json"
                    ]
    def body = [
            mode:mode, 
            autoChangeoverActive:autoChangeoverActive, 
            heatSetpoint:heatPoint, 
            coolSetPoint:coolPoint]

    def params = [ uri: uri, headers: headers, body: body]
    LogDebug("setThermosat-params ${params}")

    try
    {
        httpPostJson(params) { response -> LogInfo("SetThermostate Response: ${response.getStatus()}")}
    }
    catch (groovyx.net.http.HttpResponseException e) 
    {
        LogError("Set Api Call failed -- ${e.getLocalizedMessage()}: ${e.response.data}")
        return false;
    }

    refreshThermosat(device)
}

def setThermosatFan(com.hubitat.app.DeviceWrapper device, fan=null)
{
    LogDebug("setThermosatFan")
    def deviceID = device.getDeviceNetworkId();
    def locDelminator = deviceID.indexOf('-');
    def honeywellLocation = deviceID.substring(0, (locDelminator-1))
    def honewellDeviceID = deviceID.substring((locDelminator+2))


    if (fan == null)
    {
        fan=device.('thermostatFanMode');
    }

    if (fan == "auto")
    {
        fan = "Auto"
    }
    else if (fan == "on")
    {
        fan = "On"
    }
    else if (fan == "circulate")
    {
        fan = "Circulate"
    }
    else
    {
        LogError("Invalid Fan Mode Specified: ${fan}")
        return false;
    }


    LogDebug("Attempting to Set Fan For DeviceID: ${honewellDeviceID}, With LocationID: ${honeywellLocation}");
    def uri = global_apiURL + '/v2/devices/thermostats/'+ honewellDeviceID + '/fan' + '?apikey=' + global_conusmerKey + '&locationId=' + honeywellLocation

    def headers = [
                    Authorization: 'Bearer ' + state.access_token,
                    "Content-Type": "application/json"
                    ]
    def body = [
            mode:fan]

    def params = [ uri: uri, headers: headers, body: body]
    LogDebug("setThermosat-params ${params}")

    try
    {
        httpPostJson(params) { response -> LogInfo("SetThermostate Response: ${response.getStatus()}")}
    }
    catch (groovyx.net.http.HttpResponseException e) 
    {
        LogError("Set Fan Call failed -- ${e.getLocalizedMessage()}: ${e.response.data}")
        return false;
    }

    refreshThermosat(device)
}