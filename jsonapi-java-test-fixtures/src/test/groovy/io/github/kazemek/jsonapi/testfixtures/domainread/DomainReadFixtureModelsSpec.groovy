package io.github.kazemek.jsonapi.testfixtures.domainread

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Comment
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person
import spock.lang.Specification

// Why this spec exists: catalog integrity only inspects scenario structure. The POJO DTOs' bean
// surface, equals/hashCode, and creator-rejection path are not executed there, and Jackson adapter
// suites do not contribute JaCoCo coverage to this module. These tests pin that local surface so
// new-code coverage on the shared models stays above the Quality Gate.
class DomainReadFixtureModelsSpec extends Specification {

  def "mutable article exposes its bean surface"() {
    given:
    def article = new FlatMutableArticle()
    def author = ResourceIdentifier.of("people", "p1")

    when:
    article.setId("1")
    article.setTitle("Hello")
    article.setAuthor(author)

    then:
    article.getId() == "1"
    article.getTitle() == "Hello"
    article.getAuthor() == author
  }

  def "mutable article equals and hashCode compare all fields"() {
    given:
    def author = ResourceIdentifier.of("people", "p1")
    def article = new FlatMutableArticle("1", "Hello", author)

    expect:
    article == article
    article == new FlatMutableArticle("1", "Hello", author)
    article.hashCode() == new FlatMutableArticle("1", "Hello", author).hashCode()
    article != new FlatMutableArticle("2", "Hello", author)
    article != new FlatMutableArticle("1", "Other", author)
    article != new FlatMutableArticle("1", "Hello", ResourceIdentifier.of("people", "p2"))
    article != "not an article"
    article != null
  }

  def "blog base exposes its bean surface"() {
    given:
    def blog = new FlatBlogBase()

    when:
    blog.setId("b1")
    blog.setName("My Blog")

    then:
    blog.getId() == "b1"
    blog.getName() == "My Blog"
  }

  def "blog base equals and hashCode compare all fields"() {
    given:
    def blog = new FlatBlogBase("b1", "My Blog")

    expect:
    blog == blog
    blog == new FlatBlogBase("b1", "My Blog")
    blog.hashCode() == new FlatBlogBase("b1", "My Blog").hashCode()
    blog != new FlatBlogBase("b2", "My Blog")
    blog != new FlatBlogBase("b1", "Other")
    blog != new FlatInheritedBlog("b1", "My Blog", null)
    blog != null
  }

  def "inherited blog exposes its bean surface"() {
    given:
    def blog = new FlatInheritedBlog()

    when:
    blog.setId("b1")
    blog.setName("My Blog")
    blog.setDescription("A description")

    then:
    blog.getId() == "b1"
    blog.getName() == "My Blog"
    blog.getDescription() == "A description"
  }

  def "inherited blog equals and hashCode compare all fields"() {
    given:
    def blog = new FlatInheritedBlog("b1", "My Blog", "A description")

    expect:
    blog == blog
    blog == new FlatInheritedBlog("b1", "My Blog", "A description")
    blog.hashCode() == new FlatInheritedBlog("b1", "My Blog", "A description").hashCode()
    blog != new FlatInheritedBlog("b2", "My Blog", "A description")
    blog != new FlatInheritedBlog("b1", "Other", "A description")
    blog != new FlatInheritedBlog("b1", "My Blog", "Other")
    blog != new FlatBlogBase("b1", "My Blog")
    blog != null
  }

  def "comment article exposes its bean surface"() {
    given:
    def article = new FlatCommentArticle()
    def comments = List.of(new Comment("c1", "Hi", null))

    when:
    article.setId("1")
    article.setComments(comments)

    then:
    article.getId() == "1"
    article.getComments() == comments
  }

  def "comment article equals and hashCode compare all fields"() {
    given:
    def comments = List.of(new Comment("c1", "Hi", null))
    def article = new FlatCommentArticle("1", comments)

    expect:
    article == article
    article == new FlatCommentArticle("1", comments)
    article.hashCode() == new FlatCommentArticle("1", comments).hashCode()
    article != new FlatCommentArticle("2", comments)
    article != new FlatCommentArticle("1", List.of())
    article != "not an article"
    article != null
  }

  def "person article exposes its bean surface"() {
    given:
    def article = new FlatPersonArticle()
    def author = new Person("p1", "Alice")

    when:
    article.setId("1")
    article.setAuthor(author)

    then:
    article.getId() == "1"
    article.getAuthor() == author
  }

  def "person article equals and hashCode compare all fields"() {
    given:
    def author = new Person("p1", "Alice")
    def article = new FlatPersonArticle("1", author)

    expect:
    article == article
    article == new FlatPersonArticle("1", author)
    article.hashCode() == new FlatPersonArticle("1", author).hashCode()
    article != new FlatPersonArticle("2", author)
    article != new FlatPersonArticle("1", new Person("p2", "Bob"))
    article != "not an article"
    article != null
  }

