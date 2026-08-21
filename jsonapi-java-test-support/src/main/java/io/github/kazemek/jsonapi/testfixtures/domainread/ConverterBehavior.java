package io.github.kazemek.jsonapi.testfixtures.domainread;

/**
 * Version-neutral identifier-converter behavior a binder scenario exercises. Adapter suites map
 * each value onto their own converter registration; they do not interpret scenario ids.
 *
 * <p>{@link #DEFAULT_CONVERT_VALUE} covers both successful {@code convertValue} coercion and the
 * identifier-coercion failure case, which is distinguished by a failure expectation rather than a
 * separate discriminator value.
 */
public enum ConverterBehavior {
  DEFAULT_CONVERT_VALUE,
  CUSTOM_PARSE_INVERSION,
  PARSE_THROWING,
  PARSE_RETURNING_NULL
}
