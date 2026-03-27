package edu.mirea.qwerdsa53.taskTracker.api.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import edu.mirea.qwerdsa53.taskTracker.api.dto.UserRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.UserResponse;
import edu.mirea.qwerdsa53.taskTracker.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserResponse> list() {
		return userService.findAll();
	}

	@GetMapping("/{id}")
	public UserResponse get(@PathVariable Long id) {
		return userService.getById(id);
	}

	@PostMapping
	@Operation(security = {})
	public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
		UserResponse body = userService.create(request);
		URI location = URI.create("/api/v1/users/" + body.id());
		return ResponseEntity.created(location).body(body);
	}

	@PutMapping("/{id}")
	public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
		return userService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		userService.delete(id);
	}
}
