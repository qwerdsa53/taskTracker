-- Tokens moved to Redis (TTL); columns no longer used.
DROP INDEX IF EXISTS uk_users_email_verification_token;
ALTER TABLE users DROP COLUMN IF EXISTS email_verification_token;
ALTER TABLE users DROP COLUMN IF EXISTS email_verification_expires_at;
