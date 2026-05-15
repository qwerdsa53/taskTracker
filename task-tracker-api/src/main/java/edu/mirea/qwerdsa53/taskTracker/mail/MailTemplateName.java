package edu.mirea.qwerdsa53.taskTracker.mail;

// Each value maps to mail/templates/<name>.html
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
