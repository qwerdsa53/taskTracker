*.data files are comma-separated CSV with a header row.

Load path: fixtures/load.sql runs COPY from /fixtures/*.data inside the Postgres container.

One-shot: bash scripts/fixture-db.sh
