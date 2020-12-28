#!/bin/bash

usage="Usage: $(basename "$0") [-v | --version]<version in simver formate> [-c|--codePath]<root path to code> [-r|--releasenotes]<releasenotes>)"

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -v|--version) version="$2"; shift ;;
        -c|--codePath) codePath="$2"; shift;;
        -r|--releasenotes) relNotes="$2"; shift ;;
        *) echo "Unknown parameter passed: $1"; echo $usage; exit 1 ;;
    esac
    shift
done

if [[ -z "${version}" ]] || [[ -z "${codePath}" ]] || [[ -z "${relNotes}" ]]; then
  echo "Not all requied parmiters passed!"; echo $usage; exit 1 ;
fi

appPath="$codePath/honeywellhomeapp.groovy"
thermDriverPath="$codePath/honeywellhomedriver.groovy"
sensorDriverPath="$codePath/honeywellremotesensordriver.groovy"

echo "Version: $version"
echo "CodePath: $codePath"
echo "AppPath: $appPath"
echo "ThermDriverPath: $thermDriverPath"
echo "SensorDriverPath: $sensorDriverPath"
echo "ReleaseNotes: $relNotes"

hpm manifest-modify-driver --id 390cc6e1-acbb-4af3-be52-7a68e4bcc580 --version=$version --location $thermDriverPath ../hubitat-packages/packages/honeywellManifest.json
hpm manifest-modify-driver --id ebe84e87-e064-4029-9e5b-ad0cb7939230 --version=$version --location $sensorDriverPath ../hubitat-packages/packages/honeywellManifest.json
hpm manifest-modify-app --id fe2bb542-854f-47b4-8d20-cde1ead99f2d --version=$version --location $appPath ../hubitat-packages/packages/honeywellManifest.json
hpm manifest-modify --releasenotes="$relNotes" --version=$version ../hubitat-packages/packages/honeywellManifest.json