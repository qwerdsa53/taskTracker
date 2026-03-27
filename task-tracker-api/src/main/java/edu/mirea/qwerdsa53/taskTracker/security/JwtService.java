package edu.mirea.qwerdsa53.taskTracker.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import edu.mirea.qwerdsa53.taskTracker.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final String CLAIM_TYP = "typ";
	private static final String CLAIM_EMAIL = "email";
	private static final String TYP_ACCESS = "access";
	private static final String TYP_REFRESH = "refresh";

	private final JwtProperties props;

	public JwtService(JwtProperties props) {
		this.props = props;
	}

	private SecretKey key() {
		byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("app.jwt.secret must be at least 32 bytes (256 bits) for HS256");
		}
		return Keys.hmacShaKeyFor(bytes);
	}

	public String createAccessToken(long userId, String email, String jti) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.id(jti)
				.claim(CLAIM_TYP, TYP_ACCESS)
				.claim(CLAIM_EMAIL, email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(props.getAccessTtl())))
				.signWith(key())
				.compact();
	}

	public String createRefreshToken(long userId, String jti) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.id(jti)
				.claim(CLAIM_TYP, TYP_REFRESH)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(props.getRefreshTtl())))
				.signWith(key())
				.compact();
	}

	public ParsedAccessToken parseAccessToken(String token) {
		Claims claims = parseSignedClaims(token);
		if (!TYP_ACCESS.equals(claims.get(CLAIM_TYP))) {
			throw new JwtException("Not an access token");
		}
		long userId = Long.parseLong(claims.getSubject());
		String email = claims.get(CLAIM_EMAIL, String.class);
		String jti = claims.getId();
		Instant exp = claims.getExpiration().toInstant();
		return new ParsedAccessToken(userId, email, jti, exp);
	}

	public Claims parseSignedClaims(String token) {
		return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
	}

	public Duration remainingTtl(ParsedAccessToken access) {
		Duration d = Duration.between(Instant.now(), access.expiresAt());
		if (d.isNegative() || d.isZero()) {
			return Duration.ZERO;
		}
		return d;
	}
}
