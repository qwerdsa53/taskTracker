package edu.mirea.qwerdsa53.taskTracker.mail;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import edu.mirea.qwerdsa53.taskTracker.config.AppMailProperties;

@Service
public class MailTemplateService {

	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	private final TemplateEngine mailTemplateEngine;
	private final AppMailProperties mailProperties;

	public MailTemplateService(
			@Qualifier("mailTemplateEngine") TemplateEngine mailTemplateEngine,
			AppMailProperties mailProperties) {
		this.mailTemplateEngine = mailTemplateEngine;
		this.mailProperties = mailProperties;
	}

	public RenderedEmail renderVerification(String verificationUrl) {
		Context ctx = new Context(DEFAULT_LOCALE);
		ctx.setVariable("verificationUrl", verificationUrl);
		String subject = mailProperties.getVerification().getSubject();
		String html = process(MailTemplateName.VERIFICATION, ctx);
		return new RenderedEmail(subject, html);
	}

	/** Renders a Thymeleaf template from {@code mail/templates/{name}.html} for the given {@link MailTemplateName}. */
	public String process(MailTemplateName template, Context context) {
		return mailTemplateEngine.process(template.templateFileName(), context);
	}
}
