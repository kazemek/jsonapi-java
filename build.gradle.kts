// Root project — no source code.
// Shared build logic lives in build-logic/ convention plugins.

plugins {
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "kazemek_jsonapi-java")
        property("sonar.organization", "kazemek")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.qualitygate.wait", "true")
    }
}
