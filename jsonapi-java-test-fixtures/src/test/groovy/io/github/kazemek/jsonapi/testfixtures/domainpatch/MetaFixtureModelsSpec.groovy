package io.github.kazemek.jsonapi.testfixtures.domainpatch

import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatMetaArticle
import java.util.Optional
import spock.lang.Specification

// Why this spec exists: catalog integrity only inspects scenario structure, and Jackson adapter
// suites do not contribute JaCoCo coverage to this module. These tests pin the meta fixture
// models' record surface so new-code coverage on the shared meta models stays above the Quality
// Gate (ADR-015).
class MetaFixtureModelsSpec extends Specification {

  def "whole-meta value records expose accessors and equality"() {
    expect:
    def articleMeta = new ArticleMeta("cms", "n")
    articleMeta.source() == "cms"
    articleMeta.note() == "n"
    articleMeta == new ArticleMeta("cms", "n")
    articleMeta.hashCode() == new ArticleMeta("cms", "n").hashCode()
    articleMeta != new ArticleMeta("cms", "other")
    articleMeta != null

    def authorMeta = new AuthorMeta("Alice")
    authorMeta.displayName() == "Alice"
    authorMeta == new AuthorMeta("Alice")

    def patch = new ArticleMetaPatch(PatchPresence.present("cms"), PatchPresence.omitted())
    patch.source() == PatchPresence.present("cms")
    patch.note() == PatchPresence.omitted()
    patch == new ArticleMetaPatch(PatchPresence.present("cms"), PatchPresence.omitted())
  }

  def "ordinary meta domain models expose their record accessors"() {
    expect:
    def article = new ArticleWithMeta(
        "1", "T", null, new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))
    article.id() == "1"
    article.title() == "T"
    article.meta() == new ArticleMeta("cms", "n")
    article.authorMeta() == new AuthorMeta("Alice")

    new ArticleWithOptionalMeta(
        "1", "T", null, Optional.of(new ArticleMeta("cms", "n")), Optional.empty())
        .meta().get().source() == "cms"

    new ArticleWithMapMeta("1", "T", null, [source: "cms"], [displayName: "Alice"])
    .authorMeta() == [displayName: "Alice"]
  }

  def "typed meta PATCH DTOs expose their record accessors"() {
    expect:
    def patch = new ArticleWithMetaPatch(
        "1",
        PatchPresence.present("T"),
        PatchPresence.omitted(),
        PatchPresence.present(new ArticleMetaPatch(PatchPresence.present("cms"), PatchPresence.omitted())),
        PatchPresence.present(new AuthorMeta("Alice")))
    patch.id() == "1"
    patch.title() == PatchPresence.present("T")
    patch.meta() ==
        PatchPresence.present(new ArticleMetaPatch(PatchPresence.present("cms"), PatchPresence.omitted()))
    patch.authorMeta() == PatchPresence.present(new AuthorMeta("Alice"))

    new ArticleWithMapMetaPatch(
        "1", PatchPresence.present("T"), PatchPresence.omitted(),
        PatchPresence.present([source: "cms"]), PatchPresence.present([displayName: "Alice"]))
        .meta() == PatchPresence.present([source: "cms"])

    new ArticleWithOptionalMetaPatch(
        "1", PatchPresence.present("T"), PatchPresence.omitted(),
        PatchPresence.present(Optional.of(new ArticleMeta("cms", "n"))))
        .meta() == PatchPresence.present(Optional.of(new ArticleMeta("cms", "n")))
  }

  def "read-side meta DTO exposes its record accessors"() {
    expect:
    def dto = new FlatMetaArticle("1", "T", null, new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))
    dto.id() == "1"
    dto.meta() == new ArticleMeta("cms", "n")
    dto.authorMeta() == new AuthorMeta("Alice")
  }
}
