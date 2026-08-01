package io.github.kazemek.jsonapi.jackson3.testmodel;

import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = TitleSerializer.class)
public record FormattedTitle(String text) {}
