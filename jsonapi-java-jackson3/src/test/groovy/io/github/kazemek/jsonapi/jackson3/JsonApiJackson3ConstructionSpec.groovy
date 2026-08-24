package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.DocumentReadContext
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import java.lang.reflect.Modifier
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class JsonApiJackson3ConstructionSpec extends Specification {

  def "public facade factories use configured mapper instances rather than builders"() {
    given:
    def factories = JsonApiJackson3.declaredMethods.findAll {
      Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers)
    }

    expect:
    !factories.isEmpty()
    factories.every { it.parameterTypes && it.parameterTypes[0] == JsonMapper }
    factories.every { !it.parameterTypes.contains(JsonMapper.Builder) }
  }

  def "each capability has a mapper-instance canonical factory form"() {
    expect:
    canonicalFactories().every { expected ->
      JsonApiJackson3.declaredMethods.any { method ->
        method.name == expected.name &&
            method.returnType == expected.returnType &&
            method.parameterTypes.toList() == expected.parameters
      }
    }
  }

  def "capability instances are constructed through the facade"() {
    expect:
    capabilityTypes().every { capability ->
      capability.declaredConstructors.every { !Modifier.isPublic(it.modifiers) }
    }
  }

  private static List<FactoryShape> canonicalFactories() {
    [
      new FactoryShape('writer', JsonApiDocumentWriter, [JsonMapper, ValidationContext]),
      new FactoryShape('reader', JsonApiDocumentReader, [
        JsonMapper,
        DocumentReadContext
      ]),
      new FactoryShape('resourceMapper', JsonApiResourceMapper,
      [
        JsonMapper,
        IdentifierConverter
      ]),
      new FactoryShape('resourceBinder', JsonApiResourceBinder,
      [
        JsonMapper,
        IdentifierConverter,
        Map
      ]),
      new FactoryShape('domainDocumentReader', JsonApiDomainDocumentReader,
      [
        JsonMapper,
        DocumentReadContext,
        ResourceTypeRegistry,
        IdentifierConverter,
        Map
      ]),
      new FactoryShape('patchReader', JsonApiPatchReader,
      [
        JsonMapper,
        ValidationContext,
        IdentifierConverter,
        Map
      ]),
      new FactoryShape('patchDtoReader', JsonApiPatchDtoReader,
      [
        JsonMapper,
        ValidationContext,
        IdentifierConverter,
        Map
      ]),
    ]
  }

  private static List<Class<?>> capabilityTypes() {
    [
      JsonApiDocumentWriter,
      JsonApiDocumentReader,
      JsonApiResourceMapper,
      JsonApiResourceBinder,
      JsonApiDomainDocumentReader,
      JsonApiPatchReader,
      JsonApiPatchDtoReader,
    ]
  }

  private static final class FactoryShape {
    final String name
    final Class<?> returnType
    final List<Class<?>> parameters

    FactoryShape(String name, Class<?> returnType, List<Class<?>> parameters) {
      this.name = name
      this.returnType = returnType
      this.parameters = parameters
    }
  }
}
