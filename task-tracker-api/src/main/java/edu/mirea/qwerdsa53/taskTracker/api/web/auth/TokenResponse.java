package edu.mirea.qwerdsa53.taskTracker.api.web.auth;

public record TokenResponse(
		String accessToken, String refreshToken, String tokenType, long expiresIn) {}
