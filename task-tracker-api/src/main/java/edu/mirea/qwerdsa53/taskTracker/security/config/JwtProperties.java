package edu.mirea.qwerdsa53.taskTracker.security.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	/** HMAC secret (min 32 bytes for HS256). Override via JWT_SECRET in production. */
	private String secret =
			"dev-only-change-me-use-at-least-32-chars-secret-key-for-hs256";

	private Duration accessTtl = Duration.ofMinutes(15);

	private Duration refreshTtl = Duration.ofDays(7);

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Duration getAccessTtl() {
		return accessTtl;
	}

	public void setAccessTtl(Duration accessTtl) {
		this.accessTtl = accessTtl;
	}

	public Duration getRefreshTtl() {
		return refreshTtl;
	}

	public void setRefreshTtl(Duration refreshTtl) {
		this.refreshTtl = refreshTtl;
	}
}
