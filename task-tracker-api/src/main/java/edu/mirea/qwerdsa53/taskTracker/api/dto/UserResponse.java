package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.time.Instant;

public record UserResponse(
		Long id,
		String email,
		String username,
		String timezone,
		boolean emailVerified,
		Instant createdAt,
		Instant updatedAt) {
}
