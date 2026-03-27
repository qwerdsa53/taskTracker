package edu.mirea.qwerdsa53.taskTracker.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

	private boolean enabled = true;

	private String fromAddress = "";

	private Duration verificationTokenTtl = Duration.ofHours(24);

	private String verificationBaseUrl = "http://localhost:8080";

	private Verification verification = new Verification();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public Duration getVerificationTokenTtl() {
		return verificationTokenTtl;
	}

	public void setVerificationTokenTtl(Duration verificationTokenTtl) {
		this.verificationTokenTtl = verificationTokenTtl;
	}

	public String getVerificationBaseUrl() {
		return verificationBaseUrl;
	}

	public void setVerificationBaseUrl(String verificationBaseUrl) {
		this.verificationBaseUrl = verificationBaseUrl;
	}

	public Verification getVerification() {
		return verification;
	}

	public void setVerification(Verification verification) {
		this.verification = verification;
	}

	public static class Verification {

		private String subject = "Confirm email — Task Tracker";

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}
	}
}
