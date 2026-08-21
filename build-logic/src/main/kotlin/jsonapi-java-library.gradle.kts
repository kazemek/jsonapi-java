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

// Per-module JaCoCo instruction/branch floors (sole numeric authority). Re-measure with
// `./gradlew jacocoTestReport`, then set each minimum to floor(measuredPercent) / 100.
// Intentional coverage drops must update this map in the same change.
data class JacocoCoverageFloors(
    val instructionMinimum: java.math.BigDecimal,
    val branchMinimum: java.math.BigDecimal,
    val includePatterns: List<String> = emptyList(),
)

// Test-support coverage is limited to executable catalog/resource/invariant types. Inert fixture
// POJO/record accessors are excluded so they do not require synthetic tests solely for JaCoCo.
// Production-module floors do not use includePatterns and remain unchanged.
val testSupportJacocoIncludes =
    listOf(
        "io/github/kazemek/jsonapi/testfixtures/FixtureCatalog.class",
        "io/github/kazemek/jsonapi/testfixtures/ImmutableFixtureCatalog.class",
        "io/github/kazemek/jsonapi/testfixtures/JsonApiFixtures.class",
        "io/github/kazemek/jsonapi/testfixtures/Scenario.class",
        "io/github/kazemek/jsonapi/testfixtures/TestSupportResources.class",
        "io/github/kazemek/jsonapi/testfixtures/codec/**",
        "io/github/kazemek/jsonapi/testfixtures/domainwrite/DomainWrite*.class",
        "io/github/kazemek/jsonapi/testfixtures/domainread/DomainRead*.class",
        "io/github/kazemek/jsonapi/testfixtures/domainread/ConverterBehavior.class",
        "io/github/kazemek/jsonapi/testfixtures/compoundwrite/CompoundWrite*.class",
        "io/github/kazemek/jsonapi/testfixtures/compoundwrite/IncludedResourceRef.class",
        "io/github/kazemek/jsonapi/testfixtures/sparsefieldset/SparseFieldset*.class",
        "io/github/kazemek/jsonapi/testfixtures/sparsefieldset/FieldsetResourceState.class",
        "io/github/kazemek/jsonapi/testfixtures/sparsefieldset/ZeroReadGuarantee.class",
        "io/github/kazemek/jsonapi/testfixtures/enveloperead/Envelope*.class",
        "io/github/kazemek/jsonapi/testfixtures/enveloperead/IncludedExpectation.class",
        "io/github/kazemek/jsonapi/testfixtures/domainpatch/PatchScenarios.class",
        "io/github/kazemek/jsonapi/testfixtures/domainpatch/PatchDtoScenarios.class",
        "io/github/kazemek/jsonapi/testfixtures/domainpatch/PatchScenario.class",
        "io/github/kazemek/jsonapi/testfixtures/domainpatch/PatchDtoScenario.class",
        "io/github/kazemek/jsonapi/testfixtures/domainpatch/PatchExpectation*.class",
        "io/github/kazemek/jsonapi/testfixtures/domainpatch/PatchDtoExpectation*.class",
    )

val jacocoCoverageFloorsByProject =
    mapOf(
        "jsonapi-java-core" to
            JacocoCoverageFloors("0.91".toBigDecimal(), "0.80".toBigDecimal()),
        "jsonapi-java-jackson-common" to
            JacocoCoverageFloors("0.83".toBigDecimal(), "0.78".toBigDecimal()),
        "jsonapi-java-jackson3" to
            JacocoCoverageFloors("0.93".toBigDecimal(), "0.81".toBigDecimal()),
        "jsonapi-java-test-support" to
            JacocoCoverageFloors(
                "0.98".toBigDecimal(),
                "0.87".toBigDecimal(),
                testSupportJacocoIncludes,
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
        if (jacocoFloors.includePatterns.isNotEmpty()) {
            val filteredClasses =
                files(
                    sourceSets.named("main").get().output.classesDirs.map { dir ->
                        fileTree(dir) {
                            include(jacocoFloors.includePatterns)
                        }
                    },
                )
            // Floor verification excludes inert fixture carriers. The published JaCoCo report stays
            // complete so Sonar still sees coverage from remaining carrier tests until KAZ-91.
            tasks.jacocoTestCoverageVerification {
                classDirectories.setFrom(filteredClasses)
            }
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
