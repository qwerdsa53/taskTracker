package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HabitCompletionRequest(
		@NotNull LocalDate completedOn,
		String note,
		@NotNull @Positive Integer quantity) {
}
