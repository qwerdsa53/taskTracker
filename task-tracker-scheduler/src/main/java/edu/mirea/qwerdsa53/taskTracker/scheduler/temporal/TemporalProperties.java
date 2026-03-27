package edu.mirea.qwerdsa53.taskTracker.scheduler.temporal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "temporal")
public class TemporalProperties {

	private boolean enabled = true;

	/** Temporal frontend gRPC address (host:port). */
	private String target = "127.0.0.1:7233";

	private String namespace = "default";

	private String taskQueue = "task-tracker-reminders";

	/** Fixed workflow id for the long-running reminder poll (survives worker restarts). */
	private String pollingWorkflowId = "reminder-polling-global";

	private int activityTimeoutSeconds = 30;

	private int tickIntervalSeconds = 60;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(String taskQueue) {
		this.taskQueue = taskQueue;
	}

	public String getPollingWorkflowId() {
		return pollingWorkflowId;
	}

	public void setPollingWorkflowId(String pollingWorkflowId) {
		this.pollingWorkflowId = pollingWorkflowId;
	}

	public int getActivityTimeoutSeconds() {
		return activityTimeoutSeconds;
	}

	public void setActivityTimeoutSeconds(int activityTimeoutSeconds) {
		this.activityTimeoutSeconds = activityTimeoutSeconds;
	}

	public int getTickIntervalSeconds() {
		return tickIntervalSeconds;
	}

	public void setTickIntervalSeconds(int tickIntervalSeconds) {
		this.tickIntervalSeconds = tickIntervalSeconds;
	}
}
