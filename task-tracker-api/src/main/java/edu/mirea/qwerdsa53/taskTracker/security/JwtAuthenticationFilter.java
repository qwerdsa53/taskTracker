package edu.mirea.qwerdsa53.taskTracker.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final TokenBlacklistStore tokenBlacklistStore;
	private final PublicApiPathMatcher publicApiPathMatcher;

	public JwtAuthenticationFilter(
			JwtService jwtService,
			TokenBlacklistStore tokenBlacklistStore,
			PublicApiPathMatcher publicApiPathMatcher) {
		this.jwtService = jwtService;
		this.tokenBlacklistStore = tokenBlacklistStore;
		this.publicApiPathMatcher = publicApiPathMatcher;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			boolean isPublic = publicApiPathMatcher.isPublic(request);
			try {
				ParsedAccessToken parsed = jwtService.parseAccessToken(token);
				if (tokenBlacklistStore.isBlacklisted(parsed.jti())) {
					if (!isPublic) {
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
						return;
					}
				} else {
					UserPrincipal principal = new UserPrincipal(parsed.userId(), parsed.email());
					var auth =
							new UsernamePasswordAuthenticationToken(
									principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (JwtException | IllegalArgumentException ex) {
				if (!isPublic) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}
