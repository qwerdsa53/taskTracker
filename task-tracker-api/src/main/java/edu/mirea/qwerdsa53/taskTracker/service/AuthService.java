package edu.mirea.qwerdsa53.taskTracker.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.mirea.qwerdsa53.taskTracker.api.web.auth.LoginRequest;
import edu.mirea.qwerdsa53.taskTracker.api.web.auth.RefreshRequest;
import edu.mirea.qwerdsa53.taskTracker.api.web.auth.TokenResponse;
import edu.mirea.qwerdsa53.taskTracker.domain.user.User;
import edu.mirea.qwerdsa53.taskTracker.repository.UserRepository;
import edu.mirea.qwerdsa53.taskTracker.security.JwtService;
import edu.mirea.qwerdsa53.taskTracker.security.ParsedAccessToken;
import edu.mirea.qwerdsa53.taskTracker.security.RefreshTokenStore;
import edu.mirea.qwerdsa53.taskTracker.security.TokenBlacklistStore;
import edu.mirea.qwerdsa53.taskTracker.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
@Transactional
public class AuthService {

	private static final String CLAIM_TYP = "typ";
	private static final String TYP_REFRESH = "refresh";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final RefreshTokenStore refreshTokenStore;
	private final TokenBlacklistStore tokenBlacklistStore;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			JwtProperties jwtProperties,
			RefreshTokenStore refreshTokenStore,
			TokenBlacklistStore tokenBlacklistStore) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
		this.refreshTokenStore = refreshTokenStore;
		this.tokenBlacklistStore = tokenBlacklistStore;
	}

	public TokenResponse login(LoginRequest request) {
		User user =
				userRepository
						.findByEmail(request.email().trim())
						.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid credentials");
		}
		return issueTokens(user);
	}

	public TokenResponse refresh(RefreshRequest request) {
		Claims claims;
		try {
			claims = jwtService.parseSignedClaims(request.refreshToken());
		} catch (JwtException ex) {
			throw new BadCredentialsException("Invalid refresh token");
		}
		if (!TYP_REFRESH.equals(claims.get(CLAIM_TYP))) {
			throw new BadCredentialsException("Invalid refresh token");
		}
		long userId = Long.parseLong(claims.getSubject());
		String jti = claims.getId();
		if (!refreshTokenStore.isValid(jti, userId)) {
			throw new BadCredentialsException("Invalid refresh token");
		}
		User user =
				userRepository
						.findById(userId)
						.orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
		refreshTokenStore.remove(jti);
		return issueTokens(user);
	}

	private TokenResponse issueTokens(User user) {
		String accessJti = UUID.randomUUID().toString();
		String refreshJti = UUID.randomUUID().toString();
		String access = jwtService.createAccessToken(user.getId(), user.getEmail(), accessJti);
		String refresh = jwtService.createRefreshToken(user.getId(), refreshJti);
		refreshTokenStore.store(refreshJti, user.getId(), jwtProperties.getRefreshTtl());
		return new TokenResponse(
				access,
				refresh,
				"Bearer",
				jwtProperties.getAccessTtl().toSeconds());
	}

	public void logout(String authorizationHeader, String refreshTokenOptional) {
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			String access = authorizationHeader.substring(7);
			try {
				ParsedAccessToken parsed = jwtService.parseAccessToken(access);
				Duration ttl = jwtService.remainingTtl(parsed);
				if (!ttl.isZero()) {
					tokenBlacklistStore.blacklist(parsed.jti(), ttl);
				}
			} catch (JwtException ignored) {
				// ignore invalid access token on logout
			}
		}
		if (refreshTokenOptional != null && !refreshTokenOptional.isBlank()) {
			try {
				Claims c = jwtService.parseSignedClaims(refreshTokenOptional);
				if (TYP_REFRESH.equals(c.get(CLAIM_TYP))) {
					refreshTokenStore.remove(c.getId());
				}
			} catch (JwtException ignored) {
				// ignore
			}
		}
	}
}
