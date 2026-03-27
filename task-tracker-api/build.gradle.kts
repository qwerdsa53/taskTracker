plugins {
	java
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.netflix.dgs.codegen") version "8.3.0"
	id("org.flywaydb.flyway") version "10.22.0"
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

extra["netflixDgsVersion"] = "11.0.0"

dependencies {
	implementation(project(":task-tracker-common"))
	implementation(project(":task-tracker-openapi"))
	implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.thymeleaf:thymeleaf")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")
	implementation("org.postgresql:postgresql")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter-test")
	testImplementation("com.intuit.karate:karate-junit5:1.4.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("com.h2database:h2")
}

dependencyManagement {
	imports {
		mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${property("netflixDgsVersion")}")
	}
}

tasks.generateJava {
	schemaPaths.add("${projectDir}/src/main/resources/graphql-client")
	packageName = "edu.mirea.qwerdsa53.taskTracker.codegen"
	generateClient = true
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<Test>("test") {
	description = "Unit-тесты (профиль testing, H2). Тесты с @Tag(\"integration\") и @Tag(\"karate\") не запускаются."
	useJUnitPlatform {
		excludeTags("integration", "karate")
	}
}

tasks.register<Test>("integrationTest") {
	group = "verification"
	description = "Интеграционные тесты (@Tag integration). Postgres integration на localhost:5433 (docker compose + fixture-db-integration.sh)."
	testClassesDirs = tasks.test.get().testClassesDirs
	classpath = tasks.test.get().classpath
	useJUnitPlatform {
		includeTags("integration")
	}
	shouldRunAfter(tasks.test)
}

flyway {
	url = System.getenv("SPRING_DATASOURCE_URL")
		?: project.findProperty("flyway.url")?.toString()
		?: "jdbc:postgresql://127.0.0.1:5432/tasktracker"
	user = System.getenv("DB_USER") ?: project.findProperty("flyway.user")?.toString() ?: "postgres"
	password = System.getenv("DB_PASSWORD") ?: project.findProperty("flyway.password")?.toString() ?: "postgres"
	locations = arrayOf("filesystem:${project.projectDir}/src/main/resources/db/migration")
}

tasks.register<Exec>("fixtureDb") {
	group = "development"
	description = "Основная БД (порт DB_PORT): Flyway + fixtures/load.sql"
	workingDir = rootProject.projectDir
	commandLine("bash", "scripts/fixture-db.sh")
}

tasks.register<Exec>("fixtureDbIntegration") {
	group = "development"
	description = "Интеграционная БД (порт DB_INTEGRATION_PORT): Flyway + fixtures/load.sql"
	workingDir = rootProject.projectDir
	commandLine("bash", "scripts/fixture-db-integration.sh")
}

val karateE2eInfra =
		tasks.register<Exec>("karateE2eInfra") {
			group = "verification"
			description =
					"Docker (docker-compose.karate.yml): Postgres, Redis, Flyway, фикстуры, API. Без Karate (без вложенного Gradle)."
			dependsOn(tasks.bootJar)
			workingDir = rootProject.projectDir
			commandLine("bash", "scripts/karate-e2e-infra.sh")
		}

tasks.register<Test>("karateTest") {
	group = "verification"
	description = "Karate API (тег @karate). После karateE2eInfra или вручную: bash scripts/karate-e2e-infra.sh"
	testClassesDirs = tasks.test.get().testClassesDirs
	classpath = tasks.test.get().classpath
	useJUnitPlatform {
		includeTags("karate")
	}
	val karatePort = System.getenv("KARATE_APP_PORT") ?: "8080"
	systemProperty(
			"karate.baseUrl",
			System.getProperty("karate.baseUrl") ?: "http://localhost:$karatePort")
	shouldRunAfter(tasks.test)
	mustRunAfter(karateE2eInfra)
}

tasks.register("karateE2e") {
	group = "verification"
	description = "karateE2eInfra + karateTest (один Gradle — без вложенного ./gradlew из скрипта)"
	dependsOn(tasks.named("karateE2eInfra"), tasks.named("karateTest"))
}

tasks.register<Exec>("karateDockerDown") {
	group = "verification"
	description = "Остановить стек Karate: docker compose -f docker-compose.karate.yml down"
	workingDir = rootProject.projectDir
	commandLine("bash", "scripts/karate-docker-down.sh")
}

tasks.bootJar {
	archiveBaseName.set("task-tracker-api")
}
