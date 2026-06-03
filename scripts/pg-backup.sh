#!/usr/bin/env bash
# Snapshot Postgres before a production deploy.
# Идемпотентен: создаёт файл с timestamp в имени, хранит N последних.
set -euo pipefail

cd "$(dirname "$0")/.."

BACKUP_DIR="${BACKUP_DIR:-/var/backups/tasktracker}"
RETAIN="${BACKUP_RETAIN:-7}"
TS=$(date -u +%Y%m%dT%H%M%SZ)
OUT="$BACKUP_DIR/pg-$TS.sql.gz"

mkdir -p "$BACKUP_DIR"

# Find the running postgres task (swarm container).
PG_CID=$(docker ps --filter "name=tasktracker_postgres" --format '{{.ID}}' | head -1)
if [ -z "$PG_CID" ]; then
  echo "no running postgres container found — skipping backup (first deploy?)" >&2
  exit 0
fi

DB_USER=$(cat .secrets/db_user.txt)
DB_NAME="${DB_NAME:-tasktracker}"

docker exec -e PGPASSWORD="$(cat .secrets/db_password.txt)" "$PG_CID" \
  pg_dump -U "$DB_USER" -d "$DB_NAME" --no-owner --clean --if-exists \
  | gzip -c > "$OUT"

echo "wrote $OUT ($(du -h "$OUT" | cut -f1))"

# Rotate: keep newest $RETAIN
ls -1t "$BACKUP_DIR"/pg-*.sql.gz 2>/dev/null | tail -n +"$((RETAIN+1))" | xargs -r rm -v
