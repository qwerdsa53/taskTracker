package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.time.LocalTime;
import java.util.List;

public record ReminderResponse(
		Long id,
		Long habitId,
		boolean enabled,
		LocalTime localTime,
		List<String> weekdays) {
}
