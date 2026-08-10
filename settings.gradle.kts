pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "jsonapi-java"
include("jsonapi-java-core")
include("jsonapi-java-annotations")
include("jsonapi-java-jackson-common")
include("jsonapi-java-jackson3")
include("jsonapi-java-test-fixtures")
