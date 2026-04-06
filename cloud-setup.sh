#!/usr/bin/env bash
set -euo pipefail

# Java / Gradle 환경 확인
java -version || true

# 모듈 위치 자동 감지
if [ -d "moeats" ]; then
  cd moeats
fi

# Gradle wrapper 권한
if [ -f "./gradlew" ]; then
  chmod +x ./gradlew
fi

# 의존성 선설치 및 빌드 캐시 준비
if [ -f "./gradlew" ]; then
  ./gradlew dependencies >/dev/null || true
  ./gradlew testClasses >/dev/null || true
fi
