package edu.mirea.qwerdsa53.taskTracker.scheduler.temporal;

import java.time.Duration;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

public class ReminderPollingWorkflowImpl implements ReminderPollingWorkflow {

	private static final int TICK_SECONDS = 60;

	@Override
	public void runForever() {
		ReminderActivities activities =
				Workflow.newActivityStub(
						ReminderActivities.class,
						ActivityOptions.newBuilder()
								.setStartToCloseTimeout(Duration.ofSeconds(90))
								.build());
		while (true) {
			activities.pollDueReminders();
			Workflow.sleep(Duration.ofSeconds(TICK_SECONDS));
		}
	}
}
