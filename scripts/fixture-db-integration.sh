#!/usr/bin/env bash
# Flyway + fixtures/load.sql against the integration Postgres (docker service postgres-integration, host port DB_INTEGRATION_PORT default 5433).
# Main/dev DB uses scripts/fixture-db.sh (port DB_PORT default 5432).
#
# Data: volume tasktracker_pgdata_integration — survives restarts; `docker compose down -v` wipes.
set -euo pipefail

_this_script="${BASH_SOURCE[0]}"
case "$_this_script" in
	/*) ;;
	*) _this_script="$(pwd)/$_this_script" ;;
esac
SCRIPT_DIR="$(cd "$(dirname "$_this_script")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"
unset _this_script

FLYWAY_IMAGE="${FLYWAY_IMAGE:-flyway/flyway:10.22-alpine}"
DB_INTEGRATION_PORT="${DB_INTEGRATION_PORT:-5433}"

if [ ! -f "$SCRIPT_DIR/compose.sh" ]; then
	echo "compose.sh not found (expected next to this script): $SCRIPT_DIR/compose.sh" >&2
	exit 1
fi
# shellcheck source=compose.sh
source "$SCRIPT_DIR/compose.sh"

echo "==> docker compose up"
compose_run up -d

echo "==> waiting for Postgres (integration)"
ready=0
for _ in $(seq 1 40); do
	if compose_run exec -T postgres-integration pg_isready -U postgres -d tasktracker >/dev/null 2>&1; then
		ready=1
		break
	fi
	sleep 1
done
if [ "$ready" != 1 ]; then
	echo "Error: postgres-integration did not become ready within 40s (pg_isready)." >&2
	compose_run ps
	exit 1
fi

echo "==> Flyway migrate (DDL from task-tracker-api/src/main/resources/db/migration)"
docker run --rm \
	--network "container:tasktracker-postgres-integration" \
	-v "$ROOT/task-tracker-api/src/main/resources/db/migration:/flyway/sql" \
	"$FLYWAY_IMAGE" \
	-url=jdbc:postgresql://127.0.0.1:5432/tasktracker \
	-user=postgres \
	-password=postgres \
	-locations=filesystem:/flyway/sql \
	migrate

echo "==> loading fixtures/load.sql (data from *.data)"
compose_run exec -T postgres-integration psql -U postgres -d tasktracker -v ON_ERROR_STOP=1 -f /fixtures/load.sql

echo "==> done (integration). JDBC: jdbc:postgresql://localhost:${DB_INTEGRATION_PORT}/tasktracker user=postgres"
echo "    Run integration tests: ./gradlew :task-tracker-api:integrationTest"
