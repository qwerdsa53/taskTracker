#!/usr/bin/env bash
# test-graceful-shutdown.sh — verify task-tracker-api graceful shutdown.
#
# What is tested:
#   1. API is healthy before the signal (readiness probe → 200).
#   2. SIGTERM is sent; container exits within stop_grace_period (≤ 30 s).
#   3. Container exits with code 0 or 143 (clean SIGTERM exit), not 137 (SIGKILL).
#   4. Response timeline: last 200 precedes the first non-2xx — no traffic accepted
#      after shutdown starts.
#   5. No broken connections: every curl invocation gets a complete HTTP response,
#      not a mid-stream RST (connection not closed while response was in flight).
#
# Limitation: this script cannot prove that a slow request *already processing*
# completes, because the project has no deliberately slow endpoint.  To test that
# behaviour add a debug endpoint (e.g. GET /api/v1/debug/slow?ms=3000) and run
# a request against it before sending SIGTERM.
#
# Prerequisites:
#   docker compose up -d   (at minimum: postgres, redis, api, nginx)
#   curl, awk, bc
#
# Usage:
#   ./scripts/test-graceful-shutdown.sh [--api-url URL] [--no-restart]
#
#   --api-url URL     Base URL to hit (default: http://localhost:8080)
#   --no-restart      Do not bring the api back up after the test

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/compose.sh
source "$SCRIPT_DIR/compose.sh"

# ── Defaults ──────────────────────────────────────────────────────────────────
API_URL="http://localhost:8080"
HEALTH_PATH="/actuator/health/readiness"
POLL_INTERVAL="0.2"      # seconds between request-storm polls
GRACE_PERIOD=35          # seconds — slightly above docker-compose stop_grace_period:30s
AUTO_RESTART=true

# ── Argument parsing ──────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
    case "$1" in
        --api-url)   API_URL="$2";  shift 2 ;;
        --no-restart) AUTO_RESTART=false; shift ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

# ── Colours ───────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

FAILURES=0
pass()  { echo -e "  ${GREEN}✓${NC} $*"; }
fail()  { echo -e "  ${RED}✗${NC} $*"; FAILURES=$(( FAILURES + 1 )); }
info()  { echo -e "  ${CYAN}→${NC} $*"; }
warn()  { echo -e "  ${YELLOW}!${NC} $*"; }
header(){ echo -e "\n${BOLD}$*${NC}"; }

WORK_DIR=$(mktemp -d)
RESPONSE_LOG="$WORK_DIR/responses.log"   # <timestamp_ms> <http_code|ERR>
trap 'rm -rf "$WORK_DIR"' EXIT

# ── Helpers ───────────────────────────────────────────────────────────────────
require_commands() {
    for cmd in curl docker awk; do
        command -v "$cmd" >/dev/null 2>&1 || { echo "ERROR: '$cmd' not found"; exit 1; }
    done
}

get_container_id() {
    compose_run ps -q api 2>/dev/null | head -1
}

# Returns HTTP status code or "000" on connection failure.
http_status() {
    curl -s -o /dev/null -w "%{http_code}" --max-time 2 "$API_URL$HEALTH_PATH" 2>/dev/null \
        || echo "000"
}

# Runs a curl and records "<ts_ms> <code|ERR>" to RESPONSE_LOG.
# Non-zero curl exit (RST, timeout) is logged as "ERR".
record_request() {
    local ts
    ts=$(date +%s%3N)
    local code
    if code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 \
                   "$API_URL$HEALTH_PATH" 2>/dev/null); then
        echo "$ts $code" >> "$RESPONSE_LOG"
    else
        echo "$ts ERR" >> "$RESPONSE_LOG"
    fi
}

# ── Banner ────────────────────────────────────────────────────────────────────
require_commands

echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}  Graceful Shutdown Test — task-tracker-api${NC}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# ── Step 1: Pre-flight ────────────────────────────────────────────────────────
header "1. Pre-flight"

