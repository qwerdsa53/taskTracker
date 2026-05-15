#!/usr/bin/env bash
# Prefer Docker Compose V2 (`docker compose`); fall back to legacy `docker-compose`.
# Usage: source "$(dirname "${BASH_SOURCE[0]}")/compose.sh"

compose_run() {
	if ! docker buildx version >/dev/null 2>&1; then
		echo "Install docker buildx: https://docs.docker.com/build/buildx/install/" >&2
		return 1
	fi
	export DOCKER_BUILDKIT=1
	export COMPOSE_DOCKER_CLI_BUILD=1
	if docker compose version >/dev/null 2>&1; then
		docker compose "$@"
	elif command -v docker-compose >/dev/null 2>&1; then
		docker-compose "$@"
	else
		echo "Docker Compose is required: install the V2 plugin (docker compose) or docker-compose." >&2
		echo "See https://docs.docker.com/compose/install/" >&2
		exit 1
	fi
}
