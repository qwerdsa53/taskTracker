package edu.mirea.qwerdsa53.taskTracker.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HabitRequest(
		@NotBlank @Size(max = 255) String title,
		@Size(max = 2000) String description,
		@Size(max = 32) String color,
		@Size(max = 64) String iconKey,
		boolean archived,
		@NotNull @Valid FrequencyDto schedule) {
}
