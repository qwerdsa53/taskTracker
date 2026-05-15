package edu.mirea.qwerdsa53.taskTracker.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mirea.qwerdsa53.taskTracker.domain.reminder.Reminder;

public interface ReminderSchedulerRepository extends JpaRepository<Reminder, Long> {

	long countByEnabledTrue();
}
