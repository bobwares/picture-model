#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="${ROOT_DIR:-"$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"}"
TRIGGERS_DIR="${TRIGGERS_DIR:-"$ROOT_DIR/triggers"}"
REQUESTS_DIR="${REQUESTS_DIR:-"$ROOT_DIR/requests"}"
OUTPUT_DIR="${OUTPUT_DIR:-"$ROOT_DIR/responses"}"
POLL_INTERVAL="${POLL_INTERVAL:-1}"

CODEX_CMD="${CODEX_CMD:-codex}"
CODEX_ARGS="${CODEX_ARGS:-}"

IFS=' ' read -r -a CODEX_ARGS_ARR <<< "$CODEX_ARGS"

mkdir -p "$TRIGGERS_DIR" "$REQUESTS_DIR" "$OUTPUT_DIR"

log() {
  printf '%s %s\n' "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" "$*"
}

process_trigger() {
  local trigger="$1"
  local request_name request_path output_path

  request_name="$(tr -d '\r\n' < "$trigger")"
  if [[ -z "$request_name" ]]; then
    log "empty trigger: $trigger"
    rm -f "$trigger"
    return
  fi

  request_path="$REQUESTS_DIR/$request_name"
  if [[ ! -f "$request_path" ]]; then
    log "missing request: $request_path"
    rm -f "$trigger"
    return
  fi

  output_path="$OUTPUT_DIR/${request_name%.*}.out"
  log "processing request: $request_name"

  if ! cat "$request_path" | "$CODEX_CMD" "${CODEX_ARGS_ARR[@]}" > "$output_path" 2>&1; then
    log "codex failed for: $request_name (see $output_path)"
  else
    log "completed request: $request_name (output $output_path)"
  fi

  rm -f "$trigger"
}

log "watching triggers in $TRIGGERS_DIR (poll ${POLL_INTERVAL}s)"
while true; do
  shopt -s nullglob
  for trigger in "$TRIGGERS_DIR"/*; do
    [[ -f "$trigger" ]] || continue
    process_trigger "$trigger"
  done
  shopt -u nullglob
  sleep "$POLL_INTERVAL"
done
