#!/usr/bin/env bash
# Bootstrap secrets for `docker stack deploy -c docker-stack.yml tasktracker`.
# Idempotent: only writes files that don't already exist.
set -euo pipefail

cd "$(dirname "$0")/.."
mkdir -p .secrets
chmod 700 .secrets

write_if_missing() {
  local path=$1
  local value=$2
  if [[ ! -s "$path" ]]; then
    printf '%s' "$value" > "$path"
    chmod 600 "$path"
    echo "  wrote $path"
  else
    echo "  $path exists, skipping"
  fi
}

echo "Seeding swarm secrets in .secrets/ ..."
write_if_missing .secrets/db_user.txt      "${DB_USER:-postgres}"
write_if_missing .secrets/db_password.txt  "${DB_PASSWORD:-$(openssl rand -hex 16)}"
write_if_missing .secrets/jwt_secret.txt   "${JWT_SECRET:-$(openssl rand -hex 32)}"

echo
echo "Init swarm (idempotent):"
docker info --format '{{.Swarm.LocalNodeState}}' | grep -q active \
  && echo "  swarm already active" \
  || docker swarm init

echo
echo "Deploy:"
echo "  docker stack deploy -c docker-stack.yml tasktracker"
