package edu.mirea.qwerdsa53.taskTracker.scheduler.temporal;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

@Component
@ConditionalOnProperty(prefix = "temporal", name = "enabled", havingValue = "true")
public class TemporalWorkerBootstrap implements SmartLifecycle {

	private static final Logger log = LoggerFactory.getLogger(TemporalWorkerBootstrap.class);

	private final TemporalProperties properties;
	private final ReminderActivitiesImpl reminderActivities;

	private WorkflowServiceStubs serviceStubs;
	private WorkflowClient workflowClient;
	private WorkerFactory workerFactory;
	private volatile boolean running;

	public TemporalWorkerBootstrap(TemporalProperties properties, ReminderActivitiesImpl reminderActivities) {
		this.properties = properties;
		this.reminderActivities = reminderActivities;
	}

	@Override
	public void start() {
		if (running) {
			return;
		}
		this.serviceStubs =
				WorkflowServiceStubs.newServiceStubs(
						WorkflowServiceStubsOptions.newBuilder().setTarget(properties.getTarget()).build());
		this.workflowClient =
				WorkflowClient.newInstance(
						serviceStubs,
						WorkflowClientOptions.newBuilder().setNamespace(properties.getNamespace()).build());
		this.workerFactory = WorkerFactory.newInstance(workflowClient);
		Worker worker = workerFactory.newWorker(properties.getTaskQueue());
		worker.registerWorkflowImplementationTypes(ReminderPollingWorkflowImpl.class);
		worker.registerActivitiesImplementations(reminderActivities);
		workerFactory.start();
		startPollingWorkflow();
		this.running = true;
		log.info(
				"Temporal worker started: target={}, namespace={}, taskQueue={}",
				properties.getTarget(),
				properties.getNamespace(),
				properties.getTaskQueue()
		);
	}

	private void startPollingWorkflow() {
		WorkflowOptions opts =
				WorkflowOptions.newBuilder()
						.setTaskQueue(properties.getTaskQueue())
						.setWorkflowId(properties.getPollingWorkflowId())
						.setWorkflowIdReusePolicy(
								WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
						.build();
		ReminderPollingWorkflow stub = workflowClient.newWorkflowStub(ReminderPollingWorkflow.class, opts);
		try {
			WorkflowClient.start(stub::runForever);
			log.info(
					"Workflow {} started",
					properties.getPollingWorkflowId()
			);
		} catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
			log.info(
					"Workflow {} already running; skip duplicate start",
					properties.getPollingWorkflowId()
			);
		}
	}

	@Override
	public void stop() {
		if (!running) {
			return;
		}
		if (workerFactory != null) {
			workerFactory.shutdown();
			workerFactory.awaitTermination(30, TimeUnit.SECONDS);
		}
		if (serviceStubs != null) {
			serviceStubs.shutdown();
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}
}
