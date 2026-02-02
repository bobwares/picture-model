/**
 * App: Picture Model
 * Package: e2e
 * File: crawler-start.sh
 * Version: 0.1.1
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T22:37:13Z
 * Exports: None
 * Description: Curl test for starting a crawl job.
 * crawler-start.sh - posts a start crawl request to the API.
 */
#!/usr/bin/env bash
set -euo pipefail

API_BASE="${API_BASE:-http://localhost:8080}"
DRIVE_ID="${DRIVE_ID:-}"
ROOT_PATH="${ROOT_PATH:-}"
INCREMENTAL="${INCREMENTAL:-false}"

if [[ -z "$DRIVE_ID" ]]; then
  echo "DRIVE_ID is required. Example:"
  echo "  DRIVE_ID=your-drive-uuid API_BASE=http://localhost:8080 e2e/crawler-start.sh"
  exit 1
fi

payload=$(cat <<JSON
{
  "driveId": "${DRIVE_ID}",
  "rootPath": ${ROOT_PATH:+\"${ROOT_PATH}\"},
  "isIncremental": ${INCREMENTAL}
}
JSON
)

response=$(curl -sS -X POST "${API_BASE}/api/crawler/start" \
  -H "Content-Type: application/json" \
  -d "${payload}")

if command -v jq >/dev/null 2>&1; then
  echo "${response}" | jq .
else
  echo "${response}"
fi
