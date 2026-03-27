package edu.mirea.qwerdsa53.taskTracker.scheduler.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import edu.mirea.qwerdsa53.taskTracker.scheduler.SchedulerJob;
import edu.mirea.qwerdsa53.taskTracker.scheduler.repository.ReminderSchedulerRepository;
import edu.mirea.qwerdsa53.taskTracker.scheduler.spring.SchedulerJobSpringSchedule;

@SchedulerJobSpringSchedule(
		initialDelayMs = "${scheduler.reminder.initial-delay-ms:15000}",
		fixedDelayMs = "${scheduler.reminder.poll-interval-ms:60000}",
		cron = "${scheduler.reminder.cron:}")
public class ReminderSchedulerJob implements SchedulerJob {

	private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerJob.class);

	private final ReminderSchedulerRepository reminderSchedulerRepository;

	public ReminderSchedulerJob(ReminderSchedulerRepository reminderSchedulerRepository) {
		this.reminderSchedulerRepository = reminderSchedulerRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public void execute() {
		long enabled = reminderSchedulerRepository.countByEnabledTrue();
		log.info(
				"Reminder job tick: {} enabled reminders (placeholder until due checks and notifications)",
				enabled
		);
	}
}
