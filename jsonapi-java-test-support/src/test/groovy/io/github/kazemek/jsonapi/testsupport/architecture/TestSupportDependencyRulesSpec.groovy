package io.github.kazemek.jsonapi.testsupport.architecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import spock.lang.Shared
import spock.lang.Specification

class TestSupportDependencyRulesSpec extends Specification {

  @Shared
  JavaClasses testSupportClasses = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.testsupport")

  def "test-support production types depend only on allowed packages"() {
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.testsupport..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "jakarta.json..",
        "org.eclipse.parsson..",
        "io.github.kazemek.jsonapi.annotation..",
        "io.github.kazemek.jsonapi.core.model..",
        "io.github.kazemek.jsonapi.core.validation..",
        "io.github.kazemek.jsonapi.jackson..",
        "io.github.kazemek.jsonapi.testsupport..",
        "com.fasterxml.jackson.annotation..")
        .check(testSupportClasses)
  }

  def "test-support production types never depend on Jackson databind, adapter majors, core.internal, or Groovy"() {
    expect:
    noClasses()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.testsupport..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
        "tools.jackson..",
        "com.fasterxml.jackson.databind..",
        "io.github.kazemek.jsonapi.jackson2..",
        "io.github.kazemek.jsonapi.jackson3..",
        "io.github.kazemek.jsonapi.core.internal..",
        "groovy..",
        "org.codehaus.groovy..")
        .check(testSupportClasses)
  }

  def "passive fixture carriers depend only on application-shaped packages"() {
    // The fixtures hierarchy is the coverage-exempt passive-carrier boundary. Allowing only
    // application-shaped dependencies keeps executable support logic out structurally: scenario
    // descriptors, catalogs (FixtureCatalog / *Scenarios), resource loaders
    // (TestSupportResources), and invariant services live outside this allowlist, so support code
    // cannot hide under fixtures to escape coverage.
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.testsupport.fixtures..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "io.github.kazemek.jsonapi.annotation..",
        "io.github.kazemek.jsonapi.core.model..",
        "io.github.kazemek.jsonapi.jackson..",
        "io.github.kazemek.jsonapi.testsupport.fixtures..",
        "com.fasterxml.jackson.annotation..")
        .check(testSupportClasses)
  }
}
