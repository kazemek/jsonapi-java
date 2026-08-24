package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Shared direct typed PATCH DTO with a nested container-inner member (atomic boundary). */
@JsonApiResource(type = "articles")
public record ArticleWithAddressTagsPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<AddressWithTagsPatch> address) {}
