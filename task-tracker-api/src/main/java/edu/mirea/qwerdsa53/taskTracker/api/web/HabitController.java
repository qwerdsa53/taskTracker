package edu.mirea.qwerdsa53.taskTracker.api.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitResponse;
import edu.mirea.qwerdsa53.taskTracker.service.HabitService;

@RestController
@RequestMapping("/api/v1/users/{userId}/habits")
@SecurityRequirement(name = "bearer-jwt")
public class HabitController {

	private final HabitService habitService;

	public HabitController(HabitService habitService) {
		this.habitService = habitService;
	}

	@GetMapping
	public List<HabitResponse> list(@PathVariable Long userId) {
		return habitService.findAllForUser(userId);
	}

	@GetMapping("/{habitId}")
	public HabitResponse get(@PathVariable Long userId, @PathVariable Long habitId) {
		return habitService.getByIdForUser(userId, habitId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public HabitResponse create(@PathVariable Long userId, @Valid @RequestBody HabitRequest request) {
		return habitService.create(userId, request);
	}

	@PutMapping("/{habitId}")
	public HabitResponse update(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@Valid @RequestBody HabitRequest request) {
		return habitService.update(userId, habitId, request);
	}

	@DeleteMapping("/{habitId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long userId, @PathVariable Long habitId) {
		habitService.delete(userId, habitId);
	}
}
