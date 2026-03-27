package edu.mirea.qwerdsa53.taskTracker.mail.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class MailTemplateEngineConfig {

	@Bean
	public TemplateEngine mailTemplateEngine() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("mail/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
		resolver.setCacheable(true);
		resolver.setCheckExistence(true);

		TemplateEngine engine = new TemplateEngine();
		engine.setTemplateResolver(resolver);
		return engine;
	}
}
