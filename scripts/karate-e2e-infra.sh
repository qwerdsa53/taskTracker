#!/usr/bin/env bash
# Postgres + Redis + Flyway + fixtures + API (docker-compose.karate.yml). Без Karate / без вложенного Gradle.
# Вызывается из :task-tracker-api:karateE2eInfra; затем Gradle запускает :task-tracker-api:karateTest.
# Вручную: bash scripts/karate-e2e-infra.sh && ./gradlew :task-tracker-api:karateTest
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

export KARATE_DB_PORT="${KARATE_DB_PORT:-5435}"
export KARATE_APP_PORT="${KARATE_APP_PORT:-8080}"
export KARATE_REDIS_PORT="${KARATE_REDIS_PORT:-6379}"

FLYWAY_IMAGE="${FLYWAY_IMAGE:-flyway/flyway:10.22-alpine}"
COMPOSE_FILE="$ROOT/docker-compose.karate.yml"
JAR="$ROOT/task-tracker-api/build/libs/task-tracker-api.jar"

compose_karate() {
	if docker compose version >/dev/null 2>&1; then
		docker compose -f "$COMPOSE_FILE" "$@"
	elif command -v docker-compose >/dev/null 2>&1; then
		docker-compose -f "$COMPOSE_FILE" "$@"
	else
		echo "Docker Compose required (docker compose or docker-compose)." >&2
		exit 1
	fi
}

if [ ! -f "$JAR" ]; then
	echo "Missing $JAR — run: ./gradlew :task-tracker-api:bootJar" >&2
	exit 1
fi

echo "==> docker compose (Postgres + Redis)"
compose_karate up -d postgres redis

echo "==> waiting for Postgres"
ready=0
for _ in $(seq 1 40); do
	if compose_karate exec -T postgres pg_isready -U postgres -d tasktracker >/dev/null 2>&1; then
		ready=1
		break
	fi
	sleep 1
done
if [ "$ready" != 1 ]; then
	echo "Error: Postgres (karate) not ready." >&2
	compose_karate ps
	exit 1
fi

echo "==> Flyway migrate"
docker run --rm \
	--network "container:tasktracker-karate-postgres" \
	-v "$ROOT/task-tracker-api/src/main/resources/db/migration:/flyway/sql" \
	"$FLYWAY_IMAGE" \
	-url=jdbc:postgresql://127.0.0.1:5432/tasktracker \
	-user=postgres \
	-password=postgres \
	-locations=filesystem:/flyway/sql \
	migrate

echo "==> fixtures/load.sql"
compose_karate exec -T postgres psql -U postgres -d tasktracker -v ON_ERROR_STOP=1 -f /fixtures/load.sql

echo "==> build + start API (profile karate)"
compose_karate up -d --build api

echo "==> waiting for /actuator/health on http://127.0.0.1:${KARATE_APP_PORT}"
ready=0
for _ in $(seq 1 90); do
	if curl -sf "http://127.0.0.1:${KARATE_APP_PORT}/actuator/health" >/dev/null 2>&1; then
		ready=1
		break
	fi
	sleep 1
done
if [ "$ready" != 1 ]; then
	echo "Error: API did not become healthy in time. Logs:" >&2
	compose_karate logs api --tail 80
	exit 1
fi

echo "==> Infra ready. Karate: ./gradlew :task-tracker-api:karateTest -Dkarate.baseUrl=http://127.0.0.1:${KARATE_APP_PORT}"
