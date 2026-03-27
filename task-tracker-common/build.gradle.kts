plugins {
	`java-library`
	id("io.spring.dependency-management") version "1.1.7"
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.5")
	}
}

dependencies {
	api("jakarta.persistence:jakarta.persistence-api")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
}
