package edu.mirea.qwerdsa53.taskTracker.api.dto;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mirea.qwerdsa53.taskTracker.domain.completion.HabitCompletion;
import edu.mirea.qwerdsa53.taskTracker.domain.habit.Frequency;
import edu.mirea.qwerdsa53.taskTracker.domain.habit.Habit;
import edu.mirea.qwerdsa53.taskTracker.domain.reminder.Reminder;
import edu.mirea.qwerdsa53.taskTracker.domain.user.User;

public final class DtoMapper {

	private DtoMapper() {}

	public static UserResponse toUserResponse(User u) {
		return new UserResponse(
				u.getId(),
				u.getEmail(),
				u.getUsername(),
				u.getTimezone(),
				u.isEmailVerified(),
				u.getCreatedAt(),
				u.getUpdatedAt());
	}

	public static FrequencyDto toFrequencyDto(Frequency f) {
		if (f == null) {
			return null;
		}
		List<String> days =
				f.getActiveWeekdays().stream()
						.map(Enum::name)
						.sorted()
						.toList();
		return new FrequencyDto(f.getType(), f.getTargetPerWeek(), days);
	}

	public static Frequency toFrequency(FrequencyDto dto) {
		Frequency frequency = new Frequency();
		frequency.setType(dto.type());
		frequency.setTargetPerWeek(dto.targetPerWeek());
		Set<DayOfWeek> set = new HashSet<>();
		for (String s : dto.activeWeekdays()) {
			set.add(DayOfWeek.valueOf(s.trim().toUpperCase()));
		}
		frequency.setActiveWeekdays(set);
		return frequency;
	}

	public static HabitResponse toHabitResponse(Habit h) {
		return new HabitResponse(
				h.getId(),
				h.getOwner().getId(),
				h.getTitle(),
				h.getDescription(),
				h.getColor(),
				h.getIconKey(),
				h.isArchived(),
				toFrequencyDto(h.getSchedule()),
				h.getCreatedAt(),
				h.getUpdatedAt());
	}

	public static HabitCompletionResponse toCompletionResponse(HabitCompletion c) {
		return new HabitCompletionResponse(
				c.getId(),
				c.getHabit().getId(),
				c.getCompletedOn(),
				c.getNote(),
				c.getQuantity(),
				c.getCreatedAt());
	}

	public static ReminderResponse toReminderResponse(Reminder r) {
		List<String> days =
				r.getWeekdays().stream()
						.map(Enum::name)
						.sorted(Comparator.naturalOrder())
						.toList();
		return new ReminderResponse(
				r.getId(),
				r.getHabit().getId(),
				r.isEnabled(),
				r.getLocalTime(),
				days);
	}

	public static void applyReminder(Reminder r, ReminderRequest req) {
		r.setEnabled(req.enabled());
		r.setLocalTime(req.localTime());
		Set<DayOfWeek> set = new HashSet<>();
		for (String s : req.weekdays()) {
			set.add(DayOfWeek.valueOf(s.trim().toUpperCase()));
		}
		r.setWeekdays(set);
	}

	public static Reminder newReminder(ReminderRequest req) {
		Reminder reminder = new Reminder();
		applyReminder(reminder, req);
		return reminder;
	}
}
