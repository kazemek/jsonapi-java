package io.github.kazemek.jsonapi.core.architecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import spock.lang.Shared
import spock.lang.Specification

class CoreDependencyRulesSpec extends Specification {

  @Shared
  JavaClasses coreClasses = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.core")

  def "core production types depend only on JDK, JSpecify annotations, and other core types"() {
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.core..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "io.github.kazemek.jsonapi.core..")
        .check(coreClasses)
  }
}
