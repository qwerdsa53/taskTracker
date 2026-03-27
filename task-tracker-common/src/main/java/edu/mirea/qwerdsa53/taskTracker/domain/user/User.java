package edu.mirea.qwerdsa53.taskTracker.domain.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
		name = "users",
		uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "habits")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private String timezone;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified = false;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Habit> habits = new ArrayList<>();

	public void addHabit(Habit habit) {
		habits.add(habit);
		habit.setOwner(this);
	}

	public void removeHabit(Habit habit) {
		habits.remove(habit);
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
