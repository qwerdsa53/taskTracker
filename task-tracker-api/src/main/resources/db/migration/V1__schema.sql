CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    timezone VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE habits (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    color VARCHAR(32),
    icon_key VARCHAR(64),
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    schedule_type VARCHAR(255) NOT NULL,
    target_per_week INTEGER
);

CREATE INDEX idx_habits_owner_id ON habits (owner_id);

CREATE TABLE habit_schedule_weekdays (
    habit_id BIGINT NOT NULL REFERENCES habits (id) ON DELETE CASCADE,
    day_of_week VARCHAR(255) NOT NULL,
    PRIMARY KEY (habit_id, day_of_week)
);

CREATE TABLE habit_completions (
    id BIGSERIAL PRIMARY KEY,
    habit_id BIGINT NOT NULL REFERENCES habits (id) ON DELETE CASCADE,
    completed_on DATE NOT NULL,
    note VARCHAR(2000),
    quantity INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_habit_completion_day UNIQUE (habit_id, completed_on)
);

CREATE INDEX idx_habit_completions_habit_id ON habit_completions (habit_id);

CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    habit_id BIGINT NOT NULL REFERENCES habits (id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    local_time TIME NOT NULL
);

CREATE INDEX idx_reminders_habit_id ON reminders (habit_id);

CREATE TABLE reminder_weekdays (
    reminder_id BIGINT NOT NULL REFERENCES reminders (id) ON DELETE CASCADE,
    day_of_week VARCHAR(255) NOT NULL,
    PRIMARY KEY (reminder_id, day_of_week)
);
