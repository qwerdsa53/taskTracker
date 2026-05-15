package edu.mirea.qwerdsa53.taskTracker.mail;

public record RenderedEmail(String subject, String htmlBody) {

	public RenderedEmail {
		if (subject == null || subject.isBlank()) {
			throw new IllegalArgumentException("subject must not be blank");
		}
		if (htmlBody == null) {
			throw new IllegalArgumentException("htmlBody must not be null");
		}
	}
}
