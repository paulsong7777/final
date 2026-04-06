#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT_DIR"

check_file() {
  local path="$1"
  if [ -e "$path" ]; then
    echo "[OK] $path"
  else
    echo "[MISS] $path"
  fi
}

echo "## Files"
check_file "build.gradle"
check_file "settings.gradle"
check_file "src/main/resources/application.properties"
check_file "src/main/resources/application-dev.properties"
check_file "src/main/resources/mappers"
check_file "src/main/java"
check_file "src/test"

if [ -d "moeats" ]; then
  echo "## Nested module detected: moeats"
  cd moeats
  check_file "build.gradle"
  check_file "src/main/resources/application.properties"
  check_file "src/main/resources/application-dev.properties"
  check_file "src/main/resources/mappers"
  check_file "src/main/java"
  check_file "src/test"
fi
