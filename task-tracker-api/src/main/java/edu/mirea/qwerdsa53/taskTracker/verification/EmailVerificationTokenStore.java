package edu.mirea.qwerdsa53.taskTracker.verification;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationTokenStore {

	void store(String token, long userId, Duration ttl);

	/** Validates token, removes it (and user mapping), returns user id if found. */
	Optional<Long> consume(String token);
}
