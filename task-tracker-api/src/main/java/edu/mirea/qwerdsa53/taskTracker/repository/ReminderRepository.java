package edu.mirea.qwerdsa53.taskTracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mirea.qwerdsa53.taskTracker.domain.reminder.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

	List<Reminder> findByHabit_Id(Long habitId);

	Optional<Reminder> findByIdAndHabit_Id(Long id, Long habitId);
}
