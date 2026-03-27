package edu.mirea.qwerdsa53.taskTracker.security;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisTokenBlacklistStore implements TokenBlacklistStore {

	private static final String PREFIX = "blacklist:jti:";

	private final StringRedisTemplate redis;

	public RedisTokenBlacklistStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void blacklist(String jti, Duration ttl) {
		if (ttl.isNegative() || ttl.isZero()) {
			return;
		}
		redis.opsForValue().set(PREFIX + jti, "1", ttl);
	}

	@Override
	public boolean isBlacklisted(String jti) {
		return Boolean.TRUE.equals(redis.hasKey(PREFIX + jti));
	}
}
