plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-jackson-common"))
    api(project(":jsonapi-java-annotations"))
    api(project(":jsonapi-java-core"))
    api(libs.jackson3.databind)
    testImplementation(project(":jsonapi-java-test-fixtures"))
    testImplementation(libs.archunit)
    testImplementation(libs.json.schema.validator)
}

tasks.test {
    systemProperty(
        "jsonapi.fixtures.dir",
        rootProject.layout.projectDirectory
            .dir("fixtures/jsonapi-1.1")
            .asFile.absolutePath,
    )
    systemProperty(
        "jsonapi.schema.fixtures.dir",
        rootProject.layout.projectDirectory
            .dir("fixtures/jsonapi-schema/1.1-pr1603")
            .asFile.absolutePath,
    )
}
