package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ReminderRequest(
		boolean enabled,
		@NotNull LocalTime localTime,
		List<String> weekdays) {

	public ReminderRequest {
		if (weekdays == null) {
			weekdays = new ArrayList<>();
		}
	}
}
