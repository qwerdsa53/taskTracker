package edu.mirea.qwerdsa53.taskTracker.domain.habit;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Frequency implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	@Column(name = "schedule_type", nullable = false)
	private FrequencyType type;

	@Column(name = "target_per_week")
	private Integer targetPerWeek;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "habit_schedule_weekdays", joinColumns = @JoinColumn(name = "habit_id"))
	@Column(name = "day_of_week")
	@Enumerated(EnumType.STRING)
	private Set<DayOfWeek> activeWeekdays = new HashSet<>();
}
