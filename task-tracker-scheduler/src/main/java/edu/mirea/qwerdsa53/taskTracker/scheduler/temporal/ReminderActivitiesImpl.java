package edu.mirea.qwerdsa53.taskTracker.scheduler.temporal;

import org.springframework.stereotype.Component;

import edu.mirea.qwerdsa53.taskTracker.scheduler.reminder.ReminderSchedulerJob;

@Component
public class ReminderActivitiesImpl implements ReminderActivities {

	private final ReminderSchedulerJob reminderJob;

	public ReminderActivitiesImpl(ReminderSchedulerJob reminderJob) {
		this.reminderJob = reminderJob;
	}

	@Override
	public void pollDueReminders() {
		reminderJob.execute();
	}
}
