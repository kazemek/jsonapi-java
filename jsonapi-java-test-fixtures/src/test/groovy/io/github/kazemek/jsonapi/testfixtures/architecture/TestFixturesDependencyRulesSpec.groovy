package io.github.kazemek.jsonapi.testfixtures.architecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import spock.lang.Shared
import spock.lang.Specification

class TestFixturesDependencyRulesSpec extends Specification {

  @Shared
  JavaClasses testFixturesClasses = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.testfixtures")

  def "test-fixtures production types depend only on allowed packages"() {
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.testfixtures..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "groovy..",
        "org.codehaus.groovy..",
        "io.github.kazemek.jsonapi.annotation..",
        "io.github.kazemek.jsonapi.core.model..",
        "io.github.kazemek.jsonapi.core.validation..",
        "io.github.kazemek.jsonapi.jackson..",
        "io.github.kazemek.jsonapi.testfixtures..",
        "com.fasterxml.jackson.annotation..")
        .check(testFixturesClasses)
  }

  def "test-fixtures production types never depend on Jackson databind, adapter majors, or core.internal"() {
    expect:
    noClasses()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.testfixtures..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
        "tools.jackson..",
        "com.fasterxml.jackson.databind..",
        "io.github.kazemek.jsonapi.jackson2..",
        "io.github.kazemek.jsonapi.jackson3..",
        "io.github.kazemek.jsonapi.core.internal..")
        .check(testFixturesClasses)
  }
}
