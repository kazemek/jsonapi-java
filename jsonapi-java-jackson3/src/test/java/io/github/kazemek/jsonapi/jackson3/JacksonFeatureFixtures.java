package io.github.kazemek.jsonapi.jackson3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Jackson-feature probe models owned by {@code ResourceMappingJacksonFeaturesSpec}: custom value
 * serializers, creator-based immutable beans, inheritance hierarchies, arrays, and Optional-wrapped
 * identifiers/attributes/relationships on the write path.
 */
public final class JacksonFeatureFixtures {

  private JacksonFeatureFixtures() {}

  public static final class TitleSerializer extends ValueSerializer<FormattedTitle> {

    @Override
    public Class<FormattedTitle> handledType() {
      return FormattedTitle.class;
    }

    @Override
    public void serialize(FormattedTitle value, JsonGenerator gen, SerializationContext context)
        throws JacksonException {
      gen.writeString("[FORMATTED] " + value.text());
    }
  }

  @JsonSerialize(using = TitleSerializer.class)
  public record FormattedTitle(String text) {}

  @JsonApiResource(type = "articles")
  public record ArticleWithFormattedTitle(
      @JsonApiId String id, @JsonApiAttribute FormattedTitle title) {}

  @JsonApiResource(type = "articles")
  public record ArticleWithOptional(
      @JsonApiId String id, String title, Optional<String> subtitle) {}

  @JsonApiResource(type = "articles")
  public record ArticleWithOptionalId(@JsonApiId Optional<String> id, String title) {}

  @JsonApiResource(type = "articles")
  public record ArticleWithOptionalRelationship(
      @JsonApiId String id, @JsonApiRelationship Optional<Comment> comment) {}

  @JsonApiResource(type = "articles")
  @SuppressWarnings("ArrayRecordComponent")
  public record ArticleWithArray(
      @JsonApiId String id, String title, @JsonApiRelationship Comment[] comments) {}

  @JsonApiResource(type = "articles")
  public static final class CreatorBasedArticle {

    private final String id;
    private final String title;

    @JsonCreator
    public CreatorBasedArticle(
        @JsonProperty("id") @JsonApiId String id, @JsonProperty("title") String title) {
      this.id = id;
      this.title = title;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }
  }

  public abstract static class BaseBlog {

    protected String id;
    protected String name;

    protected BaseBlog() {}

    protected BaseBlog(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @JsonApiResource(type = "blogs")
  public static final class ExtendedBlog extends BaseBlog {

    private String description;

    public ExtendedBlog() {
      super();
    }

    public ExtendedBlog(String id, String name, String description) {
      super(id, name);
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }
}
