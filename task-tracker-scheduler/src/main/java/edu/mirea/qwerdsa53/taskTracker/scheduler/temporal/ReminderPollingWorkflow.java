package edu.mirea.qwerdsa53.taskTracker.scheduler.temporal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ReminderPollingWorkflow {

	/** Long-running loop: run poll activity, then sleep between ticks. */
	@WorkflowMethod
	void runForever();
}
