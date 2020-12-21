/**
 *  Honeywell Home Remote Sensor Driver
 *
 *  Device Type:	Honeywell Remote Sensor
 *  Author: 		Tim Yuhl
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 *  modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  History:
 *
 *
 */

import groovy.transform.Field

metadata {
	definition (
			name: "Honeywell Home Remote Sensor",
			namespace: "thecloudtaylor",
			author: "Tim Yuhl") {
			capability 	"Temperature Measurement"
			capability  "Relative Humidity Measurement"
			capability 	"Sensor"
			capability 	"Battery"
			capability	"Refresh"

			attribute "group", "number"
			attribute "room", "number"
	}
	preferences {
		section("Logging") {
			input "logging", "enum", title: "Log Level", required: false, defaultValue: "INFO", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
		}
	}
}

/**
 * Boilerplate callback methods called by the framework
 */
void configure() {
	log("configure called", "trace")
}

void installed()
{
	log("installed called", "trace")
}

void uninstalled()
{
	log("uninstalled called", "trace")
}

void updated()
{
	log("updated called", "trace")
}

void parse(String message)
{
	log("parse called with message: ${message}", "trace")
}
/* End of built-in callbacks */

def refresh()
{
	Log("refresh called", "trace");
	parent.RefreshRemoteSensor(device)
}

private determineLogLevel(data) {
	switch (data?.toUpperCase()) {
		case "TRACE":
			return 0
			break
		case "DEBUG":
			return 1
			break
		case "INFO":
			return 2
			break
		case "WARN":
			return 3
			break
		case "ERROR":
			return 4
			break
		default:
			return 1
	}
}

def log(Object data, String type) {
	data = "-- ${device.label} -- ${data ?: ''}"

	if (determineLogLevel(type) >= determineLogLevel(settings?.logging ?: "INFO")) {
		switch (type?.toUpperCase()) {
			case "TRACE":
				log.trace "${data}"
				break
			case "DEBUG":
				log.debug "${data}"
				break
			case "INFO":
				log.info "${data}"
				break
			case "WARN":
				log.warn "${data}"
				break
			case "ERROR":
				log.error "${data}"
				break
			default:
				log.error("-- ${device.label} -- Invalid Log Setting")
		}
	}
}
