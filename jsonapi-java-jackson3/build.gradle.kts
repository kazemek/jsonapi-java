plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-jackson-api"))
    api(project(":jsonapi-java-annotations"))
    api(project(":jsonapi-java-core"))
    api(libs.jackson3.databind)
    testImplementation(project(":jsonapi-java-test-support"))
    testImplementation(libs.archunit)
    testImplementation(libs.json.schema.validator)
}
