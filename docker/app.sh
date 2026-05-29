#!/bin/sh
# Entrypoint for the task-tracker-api image.
#
# Translates a "command verb" into the correct Spring Boot launch:
#   server         — long-running HTTP service (default)
#   migrate        — Flyway one-off, exits with 0/1
#   create-admin   — create admin user, exits with 0/1
#   cache-clear    — flush Redis, exits with 0/1
#
# Same image, same classpath, same config — only the entrypoint flags differ.
# This is the 12-factor "admin processes" pattern: one-off processes run on
# identical code+config to the long-running app.

set -e

cmd="${1:-server}"
if [ $# -gt 0 ]; then shift; fi

LAUNCHER="org.springframework.boot.loader.launch.JarLauncher"

# Common JVM flags for short-lived admin tasks: skip web stack init, no banner.
ADMIN_FLAGS="\
-Dspring.main.web-application-type=none \
-Dspring.main.banner-mode=off"

case "$cmd" in
  server|"")
    exec java "$LAUNCHER" "$@"
    ;;

  migrate)
    # Force Flyway on, regardless of profile (e.g. development sets it off).
    # Disable JPA schema management — Flyway is the source of truth.
    exec java \
      $ADMIN_FLAGS \
      -Dspring.flyway.enabled=true \
      -Dspring.jpa.hibernate.ddl-auto=none \
      "$LAUNCHER" --task=migrate "$@"
    ;;

  create-admin)
    # Don't re-run migrations from this one-off; expect them already applied.
    # Validate schema so the task fails fast on drift.
    exec java \
      $ADMIN_FLAGS \
      -Dspring.flyway.enabled=false \
      -Dspring.jpa.hibernate.ddl-auto=validate \
      "$LAUNCHER" --task=create-admin "$@"
    ;;

  cache-clear)
    exec java \
      $ADMIN_FLAGS \
      -Dspring.flyway.enabled=false \
      -Dspring.jpa.hibernate.ddl-auto=none \
      "$LAUNCHER" --task=cache-clear "$@"
    ;;

  shell|sh)
    # Debug only — break-glass.
    exec /bin/sh "$@"
    ;;

  help|-h|--help)
    cat <<'EOF'
Usage: app <command> [args...]

Commands:
  server                       start HTTP server (default; PID 1 long-running)
  migrate                      apply DB migrations (Flyway), exit 0 on success
  create-admin                 create a user with email_verified=true, exit
                                 required: --email=... --password=...
                                 optional: --username=... --timezone=...
  cache-clear                  FLUSHDB on Redis, exit
  shell                        drop into /bin/sh (debug only)

Examples:
  docker run --rm \
    -e DB_HOST=postgres -e DB_PORT=5432 \
    -e DB_USER=postgres -e DB_PASSWORD=*** \
    -e DB_NAME=tasktracker -e SPRING_PROFILES_ACTIVE=production \
    --network tasktracker_backend \
    ghcr.io/qwerdsa53/task-tracker-api:latest migrate

  docker run --rm \
    -e DB_HOST=postgres -e DB_USER=postgres -e DB_PASSWORD=*** \
    -e DB_NAME=tasktracker -e SPRING_PROFILES_ACTIVE=production \
    --network tasktracker_backend \
    ghcr.io/qwerdsa53/task-tracker-api:latest create-admin \
      --email=admin@example.com --password='S3cr3t!' --username=admin
EOF
    exit 0
    ;;

  *)
    echo "app: unknown command '$cmd' (try: app help)" >&2
    exit 2
    ;;
esac
