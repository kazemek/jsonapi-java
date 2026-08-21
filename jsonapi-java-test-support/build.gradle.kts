plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-jackson-common"))
    api(project(":jsonapi-java-core"))
    api(project(":jsonapi-java-annotations"))
    implementation(libs.jakarta.json.api)
    implementation(libs.jackson.annotations)
    runtimeOnly(libs.parsson)
    testImplementation(libs.archunit)
}