CONTAINER_ID=$(get_container_id)
if [[ -z "$CONTAINER_ID" ]]; then
    echo "ERROR: api container not running.  Run: docker compose up -d"
    exit 1
fi
info "Container ID : $CONTAINER_ID"
info "Target URL   : $API_URL$HEALTH_PATH"

STATUS=$(http_status)
if [[ "$STATUS" == "200" ]]; then
    pass "Readiness probe → 200 (API healthy)"
else
    fail "Readiness probe → $STATUS (expected 200)"
    echo "   Cannot proceed — is the API fully started?"
    exit 1
fi

# ── Step 2: Background request storm ─────────────────────────────────────────
header "2. Starting background request storm"

(
    while true; do
        record_request
        sleep "$POLL_INTERVAL"
    done
) &
STORM_PID=$!
info "Storm PID: $STORM_PID  (polling every ${POLL_INTERVAL}s)"

# Collect a few baseline 200s before we pull the trigger.
sleep 1.5

# ── Step 3: Send SIGTERM ──────────────────────────────────────────────────────
header "3. Sending SIGTERM"

SIGTERM_TS=$(date +%s%3N)
SHUTDOWN_START=$SECONDS

# docker compose stop sends SIGTERM and honours stop_grace_period; it does NOT
# auto-restart the container (unlike `kill`), which is what we want for a clean test.
compose_run stop api &
STOP_PID=$!

info "SIGTERM sent at ${SIGTERM_TS} ms  (stop_grace_period=30s, Spring timeout=25s)"

# ── Step 4: Wait for container to exit ───────────────────────────────────────
header "4. Waiting for container to exit (max ${GRACE_PERIOD}s)"

CONTAINER_EXITED=false
EXIT_CODE="?"

for _ in $(seq 1 $(( GRACE_PERIOD * 2 ))); do
    sleep 0.5
    STATE=$(docker inspect --format='{{.State.Status}}' "$CONTAINER_ID" 2>/dev/null || echo "gone")
    if [[ "$STATE" == "exited" || "$STATE" == "gone" ]]; then
        CONTAINER_EXITED=true
        EXIT_CODE=$(docker inspect --format='{{.State.ExitCode}}' "$CONTAINER_ID" 2>/dev/null || echo "?")
        break
    fi
done

SHUTDOWN_SECS=$(( SECONDS - SHUTDOWN_START ))

# Let the storm capture one more round of non-2xx, then stop it.
sleep 0.5
kill "$STORM_PID" 2>/dev/null || true
wait "$STOP_PID" 2>/dev/null || true

if $CONTAINER_EXITED; then
    pass "Container exited after ${SHUTDOWN_SECS}s"

    # 0  = clean Spring exit
    # 143 = 128 + SIGTERM(15) — JVM killed by SIGTERM (tini forwards correctly)
    # 137 = 128 + SIGKILL(9)  — Docker hit the grace period and force-killed
    case "$EXIT_CODE" in
        0|143) pass "Exit code $EXIT_CODE — clean shutdown (0=normal, 143=SIGTERM)" ;;
        137)   fail "Exit code 137 — container was SIGKILL'd (shutdown exceeded grace period)" ;;
        *)     fail "Exit code $EXIT_CODE — unexpected" ;;
    esac

    if [[ $SHUTDOWN_SECS -lt 30 ]]; then
        pass "Shutdown took ${SHUTDOWN_SECS}s < 30s grace period"
    else
        warn "Shutdown took ${SHUTDOWN_SECS}s — close to or over grace period"
    fi
else
    fail "Container did NOT exit within ${GRACE_PERIOD}s — possible hung shutdown"
    kill "$STORM_PID" 2>/dev/null || true
fi

# ── Step 5: Analyse response timeline ────────────────────────────────────────
header "5. Response timeline analysis"

if [[ ! -f "$RESPONSE_LOG" ]] || [[ ! -s "$RESPONSE_LOG" ]]; then
    warn "No responses captured — cannot analyse timeline"
