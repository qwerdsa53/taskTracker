package edu.mirea.qwerdsa53.taskTracker.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(RedisConnectionFactory.class)
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

	private final ConcurrentHashMap<String, Entry> byJti = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, String> userToJti = new ConcurrentHashMap<>();

	private record Entry(long userId, Instant expiresAt) {}

	@Override
	public void store(String refreshJti, long userId, Duration ttl) {
		Instant until = Instant.now().plus(ttl);
		String oldJti = userToJti.put(userId, refreshJti);
		if (oldJti != null && !oldJti.equals(refreshJti)) {
			byJti.remove(oldJti);
		}
		byJti.put(refreshJti, new Entry(userId, until));
	}

	@Override
	public boolean isValid(String refreshJti, long userId) {
		Entry e = byJti.get(refreshJti);
		if (e == null || e.userId != userId) {
			return false;
		}
		if (Instant.now().isAfter(e.expiresAt)) {
			byJti.remove(refreshJti);
			userToJti.remove(userId, refreshJti);
			return false;
		}
		String current = userToJti.get(userId);
		return refreshJti.equals(current);
	}

	@Override
	public void remove(String refreshJti) {
		Entry e = byJti.remove(refreshJti);
		if (e != null) {
			userToJti.remove(e.userId, refreshJti);
		}
	}

	@Override
	public void removeAllForUser(long userId) {
		String jti = userToJti.remove(userId);
		if (jti != null) {
			byJti.remove(jti);
		}
	}
}
