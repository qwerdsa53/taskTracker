package edu.mirea.qwerdsa53.taskTracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class GracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {

	private static final Logger log = LoggerFactory.getLogger(GracefulShutdownConfig.class);

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		log.info("SIGTERM/SIGINT received — flipping readiness to REFUSING_TRAFFIC, draining in-flight requests");
		AvailabilityChangeEvent.publish(event.getApplicationContext(), this, ReadinessState.REFUSING_TRAFFIC);
	}
}
