#!/usr/bin/env bash
# Runs inside the emulator job. Fails if Looscid crashes in its first 20 seconds.
set -euo pipefail
PKG=social.looscid.app

adb install -r Looscid.apk
adb logcat -c
adb shell am start -W -n "$PKG/.MainActivity"
sleep 20

if ! adb shell pidof "$PKG" >/dev/null; then
  echo "Looscid is not running after 20 seconds."
  adb logcat -d | tail -n 200
  exit 1
fi

if adb logcat -d | grep -A 30 "FATAL EXCEPTION" | grep -q "$PKG"; then
  echo "Looscid crashed:"
  adb logcat -d | grep -A 30 "FATAL EXCEPTION"
  exit 1
fi

echo "Looscid is still running and nothing crashed."
