#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT_DIR"

PATTERNS=(
  "selection_status"
  "menu_status"
  "payment_mode"
  "selected_delivery_address_idx"
  "payment_expires_at"
  "payment_share"
  "room_code"
  "ACCEPTED"
  "PREPARING"
  "READY"
  "COMPLETED"
)

for pattern in "${PATTERNS[@]}"; do
  echo "===== $pattern ====="
  grep -RIn --exclude-dir=.git --exclude-dir=build --exclude-dir=.gradle -- "$pattern" . || true
  echo
done
