package edu.mirea.qwerdsa53.taskTracker.domain.habit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import edu.mirea.qwerdsa53.taskTracker.domain.completion.HabitCompletion;
import edu.mirea.qwerdsa53.taskTracker.domain.reminder.Reminder;
import edu.mirea.qwerdsa53.taskTracker.domain.user.User;

@Entity
@Table(name = "habits")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"owner", "completions", "reminders"})
public class Habit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false)
	private String title;

	@Column(length = 2000)
	private String description;

	@Column(length = 32)
	private String color;

	@Column(name = "icon_key", length = 64)
	private String iconKey;

	@Column(nullable = false)
	private boolean archived = false;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Embedded
	private Frequency schedule;

	@OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<HabitCompletion> completions = new ArrayList<>();

	@OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Reminder> reminders = new ArrayList<>();

	public void addCompletion(HabitCompletion completion) {
		completions.add(completion);
		completion.setHabit(this);
	}

	public void removeCompletion(HabitCompletion completion) {
		completions.remove(completion);
	}

	public void addReminder(Reminder reminder) {
		reminders.add(reminder);
		reminder.setHabit(this);
	}

	public void removeReminder(Reminder reminder) {
		reminders.remove(reminder);
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
