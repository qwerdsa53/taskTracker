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

import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitCompletionRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitCompletionResponse;
import edu.mirea.qwerdsa53.taskTracker.service.HabitCompletionService;

@RestController
@RequestMapping("/api/v1/users/{userId}/habits/{habitId}/completions")
@SecurityRequirement(name = "bearer-jwt")
public class HabitCompletionController {

	private final HabitCompletionService completionService;

	public HabitCompletionController(HabitCompletionService completionService) {
		this.completionService = completionService;
	}

	@GetMapping
	public List<HabitCompletionResponse> list(@PathVariable Long userId, @PathVariable Long habitId) {
		return completionService.findAll(userId, habitId);
	}

	@GetMapping("/{completionId}")
	public HabitCompletionResponse get(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@PathVariable Long completionId) {
		return completionService.getById(userId, habitId, completionId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public HabitCompletionResponse create(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@Valid @RequestBody HabitCompletionRequest request) {
		return completionService.create(userId, habitId, request);
	}

	@PutMapping("/{completionId}")
	public HabitCompletionResponse update(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@PathVariable Long completionId,
			@Valid @RequestBody HabitCompletionRequest request) {
		return completionService.update(userId, habitId, completionId, request);
	}

	@DeleteMapping("/{completionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@PathVariable Long completionId) {
		completionService.delete(userId, habitId, completionId);
	}
}
