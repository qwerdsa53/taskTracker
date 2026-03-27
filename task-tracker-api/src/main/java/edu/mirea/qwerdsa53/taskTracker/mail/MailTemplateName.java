package edu.mirea.qwerdsa53.taskTracker.mail;

/** Maps to {@code classpath:mail/templates/<name>.html}; enum name is the file base name. */
public enum MailTemplateName {

	VERIFICATION("verification");

	private final String templateFileName;

	MailTemplateName(String templateFileName) {
		this.templateFileName = templateFileName;
	}

	public String templateFileName() {
		return templateFileName;
	}
}
