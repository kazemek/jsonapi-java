package io.github.kazemek.jsonapi.core.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/** Syntax validation for URI references, link relations, language tags, and media types. */
public final class SyntaxValidators {

  // Possessive quantifiers avoid nested-repetition stack overflow (java:S5998).
  // Primary language subtag must be 2-8 alpha (rejects single-letter tags like "a").
  private static final Pattern LANGUAGE_TAG =
      Pattern.compile("^[a-zA-Z]{2,8}+(?:-[a-zA-Z0-9]{1,8}+)*+$");

  private static final Pattern MEDIA_TYPE_TOKEN = Pattern.compile("^[a-zA-Z0-9!#$&^_.+-]++$");

  private SyntaxValidators() {}

  public static boolean isValidUriReference(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    try {
      new URI(value);
      return true;
    } catch (URISyntaxException ex) {
      return false;
    }
  }

  public static boolean isValidLinkRelation(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    if (MemberNames.isValid(value)) {
      return true;
    }
    try {
      return new URI(value).isAbsolute();
    } catch (URISyntaxException ex) {
      return false;
    }
  }

  public static boolean isValidLanguageTag(String value) {
    return value != null && LANGUAGE_TAG.matcher(value).matches();
  }

  public static boolean isValidMediaType(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    String[] parts = value.split(";", -1);
    String typeSubtype = parts[0].trim();
    int slash = typeSubtype.indexOf('/');
    if (slash <= 0 || slash == typeSubtype.length() - 1) {
      return false;
    }
    String type = typeSubtype.substring(0, slash).trim();
    String subtype = typeSubtype.substring(slash + 1).trim();
    if (!MEDIA_TYPE_TOKEN.matcher(type).matches() || !MEDIA_TYPE_TOKEN.matcher(subtype).matches()) {
      return false;
    }
    for (int i = 1; i < parts.length; i++) {
      if (!isValidMediaTypeParameter(parts[i].trim())) {
        return false;
      }
    }
    return true;
  }

  public static boolean isValidExtensionOrProfileUri(String value) {
    return isValidUriReference(value);
  }

  private static boolean isValidMediaTypeParameter(String parameter) {
    if (parameter.isEmpty()) {
      return false;
    }
    int eq = parameter.indexOf('=');
    if (eq <= 0) {
      return false;
    }
    String name = parameter.substring(0, eq).trim();
    String rawValue = parameter.substring(eq + 1).trim();
    if (!MEDIA_TYPE_TOKEN.matcher(name).matches() || rawValue.isEmpty()) {
      return false;
    }
    if (rawValue.charAt(0) == '"') {
      return rawValue.length() >= 2 && rawValue.charAt(rawValue.length() - 1) == '"';
    }
    return MEDIA_TYPE_TOKEN.matcher(rawValue).matches();
  }
}
