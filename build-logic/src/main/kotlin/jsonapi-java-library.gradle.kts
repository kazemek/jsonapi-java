plugins {
    `java-library`
    groovy
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    testImplementation(libs.findLibrary("spock-core").get())
    testImplementation(libs.findLibrary("groovy-all").get())
    testImplementation(libs.findLibrary("bytebuddy").get())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
