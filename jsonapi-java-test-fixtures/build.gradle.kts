plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-jackson-common"))
    api(project(":jsonapi-java-core"))
    api(project(":jsonapi-java-annotations"))
    implementation(libs.groovy.all)
    implementation(libs.jackson.annotations)
    testImplementation(libs.archunit)
}
