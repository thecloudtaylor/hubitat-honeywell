/*
Hubitat Driver For Honeywell Thermistate

Copyright 2020 - Taylor Brown

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Major Releases:
11-25-2020 :  Initial 
11-27-2020 :  Alpha Release (0.1)

*/
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

metadata {
    definition (
        name: "Honeywell Home Thermostat", 
        description:"Driver for Lyric (LCC) and T series (TCC) Honeywell Thermostats, Requires corisponding Honeywell Home App.",
        importUrl:"https://raw.githubusercontent.com/thecloudtaylor/hubitat-honeywell/main/honeywellhomedriver.groovy"
        namespace: "thecloudtaylor", 
        author: "Taylor Brown") {
            capability "Actuator"
            capability "Temperature Measurement"
            capability "Relative Humidity Measurement"
            capability "Thermostat"
            capability "Refresh"

        //Maybe?
		capability "Sensor"
		attribute "thermostatFanState", "string"
        attribute "autoChangeoverActive", "enum", ["true", "false"]
        attribute "allowedModes", "enum", ["EmergencyHeat", "Heat", "Off", "Cool","Auto"]

    }

    preferences{
        input ("debugLogs", "bool", 
			   title: "Enable debug logging", 
			   defaultValue: false)
		input ("descriptionText", "bool", 
			   title: "Enable description text logging", 
			   defaultValue: true)
    }
}

void LogDebug(logMessage)
{
    if(debugLogs)
    {
        log.debug "${device.displayName} ${logMessage}";
    }
}

void LogInfo(logMessage)
{
    log.info "${device.displayName} ${logMessage}";
}

void LogWarn(logMessage)
{
    log.warn "${device.displayName} ${logMessage}";
}

void disableDebugLog() 
{
    LogWarn("Disabling Debug Logging.");
    device.updateSetting("debugLogs",[value:"false",type:"bool"]);
}

void installed()
{
    LogInfo("Installing.");
    refresh()
}

void uninstalled()
{
    LogInfo("Uninstalling.");
}

void updated() 
{
    LogInfo("Updating.");
}

void parse(String message) 
{
    LogDebug("ParseCalled: ${message}");
}

void auto()
{
    LogDebug("auto called");
}

void cool()
{
    LogDebug("cool called");

    setThermostatMode("cool")
}

void emergencyHeat()
{
    LogDebug("emergencyHeat called");

    LogWarn("EmergancyHeat Not Supported")
    
}

void fanAuto()
{
    LogDebug("fanAuto called");
}

void fanCirculate()
{
    LogDebug("fanCirculate called");
}

void fanOn()
{
    LogDebug("fanOn called");
}

void heat()
{
    LogDebug("heat called");

    setThermostatMode("heat")
}

void off()
{
    LogDebug("off called");

    setThermostatMode("off")
}

//Defined Command : temperature required (NUMBER) - Cooling setpoint in degrees
void setCoolingSetpoint(temperature)
{
    LogDebug("setCoolingSetpoint() - autoChangeoverActive: ${device.currentValue("autoChangeoverActive")}");
    
    //setThermosatSetPoint(com.hubitat.app.DeviceWrapper device, mode=null, autoChangeoverActive=false, heatPoint=null, coolPoint=null)
    parent.setThermosatSetPoint(device, null, device.currentValue("autoChangeoverActive"), null, temperature);
}

//Defined Command : temperature required (NUMBER) - Heating setpoint in degrees
void setHeatingSetpoint(temperature)
{
    LogDebug("setHeatingSetpoint() - autoChangeoverActive: ${device.currentValue("autoChangeoverActive")}");

    //setThermosatSetPoint(com.hubitat.app.DeviceWrapper device, mode=null, autoChangeoverActive=false, heatPoint=null, coolPoint=null)
    parent.setThermosatSetPoint(device, null, device.currentValue("autoChangeoverActive"), temperature, null);
}

//Defined Command : JSON_OBJECT (JSON_OBJECT) - JSON_OBJECT
void setSchedule(JSON_OBJECT)
{
    LogDebug("setSchedule called");
}

//Defined Command : fanmode required (ENUM) - Fan mode to set
void setThermostatFanMode(fanmode)
{
    LogDebug("setThermostatFanMode called");
    
    parent.setThermosatFan(device, fanmode);
}

//Defined Command : thermostatmode required (ENUM) - Thermostat mode to set
void setThermostatMode(thermostatmode)
{
    LogDebug("setThermostatMode() - autoChangeoverActive: ${device.currentValue("autoChangeoverActive")}");

    //setThermosatSetPoint(com.hubitat.app.DeviceWrapper device, mode=null, autoChangeoverActive=false, heatPoint=null, coolPoint=null)
    parent.setThermosatSetPoint(device, thermostatmode, device.currentValue("autoChangeoverActive"), null, null);
}

void refresh()
{
    LogDebug("Refresh called");
    parent.refreshThermosat(device)
}
