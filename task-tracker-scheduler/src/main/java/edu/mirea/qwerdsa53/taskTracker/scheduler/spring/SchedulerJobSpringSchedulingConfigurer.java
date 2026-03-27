package edu.mirea.qwerdsa53.taskTracker.scheduler.spring;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import edu.mirea.qwerdsa53.taskTracker.scheduler.SchedulerJob;

/**
 * Registers {@link SchedulerJob} beans that carry {@link SchedulerJobSpringSchedule} when Temporal is off.
 * Uses {@link SchedulingConfigurer} (the supported extension point for custom triggers), not a bean post-processor.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "temporal", name = "enabled", havingValue = "false")
public class SchedulerJobSpringSchedulingConfigurer implements SchedulingConfigurer {

	private static final Logger log = LoggerFactory.getLogger(SchedulerJobSpringSchedulingConfigurer.class);

	private final ApplicationContext applicationContext;
	private final Environment environment;

	public SchedulerJobSpringSchedulingConfigurer(ApplicationContext applicationContext, Environment environment) {
		this.applicationContext = applicationContext;
		this.environment = environment;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		for (Map.Entry<String, SchedulerJob> entry : applicationContext.getBeansOfType(SchedulerJob.class).entrySet()) {
			SchedulerJob job = entry.getValue();
			Class<?> targetClass = AopUtils.getTargetClass(job);
			SchedulerJobSpringSchedule meta = AnnotationUtils.findAnnotation(targetClass, SchedulerJobSpringSchedule.class);
			if (meta == null) {
				continue;
			}
			Runnable task = job::execute;
			String cron = resolve(meta.cron());
			if (!cron.isBlank()) {
				taskRegistrar.addTriggerTask(task, new CronTrigger(cron));
				log.info(
						"Spring schedule (non-Temporal): bean={} mode=cron expression={}",
						entry.getKey(),
						cron
				);
			} else {
				long initialMs = Long.parseLong(resolve(meta.initialDelayMs()));
				long fixedMs = Long.parseLong(resolve(meta.fixedDelayMs()));
				PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofMillis(fixedMs));
				trigger.setInitialDelay(Duration.ofMillis(initialMs));
				taskRegistrar.addTriggerTask(task, trigger);
				log.info(
						"Spring schedule (non-Temporal): bean={} mode=fixed-delay initial={}ms interval={}ms",
						entry.getKey(),
						initialMs,
						fixedMs
				);
			}
		}
	}

	private String resolve(String raw) {
		if (raw == null) {
			return "";
		}
		String s = raw.trim();
		if (s.isEmpty()) {
			return "";
		}
		return environment.resolvePlaceholders(s);
	}
}
