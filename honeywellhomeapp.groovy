
/*
Hubitat Driver For Honeywell Thermistate
(C) 2020 - Taylor Brown

11-25-2020 :  Initial 
*/
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field


@Field static String apiURL = "https://api.honeywell.com/oauth2/authorize"
@Field static String redirectURL = "https://cloud.hubitat.com/oauth/stateredirect"
@Field static String conusmerKey = "DEb39Y2eKMrv3fGpoKudWvLOZ9LDey6N"
@Field static String consumerSecret = "hGyrQFX5TU4frGG5"

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

    def state = java.net.URLEncoder.encode("${getHubUID()}/apps/${app.id}", "UTF-8")
    def escapedRedirectURL = java.net.URLEncoder.encode(redirectURL, "UTF-8")
    def authQueryString = "response_type=code&redirect_uri=${escapedRedirectURL}&client_id=${conusmerKey}&state=${state}";

	def params = [
    	uri: apiURL,
        path: "/oauth2/authorize",
        queryString: authQueryString.toString()
    ]
    LogDebug("honeywell_auth request params: ${params}");

    try {
		httpPost(params) { response -> 
			if (response.status == 302) 
            {
                LogDebug("Response 302, getting redirect")
                def redirectLocation = response.headers.'Location'
				LogDebug("Redirect: ${redirectLocation}");
                def redirectURL = (redirectLocation).toString();
                redirectURL=("'${redirectURL}' target='_blank'");
				LogDebug("RedirectString: ${redirectURL}");
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
                        href url:redirectURL, external:true, required:false, title:"Connect to Honeywell:", description:description
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


def honeywell_auth() 
{
    LogDebug("honeywell_auth()");

    def state = java.net.URLEncoder.encode("${getHubUID()}/apps/${app.id}", "UTF-8")
    def escapedRedirectURL = java.net.URLEncoder.encode(redirectURL, "UTF-8")
    
    def authQueryString = "response_type=code&redirect_uri=${escapedRedirectURL}&client_id=${conusmerKey}&state=${state}";

	def params = [
    	uri: apiURL,
        path: "/oauth2/authorize",
        queryString: authQueryString.toString()
    ]
    LogDebug("honeywell_auth request params: ${params}");

	try {
		httpPost(params) { response -> 
			if (response.status == 302) 
            {
                LogDebug("Response 302, getting redirect")
                def redirectLocation = response.headers.'Location'
				LogDebug("Redirect: ${redirectLocation}");
                return (redirectLocation).toString();
			}
            else
            {
				LogError("Auth request Returned Invalid HTTP Response: ${response.status}")
        		return false
			} 
		}
	}
	catch (e)	{
		LogError("Exception In API Auth: ${e}");
		return false
	}
}