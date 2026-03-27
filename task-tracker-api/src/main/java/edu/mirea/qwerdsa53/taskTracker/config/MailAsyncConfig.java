package edu.mirea.qwerdsa53.taskTracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class MailAsyncConfig {

	@Bean(name = "mailTaskExecutor")
	public TaskExecutor mailTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("mail-");
		executor.initialize();
		return executor;
	}
}
