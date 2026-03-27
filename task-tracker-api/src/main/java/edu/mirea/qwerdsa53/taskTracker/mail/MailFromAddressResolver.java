package edu.mirea.qwerdsa53.taskTracker.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.mirea.qwerdsa53.taskTracker.config.AppMailProperties;

@Component
public class MailFromAddressResolver {

	@Value("${spring.mail.username:}")
	private String springMailUsername;

	public String resolve(AppMailProperties mailProperties) {
		String from = mailProperties.getFromAddress();
		if (from != null && !from.isBlank()) {
			return from.trim();
		}
		if (springMailUsername != null && !springMailUsername.isBlank()) {
			return springMailUsername.trim();
		}
		throw new IllegalStateException(
				"Set sender: app.mail.from-address or spring.mail.username (full mailbox address for Yandex).");
	}
}
