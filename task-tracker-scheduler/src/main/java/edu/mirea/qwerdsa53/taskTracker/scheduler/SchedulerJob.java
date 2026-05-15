package edu.mirea.qwerdsa53.taskTracker.scheduler;

// One unit of background work, invoked by Temporal or Spring scheduler.
public interface SchedulerJob {

	void execute();
}
