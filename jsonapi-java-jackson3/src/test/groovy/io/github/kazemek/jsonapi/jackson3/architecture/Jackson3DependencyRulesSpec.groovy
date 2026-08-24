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
  .importPackages("io.github.kazemek.jsonapi.jackson")

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
    error.message.contains("public Jackson-common contract")
  }

  private static ArchRule noCommonContractRedeclarations(JavaClasses commonClasses) {
    def commonContractNames =
        commonClasses.findAll { JavaClass candidate ->
          candidate.packageName == "io.github.kazemek.jsonapi.jackson" &&
              candidate.modifiers.contains(JavaModifier.PUBLIC) && candidate.topLevelClass
        }.collect { JavaClass candidate -> candidate.simpleName }.toSet()

    return classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.jackson3..")
        .should(notRedeclare(commonContractNames))
  }

  private static ArchCondition<JavaClass> notRedeclare(Set<String> commonContractNames) {
    return new ArchCondition<JavaClass>("not redeclare a public Jackson-common contract") {
          @Override
          void check(JavaClass item, ConditionEvents events) {
            if (commonContractNames.contains(item.simpleName)) {
              events.add(
                  SimpleConditionEvent.violated(
                  item,
                  "${item.name} redeclares the public Jackson-common contract ${item.simpleName}"))
            }
          }
        }
  }
}
