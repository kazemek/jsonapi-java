// Root project — no source code.
// Shared build logic lives in build-logic/ convention plugins.

plugins {
    alias(libs.plugins.sonarqube)
    id("jsonapi-java-spotless")
}

sonar {
    properties {
        property("sonar.projectKey", "kazemek_jsonapi-java")
        property("sonar.organization", "kazemek")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.qualitygate.wait", "true")
    }
}

gradle.projectsEvaluated {
    val exclusions =
        subprojects
            .mapNotNull { it.extensions.extraProperties.properties["sonarCoverageExclusions"] as? String }
            .filter { it.isNotBlank() }
            .joinToString(",")
    if (exclusions.isNotEmpty()) {
        sonar.properties {
            property("sonar.coverage.exclusions", exclusions)
        }
    }
}
