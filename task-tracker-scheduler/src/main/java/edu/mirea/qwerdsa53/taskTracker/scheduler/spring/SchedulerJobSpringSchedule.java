package edu.mirea.qwerdsa53.taskTracker.scheduler.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Stereotype: registers the class as a Spring bean ({@link Component}) and, when Temporal is off,
 * {@link SchedulerJobSpringSchedulingConfigurer} wires schedule metadata to the task scheduler.
 * REST list/run use {@link Class#getSimpleName()} of the implementation class.
 * Prefer {@link #cron()} for cron, otherwise {@link #initialDelayMs()} and {@link #fixedDelayMs()}.
 * Placeholders use {@link Environment#resolvePlaceholders(String)}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SchedulerJobSpringSchedule {

	/** Initial delay (ms) before the first run when using fixed delay; placeholders allowed. */
	String initialDelayMs() default "0";

	/** Fixed delay (ms) between the end of one run and the start of the next; ignored if cron is set. */
	String fixedDelayMs() default "60000";

	/**
	 * Spring cron expression (optional six-field). If non-blank after placeholder resolution, it takes
	 * precedence over fixed delay.
	 */
	String cron() default "";
}
