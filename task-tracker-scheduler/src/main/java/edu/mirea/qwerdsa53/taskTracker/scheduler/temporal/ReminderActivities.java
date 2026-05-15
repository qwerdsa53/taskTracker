package edu.mirea.qwerdsa53.taskTracker.scheduler.temporal;

import edu.mirea.qwerdsa53.taskTracker.scheduler.SchedulerJob;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ReminderActivities {

	@ActivityMethod
	void pollDueReminders();
}
