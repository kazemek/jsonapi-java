package io.github.kazemek.jsonapi.testfixtures.writer.cases

import java.math.BigDecimal
import java.math.BigInteger

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class OpenValuesCase {
  private OpenValuesCase() {}

  static WriterFixture fixture() {
    def attributes = Attributes.ofAttributes([
      nullable: null,
      nested: [
        tags: ['a', 'b'],
        counts: [views: 2],
      ],
      intValue: Integer.valueOf(42),
      longValue: Long.valueOf(9007199254740991L),
      floatValue: Float.valueOf(1.5f),
      doubleValue: Double.valueOf(2.25d),
      bigIntValue: new BigInteger('123456789012345678901234567890'),
      bigDecimalValue: new BigDecimal('1234567890.123456789'),
    ])
    def article = Models.resource('articles', '1', attributes: attributes)
    return WriterFixture.of(
        id: 'open-values',
        notes: 'Open JSON null, nested object/array, and numeric families in attributes/meta',
        expectedPath: 'documents/open-values.json',
        document: new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        Meta.of([
          flag: true,
          nullMeta: null,
        ]),
        null,
        null,
        null,
        [:]))
  }
}
