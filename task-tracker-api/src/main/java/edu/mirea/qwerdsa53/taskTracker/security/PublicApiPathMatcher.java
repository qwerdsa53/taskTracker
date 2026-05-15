package edu.mirea.qwerdsa53.taskTracker.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

// Defines which paths are public. Must stay in sync with SecurityConfig.
@Component
public class PublicApiPathMatcher {

	public boolean isPublic(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String method = request.getMethod();
		if (uri.startsWith("/api/v1/auth/")) {
			return true;
		}
		if (uri.startsWith("/api/v1/verification/")) {
			return true;
		}
		if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
			return true;
		}
		if (uri.startsWith("/graphql")) {
			return true;
		}
		if (uri.startsWith("/ws")) {
			return true;
		}
		if ("/error".equals(uri)) {
			return true;
		}
		if (uri.startsWith("/actuator/")) {
			return true;
		}
		if ("POST".equalsIgnoreCase(method) && "/api/v1/users".equals(uri)) {
			return true;
		}
		return "/".equals(uri) || "/swagger-ui.html".equals(uri);
	}
}
