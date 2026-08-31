package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "blogs")
public record BlogWithJsonProperty(
    @JsonApiId @JsonProperty("blog_id") String id,
    @JsonApiAttribute @JsonProperty("blog_title") String title) {}
