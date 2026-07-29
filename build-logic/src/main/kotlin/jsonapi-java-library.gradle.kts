import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    `java-library`
    groovy
    jacoco
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
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
    compileOnly(libs.findLibrary("jspecify").get())
    errorprone(libs.findLibrary("errorprone-core").get())
    errorprone(libs.findLibrary("nullaway").get())
    testImplementation(libs.findLibrary("spock-core").get())
    testImplementation(libs.findLibrary("groovy-all").get())
    testImplementation(libs.findLibrary("bytebuddy").get())
}

nullaway {
    onlyNullMarked.set(true)
    jspecifyMode.set(true)
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableAllChecks.set(true)
        error("NullAway")
        error("RequireExplicitNullMarking")
        nullaway {
            error()
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
