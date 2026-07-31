/**
 * Jackson 3 codecs for validating, writing, and reading JSON:API document envelopes.
 *
 * <p>Java {@code null} on model components means member absence. Explicit JSON {@code null} uses
 * sealed variants such as {@link io.github.kazemek.jsonapi.core.model.DocumentData.NullData}. Use
 * {@link JsonApiJackson3#writer} and {@link JsonApiJackson3#reader} as the sole public codec paths;
 * writers validate before emission, and readers validate before returning a document.
 */
@NullMarked
package io.github.kazemek.jsonapi.jackson3;

import org.jspecify.annotations.NullMarked;
