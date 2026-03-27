package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.time.Instant;

public record HabitResponse(
		Long id,
		Long ownerId,
		String title,
		String description,
		String color,
		String iconKey,
		boolean archived,
		FrequencyDto schedule,
		Instant createdAt,
		Instant updatedAt) {
}
