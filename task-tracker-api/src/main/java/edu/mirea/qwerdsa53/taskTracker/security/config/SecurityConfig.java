package edu.mirea.qwerdsa53.taskTracker.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import edu.mirea.qwerdsa53.taskTracker.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
			throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(
						auth ->
								auth.requestMatchers("/api/v1/auth/**")
										.permitAll()
										.requestMatchers(HttpMethod.POST, "/api/v1/users")
										.permitAll()
										.requestMatchers("/api/v1/verification/**")
										.permitAll()
										.requestMatchers(
												"/swagger-ui.html",
												"/swagger-ui/**",
												"/v3/api-docs",
												"/v3/api-docs/**")
										.permitAll()
										.requestMatchers("/graphql", "/graphql/**")
										.permitAll()
										.requestMatchers("/ws", "/ws/**")
										.permitAll()
										.requestMatchers("/error")
										.permitAll()
										.requestMatchers("/actuator/health", "/actuator/health/**")
										.permitAll()
										.requestMatchers(HttpMethod.GET, "/")
										.permitAll()
										.anyRequest()
										.authenticated())
				.exceptionHandling(
						ex ->
								ex.authenticationEntryPoint(
												(request, response, authException) -> {
													response.setStatus(401);
													response.setContentType(MediaType.APPLICATION_JSON_VALUE);
													response.getWriter().write("{\"message\":\"Unauthorized\"}");
												})
										.accessDeniedHandler(
												(request, response, accessDeniedException) -> {
													response.setStatus(403);
													response.setContentType(MediaType.APPLICATION_JSON_VALUE);
													response.getWriter().write("{\"message\":\"Forbidden\"}");
												}));
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
