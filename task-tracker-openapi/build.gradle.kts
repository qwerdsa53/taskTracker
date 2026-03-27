plugins {
	`java-library`
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

dependencies {
	implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))
	implementation("org.springframework.boot:spring-boot-autoconfigure")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
}
