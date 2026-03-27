package edu.mirea.qwerdsa53.taskTracker.security;

import java.time.Duration;

public interface RefreshTokenStore {

	void store(String refreshJti, long userId, Duration ttl);

	boolean isValid(String refreshJti, long userId);

	void remove(String refreshJti);

	void removeAllForUser(long userId);
}
