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

tasks.named<JavaCompile>("compileJava").configure {
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
    // Centralized fixture wiring: every module's tests resolve the shared JSON:API 1.1 document
    // corpus and the pinned draft-schema fixtures from these two root-relative directories.
    // Contents are fingerprinted as relocatable inputs; absolute -D paths are supplied only at
    // execution time.
    jvmArgumentProviders.add(
        objects.newInstance<FixtureDirectoryArgumentProvider>().apply {
            fixturesDir.set(rootProject.layout.projectDirectory.dir("fixtures/jsonapi-1.1"))
            schemaFixturesDir.set(
                rootProject.layout.projectDirectory.dir("fixtures/jsonapi-schema/1.1-pr1603"),
            )
        },
    )
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Per-module JaCoCo instruction/branch floors (sole numeric authority). Re-measure with
// `./gradlew jacocoTestReport`, then set each minimum to floor(measuredPercent) / 100.
// Intentional coverage drops must update this map in the same change.
data class JacocoCoverageFloors(
    val instructionMinimum: java.math.BigDecimal,
    val branchMinimum: java.math.BigDecimal,
)

val jacocoCoverageFloorsByProject =
    mapOf(
        "jsonapi-java-core" to
            JacocoCoverageFloors("0.91".toBigDecimal(), "0.80".toBigDecimal()),
        "jsonapi-java-jackson-common" to
            JacocoCoverageFloors("0.83".toBigDecimal(), "0.78".toBigDecimal()),
        "jsonapi-java-jackson3" to
            JacocoCoverageFloors("0.93".toBigDecimal(), "0.81".toBigDecimal()),
        "jsonapi-java-test-fixtures" to
            JacocoCoverageFloors("0.97".toBigDecimal(), "0.87".toBigDecimal()),
    )

// Annotation-only classfiles produce no instruction/branch counters; do not attach minima.
val jacocoCoverageFloorSkipProjects = setOf("jsonapi-java-annotations")

val jacocoFloors = jacocoCoverageFloorsByProject[project.name]
val jacocoFloorSkipped = project.name in jacocoCoverageFloorSkipProjects
when {
    jacocoFloors != null && jacocoFloorSkipped -> {
        error(
            "Project '${project.name}' is listed in both jacocoCoverageFloorsByProject and " +
                "jacocoCoverageFloorSkipProjects; map and skip set must be disjoint.",
        )
    }

    jacocoFloors == null && !jacocoFloorSkipped -> {
        error(
            "Project '${project.name}' applies jsonapi-java-library but has neither a " +
                "jacocoCoverageFloorsByProject entry nor a jacocoCoverageFloorSkipProjects entry. " +
                "Add exactly one before merge.",
        )
    }

    jacocoFloors != null -> {
        tasks.jacocoTestCoverageVerification {
            violationRules {
                rule {
                    limit {
                        counter = "INSTRUCTION"
                        minimum = jacocoFloors.instructionMinimum
                    }
                    limit {
                        counter = "BRANCH"
                        minimum = jacocoFloors.branchMinimum
                    }
                }
            }
        }
        tasks.named("check") {
            dependsOn(tasks.jacocoTestCoverageVerification)
        }
    }
}
