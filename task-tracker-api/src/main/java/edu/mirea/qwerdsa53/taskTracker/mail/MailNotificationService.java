package edu.mirea.qwerdsa53.taskTracker.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import edu.mirea.qwerdsa53.taskTracker.config.AppMailProperties;

@Service
public class MailNotificationService {

	private static final Logger log = LoggerFactory.getLogger(MailNotificationService.class);

	private final AppMailProperties mailProperties;
	private final MailTemplateService mailTemplateService;
	private final MailFromAddressResolver fromAddressResolver;
	private final HtmlMailSender htmlMailSender;
	private final TaskExecutor mailTaskExecutor;

	public MailNotificationService(
			AppMailProperties mailProperties,
			MailTemplateService mailTemplateService,
			MailFromAddressResolver fromAddressResolver,
			HtmlMailSender htmlMailSender,
			@Qualifier("mailTaskExecutor") TaskExecutor mailTaskExecutor) {
		this.mailProperties = mailProperties;
		this.mailTemplateService = mailTemplateService;
		this.fromAddressResolver = fromAddressResolver;
		this.htmlMailSender = htmlMailSender;
		this.mailTaskExecutor = mailTaskExecutor;
	}

	/**
	 * Schedules SMTP send after the current transaction commits (if any), so the HTTP handler returns
	 * without waiting for the mail server.
	 */
	public void sendEmailVerification(String toEmail, String verificationUrl) {
		if (!mailProperties.isEnabled()) {
			log.info(
					"Mail disabled (app.mail.enabled=false). Verification link for {}: {}",
					toEmail,
					verificationUrl
			);
			return;
		}
		Runnable send =
				() -> {
					try {
						String from = fromAddressResolver.resolve(mailProperties);
						RenderedEmail rendered = mailTemplateService.renderVerification(verificationUrl);
						htmlMailSender.sendHtml(from, toEmail, rendered);
						log.debug("Verification email sent to {}", toEmail);
					} catch (RuntimeException e) {
						log.error("Failed to send verification email to {}", toEmail, e);
					}
				};
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(
					new TransactionSynchronization() {
						@Override
						public void afterCommit() {
							mailTaskExecutor.execute(send);
						}
					});
		} else {
			mailTaskExecutor.execute(send);
		}
	}
}
