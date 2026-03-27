ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verification_token VARCHAR(64),
    ADD COLUMN email_verification_expires_at TIMESTAMPTZ;

CREATE UNIQUE INDEX uk_users_email_verification_token ON users (email_verification_token)
    WHERE email_verification_token IS NOT NULL;

-- Fixture users are treated as already verified
UPDATE users SET email_verified = TRUE;
