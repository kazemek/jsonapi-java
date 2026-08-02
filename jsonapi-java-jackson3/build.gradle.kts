plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-annotations"))
    api(project(":jsonapi-java-core"))
    api(libs.jackson3.databind)
    testImplementation(project(":jsonapi-java-test-fixtures"))
    testImplementation(libs.archunit)
}

tasks.test {
    systemProperty(
        "jsonapi.fixtures.dir",
        rootProject.layout.projectDirectory
            .dir("fixtures/jsonapi-1.1")
            .asFile.absolutePath,
    )
}
