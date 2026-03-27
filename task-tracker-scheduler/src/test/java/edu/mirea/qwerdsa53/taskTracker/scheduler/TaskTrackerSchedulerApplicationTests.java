package edu.mirea.qwerdsa53.taskTracker.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.mirea.qwerdsa53.taskTracker.TaskTrackerSchedulerApplication;

@SpringBootTest(classes = TaskTrackerSchedulerApplication.class)
@ActiveProfiles("test")
class TaskTrackerSchedulerApplicationTests {

	@Test
	void contextLoads() {
	}
}
