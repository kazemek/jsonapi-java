package io.github.kazemek.jsonapi.testfixtures.codec

import spock.lang.Specification

/**
 * One-time conversion oracle for Phase 2.27: the ordered id list and per-id metadata transcribed
 * from the Groovy codec sources. Catalog specs and jackson3 adapter specs remain the semantic
 * equivalence check; this spec pins notes, capability flags, {@code toString() == id}, and catalog
 * order so a conversion drift fails in this module.
 */
class CodecFixtureConversionEquivalenceSpec extends Specification {

  private static final List<String> EXPECTED_IDS = [
    'single-resource',
    'resource-collection',
    'single-identifier',
    'identifier-collection',
    'null-data',
    'meta-only',
    'empty-identifier-collection',
    'empty-wrappers',
    'empty-errors',
    'empty-included',
    'open-values',
    'relationship-null-linkage',
    'relationship-empty-to-many',
    'relationship-link-only',
    'relationship-meta-only',
    'string-and-object-links',
    'errors-document',
    'jsonapi-object',
    'compound-document',
    'compound-nested-intermediate',
    'compound-shared-identity',
    'local-identifier',
    'extension-and-at-members',
    'member-order',
  ]

  private static final Map<String, Map> EXPECTED_METADATA = [
    'single-resource': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Single resource primary data',
    ],
    'resource-collection': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Resource collection primary data',
    ],
    'single-identifier': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Single resource identifier primary data',
    ],
    'identifier-collection': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Identifier collection primary data',
    ],
    'null-data': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Explicit data null with meta',
    ],
    'meta-only': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Absent data; meta-only document',
    ],
    'empty-identifier-collection': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Empty primary data array',
    ],
    'empty-wrappers': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Present-empty attributes, relationships, links, meta',
    ],
    'empty-errors': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Present-empty errors array',
    ],
    'empty-included': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Present-empty included array with primary data',
    ],
    'open-values': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Open JSON null, nested object/array, and numeric families in attributes/meta',
    ],
    'relationship-null-linkage': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Explicit null to-one relationship data',
    ],
    'relationship-empty-to-many': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Empty to-many relationship data array',
    ],
    'relationship-link-only': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Link-only relationship without data',
    ],
    'relationship-meta-only': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Meta-only relationship without data',
    ],
    'string-and-object-links': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: true,
      schemaKind: SchemaKind.RESPONSE, notes: 'String link, object link, null link, canonical hreflang array',
    ],
    'errors-document': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Top-level errors with source and links',
    ],
    'jsonapi-object': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'jsonapi version, ext, profile, and meta',
    ],
    'compound-document': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Compound document with included resources',
    ],
    'compound-nested-intermediate': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Compound document with nested comments.author intermediates',
    ],
    'compound-shared-identity': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Compound collection sharing one included author identity',
    ],
    'local-identifier': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.CREATE, notes: 'Resource and linkage with lid',
    ],
    'extension-and-at-members': [
      writable: true, readable: true, assertExactUtf8: false, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Extension and @ members on document and resource',
    ],
    'member-order': [
      writable: true, readable: true, assertExactUtf8: true, assertHreflangArray: false,
      schemaKind: SchemaKind.RESPONSE, notes: 'Canonical standard member order then additional members',
    ],
  ]

  def "catalog order matches the transcribed Groovy id list"() {
    expect:
    CodecFixtures.all()*.id == EXPECTED_IDS
  }

  def "per-id capability flags, schema kind, notes, and toString match the Groovy sources"() {
    expect:
    EXPECTED_IDS.every { id ->
      def fixture = CodecFixtures.byId(id)
      def expected = EXPECTED_METADATA[id]
      assert fixture.writable == expected.writable
      assert fixture.readable == expected.readable
      assert fixture.assertExactUtf8 == expected.assertExactUtf8
      assert fixture.assertHreflangArray == expected.assertHreflangArray
      assert fixture.schemaKind == expected.schemaKind
      assert fixture.notes == expected.notes
      assert fixture.toString() == id
      true
    }
  }

  def "ambiguous catalog order, notes, and toString match the Groovy sources"() {
    given:
    def expected = [
      [
        id: 'ambiguous-object-primary-data',
        notes: 'Object primary data decoding to either a resource or an identifier model',
      ],
      [
        id: 'ambiguous-empty-array-primary-data',
        notes: 'Empty-array primary data decoding to either a resource or an identifier model',
      ],
    ]

    expect:
    AmbiguousPrimaryDataCases.all()*.id == expected*.id
    AmbiguousPrimaryDataCases.all().eachWithIndex { fixture, i ->
      assert fixture.notes == expected[i].notes
      assert fixture.toString() == expected[i].id
    }
  }
}
