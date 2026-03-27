package edu.mirea.qwerdsa53.taskTracker.scheduler;

/**
 * One unit of background work. Invoked by Temporal activities or Spring triggers; implementations hold
 * domain logic.
 */
public interface SchedulerJob {

	void execute();
}
