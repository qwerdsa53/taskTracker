package edu.mirea.qwerdsa53.taskTracker.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openapi")
public class OpenApiInfoProperties {

	private boolean enabled = true;

	private String title = "API";

	private String description = "";

	private String version = "0.0.1";

	/**
	 * If true, GET / redirects to Swagger UI (springdoc default path).
	 */
	private boolean redirectRootToSwagger = false;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isRedirectRootToSwagger() {
		return redirectRootToSwagger;
	}

	public void setRedirectRootToSwagger(boolean redirectRootToSwagger) {
		this.redirectRootToSwagger = redirectRootToSwagger;
	}
}
