package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.ErrorObject;
import io.github.kazemek.jsonapi.core.model.ErrorSource;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.JsonApiMembers;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonGenerator;

/**
 * Emits JSON:API document and nested model values with deterministic member order and wire-state
 * preservation (absence vs explicit null, flat wrappers, sealed variants).
 */
final class JsonApiWireWriter {

  private JsonApiWireWriter() {}

  static void writeDocument(JsonApiDocument document, JsonGenerator gen) {
    gen.writeStartObject();
    if (document.hasDataMember()) {
      gen.writeName(JsonApiMembers.DATA);
      writeDocumentData(document.data(), gen);
    }
    if (document.hasErrorsMember()) {
      gen.writeName(JsonApiMembers.ERRORS);
      writeErrorObjects(document.errors(), gen);
    }
    if (document.meta() != null) {
      gen.writeName(JsonApiMembers.META);
      writeMeta(document.meta(), gen);
    }
    if (document.jsonapi() != null) {
      gen.writeName(JsonApiMembers.JSONAPI);
      writeJsonApiObject(document.jsonapi(), gen);
    }
    if (document.links() != null) {
      gen.writeName(JsonApiMembers.LINKS);
      writeLinks(document.links(), gen);
    }
    if (document.hasIncludedMember()) {
      gen.writeName(JsonApiMembers.INCLUDED);
      writeResourceObjects(document.included(), gen);
    }
    writeAdditionalMembers(document.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeDocumentData(@Nullable DocumentData data, JsonGenerator gen) {
    switch (data) {
      case null -> gen.writeNull();
      case DocumentData.NullData ignored -> gen.writeNull();
      case DocumentData.SingleResource single -> writeResourceObject(single.resource(), gen);
      case DocumentData.ResourceCollection collection ->
          writeResourceObjects(collection.resources(), gen);
      case DocumentData.SingleIdentifier single ->
          writeResourceIdentifier(single.identifier(), gen);
      case DocumentData.IdentifierCollection collection ->
          writeResourceIdentifiers(collection.identifiers(), gen);
    }
  }

  static void writeResourceObjects(@Nullable List<ResourceObject> resources, JsonGenerator gen) {
    gen.writeStartArray();
    if (resources != null) {
      for (ResourceObject resource : resources) {
        writeResourceObject(resource, gen);
      }
    }
    gen.writeEndArray();
  }

  static void writeResourceObject(ResourceObject resource, JsonGenerator gen) {
    gen.writeStartObject();
    gen.writeStringProperty(JsonApiMembers.TYPE, resource.type());
    if (resource.id() != null) {
      gen.writeStringProperty(JsonApiMembers.ID, resource.id());
    }
    if (resource.lid() != null) {
      gen.writeStringProperty(JsonApiMembers.LID, resource.lid());
    }
    if (resource.attributes() != null) {
      gen.writeName(JsonApiMembers.ATTRIBUTES);
      writeAttributes(resource.attributes(), gen);
    }
    if (resource.relationships() != null) {
      gen.writeName(JsonApiMembers.RELATIONSHIPS);
      writeRelationships(resource.relationships(), gen);
    }
    if (resource.links() != null) {
      gen.writeName(JsonApiMembers.LINKS);
      writeLinks(resource.links(), gen);
    }
    if (resource.meta() != null) {
      gen.writeName(JsonApiMembers.META);
      writeMeta(resource.meta(), gen);
    }
    writeAdditionalMembers(resource.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeResourceIdentifiers(List<ResourceIdentifier> identifiers, JsonGenerator gen) {
    gen.writeStartArray();
    for (ResourceIdentifier identifier : identifiers) {
      writeResourceIdentifier(identifier, gen);
    }
    gen.writeEndArray();
  }

  static void writeResourceIdentifier(ResourceIdentifier identifier, JsonGenerator gen) {
    gen.writeStartObject();
    gen.writeStringProperty(JsonApiMembers.TYPE, identifier.type());
    if (identifier.id() != null) {
      gen.writeStringProperty(JsonApiMembers.ID, identifier.id());
    }
    if (identifier.lid() != null) {
      gen.writeStringProperty(JsonApiMembers.LID, identifier.lid());
    }
    if (identifier.meta() != null) {
      gen.writeName(JsonApiMembers.META);
      writeMeta(identifier.meta(), gen);
    }
    writeAdditionalMembers(identifier.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeAttributes(Attributes attributes, JsonGenerator gen) {
    writeOpenObject(attributes.flatten(), gen);
  }

  static void writeRelationships(Relationships relationships, JsonGenerator gen) {
    gen.writeStartObject();
    for (Map.Entry<String, @Nullable Object> entry : relationships.flatten().entrySet()) {
      gen.writeName(entry.getKey());
      Object value = entry.getValue();
      if (value instanceof Relationship relationship) {
        writeRelationship(relationship, gen);
      } else {
        writeOpenValue(value, gen);
      }
    }
    gen.writeEndObject();
  }

  static void writeRelationship(Relationship relationship, JsonGenerator gen) {
    gen.writeStartObject();
    if (relationship.hasDataMember()) {
      gen.writeName(JsonApiMembers.DATA);
      writeRelationshipData(relationship.data(), gen);
    }
    if (relationship.links() != null) {
      gen.writeName(JsonApiMembers.LINKS);
      writeLinks(relationship.links(), gen);
    }
    if (relationship.meta() != null) {
      gen.writeName(JsonApiMembers.META);
      writeMeta(relationship.meta(), gen);
    }
    writeAdditionalMembers(relationship.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeRelationshipData(@Nullable RelationshipData data, JsonGenerator gen) {
    switch (data) {
      case null -> gen.writeNull();
      case RelationshipData.NullLinkage ignored -> gen.writeNull();
      case RelationshipData.SingleLinkage single ->
          writeResourceIdentifier(single.identifier(), gen);
      case RelationshipData.IdentifierCollectionLinkage collection ->
          writeResourceIdentifiers(collection.identifiers(), gen);
    }
  }

  static void writeLinks(Links links, JsonGenerator gen) {
    gen.writeStartObject();
    for (Map.Entry<String, @Nullable Object> entry : links.flatten().entrySet()) {
      gen.writeName(entry.getKey());
      switch (entry.getValue()) {
        case null -> writeLink(null, gen);
        case Link link -> writeLink(link, gen);
        case Object other -> writeOpenValue(other, gen);
      }
    }
    gen.writeEndObject();
  }

  static void writeLink(@Nullable Link link, JsonGenerator gen) {
    switch (link) {
      case null -> gen.writeNull();
      case Link.StringLink stringLink -> gen.writeString(stringLink.href());
      case Link.ObjectLink objectLink -> writeObjectLink(objectLink, gen);
    }
  }

  static void writeObjectLink(Link.ObjectLink link, JsonGenerator gen) {
    gen.writeStartObject();
    gen.writeStringProperty(JsonApiMembers.HREF, link.href());
    if (link.rel() != null) {
      gen.writeStringProperty(JsonApiMembers.REL, link.rel());
    }
    if (link.describedby() != null) {
      gen.writeStringProperty(JsonApiMembers.DESCRIBEDBY, link.describedby());
    }
    if (link.title() != null) {
      gen.writeStringProperty(JsonApiMembers.TITLE, link.title());
    }
    if (link.type() != null) {
      gen.writeStringProperty(JsonApiMembers.TYPE, link.type());
    }
    if (link.hreflang() != null) {
      gen.writeName(JsonApiMembers.HREFLANG);
      gen.writeStartArray();
      for (String tag : link.hreflang()) {
        gen.writeString(tag);
      }
      gen.writeEndArray();
    }
    if (link.meta() != null) {
      gen.writeName(JsonApiMembers.META);
      writeMeta(link.meta(), gen);
    }
    writeAdditionalMembers(link.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeMeta(Meta meta, JsonGenerator gen) {
    writeOpenObject(meta.members(), gen);
  }

  static void writeJsonApiObject(JsonApiObject jsonapi, JsonGenerator gen) {
    gen.writeStartObject();
    if (jsonapi.version() != null) {
      gen.writeStringProperty(JsonApiMembers.VERSION, jsonapi.version());
    }
    if (jsonapi.ext() != null) {
      gen.writeName(JsonApiMembers.EXT);
      writeStringArray(jsonapi.ext(), gen);
    }
    if (jsonapi.profile() != null) {
      gen.writeName(JsonApiMembers.PROFILE);
      writeStringArray(jsonapi.profile(), gen);
    }
    if (jsonapi.meta() != null) {
      gen.writeName(JsonApiMembers.META);
      writeMeta(jsonapi.meta(), gen);
    }
    writeAdditionalMembers(jsonapi.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeErrorObjects(@Nullable List<ErrorObject> errors, JsonGenerator gen) {
    gen.writeStartArray();
    if (errors != null) {
      for (ErrorObject error : errors) {
        writeErrorObject(error, gen);
      }
    }
    gen.writeEndArray();
  }

  static void writeErrorObject(ErrorObject error, JsonGenerator gen) {
    gen.writeStartObject();
    if (error.id() != null) {
      gen.writeStringProperty(JsonApiMembers.ID, error.id());
    }
    if (error.links() != null) {
      gen.writeName(JsonApiMembers.LINKS);
      writeLinks(error.links(), gen);
    }
    if (error.status() != null) {
      gen.writeStringProperty(JsonApiMembers.STATUS, error.status());
    }
    if (error.code() != null) {
      gen.writeStringProperty(JsonApiMembers.CODE, error.code());
    }
    if (error.title() != null) {
      gen.writeStringProperty(JsonApiMembers.TITLE, error.title());
    }
    if (error.detail() != null) {
      gen.writeStringProperty(JsonApiMembers.DETAIL, error.detail());
    }
    if (error.source() != null) {
      gen.writeName(JsonApiMembers.SOURCE);
      writeErrorSource(error.source(), gen);
    }
    if (error.meta() != null) {
      gen.writeName(JsonApiMembers.META);
      writeMeta(error.meta(), gen);
    }
    writeAdditionalMembers(error.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeErrorSource(ErrorSource source, JsonGenerator gen) {
    gen.writeStartObject();
    if (source.pointer() != null) {
      gen.writeStringProperty(JsonApiMembers.POINTER, source.pointer());
    }
    if (source.parameter() != null) {
      gen.writeStringProperty(JsonApiMembers.PARAMETER, source.parameter());
    }
    if (source.header() != null) {
      gen.writeStringProperty(JsonApiMembers.HEADER, source.header());
    }
    writeAdditionalMembers(source.additionalMembers(), gen);
    gen.writeEndObject();
  }

  static void writeAdditionalMembers(Map<String, @Nullable Object> members, JsonGenerator gen) {
    for (Map.Entry<String, @Nullable Object> entry : members.entrySet()) {
      gen.writeName(entry.getKey());
      writeOpenValue(entry.getValue(), gen);
    }
  }

  static void writeOpenObject(Map<String, @Nullable Object> members, JsonGenerator gen) {
    gen.writeStartObject();
    for (Map.Entry<String, @Nullable Object> entry : members.entrySet()) {
      gen.writeName(entry.getKey());
      writeOpenValue(entry.getValue(), gen);
    }
    gen.writeEndObject();
  }

  static void writeOpenValue(@Nullable Object value, JsonGenerator gen) {
    switch (value) {
      case null -> gen.writeNull();
      case String s -> gen.writeString(s);
      case Boolean b -> gen.writeBoolean(b);
      case Number n -> writeNumber(n, gen);
      case List<?> list -> {
        gen.writeStartArray();
        for (Object element : list) {
          writeOpenValue(element, gen);
        }
        gen.writeEndArray();
      }
      case Map<?, ?> map -> {
        gen.writeStartObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          gen.writeName(String.valueOf(entry.getKey()));
          writeOpenValue(entry.getValue(), gen);
        }
        gen.writeEndObject();
      }
      default ->
          throw new IllegalArgumentException(
              "Unsupported open JSON value type: " + value.getClass().getName());
    }
  }

  private static void writeNumber(Number number, JsonGenerator gen) {
    switch (number) {
      case BigDecimal bigDecimal -> gen.writeNumber(bigDecimal);
      case BigInteger bigInteger -> gen.writeNumber(bigInteger);
      case Double d -> gen.writeNumber(d);
      case Float f -> gen.writeNumber(f);
      case Long l -> gen.writeNumber(l);
      case Integer i -> gen.writeNumber(i);
      case Short s -> gen.writeNumber(s);
      case Byte b -> gen.writeNumber(b.shortValue());
      default -> gen.writeNumber(number.toString());
    }
  }

  private static void writeStringArray(List<String> values, JsonGenerator gen) {
    gen.writeStartArray();
    for (String value : values) {
      gen.writeString(value);
    }
    gen.writeEndArray();
  }
}