  def "defaulted article exposes its bean surface"() {
    given:
    def article = new FlatDefaultedArticle()

    expect:
    article.getTitle() == "default"
    article.getBody() == "default"

    when:
    article.setId("1")
    article.setTitle("Hello")
    article.setBody("Content")

    then:
    article.getId() == "1"
    article.getTitle() == "Hello"
    article.getBody() == "Content"
  }

  def "defaulted article equals and hashCode compare all fields"() {
    given:
    def article = new FlatDefaultedArticle("1", "Hello", "Content")

    expect:
    article == article
    article == new FlatDefaultedArticle("1", "Hello", "Content")
    article.hashCode() == new FlatDefaultedArticle("1", "Hello", "Content").hashCode()
    article != new FlatDefaultedArticle("2", "Hello", "Content")
    article != new FlatDefaultedArticle("1", "Other", "Content")
    article != new FlatDefaultedArticle("1", "Hello", "Other")
    article != "not an article"
    article != null
  }

  def "counted thing exposes its bean surface"() {
    given:
    def thing = new FlatCountedThing()

    when:
    thing.setId("1")
    thing.setCount(3)

    then:
    thing.getId() == "1"
    thing.getCount() == 3
  }

  def "counted thing equals and hashCode compare all fields"() {
    given:
    def thing = new FlatCountedThing("1", 3)

    expect:
    thing == thing
    thing == new FlatCountedThing("1", 3)
    thing.hashCode() == new FlatCountedThing("1", 3).hashCode()
    thing != new FlatCountedThing("2", 3)
    thing != new FlatCountedThing("1", 4)
    thing != "not a thing"
    thing != null
  }

  def "ignored-attribute thing exposes its bean surface"() {
    given:
    def thing = new FlatThingWithIgnored()

    when:
    thing.setId("1")
    thing.setName("visible")
    thing.setConfidential("secret")

    then:
    thing.getId() == "1"
    thing.getName() == "visible"
    thing.getConfidential() == "secret"
  }

  def "ignored-attribute thing equals and hashCode compare all fields"() {
    given:
    def thing = new FlatThingWithIgnored("1", "visible", "secret")

    expect:
    thing == thing
    thing == new FlatThingWithIgnored("1", "visible", "secret")
    thing.hashCode() == new FlatThingWithIgnored("1", "visible", "secret").hashCode()
    thing != new FlatThingWithIgnored("2", "visible", "secret")
    thing != new FlatThingWithIgnored("1", "other", "secret")
    thing != new FlatThingWithIgnored("1", "visible", "other")
    thing != "not a thing"
    thing != null
  }

  def "creator article getters and equals cover the immutable surface"() {
    given:
    def article = new FlatCreatorArticle("42", "Creator")

    expect:
    article.getId() == "42"
    article.getTitle() == "Creator"
    article == article
    article == new FlatCreatorArticle("42", "Creator")
    article.hashCode() == new FlatCreatorArticle("42", "Creator").hashCode()
    article != new FlatCreatorArticle("1", "Creator")
    article != new FlatCreatorArticle("42", "Other")
    article != "not an article"
    article != null
  }

  def "required thing getters and equals cover the immutable surface"() {
    given:
    def thing = new FlatRequiredThing("1", "present")

    expect:
    thing.getId() == "1"
    thing.getRequired() == "present"
    thing == thing
    thing == new FlatRequiredThing("1", "present")
    thing.hashCode() == new FlatRequiredThing("1", "present").hashCode()
    thing != new FlatRequiredThing("2", "present")
    thing != new FlatRequiredThing("1", "other")
    thing != "not a thing"
    thing != null
  }

  def "throwing creator accepts a non-rejected title"() {
    given:
    def thing = new FlatThrowingCreatorThing("1", "ok")

    expect:
    thing.getId() == "1"
    thing.getTitle() == "ok"
    thing == thing
    thing == new FlatThrowingCreatorThing("1", "ok")
    thing.hashCode() == new FlatThrowingCreatorThing("1", "ok").hashCode()
    thing != new FlatThrowingCreatorThing("2", "ok")
    thing != new FlatThrowingCreatorThing("1", "other")
    thing != "not a thing"
    thing != null
  }

  def "throwing creator rejects the boom title"() {
    when:
    new FlatThrowingCreatorThing("1", "boom")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "creator rejected value"
  }

  def "array relationship DTO copies comments on construction and access"() {
    given:
    def original = [
      ResourceIdentifier.of("comments", "c1")
    ] as ResourceIdentifier[]

    when:
    def article = new FlatArticleWithArray("1", "T", original)
    original[0] = ResourceIdentifier.of("comments", "mutated")
    def first = article.comments()
    first[0] = ResourceIdentifier.of("comments", "mutated-copy")

    then:
    article.id() == "1"
    article.title() == "T"
    article.comments() == ([
      ResourceIdentifier.of("comments", "c1")
    ] as ResourceIdentifier[])
    !article.comments().is(first)
  }

  def "array relationship DTO preserves a null comments component"() {
    expect:
    new FlatArticleWithArray("1", null, null).comments() == null
  }
}
