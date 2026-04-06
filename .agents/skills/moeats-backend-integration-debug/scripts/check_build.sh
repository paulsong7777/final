#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT_DIR"

if [ -f "./gradlew" ]; then
  chmod +x ./gradlew || true
  ./gradlew clean build --stacktrace
elif [ -f "./moeats/gradlew" ]; then
  cd ./moeats
  chmod +x ./gradlew || true
  ./gradlew clean build --stacktrace
else
  echo "gradlew not found"
  exit 1
fi
