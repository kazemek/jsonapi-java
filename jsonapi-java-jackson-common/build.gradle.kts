plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-core"))
    testImplementation(libs.archunit)
}
