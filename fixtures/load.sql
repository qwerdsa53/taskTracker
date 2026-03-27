-- Run inside the Postgres container; CSV paths are under /fixtures
-- Prerequisites: docker compose up -d, ./gradlew :task-tracker-api:flywayMigrate

BEGIN;

TRUNCATE TABLE reminder_weekdays, reminders, habit_schedule_weekdays, habit_completions, habits, users RESTART IDENTITY CASCADE;

COPY users (id, email, password_hash, username, timezone, created_at, updated_at, email_verified)
FROM '/fixtures/users.data' CSV HEADER;

COPY habits (id, owner_id, title, description, color, icon_key, archived, created_at, updated_at, schedule_type, target_per_week)
FROM '/fixtures/habits.data' CSV HEADER;

COPY habit_schedule_weekdays (habit_id, day_of_week)
FROM '/fixtures/habit_schedule_weekdays.data' CSV HEADER;

COPY habit_completions (id, habit_id, completed_on, note, quantity, created_at)
FROM '/fixtures/habit_completions.data' CSV HEADER;

COPY reminders (id, habit_id, enabled, local_time)
FROM '/fixtures/reminders.data' CSV HEADER;

COPY reminder_weekdays (reminder_id, day_of_week)
FROM '/fixtures/reminder_weekdays.data' CSV HEADER;

SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('habits', 'id'), COALESCE((SELECT MAX(id) FROM habits), 1));
SELECT setval(pg_get_serial_sequence('habit_completions', 'id'), COALESCE((SELECT MAX(id) FROM habit_completions), 1));
SELECT setval(pg_get_serial_sequence('reminders', 'id'), COALESCE((SELECT MAX(id) FROM reminders), 1));

COMMIT;
