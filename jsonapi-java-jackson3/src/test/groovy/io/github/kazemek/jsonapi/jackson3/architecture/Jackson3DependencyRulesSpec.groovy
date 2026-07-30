package io.github.kazemek.jsonapi.jackson3.architecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import spock.lang.Shared
import spock.lang.Specification

class Jackson3DependencyRulesSpec extends Specification {

  @Shared
  JavaClasses jackson3Classes = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.jackson3")

  def "jackson3 production types depend only on allowed packages"() {
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.jackson3..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "io.github.kazemek.jsonapi.core.model..",
        "io.github.kazemek.jsonapi.core.validation..",
        "io.github.kazemek.jsonapi.annotation..",
        "io.github.kazemek.jsonapi.jackson3..",
        "tools.jackson..")
        .check(jackson3Classes)
  }

  def "jackson3 production types never depend on core.internal or Jackson 2"() {
    expect:
    noClasses()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.jackson3..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
        "io.github.kazemek.jsonapi.core.internal..",
        "com.fasterxml.jackson..")
        .check(jackson3Classes)
  }
}
