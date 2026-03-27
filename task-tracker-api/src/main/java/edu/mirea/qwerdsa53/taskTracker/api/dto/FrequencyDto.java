package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import edu.mirea.qwerdsa53.taskTracker.domain.habit.FrequencyType;

public record FrequencyDto(
		@NotNull FrequencyType type,
		Integer targetPerWeek,
		List<String> activeWeekdays) {

	public FrequencyDto {
		if (activeWeekdays == null) {
			activeWeekdays = new ArrayList<>();
		}
	}
}
