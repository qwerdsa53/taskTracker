package edu.mirea.qwerdsa53.taskTracker.api.error;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
		String msg =
				ex.getBindingResult().getFieldErrors().stream()
						.map(e -> e.getField() + ": " + e.getDefaultMessage())
						.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(Map.of("message", msg));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("message", "Database constraint violation (possible duplicate)"));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
		String msg = ex.getMessage() != null ? ex.getMessage() : "Unauthorized";
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", msg));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Forbidden"));
	}
}
