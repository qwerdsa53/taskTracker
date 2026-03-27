package edu.mirea.qwerdsa53.taskTracker.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class HtmlMailSender {

	private final JavaMailSender mailSender;

	public HtmlMailSender(@Autowired(required = false) JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendHtml(String from, String to, RenderedEmail email) {
		if (mailSender == null) {
			throw new IllegalStateException(
					"Mail is enabled but SMTP is not configured. Set spring.mail.host, username, and password "
							+ "(e.g. Yandex: smtp.yandex.ru, MAIL_USERNAME, MAIL_PASSWORD).");
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(email.subject());
			helper.setText(email.htmlBody(), true);
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to build MIME message", e);
		} catch (MailException e) {
			throw new IllegalStateException(
					"SMTP rejected the message; check MAIL_USERNAME, MAIL_PASSWORD, and app password",
					e);
		}
	}
}
