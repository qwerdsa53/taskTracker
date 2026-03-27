package edu.mirea.qwerdsa53.taskTracker.scheduler.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.mirea.qwerdsa53.taskTracker.scheduler.SchedulerJob;

@RestController
@RequestMapping("/api/v1/jobs")
public class SchedulerJobAdminController {

	private final SchedulerJobRegistry registry;

	public SchedulerJobAdminController(SchedulerJobRegistry registry) {
		this.registry = registry;
	}

	@GetMapping
	public List<String> listJobs() {
		return registry.jobNames();
	}

	@PostMapping("/{name}/run")
	public ResponseEntity<?> run(@PathVariable String name) {
		var job = registry.find(name);
		if (job.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		job.get().execute();
		return ResponseEntity.ok(new SchedulerJobRunResponse(name, "ok"));
	}
}
