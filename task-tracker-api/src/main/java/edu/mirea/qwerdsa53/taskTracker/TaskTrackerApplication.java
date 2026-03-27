package edu.mirea.qwerdsa53.taskTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import edu.mirea.qwerdsa53.taskTracker.config.AppMailProperties;
import edu.mirea.qwerdsa53.taskTracker.security.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppMailProperties.class, JwtProperties.class})
public class TaskTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTrackerApplication.class, args);
	}

}
