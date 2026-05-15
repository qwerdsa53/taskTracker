package edu.mirea.qwerdsa53.taskTracker.scheduler.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

// Marks a SchedulerJob as a Spring bean and carries its schedule settings.
// Used when Temporal is disabled.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SchedulerJobSpringSchedule {

	String initialDelayMs() default "0";

	String fixedDelayMs() default "60000";

	// If set, overrides fixedDelayMs. Spring six-field cron format.
	String cron() default "";
}
