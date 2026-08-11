plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-jackson-common"))
    api(project(":jsonapi-java-core"))
    implementation(libs.groovy.all)
}
