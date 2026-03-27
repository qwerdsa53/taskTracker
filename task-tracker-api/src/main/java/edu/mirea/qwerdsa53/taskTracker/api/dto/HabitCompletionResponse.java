package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public record HabitCompletionResponse(
		Long id,
		Long habitId,
		LocalDate completedOn,
		String note,
		Integer quantity,
		Instant createdAt) {
}
