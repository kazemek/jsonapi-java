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
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Per-module JaCoCo instruction/branch floors (sole numeric authority). They are regression
// ratchets, not evidence that tests adequately own a contract. After meaningful test additions,
// re-measure with `./gradlew jacocoTestReport` and set each justified minimum to
// floor(measuredPercent) / 100. Intentional coverage drops must update this map in the same change.
data class JacocoCoverageFloors(
    val instructionMinimum: java.math.BigDecimal,
    val branchMinimum: java.math.BigDecimal,
    val excludePatterns: List<String> = emptyList(),
)

// Coverage is on by default: floor verification and Sonar cover every test-support production
// class except passive application-shaped carriers under testsupport.fixtures.., which are
// exempt by package placement alone. New executable support code outside that hierarchy is
// therefore coverage-gated automatically, with no per-class list to maintain. Production-module
// floors do not use excludePatterns and remain unchanged.
val testSupportFixtureExclusions = listOf("io/github/kazemek/jsonapi/testsupport/fixtures/**")

val jacocoCoverageFloorsByProject =
    mapOf(
        "jsonapi-java-core" to
            JacocoCoverageFloors("0.91".toBigDecimal(), "0.80".toBigDecimal()),
        "jsonapi-java-jackson-common" to
            JacocoCoverageFloors("0.95".toBigDecimal(), "0.86".toBigDecimal()),
        "jsonapi-java-jackson3" to
            JacocoCoverageFloors("0.93".toBigDecimal(), "0.81".toBigDecimal()),
        "jsonapi-java-test-support" to
            JacocoCoverageFloors(
                "0.98".toBigDecimal(),
                "0.87".toBigDecimal(),
                testSupportFixtureExclusions,
            ),
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
        if (jacocoFloors.excludePatterns.isNotEmpty()) {
            val verificationClasses =
                files(
                    sourceSets.named("main").get().output.classesDirs.map { dir ->
                        fileTree(dir) {
                            exclude(jacocoFloors.excludePatterns)
                        }
                    },
                )
            // Floor verification excludes only the passive-fixture hierarchy; every other
            // production class is verified by default. The published JaCoCo XML report stays
            // complete for local HTML inspection; Sonar coverage uses the equivalent single
            // package-level exclusion so carriers cannot fail new_coverage.
            tasks.jacocoTestCoverageVerification {
                classDirectories.setFrom(verificationClasses)
            }
            val modulePrefix =
                projectDir.relativeTo(rootDir).invariantSeparatorsPath + "/src/main/java/"
            extra["sonarCoverageExclusions"] =
                jacocoFloors.excludePatterns.joinToString(",") { modulePrefix + it }
        }
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
