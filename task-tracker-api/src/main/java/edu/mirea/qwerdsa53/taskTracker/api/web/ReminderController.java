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

import edu.mirea.qwerdsa53.taskTracker.api.dto.ReminderRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.ReminderResponse;
import edu.mirea.qwerdsa53.taskTracker.service.ReminderService;

@RestController
@RequestMapping("/api/v1/users/{userId}/habits/{habitId}/reminders")
public class ReminderController {

	private final ReminderService reminderService;

	public ReminderController(ReminderService reminderService) {
		this.reminderService = reminderService;
	}

	@GetMapping
	public List<ReminderResponse> list(@PathVariable Long userId, @PathVariable Long habitId) {
		return reminderService.findAll(userId, habitId);
	}

	@GetMapping("/{reminderId}")
	public ReminderResponse get(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@PathVariable Long reminderId) {
		return reminderService.getById(userId, habitId, reminderId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ReminderResponse create(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@Valid @RequestBody ReminderRequest request) {
		return reminderService.create(userId, habitId, request);
	}

	@PutMapping("/{reminderId}")
	public ReminderResponse update(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@PathVariable Long reminderId,
			@Valid @RequestBody ReminderRequest request) {
		return reminderService.update(userId, habitId, reminderId, request);
	}

	@DeleteMapping("/{reminderId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable Long userId,
			@PathVariable Long habitId,
			@PathVariable Long reminderId) {
		reminderService.delete(userId, habitId, reminderId);
	}
}
