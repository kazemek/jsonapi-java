package io.github.kazemek.jsonapi.annotation.architecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import spock.lang.Shared
import spock.lang.Specification

class AnnotationDependencyRulesSpec extends Specification {

  @Shared
  JavaClasses annotationClasses = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .importPackages("io.github.kazemek.jsonapi.annotation")

  def "annotation production types depend only on JDK, JSpecify annotations, and other annotation types"() {
    expect:
    classes()
        .that()
        .resideInAPackage("io.github.kazemek.jsonapi.annotation..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
        "java..",
        "org.jspecify.annotations..",
        "io.github.kazemek.jsonapi.annotation..")
        .check(annotationClasses)
  }
}
