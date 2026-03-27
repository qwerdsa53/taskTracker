package edu.mirea.qwerdsa53.taskTracker.security;

import java.time.Duration;

public interface TokenBlacklistStore {

	void blacklist(String jti, Duration ttl);

	boolean isBlacklisted(String jti);
}
