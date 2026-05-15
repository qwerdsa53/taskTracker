package edu.mirea.qwerdsa53.taskTracker.scheduler.admin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import edu.mirea.qwerdsa53.taskTracker.scheduler.SchedulerJob;
import edu.mirea.qwerdsa53.taskTracker.scheduler.spring.SchedulerJobSpringSchedule;

// Holds all scheduler jobs indexed by their simple class name.
@Component
public class SchedulerJobRegistry {

	private final Map<String, SchedulerJob> jobsByName = new LinkedHashMap<>();

	public SchedulerJobRegistry(Map<String, SchedulerJob> schedulerJobBeans) {
		for (Map.Entry<String, SchedulerJob> entry : schedulerJobBeans.entrySet()) {
			SchedulerJob job = entry.getValue();
			Class<?> targetClass = AopUtils.getTargetClass(job);
			SchedulerJobSpringSchedule meta = AnnotationUtils.findAnnotation(targetClass, SchedulerJobSpringSchedule.class);
			if (meta == null) {
				continue;
			}
			String simpleName = targetClass.getSimpleName();
			if (jobsByName.containsKey(simpleName)) {
				throw new IllegalStateException(
						"Duplicate scheduler job simple class name: " + simpleName + " (use unique class names per package or merge jobs)");
			}
			jobsByName.put(simpleName, job);
		}
	}

	public List<String> jobNames() {
		return List.copyOf(jobsByName.keySet());
	}

	public Optional<SchedulerJob> find(String name) {
		return Optional.ofNullable(jobsByName.get(name));
	}
}
