package io.github.kazemek.jsonapi.jackson.architecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import spock.lang.Shared
import spock.lang.Specification

class JacksonCommonDependencyRulesSpec extends Specification {

  @Shared
  JavaClasses commonClasses = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.jackson..")

  def "common contract production types depend only on allowed packages"() {
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.jackson..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "io.github.kazemek.jsonapi.core.model..",
        "io.github.kazemek.jsonapi.core.validation..",
        "io.github.kazemek.jsonapi.jackson..")
        .check(commonClasses)
  }

  def "common contract production types never depend on Jackson majors, adapters, or core.internal"() {
    expect:
    noClasses()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.jackson..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
        "io.github.kazemek.jsonapi.core.internal..",
        "io.github.kazemek.jsonapi.jackson2..",
        "io.github.kazemek.jsonapi.jackson3..",
        "tools.jackson..",
        "com.fasterxml.jackson..")
        .check(commonClasses)
  }
}
