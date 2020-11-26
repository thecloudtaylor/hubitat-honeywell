
/*
Hubitat Driver For Honeywell Thermistate
(C) 2020 - Taylor Brown

11-25-2020 :  Initial 
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
    page(name: "connectToHoneywell", title: "Connection to Honeywell Home", install: true)
    page(name: "deviceSelection", title: "Select Devices", install: true)
}

mappings {
    path("/handleAuth") {
        action: [
            GET: "handleAuthRedirect"
        ]
    }
}


def LogDebug(logMessage)
{
    //if(settings?.debugOutput)
    //{
        log.debug "${logMessage}";
    //}
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

def uninstalled() 
{
    LogInfo("Uninstalling Honeywell Home.");

	for (device in getChildDevices())
	{
		//deleteChildDevice(device.deviceNetworkId)
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
	catch (e)	{
		LogError("Exception In API Auth: ${e}");
        return false;
	}

    return dynamicPage(name: "connectToHoneywell", title: "Connect with HoneyWell Home", nextPage:"deviceSelection", install:true, uninstall: false) {
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
                    section("Settings")
                    {
                        input("debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: false, required: false)
                    }
                }
}

def deviceSelection()
{
    LogDebug("deviceSelection()");


    return dynamicPage(name: "deviceSelection", title: "Temp", install:false, uninstall:true) {
        section("") 
        { 
            paragraph "Temp"
        }
    }
}

def updated() 
{
	LogDebug("Updated with config: ${settings}");

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


//BUGBUG: should be ensuring refresh is valid.
def refreshToken(authCode)
{
    LogDebug("getToken()");

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

//BUGBUG: Should be starting a timer to refresh token.
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