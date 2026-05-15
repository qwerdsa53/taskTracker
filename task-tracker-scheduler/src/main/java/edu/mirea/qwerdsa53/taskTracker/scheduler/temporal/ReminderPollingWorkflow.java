package edu.mirea.qwerdsa53.taskTracker.scheduler.temporal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ReminderPollingWorkflow {

	// Runs forever: polls reminders, then sleeps until the next tick.
	@WorkflowMethod
	void runForever();
}
