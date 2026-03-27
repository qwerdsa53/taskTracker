package edu.mirea.qwerdsa53.taskTracker.verification;

import java.time.Duration;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisEmailVerificationTokenStore implements EmailVerificationTokenStore {

	private static final String TOK_PREFIX = "email-verify:tok:";
	private static final String USER_PREFIX = "email-verify:user:";

	private final StringRedisTemplate redis;

	public RedisEmailVerificationTokenStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	@Override
	public void store(String token, long userId, Duration ttl) {
		String userKey = USER_PREFIX + userId;
		String oldTok = redis.opsForValue().get(userKey);
		if (oldTok != null && !oldTok.equals(token)) {
			redis.delete(TOK_PREFIX + oldTok);
		}
		redis.opsForValue().set(TOK_PREFIX + token, String.valueOf(userId), ttl);
		redis.opsForValue().set(userKey, token, ttl);
	}

	@Override
	public Optional<Long> consume(String token) {
		String uid = redis.opsForValue().get(TOK_PREFIX + token);
		if (uid == null) {
			return Optional.empty();
		}
		long userId = Long.parseLong(uid);
		redis.delete(TOK_PREFIX + token);
		String userKey = USER_PREFIX + userId;
		String current = redis.opsForValue().get(userKey);
		if (token.equals(current)) {
			redis.delete(userKey);
		}
		return Optional.of(userId);
	}
}
