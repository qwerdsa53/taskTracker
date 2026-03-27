package edu.mirea.qwerdsa53.taskTracker.security;

import java.time.Instant;

public record ParsedAccessToken(long userId, String email, String jti, Instant expiresAt) {}
