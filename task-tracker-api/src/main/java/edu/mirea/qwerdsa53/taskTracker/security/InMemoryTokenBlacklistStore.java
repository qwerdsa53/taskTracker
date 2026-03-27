package edu.mirea.qwerdsa53.taskTracker.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(RedisConnectionFactory.class)
public class InMemoryTokenBlacklistStore implements TokenBlacklistStore {

	private final ConcurrentHashMap<String, Instant> entries = new ConcurrentHashMap<>();

	@Override
	public void blacklist(String jti, Duration ttl) {
		Instant until = Instant.now().plus(ttl);
		entries.put(jti, until);
	}

	@Override
	public boolean isBlacklisted(String jti) {
		Instant until = entries.get(jti);
		if (until == null) {
			return false;
		}
		if (Instant.now().isAfter(until)) {
			entries.remove(jti);
			return false;
		}
		return true;
	}
}
