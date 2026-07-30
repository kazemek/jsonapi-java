/**
 * Jackson 3 codecs for validating and writing JSON:API document envelopes.
 *
 * <p>Java {@code null} on model components means member absence. Explicit JSON {@code null} uses
 * sealed variants such as {@link io.github.kazemek.jsonapi.core.model.DocumentData.NullData}. Use
 * {@link JsonApiJackson3#writer} as the sole public write path; aggregate validation always runs
 * before emission.
 */
@NullMarked
package io.github.kazemek.jsonapi.jackson3;

import org.jspecify.annotations.NullMarked;
