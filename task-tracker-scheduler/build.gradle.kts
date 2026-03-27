plugins {
	java
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
	implementation("io.temporal:temporal-sdk:1.25.2")
	implementation(project(":task-tracker-common"))
	implementation(project(":task-tracker-openapi"))
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.postgresql:postgresql")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.bootJar {
	archiveBaseName.set("task-tracker-scheduler")
}
