/*
Hubitat Driver For Honeywell Thermistate
(C) 2020 - Taylor Brown

11-25-2020 :  Initial 
*/
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

metadata {
    definition (name: "Honeywell Home Thermostat", namespace: "thecloudtaylor", author: "Taylor Brown") {
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Refresh"

        //Maybe?
		capability "Sensor"
		attribute "thermostatFanState", "string"
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
}

void emergencyHeat()
{
    LogDebug("emergencyHeat called");
    
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
}

void off()
{
    LogDebug("off called");
}

//Defined Command : temperature required (NUMBER) - Cooling setpoint in degrees
void setCoolingSetpoint(temperature)
{
    LogDebug("setCoolingSetpoint called");
}

//Defined Command : temperature required (NUMBER) - Heating setpoint in degrees
void setHeatingSetpoint(temperature)
{
    LogDebug("setHeatingSetpoint called");
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
}

//Defined Command : thermostatmode required (ENUM) - Thermostat mode to set
void setThermostatMode(thermostatmode)
{
    LogDebug("setThermostatMode called");
}

void refresh()
{
    LogDebug("Refresh called");
    parent.updateThermosat(device)
}
