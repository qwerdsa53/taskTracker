package edu.mirea.qwerdsa53.taskTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import edu.mirea.qwerdsa53.taskTracker.scheduler.temporal.TemporalProperties;

@SpringBootApplication(scanBasePackages = "edu.mirea.qwerdsa53.taskTracker")
@EnableScheduling
@EnableConfigurationProperties(TemporalProperties.class)
public class TaskTrackerSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTrackerSchedulerApplication.class, args);
	}
}
