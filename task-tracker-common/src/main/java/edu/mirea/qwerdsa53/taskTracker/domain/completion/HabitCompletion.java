package edu.mirea.qwerdsa53.taskTracker.domain.completion;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import edu.mirea.qwerdsa53.taskTracker.domain.habit.Habit;

@Entity
@Table(
		name = "habit_completions",
		uniqueConstraints =
				@UniqueConstraint(
						name = "uk_habit_completion_day",
						columnNames = {"habit_id", "completed_on"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "habit")
public class HabitCompletion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "habit_id", nullable = false)
	private Habit habit;

	@Column(name = "completed_on", nullable = false)
	private LocalDate completedOn;

	@Column(length = 2000)
	private String note;

	@Column(nullable = false)
	private Integer quantity = 1;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
