/*
Hubitat Driver For Honeywell Thermistate
(C) 2020 - Taylor Brown

11-25-2020 :  Initial 
*/
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

metadata {
    definition (name: "Honeywell Home", namespace: "thecloudtaylor", author: "Taylor Brown") {
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
    UpdateAttributes();
}


void UpdateAttributes()
{
    device.sendEvent(name: "coolingSetpoint", value: 72, unit: "F");

    device.sendEvent(name: "heatingSetpoint", value: 68, unit: "F");

    //device.sendEvent(name: "schedule - JSON_OBJECT

    //Posible Values: ENUM ["on", "circulate", "auto"]
    device.sendEvent(name: "supportedThermostatFanModes", value: ["on", "circulate", "auto"], displayed: false);

    //Posible Values: ENUM ["auto", "off", "heat", "emergency heat", "cool"]
    device.sendEvent(name: "supportedThermostatModes", value: ["auto", "off", "heat", "emergency heat", "cool"], displayed: false);

    device.sendEvent(name: "temperature", value: 70, unit: "F");

    //Posible Values: ENUM ["on", "circulate", "auto"]
    device.sendEvent(name: "thermostatFanMode", value: ["on", "circulate", "auto"], displayed: false);

    //Posible Values: ENUM ["auto", "off", "heat", "emergency heat", "cool"]
    device.sendEvent(name: "thermostatMode", value: ["auto", "off", "heat", "emergency heat", "cool"], displayed: false);

    //Posible Values: ENUM ["heating", "pending cool", "pending heat", "vent economizer", "idle", "cooling", "fan only"]
    device.sendEvent(name: "thermostatOperatingState", value: ["heating", "pending cool", "pending heat", "vent economizer", "idle", "cooling", "fan only"], displayed: false);

    device.sendEvent(name: "thermostatSetpoint", value: 69, unit: "F");

}