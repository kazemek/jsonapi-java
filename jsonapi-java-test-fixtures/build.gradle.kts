plugins {
    id("jsonapi-java-library")
}

dependencies {
    api(project(":jsonapi-java-core"))
    implementation(libs.groovy.all)
}

tasks.test {
    systemProperty(
        "jsonapi.fixtures.dir",
        rootProject.layout.projectDirectory
            .dir("fixtures/jsonapi-1.1")
            .asFile.absolutePath,
    )
}
