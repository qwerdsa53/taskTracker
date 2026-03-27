package edu.mirea.qwerdsa53.taskTracker.security;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private static final String JTI_PREFIX = "refresh:jti:";
	private static final String USER_PREFIX = "refresh:user:";

	private final StringRedisTemplate redis;

	public RedisRefreshTokenStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void store(String refreshJti, long userId, Duration ttl) {
		String userKey = USER_PREFIX + userId;
		String oldJti = redis.opsForValue().get(userKey);
		if (oldJti != null && !oldJti.equals(refreshJti)) {
			redis.delete(JTI_PREFIX + oldJti);
		}
		redis.opsForValue().set(JTI_PREFIX + refreshJti, String.valueOf(userId), ttl);
		redis.opsForValue().set(userKey, refreshJti, ttl);
	}

	@Override
	public boolean isValid(String refreshJti, long userId) {
		String uid = redis.opsForValue().get(JTI_PREFIX + refreshJti);
		if (uid == null || !uid.equals(String.valueOf(userId))) {
			return false;
		}
		String current = redis.opsForValue().get(USER_PREFIX + userId);
		return refreshJti.equals(current);
	}

	@Override
	public void remove(String refreshJti) {
		String uid = redis.opsForValue().get(JTI_PREFIX + refreshJti);
		if (uid == null) {
			return;
		}
		long userId = Long.parseLong(uid);
		redis.delete(JTI_PREFIX + refreshJti);
		String userKey = USER_PREFIX + userId;
		String current = redis.opsForValue().get(userKey);
		if (refreshJti.equals(current)) {
			redis.delete(userKey);
		}
	}

	@Override
	public void removeAllForUser(long userId) {
		String userKey = USER_PREFIX + userId;
		String jti = redis.opsForValue().get(userKey);
		if (jti != null) {
			redis.delete(JTI_PREFIX + jti);
		}
		redis.delete(userKey);
	}
}
