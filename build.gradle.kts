allprojects {
	group = "edu.mirea.qwerdsa53"
	version = "0.0.1-SNAPSHOT"
}

subprojects {
	repositories {
		mavenCentral()
	}
}

tasks.register("karateE2e") {
	group = "verification"
	description = "См. :task-tracker-api:karateE2e — Docker (Postgres, Redis, API) + Karate"
	dependsOn(":task-tracker-api:karateE2e")
}
