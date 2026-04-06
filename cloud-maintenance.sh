#!/usr/bin/env bash
set -euo pipefail

if [ -d "moeats" ]; then
  cd moeats
fi

if [ -f "./gradlew" ]; then
  chmod +x ./gradlew
  ./gradlew testClasses >/dev/null || true
fi
