package edu.mirea.qwerdsa53.taskTracker.domain.reminder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import edu.mirea.qwerdsa53.taskTracker.domain.habit.Habit;

@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "habit")
public class Reminder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "habit_id", nullable = false)
	private Habit habit;

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(name = "local_time", nullable = false)
	private LocalTime localTime;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "reminder_weekdays", joinColumns = @JoinColumn(name = "reminder_id"))
	@Column(name = "day_of_week")
	@Enumerated(EnumType.STRING)
	private Set<DayOfWeek> weekdays = new HashSet<>();
}
