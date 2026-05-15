package edu.mirea.qwerdsa53.taskTracker.verification;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationTokenStore {

	void store(String token, long userId, Duration ttl);

	// Validates and removes the token. Returns the user id if the token was valid.
	Optional<Long> consume(String token);
}
