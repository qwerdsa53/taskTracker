package edu.mirea.qwerdsa53.taskTracker.verification;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(RedisConnectionFactory.class)
public class InMemoryEmailVerificationTokenStore implements EmailVerificationTokenStore {

	private final ConcurrentHashMap<String, Entry> byToken = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, String> userToToken = new ConcurrentHashMap<>();

	private record Entry(long userId, Instant expiresAt) {}

	@Override
	public void store(String token, long userId, Duration ttl) {
		Instant until = Instant.now().plus(ttl);
		String old = userToToken.put(userId, token);
		if (old != null && !old.equals(token)) {
			byToken.remove(old);
		}
		byToken.put(token, new Entry(userId, until));
	}

	@Override
	public Optional<Long> consume(String token) {
		Entry e = byToken.remove(token);
		if (e == null) {
			return Optional.empty();
		}
		if (Instant.now().isAfter(e.expiresAt)) {
			userToToken.remove(e.userId, token);
			return Optional.empty();
		}
		userToToken.remove(e.userId, token);
		return Optional.of(e.userId);
	}
}