else
    TOTAL=$(wc -l < "$RESPONSE_LOG")
    OK_COUNT=$(awk '$2 == "200"'            "$RESPONSE_LOG" | wc -l)
    NON_OK_COUNT=$(awk '$2 != "200"'        "$RESPONSE_LOG" | wc -l)
    ERR_COUNT=$(awk '$2 == "ERR"'           "$RESPONSE_LOG" | wc -l)
    BROKEN_COUNT=$(awk '$2 == "ERR" && $1 < '"$SIGTERM_TS" "$RESPONSE_LOG" | wc -l)

    info "Total polls : $TOTAL  (2xx: $OK_COUNT, non-2xx: $NON_OK_COUNT, ERR: $ERR_COUNT)"

    LAST_OK_TS=$(awk '$2 == "200" {last=$1} END {print last+0}' "$RESPONSE_LOG")
    FIRST_FAIL_TS=$(awk -v ts="$SIGTERM_TS" '$1 >= ts && $2 != "200" {print $1; exit}' "$RESPONSE_LOG")

    if [[ "$LAST_OK_TS" -gt 0 ]]; then
        pass "Last 200 at ${LAST_OK_TS} ms"
    fi

    if [[ -n "$FIRST_FAIL_TS" ]]; then
        LATENCY_MS=$(( FIRST_FAIL_TS - SIGTERM_TS ))
        pass "First non-2xx appeared ${LATENCY_MS}ms after SIGTERM"
    else
        warn "No non-2xx captured after SIGTERM (polling window may have been too short)"
    fi

    # Stale 200s: a 200 AFTER the first non-2xx would mean traffic was accepted
    # by a "shutting down" server — would indicate graceful shutdown is broken.
    if [[ -n "$FIRST_FAIL_TS" ]]; then
        LATE_OK=$(awk -v ts="$FIRST_FAIL_TS" '$1 > ts && $2 == "200" {c++} END {print c+0}' "$RESPONSE_LOG")
        if [[ "$LATE_OK" -eq 0 ]]; then
            pass "No 200s after first shutdown signal — server correctly stopped accepting traffic"
        else
            fail "$LATE_OK request(s) returned 200 AFTER the first non-2xx — server may still be accepting traffic during shutdown"
        fi
    fi

    # Broken connections during the pre-SIGTERM window would indicate a bug
    # unrelated to shutdown.
    if [[ "$BROKEN_COUNT" -gt 0 ]]; then
        fail "$BROKEN_COUNT broken connection(s) BEFORE SIGTERM — unexpected instability"
    else
        pass "No broken connections before SIGTERM"
    fi

    echo ""
    info "Response sample (last 15 entries):"
    tail -15 "$RESPONSE_LOG" | while read -r ts code; do
        marker=""; [[ "$ts" -ge "$SIGTERM_TS" ]] && marker=" ← post-SIGTERM"
        if [[ "$code" == "200" ]]; then
            echo -e "    ${GREEN}${ts}  ${code}${NC}${marker}"
        else
            echo -e "    ${RED}${ts}  ${code}${NC}${marker}"
        fi
    done
fi

# ── Step 6: Restart api ───────────────────────────────────────────────────────
header "6. Restore api"

if $AUTO_RESTART; then
    info "Bringing api back up (--no-restart to skip)..."
    compose_run start api
    # Wait for readiness
    for _ in $(seq 1 30); do
        sleep 1
        if [[ "$(http_status)" == "200" ]]; then
            pass "API is healthy again"
            break
        fi
    done
    if [[ "$(http_status)" != "200" ]]; then
        warn "API did not become healthy within 30s after restart — check logs"
    fi
else
    warn "Auto-restart skipped. Bring it back with: docker compose start api"
fi

# ── Result ────────────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
if [[ "$FAILURES" -eq 0 ]]; then
    echo -e "  ${GREEN}${BOLD}ALL CHECKS PASSED${NC}"
else
    echo -e "  ${RED}${BOLD}$FAILURES CHECK(S) FAILED — review output above${NC}"
fi
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

exit "$FAILURES"
