#!/usr/bin/env bash
# Local Temporal. Uses Compose V2 via scripts/compose.sh.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# shellcheck source=compose.sh
source "$(dirname "${BASH_SOURCE[0]}")/compose.sh"

echo "==> Temporal (UI http://localhost:\${TEMPORAL_UI_PORT:-8233}, gRPC :7233)"
compose_run -f docker-compose.temporal.yml up -d
