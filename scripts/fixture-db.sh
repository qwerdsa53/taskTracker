#!/usr/bin/env bash
# Main/dev Postgres (service postgres, host port DB_PORT default 5432). For integration DB see fixture-db-integration.sh.
# 1) Postgres via Docker  2) Flyway DDL using flyway/flyway image (no nested ./gradlew)
# 3) COPY from fixtures/*.data via fixtures/load.sql
#
# Postgres data is persisted in Docker volume tasktracker_pgdata — restarts keep old data.
# This script resets DB content to the checked-in fixtures (TRUNCATE … CASCADE + COPY).
# To start from scratch: docker compose down -v, then up, then run this script again.
set -euo pipefail

# Resolve script path: dirname "fixture-db.sh" is "." — source ./compose.sh would use cwd and break if cwd != scripts/
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

if [ ! -f "$SCRIPT_DIR/compose.sh" ]; then
	echo "compose.sh not found (expected next to this script): $SCRIPT_DIR/compose.sh" >&2
	exit 1
fi
# shellcheck source=compose.sh
source "$SCRIPT_DIR/compose.sh"

echo "==> docker compose up"
compose_run up -d

echo "==> waiting for Postgres"
ready=0
for _ in $(seq 1 40); do
	if compose_run exec -T postgres pg_isready -U postgres -d tasktracker >/dev/null 2>&1; then
		ready=1
		break
	fi
	sleep 1
done
if [ "$ready" != 1 ]; then
	echo "Error: Postgres did not become ready within 40s (pg_isready)." >&2
	compose_run ps
	exit 1
fi

echo "==> Flyway migrate (DDL from task-tracker-api/src/main/resources/db/migration)"
# Same network namespace as tasktracker-postgres → JDBC to 127.0.0.1:5432
docker run --rm \
  --network "container:tasktracker-postgres" \
  -v "$ROOT/task-tracker-api/src/main/resources/db/migration:/flyway/sql" \
  "$FLYWAY_IMAGE" \
  -url=jdbc:postgresql://127.0.0.1:5432/tasktracker \
  -user=postgres \
  -password=postgres \
  -locations=filesystem:/flyway/sql \
  migrate

echo "==> loading fixtures/load.sql (data from *.data)"
compose_run exec -T postgres psql -U postgres -d tasktracker -v ON_ERROR_STOP=1 -f /fixtures/load.sql

echo "==> done (main DB). JDBC: jdbc:postgresql://localhost:${DB_PORT:-5432}/tasktracker user=postgres"
