plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "taskTracker"

include(
		"task-tracker-common",
		"task-tracker-openapi",
		"task-tracker-api",
		"task-tracker-scheduler",
)
