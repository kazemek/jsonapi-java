package io.github.kazemek.jsonapi.jackson3.architecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import spock.lang.Shared
import spock.lang.Specification

class Jackson3DependencyRulesSpec extends Specification {

  @Shared
  JavaClasses jackson3Classes = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.jackson3")

  @Shared
  JavaClasses commonClasses = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.jackson..")

  @Shared
  JavaClasses sharedFixtureClasses = new ClassFileImporter()
  .importPackages("io.github.kazemek.jsonapi.fixtures..")

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
        "io.github.kazemek.jsonapi.jackson..",
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

  def "jackson3 exposes no duplicate public common contract types"() {
    expect:
    noCommonContractRedeclarations(commonClasses).check(jackson3Classes)
  }

  def "common contract redeclaration guard rejects a representative adapter duplicate"() {
    given:
    def fixtureClasses = new ClassFileImporter()
        .importPackages("io.github.kazemek.jsonapi.jackson3.architecture.fixture")

    when:
    noCommonContractRedeclarations(commonClasses).check(fixtureClasses)

    then:
    def error = thrown(AssertionError)
    error.message.contains("DocumentEnvelope")
    error.message.contains("public Jackson API contract")
  }

  def "shared test fixtures are available through the test-fixtures variant"() {
    expect:
    sharedFixtureClasses.size() > 0
  }

  def "shared test fixtures depend only on allowed application-shaped packages"() {
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.fixtures..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "io.github.kazemek.jsonapi.annotation..",
        "io.github.kazemek.jsonapi.core.model..",
        "io.github.kazemek.jsonapi.jackson..",
        "io.github.kazemek.jsonapi.fixtures..",
        "com.fasterxml.jackson.annotation..")
        .check(sharedFixtureClasses)
  }

  def "shared test fixtures never depend on adapter majors, core.internal, or Groovy"() {
    expect:
    noClasses()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.fixtures..")
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
        .check(sharedFixtureClasses)
  }

  private static ArchRule noCommonContractRedeclarations(JavaClasses commonClasses) {
    def commonContractNames =
        commonClasses.findAll { JavaClass candidate ->
          candidate.packageName.startsWith("io.github.kazemek.jsonapi.jackson.") &&
              candidate.modifiers.contains(JavaModifier.PUBLIC) && candidate.topLevelClass
        }.collect { JavaClass candidate -> candidate.simpleName }.toSet()

    return classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.jackson3..")
        .should(notRedeclare(commonContractNames))
  }

  private static ArchCondition<JavaClass> notRedeclare(Set<String> commonContractNames) {
    return new ArchCondition<JavaClass>("not redeclare a public Jackson API contract") {
          @Override
          void check(JavaClass item, ConditionEvents events) {
            if (item.topLevelClass && commonContractNames.contains(item.simpleName)) {
              events.add(
                  SimpleConditionEvent.violated(
                  item,
                  "${item.name} redeclares the public Jackson API contract ${item.simpleName}"))
            }
          }
        }
  }
}
