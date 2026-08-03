#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"

./gradlew :ekeysdk:assembleRelease --no-configuration-cache

cp ekeysdk/build/outputs/aar/ekeysdk-release.aar ekeysdk/EkeySDK.aar

echo "Done: EkeySDK.aar generated at $(cd ekeysdk && pwd)/EkeySDK.aar"
