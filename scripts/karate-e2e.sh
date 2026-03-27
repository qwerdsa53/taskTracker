#!/usr/bin/env bash
# Запуск без родительского Gradle (только bash): инфра + Karate одной командой.
# Из Gradle используйте: ./gradlew :task-tracker-api:karateE2e (без вложенного gradlew внутри задач).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
export KARATE_APP_PORT="${KARATE_APP_PORT:-8080}"
bash "$ROOT/scripts/karate-e2e-infra.sh"
exec "$ROOT/gradlew" :task-tracker-api:karateTest \
	-Dkarate.baseUrl="http://127.0.0.1:${KARATE_APP_PORT}" \
	--no-daemon
