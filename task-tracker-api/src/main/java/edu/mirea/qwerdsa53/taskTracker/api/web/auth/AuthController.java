package edu.mirea.qwerdsa53.taskTracker.api.web.auth;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.mirea.qwerdsa53.taskTracker.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestBody(required = false) LogoutRequest body) {
		String refresh = body != null ? body.refreshToken() : null;
		authService.logout(authorization, refresh);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
